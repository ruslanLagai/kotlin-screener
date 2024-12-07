package ru.home.project.scheduled

import com.bybit.api.client.domain.CategoryType
import com.bybit.api.client.domain.GenericResponse
import com.bybit.api.client.domain.market.request.MarketDataRequest
import com.bybit.api.client.domain.market.response.tickers.TickersResult
import com.bybit.api.client.restApi.BybitApiAsyncMarketDataRestClient
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.home.project.model.ItemType
import ru.home.project.properties.TinkoffTradingProperties
import ru.home.project.properties.TwelveDataTradingProperties
import ru.home.project.service.LevelsDetectionService
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.Duration

/**
 * @author rlagay
 */
@Service
class DailyLevelsScanService(
    val levelsDetectionService: LevelsDetectionService,
    val tinkoffTradingProperties: TinkoffTradingProperties,
    val bybitApiMarketRestClient: BybitApiAsyncMarketDataRestClient,
    val twelveDataTradingProperties: TwelveDataTradingProperties
) {

    private val log: Logger = LoggerFactory.getLogger(DailyLevelsScanService::class.java)
    private val mapper = ObjectMapper()

    @Scheduled(cron = "\${service.tinkoff.daily-cron}")
    fun scanLevels() {
        tinkoffTradingProperties.instruments.forEach {
            kotlin.runCatching {
                log.info("Starting levels scan for {}", it)
                levelsDetectionService.detectLevels(it, CandleInterval.CANDLE_INTERVAL_DAY, CandleInterval.CANDLE_INTERVAL_HOUR, ItemType.STOCK)
                Thread.sleep(Duration.ofSeconds(120))
            }.onFailure {
                log.error("Failed to detect levels", it)
            }
        }
    }

    @Scheduled(cron = "\${service.crypto.daily-cron}")
    fun scanCryptoLevels() {
        val request = MarketDataRequest.builder().category(CategoryType.SPOT).baseCoin("USDT").build()
        bybitApiMarketRestClient.getMarketTickers(request) { genericResp ->
            val response = mapper.convertValue(genericResp, object : TypeReference<GenericResponse<TickersResult>>() {})
            response.result.tickerEntries
                .filter { it.symbol?.endsWith("USDT") ?: false }
                .filter { twelveDataTradingProperties.instruments.contains(it.symbol) }
                .forEach {
                    kotlin.runCatching {
                        log.info("Starting crypto levels scan for {}", it.symbol)
                        val twelveDataFormatSymbol = it.symbol.replace("USDT", "/USD")
                        levelsDetectionService.detectLevels(
                            twelveDataFormatSymbol,
                            CandleInterval.CANDLE_INTERVAL_DAY,
                            CandleInterval.CANDLE_INTERVAL_DAY,
                            ItemType.CRYPTO
                        )
                    }.onFailure {
                        log.error("Failed to detect crypto levels", it)
                    }
                }
        }
    }
}