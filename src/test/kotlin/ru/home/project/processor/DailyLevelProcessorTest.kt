package ru.home.project.processor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import ru.home.project.entity.CandleEntity
import ru.home.project.util.getCandles
import ru.home.project.utils.priceToDouble
import ru.home.project.utils.timestampToDate
import ru.tinkoff.piapi.contract.v1.CandleInterval


/**
 * @author rlagay
 */
@ExtendWith(MockitoExtension::class)
class DailyLevelProcessorTest {

    private val dailyLevelProcessor = DailyLevelProcessor()

    @Test
    fun `test support levels`() {
        val resp = getCandles("candles/vtb-daily-candles.json")

        val candles = resp.candlesList.map { CandleEntity(figi = "BBG004730ZJ9", low = priceToDouble(it.low), high = priceToDouble(it.high),
            open = priceToDouble(it.open), close = priceToDouble(it.close), volume = it.volume, dateTime = timestampToDate( it.time),
            ticker = "VTBR", interval = CandleInterval.CANDLE_INTERVAL_DAY) }

        val levels = dailyLevelProcessor.processStock("BBG004730ZJ9", candles)

        assertNotNull(levels)
        assertEquals(6, levels.size)
    }

    @Test
    fun `test resistance levels`() {
        val resp = getCandles("candles/vkco-daily-candles.json")

        val candles = resp.candlesList.map { CandleEntity(figi = "TCS00A106YF0", low = priceToDouble(it.low), high = priceToDouble(it.high),
            open = priceToDouble(it.open), close = priceToDouble(it.close), volume = it.volume, dateTime = timestampToDate( it.time),
            ticker = "VKCO", interval = CandleInterval.CANDLE_INTERVAL_DAY) }

        val levels = dailyLevelProcessor.processStock("TCS00A106YF0", candles)

        assertNotNull(levels)
        assertEquals(7, levels.size)
    }

    @Test
    fun `test not enough candles`() {
        val resp = getCandles("candles/vkco-daily-candles.json")

        val candles = resp.candlesList.map { CandleEntity(figi = "TCS00A106YF0", low = priceToDouble(it.low), high = priceToDouble(it.high),
            open = priceToDouble(it.open), close = priceToDouble(it.close), volume = it.volume, dateTime = timestampToDate( it.time),
            ticker = "VKCO", interval = CandleInterval.CANDLE_INTERVAL_DAY) }
        val levels = dailyLevelProcessor.processStock("TCS00A106YF0", candles.subList(0, 10))

        assertNotNull(levels)
        assertEquals(0, levels.size)
    }
}