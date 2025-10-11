package ru.home.project.service.impl

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import ru.home.project.entity.CandleEntity
import ru.home.project.model.PatternType
import ru.home.project.service.CandlesService
import ru.home.project.util.getCandles
import ru.home.project.utils.priceToDouble
import ru.home.project.utils.timestampToDate
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class TrianglePatternServiceImplTest {

    @Mock
    private lateinit var candlesService: CandlesService

    private lateinit var trianglePatternService: TrianglePatternServiceImpl

    @BeforeEach
    fun setup() {
        trianglePatternService = TrianglePatternServiceImpl(candlesService)
    }

    @Test
    fun `should return null when price left triangle`() {
        // Given
        val figi = "TEST_FIGI"
        val ticker = "TEST"
        val interval = CandleInterval.CANDLE_INTERVAL_HOUR
        
        val candles = getCandles("candles/patterns/ri-hourly-candles-after-exit-04.07-06.06.json").candlesList
            .map { CandleEntity(figi = "BBG004730N88", ticker = "SBER", open = priceToDouble(it.open), close = priceToDouble(it.close),
                high = priceToDouble(it.high), low = priceToDouble(it.low), volume = it.volume,
                dateTime = timestampToDate(it.time), interval = CandleInterval.CANDLE_INTERVAL_HOUR) }
        
        `when`(candlesService.getLastCandles(figi, interval, null,60)).thenReturn(candles)

        // When
        val result = trianglePatternService.detectPattern(figi, ticker, null, interval)

        // Then
        assertNull(result)
    }

    @Test
    fun `should detect triangle pattern`() {
        // Given
        val figi = "TEST_FIGI"
        val ticker = "TEST"
        val interval = CandleInterval.CANDLE_INTERVAL_HOUR

        val candles = getCandles("candles/patterns/ri-hourly-candles-triangle-04.07-23.05.json").candlesList
            .map { CandleEntity(figi = "BBG004730N88", ticker = "SBER", open = priceToDouble(it.open), close = priceToDouble(it.close),
                high = priceToDouble(it.high), low = priceToDouble(it.low), volume = it.volume,
                dateTime = timestampToDate(it.time), interval = CandleInterval.CANDLE_INTERVAL_HOUR) }

        `when`(candlesService.getLastCandles(figi, interval, null,60)).thenReturn(candles)

        // When
        val result = trianglePatternService.detectPattern(figi, ticker, null, interval)

        // Then
        assertEquals(PatternType.TRIANGLE, result!!.patternType)
        assertEquals(interval, result.interval)
        assertEquals(ticker, result.ticker)
        assertEquals(figi, result.figi)
        assertEquals(LocalDateTime.of(2025, 4, 28, 11, 0, 0),
            result.startDate)

    }

    @Test
    fun `should detect channel`() {
        // Given
        val figi = "TEST_FIGI"
        val ticker = "TEST"
        val interval = CandleInterval.CANDLE_INTERVAL_HOUR

        val candles = getCandles("candles/patterns/mxu-hourly-candles-channel-21.06-21.08.json").candlesList
            .map { CandleEntity(figi = "BBG004730N88", ticker = "SBER", open = priceToDouble(it.open), close = priceToDouble(it.close),
                high = priceToDouble(it.high), low = priceToDouble(it.low), volume = it.volume,
                dateTime = timestampToDate(it.time), interval = CandleInterval.CANDLE_INTERVAL_HOUR) }

        `when`(candlesService.getLastCandles(figi, interval, null, 60)).thenReturn(candles)

        // When
        val result = trianglePatternService.detectPattern(figi, ticker, null, interval)

        // Then
        assertNull(result)
    }

    @Test
    fun `should return null when no pattern is found`() {
        // Given
        val figi = "TEST_FIGI"
        val ticker = "TEST"
        val interval = CandleInterval.CANDLE_INTERVAL_1_MIN
        val now = ZonedDateTime.now(ZoneId.of("UTC"))
        
        val candles = listOf(
            CandleEntity(
                figi = figi,
                ticker = ticker,
                open = 85.0,
                high = 90.0,
                low = 85.0,
                close = 87.0,
                volume = 1000L,
                dateTime = now.minusMinutes(4),
                interval = interval
            ),
            CandleEntity(
                figi = figi,
                ticker = ticker,
                open = 90.0,
                high = 95.0,
                low = 90.0,
                close = 92.0,
                volume = 1000L,
                dateTime = now.minusMinutes(3),
                interval = interval
            ),
            CandleEntity(
                figi = figi,
                ticker = ticker,
                open = 88.0,
                high = 92.0,
                low = 88.0,
                close = 90.0,
                volume = 1000L,
                dateTime = now.minusMinutes(2),
                interval = interval
            ),
            CandleEntity(
                figi = figi,
                ticker = ticker,
                open = 89.0,
                high = 94.0,
                low = 89.0,
                close = 91.0,
                volume = 1000L,
                dateTime = now.minusMinutes(1),
                interval = interval
            )
        )

        `when`(candlesService.getLastCandles(figi, interval, null, 60)).thenReturn(candles)

        // When
        val result = trianglePatternService.detectPattern(figi, ticker, null, interval)

        // Then
        assertNull(result)
    }

    @Test
    fun `should return null when candles list is empty`() {
        // Given
        val figi = "TEST_FIGI"
        val ticker = "TEST"
        val interval = CandleInterval.CANDLE_INTERVAL_1_MIN

        `when`(candlesService.getLastCandles(figi, interval, null, 60)).thenReturn(emptyList())

        // When
        val result = trianglePatternService.detectPattern(figi, ticker, null, interval)

        // Then
        assertNull(result)
    }
} 