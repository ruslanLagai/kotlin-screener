package ru.home.project.listener

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import ru.home.project.AbstractIntegrationTest
import ru.home.project.client.TradingServiceClient
import ru.home.project.entity.MergedLevelEntity
import ru.home.project.repository.LevelStatisticsRepository
import ru.home.project.repository.MergedLevelsRepository
import ru.home.project.scheduled.CryptoCandlesReceivingService
import ru.home.project.scheduled.DailyLevelsScanService
import ru.home.project.service.CandlesService
import ru.home.project.service.ClosestLevelService
import ru.home.project.service.TelegramBotGrpcService
import java.time.Duration
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.BeforeTest

/**
 * @author rlagay
 */
class TradeEventListenerTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var statisticsRepository: LevelStatisticsRepository

    @Autowired
    private lateinit var mergedLevelsRepository: MergedLevelsRepository

    @Autowired
    private lateinit var cryptoCandlesReceivingService: CryptoCandlesReceivingService

    @Autowired
    private lateinit var dailyLevelsScanService: DailyLevelsScanService

    @MockitoBean
    private lateinit var candlesService : CandlesService

    @Autowired
    private lateinit var tradeEventListener: TradeEventListener

    @Autowired
    private lateinit var telegramBotGrpcService: TelegramBotGrpcService

    @Autowired
    private lateinit var tradingServiceClient: TradingServiceClient

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

    @Test
    fun `test processing crypto prices`() {
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
    }
}