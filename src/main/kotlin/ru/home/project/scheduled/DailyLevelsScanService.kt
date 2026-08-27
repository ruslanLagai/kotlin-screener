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
import ru.home.project.entity.InstrumentEntity
import ru.home.project.model.ItemType
import ru.home.project.properties.TinkoffTradingProperties
import ru.home.project.properties.TwelveDataTradingProperties
import ru.home.project.repository.InstrumentRepository
import ru.home.project.service.LevelsDetectionService
import ru.home.project.service.PatternsDetectionService
import ru.home.project.utils.timestampToDate
import ru.tinkoff.piapi.contract.v1.CandleInterval
import ru.tinkoff.piapi.contract.v1.InstrumentsRequest
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc
import java.time.Duration

/**
 * @author rlagay
 */
@Service
class DailyLevelsScanService(
    val levelsDetectionService: LevelsDetectionService,
    val patternsDetectionService: PatternsDetectionService,
    val tinkoffTradingProperties: TinkoffTradingProperties,
    val bybitApiMarketRestClient: BybitApiAsyncMarketDataRestClient,
    val twelveDataTradingProperties: TwelveDataTradingProperties,
    val instrumentRepository: InstrumentRepository,
    val instrumentsServiceBlockingV2Stub: InstrumentsServiceGrpc.InstrumentsServiceBlockingV2Stub
) {

    private val log: Logger = LoggerFactory.getLogger(DailyLevelsScanService::class.java)
    private val mapper = ObjectMapper()

    @Scheduled(cron = "\${service.tinkoff.daily-cron}")
    fun scanLevels() {
        log.info("Instruments size: ${tinkoffTradingProperties.instruments.size}")
        tinkoffTradingProperties.instruments.forEach {
            kotlin.runCatching {
                log.info("Starting levels scan for {}", it)
                levelsDetectionService.detectLevels(it, CandleInterval.CANDLE_INTERVAL_DAY, CandleInterval.CANDLE_INTERVAL_HOUR, ItemType.STOCK)
                Thread.sleep(Duration.ofSeconds(60))
            }.onFailure {
                log.error("Failed to detect levels", it)
            }
        }
    }

    @Scheduled(cron = "\${service.tinkoff.daily-cron}")
    fun scanPatterns() {
        instrumentsServiceBlockingV2Stub.futures(InstrumentsRequest.newBuilder().build()).instrumentsList
            .forEach {
                kotlin.runCatching {
                    var instrumentEntity = instrumentRepository.getByFigi(it.figi)
                    if (instrumentEntity == null || instrumentEntity.instrumentUid == null) {
                        if (instrumentEntity == null) {
                            instrumentEntity = instrumentRepository.save(InstrumentEntity(
                                figi = it.figi,
                                ticker = it.ticker,
                                instrumentUid = it.uid,
                                lot = it.lot,
                                currency = it.currency,
                                name = it.name,
                                first1dayCandleDate = timestampToDate(it.first1DayCandleDate)
                            ))
                        } else {
                            instrumentEntity.instrumentUid = it.uid
                            instrumentEntity = instrumentRepository.save(instrumentEntity)
                        }
                    }
                    log.info("Starting patterns scan for {}", it.ticker)

                    patternsDetectionService.detectPatterns(instrumentEntity, CandleInterval.CANDLE_INTERVAL_HOUR, ItemType.STOCK)
                    Thread.sleep(Duration.ofSeconds(10))
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
                        val instrumentEntity = InstrumentEntity(
                            figi = twelveDataFormatSymbol,
                            ticker = it.symbol,
                            instrumentUid = it.symbol,
                            lot = 1,
                            currency = "USDT",
                            name = it.symbol
                        )
                        log.info("Starting patterns scan for {}", it.symbol)

                        patternsDetectionService.detectPatterns(instrumentEntity, CandleInterval.CANDLE_INTERVAL_HOUR, ItemType.CRYPTO)

                    }.onFailure {
                        log.error("Failed to detect crypto levels", it)
                    }
                }
        }
    }
}