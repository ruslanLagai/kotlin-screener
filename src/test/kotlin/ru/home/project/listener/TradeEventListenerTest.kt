package ru.home.project.listener

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.home.project.config.FlywayConfig
import ru.home.project.entity.InstrumentEntity
import ru.home.project.properties.TinkoffTradingProperties
import ru.home.project.repository.InstrumentRepository
import ru.home.project.repository.LevelStatisticsRepository
import ru.home.project.repository.MergedLevelsRepository
import ru.home.project.scheduled.CryptoCandlesReceivingService
import ru.home.project.scheduled.DailyLevelsScanService
import java.time.Duration

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

    companion object {

        @JvmStatic
        @Container
        protected var container = MySQLContainer("mysql:8")

        @JvmStatic
        @DynamicPropertySource
        fun mysqlProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", container::getJdbcUrl)
            registry.add("spring.datasource.username", container::getUsername)
            registry.add("spring.datasource.password", container::getPassword)
            registry.add("spring.datasource.driver-class-name", container::getDriverClassName)
            registry.add("spring.flyway.schemas", container::getDatabaseName)
            registry.add("service.crypto.instruments") { "BTC/USD,ETH/USD" }
        }
    }

    @Test
    fun `test processing crypto prices`() {
        instrumentRepository.save(InstrumentEntity(figi = "BTC/USD", ticker = "BTC/USD", lot = 1, currency = "usdt", name = "Bitcoin"))
        instrumentRepository.save(InstrumentEntity(figi = "ETH/USD", ticker = "ETH/USD", lot = 1, currency = "usdt", name = "ETH"))

        dailyLevelsScanService.scanCryptoLevels()

        Thread.sleep(Duration.ofSeconds(40))

        cryptoCandlesReceivingService.retrieveCandles()

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