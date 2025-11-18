package ru.home.project.listener

import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockkStatic
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.*
import org.springframework.context.ApplicationEventPublisher
import ru.home.project.entity.ExtremumEntity
import ru.home.project.entity.InstrumentEntity
import ru.home.project.entity.PatternEntity
import ru.home.project.model.PatternType
import ru.home.project.model.events.TradeEvent
import ru.home.project.model.events.Type
import ru.home.project.properties.TelegramBotProperties
import ru.home.project.repository.InstrumentRepository
import ru.home.project.repository.LevelStatisticsRepository
import ru.home.project.repository.PatternsRepository
import ru.home.project.service.*
import ru.home.project.utils.checkPriceIsBetweenLines
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.Duration
import java.time.LocalDateTime
import java.util.Set
import java.util.concurrent.ConcurrentHashMap

class TradeEventListenerUnitTest {

    @Mock
    private lateinit var closestLevelService: ClosestLevelService

    @Mock
    private lateinit var telegramBotGrpcService: TelegramBotGrpcService

    @Mock
    private lateinit var candlesService: CandlesService

    @Mock
    private lateinit var cryptoCandlesService: CryptoCandlesService

    @Mock
    private lateinit var instrumentRepository: InstrumentRepository

    @Mock
    private lateinit var telegramBotProperties: TelegramBotProperties

    @Mock
    private lateinit var levelStatisticsRepository: LevelStatisticsRepository

    @Mock
    private lateinit var tradingBotService: TradingBotService

    @Mock
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @Mock
    private lateinit var patternsRepository: PatternsRepository

