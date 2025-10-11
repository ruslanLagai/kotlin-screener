package ru.home.project.service.impl

import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.home.project.properties.TinkoffTradingProperties
import ru.home.project.service.TinkoffStreamingService
import ru.tinkoff.piapi.contract.v1.SubscriptionInterval
import ru.ttech.piapi.core.connector.streaming.listeners.OnNextListener
import ru.ttech.piapi.core.impl.marketdata.MarketDataStreamManager
import ru.ttech.piapi.core.impl.marketdata.subscription.CandleSubscriptionSpec
import ru.ttech.piapi.core.impl.marketdata.subscription.Instrument
import ru.ttech.piapi.core.impl.marketdata.wrapper.CandleWrapper
import ru.ttech.piapi.core.impl.marketdata.wrapper.LastPriceWrapper

/**
 * @author rlagay
 */
@Service
class TinkoffStreamingServiceImpl(
    val streamManager: MarketDataStreamManager,
    val lastPriceStreamProcessor: OnNextListener<LastPriceWrapper>,
    val candlesStreamProcessor: OnNextListener<CandleWrapper>,
    val tinkoffTradingProperties: TinkoffTradingProperties,

) : TinkoffStreamingService {

    val log: Logger = LoggerFactory.getLogger(TinkoffStreamingServiceImpl::class.java)
    val instrumentsHourly = tinkoffTradingProperties.instruments
        .map { Instrument(it, SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_HOUR) }
        .toSet()
    val instrumentsDaily = tinkoffTradingProperties.instruments
        .map { Instrument(it, SubscriptionInterval.SUBSCRIPTION_INTERVAL_ONE_DAY) }
        .toSet()

    override fun subscribeTradingStream() {
        streamManager.subscribeLastPrices(instrumentsHourly, lastPriceStreamProcessor)
        streamManager.subscribeCandles(instrumentsHourly, CandleSubscriptionSpec(), candlesStreamProcessor)
        streamManager.subscribeCandles(instrumentsDaily, CandleSubscriptionSpec(), candlesStreamProcessor)
        streamManager.start()
    }

    @PostConstruct
    fun init() {
        streamManager.unsubscribeCandles(instrumentsHourly, CandleSubscriptionSpec())
        streamManager.unsubscribeCandles(instrumentsDaily, CandleSubscriptionSpec())
        streamManager.unsubscribeLastPrices(instrumentsHourly)
        streamManager.shutdown()
    }
}