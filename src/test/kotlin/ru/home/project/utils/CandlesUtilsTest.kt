package ru.home.project.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.home.project.entity.CandleEntity
import ru.home.project.util.getCandles
import ru.tinkoff.piapi.contract.v1.CandleInterval

class CandlesUtilsTest {

    @Test
    fun `test candles in asc channel - mxu 21-06 21-08`() {
        val candles = getCandles("candles/utils/mxu-hourly-candles-channel-21.06-21.08.json").candlesList
            .map { CandleEntity(figi = "BBG004730N88", ticker = "SBER", open = priceToDouble(it.open),
                close = priceToDouble(it.close), high = priceToDouble(it.high), low = priceToDouble(it.low),
                volume = it.volume, dateTime = timestampToDate(it.time), interval = CandleInterval.CANDLE_INTERVAL_HOUR) }

        val result = splitToTrends(candles, 30)

        assertEquals(5, result.size)
        assertEquals(Trend.ASCENDING, result[0].first)
        assertEquals(Trend.DESCENDING, result[1].first)
        assertEquals(Trend.ASCENDING, result[2].first)
        assertEquals(Trend.DESCENDING, result[3].first)
        assertEquals(Trend.ASCENDING, result[4].first)
    }

    @Test
    fun `test candles in desc channel - nku 25-04 19-06`() {
        val candles = getCandles("candles/utils/nku-hourly-candles-channel-25.04-19.06.json").candlesList
            .map { CandleEntity(figi = "BBG004730N88", ticker = "SBER", open = priceToDouble(it.open),
                close = priceToDouble(it.close), high = priceToDouble(it.high), low = priceToDouble(it.low),
                volume = it.volume, dateTime = timestampToDate(it.time), interval = CandleInterval.CANDLE_INTERVAL_HOUR) }

        val result = splitToTrends(candles, 30)

        assertEquals(4, result.size)
        assertEquals(Trend.DESCENDING, result[0].first)
        assertEquals(Trend.ASCENDING, result[1].first)
        assertEquals(Trend.DESCENDING, result[2].first)
        assertEquals(Trend.ASCENDING, result[3].first)

    }

    @Test
    fun `test candles in triangle`() {
        val candles = getCandles("candles/utils/gzu-hourly-candles-channel-06.08-06.09.json").candlesList
            .map { CandleEntity(figi = "BBG004730N88", ticker = "SBER", open = priceToDouble(it.open),
                close = priceToDouble(it.close), high = priceToDouble(it.high), low = priceToDouble(it.low),
                volume = it.volume, dateTime = timestampToDate(it.time), interval = CandleInterval.CANDLE_INTERVAL_HOUR) }

        val result = splitToTrends(candles, 30)

        assertEquals(3, result.size)
        assertEquals(Trend.ASCENDING, result[0].first)
        assertEquals(Trend.DESCENDING, result[1].first)
        assertEquals(Trend.ASCENDING, result[2].first)

    }

    @Test
    fun `test candles - no trend`() {
        val candles = getCandles("candles/utils/ngu-hourly-candles-channel-06.08-06.09.json").candlesList
            .map { CandleEntity(figi = "BBG004730N88", ticker = "SBER", open = priceToDouble(it.open),
                close = priceToDouble(it.close), high = priceToDouble(it.high), low = priceToDouble(it.low),
                volume = it.volume, dateTime = timestampToDate(it.time), interval = CandleInterval.CANDLE_INTERVAL_HOUR) }

        val result = splitToTrends(candles, 30)

        assertEquals(4, result.size)
    }
}