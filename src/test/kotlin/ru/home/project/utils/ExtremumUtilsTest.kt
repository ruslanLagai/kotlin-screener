package ru.home.project.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.home.project.entity.CandleEntity
import ru.home.project.util.getCandles
import ru.tinkoff.piapi.contract.v1.CandleInterval

class ExtremumUtilsTest {

    @Test
    fun `test get extremums in asc channel - mxu 21-06 21-08`() {
        val candles = getCandles("candles/utils/mxu-hourly-candles-channel-21.06-21.08.json").candlesList
            .map { CandleEntity(figi = "BBG004730N88", ticker = "SBER", open = priceToDouble(it.open),
                close = priceToDouble(it.close), high = priceToDouble(it.high), low = priceToDouble(it.low),
                volume = it.volume, dateTime = timestampToDate(it.time), interval = CandleInterval.CANDLE_INTERVAL_HOUR) }

        val result = getExtremums(candles)
        assertEquals(3, result.first.size)
        assertEquals(2, result.second.size)
    }

    @Test
    fun `test get extremums in desc channel - nku 25-04 19-06`() {
        val candles = getCandles("candles/utils/nku-hourly-candles-channel-25.04-19.06.json").candlesList
            .map { CandleEntity(figi = "BBG004730N88", ticker = "SBER", open = priceToDouble(it.open),
                close = priceToDouble(it.close), high = priceToDouble(it.high), low = priceToDouble(it.low),
                volume = it.volume, dateTime = timestampToDate(it.time), interval = CandleInterval.CANDLE_INTERVAL_HOUR) }

        val result = getExtremums(candles)

        assertEquals(2, result.first.size)
        assertEquals(2, result.second.size)

    }

    @Test
    fun `test get extremums in triangle`() {
        val candles = getCandles("candles/utils/gzu-hourly-candles-channel-06.08-06.09.json").candlesList
            .map { CandleEntity(figi = "BBG004730N88", ticker = "SBER", open = priceToDouble(it.open),
                close = priceToDouble(it.close), high = priceToDouble(it.high), low = priceToDouble(it.low),
                volume = it.volume, dateTime = timestampToDate(it.time), interval = CandleInterval.CANDLE_INTERVAL_HOUR) }

        val result = getExtremums(candles)

        assertEquals(2, result.first.size)
        assertEquals(1, result.second.size)

    }

    @Test
    fun `test check extremums inside asc channel - mxu 21-06 21-08`() {
        val candles = getCandles("candles/utils/mxu-hourly-candles-channel-21.06-21.08.json").candlesList
            .map { CandleEntity(figi = "BBG004730N88", ticker = "SBER", open = priceToDouble(it.open),
                close = priceToDouble(it.close), high = priceToDouble(it.high), low = priceToDouble(it.low),
                volume = it.volume, dateTime = timestampToDate(it.time), interval = CandleInterval.CANDLE_INTERVAL_HOUR) }
        val extremums = getExtremums(candles)

        val result = checkExtremumsAreBetweenLines(extremums.first, extremums.second, candles)

        assertTrue(result)
    }
}