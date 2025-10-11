package ru.home.project.utils

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.home.project.entity.CandleEntity
import ru.home.project.util.getCandles
import ru.tinkoff.piapi.contract.v1.CandleInterval

class LineUtilsTest {

    @Test
    fun `test candles in asc channel - mxu 21-06 21-08`() {
        val candles = getCandles("candles/utils/mxu-hourly-candles-channel-21.06-21.08.json").candlesList
            .map { CandleEntity(figi = "BBG004730N88", ticker = "SBER", open = priceToDouble(it.open),
                close = priceToDouble(it.close), high = priceToDouble(it.high), low = priceToDouble(it.low),
                volume = it.volume, dateTime = timestampToDate(it.time), interval = CandleInterval.CANDLE_INTERVAL_HOUR) }
        val extremums = getExtremums(candles)

        assertEquals(3, extremums.first.size)
        assertEquals(2, extremums.second.size)
    }

}