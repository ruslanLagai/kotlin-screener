package ru.home.project.listener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import ru.home.project.entity.CandleEntity
import ru.home.project.properties.TinkoffTradingProperties
import ru.home.project.repository.CandlesRepository
import ru.home.project.utils.mapToCandleInterval
import ru.ttech.piapi.core.connector.streaming.listeners.OnNextListener
import ru.ttech.piapi.core.impl.marketdata.wrapper.CandleWrapper
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
@Component
class HourlyCandlesStreamListener(
    val tinkoffTradingProperties: TinkoffTradingProperties,
    val candlesRepository: CandlesRepository
) : OnNextListener<CandleWrapper> {

    val logger: Logger = LoggerFactory.getLogger(HourlyCandlesStreamListener::class.java)
    private val candles = ArrayList<CandleEntity>()

    /**
     * Сохранение свечей в базу данных
     */
    override fun onNext(candle: CandleWrapper) {
        val candle = CandleEntity(
            figi = candle.figi, interval = mapToCandleInterval(candle.interval),
            open = candle.open.toDouble(), close = candle.close.toDouble(), high = candle.high.toDouble(),
            low = candle.low.toDouble(), volume = candle.volume,
            dateTime = ZonedDateTime.of(candle.time, ZoneId.of("UTC")),
            instrumentUid = candle.instrumentUid
        )
        candles.add(candle)
        if (candles.size >= tinkoffTradingProperties.instruments.size) {
            candlesRepository.saveAll(candles)
            candles.clear()
        }
    }
}