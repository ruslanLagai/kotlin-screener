package ru.home.project.scheduled

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
    val twelveDataTradingProperties: TwelveDataTradingProperties
) {

    @Scheduled(cron = "\${service.crypto.prices-cron}")
    fun retrieveCandles() {
        kotlin.runCatching {
            coinMarketCapService.getCurrentPrices()
                .filter { twelveDataTradingProperties.instruments.contains(it.symbol) }
                .forEach { eventPublisher.publishEvent(it) }
        }
    }

}