package ru.home.project.service.impl

import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import ru.home.project.model.events.TradeEvent
import ru.home.project.model.events.Type
import ru.home.project.properties.TinkoffTradingProperties
import ru.home.project.repository.InstrumentRepository
import ru.home.project.service.TinkoffStreamingService
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval
import ru.tinkoff.piapi.contract.v1.TradeSourceType
import ru.ttech.piapi.core.connector.streaming.listeners.OnNextListener
import ru.ttech.piapi.core.impl.marketdata.MarketDataStreamManager
import ru.ttech.piapi.core.impl.marketdata.subscription.CandleSubscriptionSpec
import ru.ttech.piapi.core.impl.marketdata.subscription.Instrument
import ru.ttech.piapi.core.impl.marketdata.wrapper.CandleWrapper

/**
 * @author rlagay
 */
@Service
class TinkoffStreamingServiceImpl(
    val streamManager: MarketDataStreamManager,
    val candlesStreamProcessor: OnNextListener<CandleWrapper>,
    val tinkoffTradingProperties: TinkoffTradingProperties,
    val instrumentRepository: InstrumentRepository,
    val eventPublisher: ApplicationEventPublisher
) : TinkoffStreamingService {

    val log: Logger = LoggerFactory.getLogger(TinkoffStreamingServiceImpl::class.java)
    val instrumentsHourly = instrumentRepository.findAll()
        .filter { it.instrumentUid != null }
        .filter { tinkoffTradingProperties.instruments.contains(it.figi) }
        .map { Instrument(it.instrumentUid, SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_HOUR) }
        .toSet()
    val instrumentsDaily = instrumentRepository.findAll()
        .filter { it.instrumentUid != null }
        .filter { tinkoffTradingProperties.instruments.contains(it.figi) }
        .map { Instrument(it.instrumentUid, SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_DAY) }
        .toSet()
    val futures = instrumentRepository.findAll()
        .filter { it.instrumentUid != null }
        .map { Instrument(it.instrumentUid, SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_HOUR) }
        .toSet()

    override fun subscribeTradingStream() {
        streamManager.subscribeTrades(futures, TradeSourceType.TRADE_SOURCE_ALL) {
            val figi = it.figi
            val event = TradeEvent(price = it.price.toDouble(), figi = figi, uuid = it.instrumentUid, type = Type.PATTERN)
            eventPublisher.publishEvent(event)
        }
        streamManager.subscribeTrades(instrumentsHourly, TradeSourceType.TRADE_SOURCE_ALL, {
            val figi = it.figi
            val event = TradeEvent(price = it.price.toDouble(), figi = figi, uuid = it.instrumentUid, type = Type.LEVEL)
            eventPublisher.publishEvent(event)
        })
        streamManager.subscribeCandles(instrumentsHourly, CandleSubscriptionSpec(), candlesStreamProcessor)
        streamManager.subscribeCandles(instrumentsDaily, CandleSubscriptionSpec(), candlesStreamProcessor)
        streamManager.start()
    }

    @PostConstruct
    fun init() {
        streamManager.unsubscribeCandles(instrumentsHourly, CandleSubscriptionSpec())
        streamManager.unsubscribeCandles(instrumentsDaily, CandleSubscriptionSpec())
        streamManager.unsubscribeLastPrices(instrumentsHourly)
        streamManager.unsubscribeLastPrices(futures)
        streamManager.shutdown()
    }
}