package ru.home.project.listener

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import ru.home.project.config.FlywayConfig
import ru.home.project.entity.CandleEntity
import ru.home.project.entity.InstrumentEntity
import ru.home.project.entity.MergedLevelEntity
import ru.home.project.model.TradeEvent
import ru.home.project.properties.TinkoffTradingProperties
import ru.home.project.repository.CandlesRepository
import ru.home.project.repository.InstrumentRepository
import ru.home.project.repository.LevelStatisticsRepository
import ru.home.project.repository.MergedLevelsRepository
import ru.home.project.scheduled.CryptoCandlesReceivingService
import ru.home.project.scheduled.DailyLevelsScanService
import ru.home.project.service.ClosestLevelService
import ru.home.project.service.TelegramBotGrpcService
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.BeforeTest

/**
 * @author rlagay
 */
@SpringBootTest
@Testcontainers
class TradeEventListenerTest {

    @MockBean
    private lateinit var tinkoffTradingProperties: TinkoffTradingProperties

    @MockBean
    private lateinit var flywayConfig: FlywayConfig

    @Autowired
    private lateinit var statisticsRepository: LevelStatisticsRepository

    @Autowired
    private lateinit var mergedLevelsRepository: MergedLevelsRepository

    @Autowired
    private lateinit var cryptoCandlesReceivingService: CryptoCandlesReceivingService

    @Autowired
    private lateinit var dailyLevelsScanService: DailyLevelsScanService

    @Autowired
    private lateinit var instrumentRepository: InstrumentRepository

    @Autowired
    private lateinit var tradeEventListener: TradeEventListener

    @MockBean
    private lateinit var candlesRepository: CandlesRepository

    @MockBean
    private lateinit var telegramBotGrpcService: TelegramBotGrpcService

    @Autowired
    private lateinit var closestLevelService: ClosestLevelService

    @BeforeTest
    fun init() {
        val level = MergedLevelEntity(figi = "BBG004S68B31", minLevel = 76.2, maxLevel = 76.2,
            minLevelDate = ZonedDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneId.of("UTC")),
            maxLevelDate = ZonedDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneId.of("UTC"))
        )
        mergedLevelsRepository.save(level)
        closestLevelService.addLevel("BBG004S68B31", level)
    }

    companion object {

        @JvmStatic
        @Container
        protected var container = MySQLContainer("mysql:8")

        @Container
        protected var redisContainer: GenericContainer<Nothing> = GenericContainer<Nothing>(DockerImageName.parse("redis:5.0.3-alpine"))
            .withExposedPorts(6379)

        @JvmStatic
        @DynamicPropertySource
        fun mysqlProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", container::getJdbcUrl)
            registry.add("spring.datasource.username", container::getUsername)
            registry.add("spring.datasource.password", container::getPassword)
            registry.add("spring.datasource.driver-class-name", container::getDriverClassName)
            registry.add("spring.flyway.schemas", container::getDatabaseName)
            registry.add("service.crypto.instruments") { "BTC/USD,ETH/USD,TON/USD" }
            registry.add("spring.data.redis.host", redisContainer::getHost)
            registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort)

        }
    }

    @Test
    fun `test processing crypto prices`() {
        instrumentRepository.save(InstrumentEntity(figi = "BTC/USD", ticker = "BTC/USD", lot = 1, currency = "usdt", name = "Bitcoin"))
        instrumentRepository.save(InstrumentEntity(figi = "ETH/USD", ticker = "ETH/USD", lot = 1, currency = "usdt", name = "ETH"))
        instrumentRepository.save(InstrumentEntity(figi = "TON/USD", ticker = "TON/USD", lot = 1, currency = "usdt", name = "Toncoin"))

        dailyLevelsScanService.scanCryptoLevels()

        closestLevelService.addLevel("TON/USD", MergedLevelEntity(figi = "TON/USD", price = 0.0, minLevel = 5.77, maxLevel = 5.77,
            maxLevelDate = ZonedDateTime.now(), minLevelDate = ZonedDateTime.now())
        )

        Thread.sleep(Duration.ofSeconds(20))

        cryptoCandlesReceivingService.retrieveCandles()

        Thread.sleep(Duration.ofSeconds(20))

        var levels = mergedLevelsRepository.getLevelsByFigi("BTC/USD")
        assertTrue(levels.size > 5)
        var statistics = statisticsRepository.getByFigi("BTC/USD")
        assertTrue(statistics.size > 5)

        levels = mergedLevelsRepository.getLevelsByFigi("ETH/USD")
        assertTrue(levels.size > 5)
        statistics = statisticsRepository.getByFigi("ETH/USD")
        assertTrue(statistics.size > 5)

        cryptoCandlesReceivingService.retrieveCandles()
    }

    @Test
    fun `process tinkoff trade event`() {
        instrumentRepository.save(InstrumentEntity(figi = "BBG004S68B31", ticker = "ALRS", lot = 10, currency = "rub", name = "Алроса"))
        val candles = listOf(
            CandleEntity(figi = "BBG004S68B31", close = 75.58, high = 75.73, low = 75.41, open = 75.63,
                dateTime = ZonedDateTime.of(2024, 4, 19, 0, 0, 0, 0, ZoneId.of("UTC")),
                interval = CandleInterval.CANDLE_INTERVAL_DAY, volume = 1),
            CandleEntity(figi = "BBG004S68B31", close = 75.5, high = 76.21, low = 75.2, open = 75.99,
                dateTime = ZonedDateTime.of(2024, 4, 18, 0, 0, 0, 0, ZoneId.of("UTC")),
                interval = CandleInterval.CANDLE_INTERVAL_DAY, volume = 1),
            CandleEntity(figi = "BBG004S68B31", close = 75.87, high = 76.74, low = 75.83, open = 76.4,
                dateTime = ZonedDateTime.of(2024, 4, 17, 0, 0, 0, 0, ZoneId.of("UTC")),
                interval = CandleInterval.CANDLE_INTERVAL_DAY, volume = 1),
            CandleEntity(figi = "BBG004S68B31", close = 76.39, high = 77.1, low = 75.68, open = 76.68,
                dateTime = ZonedDateTime.of(2024, 4, 16, 0, 0, 0, 0, ZoneId.of("UTC")),
                interval = CandleInterval.CANDLE_INTERVAL_DAY, volume = 1),
            CandleEntity(figi = "BBG004S68B31", close = 76.75, high = 77.2, low = 76.57, open = 76.88,
                dateTime = ZonedDateTime.of(2024, 4, 15, 0, 0, 0, 0, ZoneId.of("UTC")),
                interval = CandleInterval.CANDLE_INTERVAL_DAY, volume = 1)
        )
        Mockito.`when`(candlesRepository.findTop14ByFigiAndIntervalOrderByDateTimeDesc(any(), any())).thenReturn(candles)
        tradeEventListener.processTradeEvent(TradeEvent(price = 75.92, figi = "BBG004S68B31"))

        verify(telegramBotGrpcService.sendMessage(any(), any(), any(), any()), times(0))
    }
}