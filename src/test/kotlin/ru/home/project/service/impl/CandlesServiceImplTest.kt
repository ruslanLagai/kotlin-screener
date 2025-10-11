package ru.home.project.service.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import ru.home.project.AbstractIntegrationTest
import ru.home.project.entity.CandleEntity
import ru.home.project.repository.CandlesRepository
import ru.home.project.service.CandlesService
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ExecutionException

/**
 * Real connection with tinkoff - testing historical candles interaction
 *
 * @author rlagay
 */
class CandlesServiceImplTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var candlesService: CandlesService

    @MockitoBean
    private lateinit var candlesRepository: CandlesRepository

    @Test
    fun `test getting candles from grpc only`() {

        `when`(candlesRepository.findByFigiAndIntervalOrderByDateTimeDesc(any(), any())).thenReturn(emptyList())
        val candles = ArrayList<CandleEntity>()

        val future = candlesService.getHistoricalCandles("BBG004730N88", "SBER", "e6123145-9665-43e0-8413-cd61b8aa9b13",
            CandleInterval.CANDLE_INTERVAL_DAY, candles)

        future.get()
        assertFalse(candles.isEmpty())
        assertTrue(candles.size > 800)
    }

    @Test
    fun `test getting candles from db + grpc`() {
        `when`(candlesRepository.findByFigiAndIntervalOrderByDateTimeDesc(any(), any())).thenReturn(listOf(
            CandleEntity(id = 1, figi = "BBG004730N88", ticker = "SBER", open = 1000.0, close = 1001.0, high = 1001.0,
                low = 999.0, volume = 1000, interval = CandleInterval.CANDLE_INTERVAL_DAY,
                dateTime = ZonedDateTime.of(2023, 11, 11, 1, 1, 1, 1, ZoneId.of("UTC")))
        ))
        val candles = ArrayList<CandleEntity>()

        val future = candlesService.getHistoricalCandles("BBG004730N88", "SBER", "e6123145-9665-43e0-8413-cd61b8aa9b13",
            CandleInterval.CANDLE_INTERVAL_DAY, candles)

        future.get()
        assertFalse(candles.isEmpty())
        assertTrue(candles.size > 40)
    }

    @Test
    fun `test getting candles from db + grpc - hour interval`() {
        `when`(candlesRepository.findByFigiAndIntervalOrderByDateTimeDesc(any(), any())).thenReturn(listOf(
            CandleEntity(id = 1, figi = "BBG004730N88", ticker = "SBER", open = 1000.0, close = 1001.0, high = 1001.0,
                low = 999.0, volume = 1000, interval = CandleInterval.CANDLE_INTERVAL_HOUR,
                dateTime = ZonedDateTime.now().minusDays(10))
        ))
        val candles = ArrayList<CandleEntity>()

        val future = candlesService.getHistoricalCandles("BBG004730N88", "SBER",
            "e6123145-9665-43e0-8413-cd61b8aa9b13",CandleInterval.CANDLE_INTERVAL_HOUR, candles)

        future.get()
        assertFalse(candles.isEmpty())
        assertTrue(candles.size > 150)
    }

    @Test
    fun `test getting candles only from grpc - hour interval`() {
        `when`(candlesRepository.findByFigiAndIntervalOrderByDateTimeDesc(any(), any())).thenReturn(emptyList())
        val candles = ArrayList<CandleEntity>()

        val future = candlesService.getHistoricalCandles("BBG004730N88", "SBER",
            "e6123145-9665-43e0-8413-cd61b8aa9b13", CandleInterval.CANDLE_INTERVAL_HOUR, candles)

        future.get()
        assertFalse(candles.isEmpty())
        assertTrue(candles.size > 1500)
    }

    @Test
    fun `test getting candles from db + grpc - exception`() {
        `when`(candlesRepository.findByFigiAndIntervalOrderByDateTimeDesc(any(), any())).thenReturn(listOf(
            CandleEntity(id = 1, figi = "BBG004730N88", ticker = "SBER", open = 1000.0, close = 1001.0, high = 1001.0,
                low = 999.0, volume = 1000, interval = CandleInterval.CANDLE_INTERVAL_1_MIN,
                dateTime = ZonedDateTime.of(2023, 10, 11, 1, 1, 1, 1, ZoneId.of("UTC")))
        ))
        val candles = ArrayList<CandleEntity>()

        val future = candlesService.getHistoricalCandles("BBG004730N88", "SBER",
            "e6123145-9665-43e0-8413-cd61b8aa9b13",CandleInterval.CANDLE_INTERVAL_1_MIN, candles)

        assertThrows<ExecutionException> { future.get() }
    }

    @Test
    fun `test cached candles`() {
        val sberFigi = "BBG004730N88"
        val result = candlesService.getLastCandles(figi = sberFigi, dayFrom = 14, interval = CandleInterval.CANDLE_INTERVAL_DAY)

        Thread.sleep(5000)

        val cached = candlesService.getLastCandles(figi = sberFigi, dayFrom = 14, interval = CandleInterval.CANDLE_INTERVAL_DAY)
        assertEquals(result.size, cached.size)
    }
}