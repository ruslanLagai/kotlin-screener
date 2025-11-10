package ru.home.project.service.impl

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
import ru.home.project.entity.InstrumentEntity
import ru.home.project.entity.PatternEntity
import ru.home.project.model.ItemType
import ru.home.project.model.Pattern
import ru.home.project.model.PatternType
import ru.home.project.repository.PatternsRepository
import ru.home.project.service.PatternService
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.LocalDateTime
import java.util.*

class PatternsDetectionServiceImplTest {

    @Mock
    private lateinit var trianglePatternServiceImpl: PatternService

    @Mock
    private lateinit var patternsRepository: PatternsRepository

    private lateinit var patternsDetectionService: PatternsDetectionServiceImpl

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        patternsDetectionService = PatternsDetectionServiceImpl(
            trianglePatternServiceImpl,
            patternsRepository
        )
    }

    @Test
    fun `detectPatterns should return early if pattern is not detected`() {
        val figi = "figi123"
        val interval = CandleInterval.CANDLE_INTERVAL_1_MIN
        val type = ItemType.STOCK
        val instrument = InstrumentEntity(
            ticker = "ticker",
            figi = figi,
            name = "name",
            instrumentUid = "instrumentUid",
            lot = 100,
            currency = "RUB",
            first1dayCandleDate = null
        )

        `when`(trianglePatternServiceImpl.detectPattern(
            figi = figi,
            ticker = instrument.ticker,
            instrumentUid = instrument.instrumentUid,
            interval = interval,
            days = 30,
            type = ItemType.STOCK
        )).thenReturn(null)

        patternsDetectionService.detectPatterns(instrument, interval, type)

        verify(trianglePatternServiceImpl).detectPattern(
            figi = figi,
            ticker = instrument.ticker,
            instrumentUid = instrument.instrumentUid,
            interval = interval,
            days = 30,
            type = ItemType.STOCK
        )
        verifyNoMoreInteractions(patternsRepository)
    }

    @Test
    fun `detectPatterns should return early if pattern already exists`() {
        val figi = "figi123"
        val interval = CandleInterval.CANDLE_INTERVAL_1_MIN
        val type = ItemType.STOCK
        val instrument = InstrumentEntity(
            ticker = "ticker",
            figi = figi,
            name = "name",
            instrumentUid = "instrumentUid",
            lot = 100,
            currency = "RUB",
            first1dayCandleDate = null
        )
        val pattern = Pattern(
            patternType = PatternType.TRIANGLE,
            startDate = LocalDateTime.now(),
            aMax = 1.0,
            bMax = 2.0,
            aMin = 0.5,
            bMin = 1.5,
            price = 30.0,
            interval = CandleInterval.CANDLE_INTERVAL_1_MIN,
            figi = figi,
            ticker = instrument.ticker
        )

        `when`(trianglePatternServiceImpl.detectPattern(
            figi = figi,
            ticker = instrument.ticker,
            instrumentUid = instrument.instrumentUid,
            interval = interval,
            days = 30,
            type = ItemType.STOCK
        )).thenReturn(pattern)
        `when`(patternsRepository.findPattern(
            figi = figi,
            maxA = pattern.aMax,
            maxB = pattern.bMax,
            minA = pattern.aMin,
            minB = pattern.bMin
        )).thenReturn(Optional.of(PatternEntity(
            ticker = instrument.ticker,
            patternType = pattern.patternType,
            interval = pattern.interval,
            startDate = pattern.startDate,
            maxA = pattern.aMax,
            maxB = pattern.bMax,
            minA = pattern.aMin,
            minB = pattern.bMin,
            figi = figi,
            instrumentUid = instrument.instrumentUid!!
        )))

        patternsDetectionService.detectPatterns(instrument, interval, type)

        verify(trianglePatternServiceImpl).detectPattern(
            figi = figi,
            ticker = instrument.ticker,
            instrumentUid = instrument.instrumentUid,
            interval = interval,
            days = 30,
            type = ItemType.STOCK
        )
        verify(patternsRepository).findPattern(
            figi = figi,
            maxA = pattern.aMax,
            maxB = pattern.bMax,
            minA = pattern.aMin,
            minB = pattern.bMin
        )
        verifyNoMoreInteractions(patternsRepository)
    }

    @Test
    fun `detectPatterns should save pattern`() {
        val figi = "figi123"
        val interval = CandleInterval.CANDLE_INTERVAL_1_MIN
        val type = ItemType.STOCK
        val instrument = InstrumentEntity(
            ticker = "ticker",
            figi = figi,
            name = "name",
            instrumentUid = "instrumentUid",
            lot = 100,
            currency = "RUB",
            first1dayCandleDate = null
        )
        val pattern = Pattern(
            patternType = PatternType.TRIANGLE,
            startDate = LocalDateTime.now(),
            aMax = 1.0,
            bMax = 2.0,
            aMin = 0.5,
            bMin = 1.5,
            price = 30.0,
            interval = CandleInterval.CANDLE_INTERVAL_1_MIN,
            figi = figi,
            ticker = instrument.ticker
        )

        `when`(trianglePatternServiceImpl.detectPattern(
            figi = figi,
            ticker = instrument.ticker,
            instrumentUid = instrument.instrumentUid,
            interval = interval,
            days = 30,
            type = ItemType.STOCK
        )).thenReturn(pattern)
        `when`(patternsRepository.findPattern(
            figi = figi,
            maxA = pattern.aMax,
            maxB = pattern.bMax,
            minA = pattern.aMin,
            minB = pattern.bMin
        )).thenReturn(Optional.empty())

        patternsDetectionService.detectPatterns(instrument, interval, type)

        verify(trianglePatternServiceImpl).detectPattern(
            figi = figi,
            ticker = instrument.ticker,
            instrumentUid = instrument.instrumentUid,
            interval = interval,
            days = 30,
            type = ItemType.STOCK
        )
        verify(patternsRepository).findPattern(
            figi = figi,
            maxA = pattern.aMax,
            maxB = pattern.bMax,
            minA = pattern.aMin,
            minB = pattern.bMin
        )
        verify(patternsRepository).save(any(PatternEntity::class.java))
    }
}
