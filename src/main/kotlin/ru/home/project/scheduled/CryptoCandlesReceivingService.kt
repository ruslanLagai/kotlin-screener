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
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.home.project.properties.TwelveDataTradingProperties
import ru.home.project.service.CoinMarketCapService

/**
 * @author rlagay
 */
@Service
class CryptoCandlesReceivingService(
    val coinMarketCapService: CoinMarketCapService,
    val eventPublisher: ApplicationEventPublisher,
    val twelveDataTradingProperties: TwelveDataTradingProperties,
    val bybitApiMarketRestClient: BybitApiAsyncMarketDataRestClient,
) {

    private val log: Logger = LoggerFactory.getLogger(CryptoCandlesReceivingService::class.java)
    private val mapper = ObjectMapper()

    @Scheduled(cron = "\${service.crypto.prices-cron}")
    fun retrieveCandles() {
        val request = MarketDataRequest.builder().category(CategoryType.SPOT).build()
        bybitApiMarketRestClient.getMarketTickers(request) { genericResp ->
            val response = mapper.convertValue(genericResp, object : TypeReference<GenericResponse<TickersResult>>() {})
            response.result.tickerEntries
                .filter { twelveDataTradingProperties.instruments.contains(it.symbol) }
                .forEach { eventPublisher.publishEvent(it) }
        }
    }

}