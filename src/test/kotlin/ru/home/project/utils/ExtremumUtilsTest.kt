package ru.home.project.utils

import io.mockk.every
import io.mockk.mockkStatic
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import ru.home.project.entity.CandleEntity
import ru.home.project.model.PatternType
import ru.home.project.util.getCandles
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.LocalDateTime

@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ExtremumUtilsTest {

    @Order(1)
    @Test
    fun `test get extremums in asc channel - mxu 21-06 21-08`() {
        val candles = getCandles("candles/utils/mxu-hourly-candles-channel-21.06-21.08.json").candlesList
            .map { CandleEntity(figi = "BBG004730N88", ticker = "SBER", open = priceToDouble(it.open),
                close = priceToDouble(it.close), high = priceToDouble(it.high), low = priceToDouble(it.low),
                volume = it.volume, dateTime = timestampToDate(it.time), interval = CandleInterval.CANDLE_INTERVAL_HOUR) }

        val result = getExtremums(candles, 30)
        assertEquals(3, result.first.size)
        assertEquals(2, result.second.size)
    }

    @Order(2)
    @Test
    fun `test get extremums in desc channel - nku 25-04 19-06`() {
        val candles = getCandles("candles/utils/nku-hourly-candles-channel-25.04-19.06.json").candlesList
            .map { CandleEntity(figi = "BBG004730N88", ticker = "SBER", open = priceToDouble(it.open),
                close = priceToDouble(it.close), high = priceToDouble(it.high), low = priceToDouble(it.low),
                volume = it.volume, dateTime = timestampToDate(it.time), interval = CandleInterval.CANDLE_INTERVAL_HOUR) }

        val result = getExtremums(candles, 30)

        assertEquals(2, result.first.size)
        assertEquals(2, result.second.size)

    }

    @Order(3)
    @Test
    fun `test get extremums in triangle`() {
        val candles = getCandles("candles/utils/gzu-hourly-candles-channel-06.08-06.09.json").candlesList
            .map { CandleEntity(figi = "BBG004730N88", ticker = "SBER", open = priceToDouble(it.open),
                close = priceToDouble(it.close), high = priceToDouble(it.high), low = priceToDouble(it.low),
                volume = it.volume, dateTime = timestampToDate(it.time), interval = CandleInterval.CANDLE_INTERVAL_HOUR) }

        val result = getExtremums(candles, 30)

        assertEquals(2, result.first.size)
        assertEquals(1, result.second.size)

    }

    @Order(4)
    @Test
    fun `test check extremums inside asc channel - mxu 21-06 21-08`() {
        val candles = getCandles("candles/utils/mxu-hourly-candles-channel-21.06-21.08.json").candlesList
            .map { CandleEntity(figi = "BBG004730N88", ticker = "SBER", open = priceToDouble(it.open),
                close = priceToDouble(it.close), high = priceToDouble(it.high), low = priceToDouble(it.low),
                volume = it.volume, dateTime = timestampToDate(it.time), interval = CandleInterval.CANDLE_INTERVAL_HOUR) }
        val extremums = getExtremums(candles,  30)

        val result = checkExtremumsAreBetweenLines(extremums.first, extremums.second, candles)

        assertEquals(PatternType.CHANNEL, result!!.patternType)
    }

    @Order(5)
    @Test
    fun `test check price inside asc channel - mxu 21-06 21-08`() {

        val fixed = LocalDateTime.of(2025, 11, 1, 19, 10, 0)
        val startDate = LocalDateTime.of(2025, 7, 1, 7, 0, 0)
        mockkStatic(LocalDateTime::class)
        every { LocalDateTime.now() } returns fixed

        val maxLine = Pair(34.345, 286900.05)
        val minLine = Pair(70.957, 243724.25)
        val result = checkPriceIsBetweenLines(maxLine, minLine, 2800000.0, startDate)

        assertFalse(result.first)
    }
}