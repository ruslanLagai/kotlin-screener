package ru.home.project.listener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import ru.home.project.model.events.TradeEvent
import ru.ttech.piapi.core.connector.streaming.listeners.OnNextListener
import ru.ttech.piapi.core.impl.marketdata.wrapper.LastPriceWrapper

/**
 * @author rlagay
 */
@Component
class TradesStreamListener(
    val eventPublisher: ApplicationEventPublisher
) : OnNextListener<LastPriceWrapper> {

    val log: Logger = LoggerFactory.getLogger(TradesStreamListener::class.java)

    /**
     * Вызывать оповещение, если текущая цена приблизилась к уровню
     */
    override fun onNext(response: LastPriceWrapper) {
        val figi = response.figi
        log.debug("Trade received: {}, {}", figi, response.price)
        val event = TradeEvent(price = response.price.toDouble(), figi = figi, uuid = response.instrumentUid)
        eventPublisher.publishEvent(event)
    }
}