    @InjectMocks
    private lateinit var tradeEventListener: TradeEventListener

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        // Use reflection to access private sentMessagesForPatterns
        val sentMessagesField = TradeEventListener::class.java.getDeclaredField("sentMessagesForPatterns")
        sentMessagesField.isAccessible = true
        sentMessagesField.set(tradeEventListener, ConcurrentHashMap<String, LocalDateTime>())
    }

    @Test
    fun `processPattern should not send message when instrument is null`() {
        val event = TradeEvent(100.0, "FIGI123", type = Type.LEVEL)
        whenever(instrumentRepository.getByFigi(event.figi)).thenReturn(null)

        tradeEventListener.processTradeEvent(event)

        verify(telegramBotGrpcService, never()).sendMessage(any(), any(), any(), any())
    }

    @Test
    fun `processPattern should not send message when pattern is not between lines`() {
        val event = TradeEvent(100.0, "FIGI123", type = Type.LEVEL)
        val instrument = InstrumentEntity(
            figi = "FIGI123",
            instrumentUid = "INSTRUMENT_UID",
            ticker = "TICKER",
            currency = "RUB",
            lot = 1,
            name = "NAME"
        )
        val pattern = PatternEntity(
            figi = "FIGI123",
            instrumentUid = "INSTRUMENT_UID",
            ticker = "TICKER",
            interval = CandleInterval.CANDLE_INTERVAL_HOUR,
            patternType = PatternType.TRIANGLE,
            maxA = 1.0,
            maxB = 2.0,
            minA = 3.0,
            minB = 4.0,
            startDate = LocalDateTime.now(),
            finished = true,
            minimums = setOf<ExtremumEntity>() as Set<ExtremumEntity>,
            maximums = setOf<ExtremumEntity>() as Set<ExtremumEntity>
        )

        whenever(instrumentRepository.getByFigi(event.figi)).thenReturn(instrument)
        whenever(patternsRepository.findPatternByFigiAndFinishedIsTrue(event.figi)).thenReturn(listOf(pattern))
        mockkStatic("ru.home.project.utils.ExtremumUtilsKt")
        every { checkPriceIsBetweenLines(any(), any(), any(), any()) } returns Pair(false, 0.0)

        tradeEventListener.processTradeEvent(event)

        verify(telegramBotGrpcService, never()).sendMessage(any(), any(), any(), any())
        verify(patternsRepository).save(pattern)
        assert(pattern.finished)
    }

    //
    @Test
    fun `processPattern should not send message when distance is more than 0_02`() {
        val event = TradeEvent(100.0, "FIGI123", type = Type.LEVEL)
        val instrument = InstrumentEntity(
            figi = "FIGI123",
            instrumentUid = "INSTRUMENT_UID",
            ticker = "TICKER",
            currency = "RUB",
            lot = 1,
            name = "NAME"
        )
        val pattern = PatternEntity(figi = "FIGI123",
            instrumentUid = "INSTRUMENT_UID",
            ticker = "TICKER",
            interval = CandleInterval.CANDLE_INTERVAL_HOUR,
            patternType = PatternType.TRIANGLE,
            maxA = 1.0,
            maxB = 2.0,
            minA = 3.0,
            minB = 4.0,
            startDate = LocalDateTime.now(),
            finished = true,
            minimums = setOf<ExtremumEntity>() as Set<ExtremumEntity>,
            maximums = setOf<ExtremumEntity>() as Set<ExtremumEntity>
        )

        whenever(instrumentRepository.getByFigi(event.figi)).thenReturn(instrument)
        whenever(patternsRepository.findPatternByFigiAndFinishedIsTrue(event.figi)).thenReturn(listOf(pattern))
        mockkStatic("ru.home.project.utils.ExtremumUtilsKt")
        every { checkPriceIsBetweenLines(any(), any(), any(), any()) } returns Pair(true, 120.0)

        tradeEventListener.processTradeEvent(event)

        verify(telegramBotGrpcService, never()).sendMessage(any(), any(), any(), any())
        verify(patternsRepository, never()).save(any())
    }

    @Test
    fun `processPattern should send message and update pattern when distance is less than 0_02`() {
        val event = TradeEvent(100.0, "FIGI123", type = Type.LEVEL)
        val instrument = InstrumentEntity(
            figi = "FIGI123",
            instrumentUid = "INSTRUMENT_UID",
            ticker = "TICKER",
            currency = "RUB",
            lot = 1,
            name = "NAME"
        )
        val pattern = PatternEntity(
            instrumentUid = "INSTRUMENT_UID",
            ticker = "TICKER",
            interval = CandleInterval.CANDLE_INTERVAL_HOUR,
            patternType = PatternType.TRIANGLE,
            maxA = 1.0,
            maxB = 2.0,
            minA = 3.0,
            minB = 4.0,
            startDate = LocalDateTime.now(),
            finished = true,
            figi = "FIGI123",
            minimums = setOf<ExtremumEntity>() as Set<ExtremumEntity>,
            maximums = setOf<ExtremumEntity>() as Set<ExtremumEntity>
        )
        val accounts = listOf("account1", "account2")

        whenever(instrumentRepository.getByFigi(event.figi)).thenReturn(instrument)
        whenever(patternsRepository.findPatternByFigiAndFinishedIsTrue(event.figi)).thenReturn(listOf(pattern))
        whenever(telegramBotProperties.accounts).thenReturn(accounts)
        mockkStatic("ru.home.project.utils.ExtremumUtilsKt")
        every { checkPriceIsBetweenLines(any(), any(), any(), any()) } returns Pair(true, 99.0) // distance = (20000 - 100)/20000 = 19900/20000 = 0.995

        tradeEventListener.processTradeEvent(event)

        verify(patternsRepository).save(pattern)
        assert(pattern.finished)
        await().atMost(Duration.ofSeconds(10)).untilAsserted {
            verify(telegramBotGrpcService, times(accounts.size)).sendMessage(any(), any(), any(), any())
        }
    }

    @Test
    fun `processPattern should not send message when message was sent recently`() {
        val event = TradeEvent(100.0, "FIGI123", type = Type.LEVEL)
        val instrument = InstrumentEntity(
            figi = "FIGI123",
            instrumentUid = "INSTRUMENT_UID",
            ticker = "TICKER",
            currency = "RUB",
            lot = 1,
            name = "NAME"
        )
        val pattern = PatternEntity(
            instrumentUid = "INSTRUMENT_UID",
            ticker = "TICKER",
            interval = CandleInterval.CANDLE_INTERVAL_HOUR,
            patternType = PatternType.TRIANGLE,
            maxA = 1.0,
            maxB = 2.0,
            minA = 3.0,
            minB = 4.0,
            startDate = LocalDateTime.now(),
            finished = true,
            figi = "FIGI123",
            minimums = setOf<ExtremumEntity>() as Set<ExtremumEntity>,
            maximums = setOf<ExtremumEntity>() as Set<ExtremumEntity>
        )

        // Set up sentMessagesForPatterns with recent date
        val sentMessagesField = TradeEventListener::class.java.getDeclaredField("sentMessagesForPatterns")
        sentMessagesField.isAccessible = true
        val sentMessagesMap = sentMessagesField.get(tradeEventListener) as ConcurrentHashMap<String, LocalDateTime>
        sentMessagesMap[event.figi] = LocalDateTime.now().minusDays(1) // Within 5 days

        whenever(instrumentRepository.getByFigi(event.figi)).thenReturn(instrument)
        whenever(patternsRepository.findPatternByFigiAndFinishedIsTrue(event.figi)).thenReturn(listOf(pattern))
        mockkStatic("ru.home.project.utils.ExtremumUtilsKt")
        every { checkPriceIsBetweenLines(any(), any(), any(), any()) } returns Pair(true, 20000.0)

        tradeEventListener.processTradeEvent(event)

        verify(telegramBotGrpcService, never()).sendMessage(any(), any(), any(), any())
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }
}
