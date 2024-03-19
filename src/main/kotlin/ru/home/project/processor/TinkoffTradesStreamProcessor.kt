package ru.home.project.processor

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import ru.home.project.model.TradeEvent
import ru.home.project.utils.priceToDouble
import ru.tinkoff.piapi.contract.v1.MarketDataResponse
import ru.tinkoff.piapi.core.stream.StreamProcessor

/**
 * @author rlagay
 */
@Component
class TinkoffTradesStreamProcessor(
    val eventPublisher: ApplicationEventPublisher
) : StreamProcessor<MarketDataResponse> {

    val log: Logger = LoggerFactory.getLogger(TinkoffTradesStreamProcessor::class.java)

    /**
     * Вызывать оповещение, если текущая цена приблизилась к уровню
     */
    override fun process(marketDataResponse: MarketDataResponse) {
        if (marketDataResponse.hasTrade()) {
            val figi = marketDataResponse.trade.figi
            val tradePrice = priceToDouble(marketDataResponse.trade.price)

            log.debug("Trade received: {}, {}", figi, tradePrice)

            val event = TradeEvent(tradePrice, figi)
            eventPublisher.publishEvent(event)
        }
    }
}