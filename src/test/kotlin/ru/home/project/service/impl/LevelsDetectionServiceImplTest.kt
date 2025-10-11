package ru.home.project.service.impl

import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import ru.home.project.entity.InstrumentEntity
import ru.home.project.entity.LevelEntity
import ru.home.project.model.ItemType
import ru.home.project.repository.InstrumentRepository
import ru.home.project.repository.LevelStatisticsRepository
import ru.home.project.repository.LevelsRepository
import ru.home.project.repository.MergedLevelsRepository
import ru.home.project.service.LevelsDetectionService
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.assertTrue

/**
 * @author rlagay
 */
@SpringBootTest
@Testcontainers
class LevelsDetectionServiceImplTest {

    @Autowired
    private lateinit var levelsDetectionService: LevelsDetectionService

    @Autowired
    private lateinit var levelsRepository: LevelsRepository

    @Autowired
    private lateinit var mergedLevelsRepository: MergedLevelsRepository

    @Autowired
    private lateinit var levelStatisticsRepository: LevelStatisticsRepository

    @MockitoBean
    private lateinit var instrumentRepository: InstrumentRepository

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
            registry.add("spring.flyway.schemas", container::getDatabaseName)
            registry.add("spring.datasource.driver-class-name", container::getDriverClassName)
            registry.add("spring.data.redis.host", redisContainer::getHost)
            registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort)
        }

    }

    @Test
    fun `test gmkn daily levels - no data in DB`() {
        `when`(instrumentRepository.getByFigi("BBG004731489")).thenReturn(
            InstrumentEntity(figi = "BBG004731489", ticker = "GMKN", lot = 1, currency = "rub", name = "Норильский никель"))

        levelsDetectionService.detectLevels("BBG004731489", CandleInterval.CANDLE_INTERVAL_DAY, CandleInterval.CANDLE_INTERVAL_HOUR, ItemType.STOCK)

        val levels = levelsRepository.getLevelsByFigi("BBG004731489")
        assertTrue { levels.isNotEmpty() }

        val levelStatistics = levelStatisticsRepository.getByFigi("BBG004731489")
        assertTrue { levelStatistics.isNotEmpty() }
    }

    @Test
    fun `test ozon daily levels - data in DB`() {
        `when`(instrumentRepository.getByFigi("BBG00Y91R9T3")).thenReturn(
            InstrumentEntity(
                id = 123123,
                figi = "BBG00Y91R9T3",
                ticker = "OZON",
                lot = 1,
                currency = "rub",
                name = "Ozon",
                version = 2
            )
        )
        levelsRepository.save(LevelEntity(figi = "BBG00Y91R9T3", ticker = "OZON", level = 3012.5,
            levelDate = ZonedDateTime.of(2023, 11, 22, 0, 0, 0, 0, ZoneId.of("UTC"))))

        levelsDetectionService.detectLevels("BBG00Y91R9T3", CandleInterval.CANDLE_INTERVAL_DAY, CandleInterval.CANDLE_INTERVAL_HOUR, ItemType.STOCK)

        val levels = levelsRepository.getLevelsByFigi("BBG00Y91R9T3")
        assertTrue { levels.isNotEmpty() }

        val levelStatistics = levelStatisticsRepository.getByFigi("BBG00Y91R9T3")
        assertTrue { levelStatistics.isNotEmpty() }
    }

    @Test
    fun `test SMLT daily levels - result validation`() {
        `when`(instrumentRepository.getByFigi("BBG00F6NKQX3")).thenReturn(
            InstrumentEntity(
                id = 123124, figi = "BBG00F6NKQX3", ticker = "SMLT", lot = 1, currency = "rub", name = "ГК Самолет", version = 2)
        )
        levelsRepository.save(LevelEntity(figi = "BBG00F6NKQX3", ticker = "SMLT", level = 3600.0,
            levelDate = ZonedDateTime.of(2023, 11, 22, 0, 0, 0, 0, ZoneId.of("UTC"))))

        levelsDetectionService.detectLevels("BBG00F6NKQX3", CandleInterval.CANDLE_INTERVAL_DAY, CandleInterval.CANDLE_INTERVAL_HOUR, ItemType.STOCK)

        Thread.sleep(5000)

        val levels = levelsRepository.getLevelsByFigi("BBG00F6NKQX3")
        assertTrue { levels.isNotEmpty() }

        val levelStatistics = levelStatisticsRepository.getByFigi("BBG00F6NKQX3")
        assertTrue { levelStatistics.isNotEmpty() }
    }


    @Test
    fun `test PISO daily levels - result validation`() {
        `when`(instrumentRepository.getByFigi("TCS00A103X66")).thenReturn(
                InstrumentEntity(id = 123120, figi = "TCS00A103X66", ticker = "POSI", lot = 1, currency = "rub",
                    name = "POSI", version = 2)
        )
        levelsDetectionService.detectLevels("TCS00A103X66", CandleInterval.CANDLE_INTERVAL_DAY, CandleInterval.CANDLE_INTERVAL_HOUR, ItemType.STOCK)

        Thread.sleep(5000)

        val levels = levelsRepository.getLevelsByFigi("TCS00A103X66")
        assertTrue { levels.isNotEmpty() }

        val levelStatistics = levelStatisticsRepository.getByFigi("TCS00A103X66")
        assertTrue { levelStatistics.isNotEmpty() }

        val merged = mergedLevelsRepository.getLevelsByFigi("TCS00A103X66")
        assertTrue { merged.isNotEmpty() }
    }

    @Test
    fun `test BTCUSD daily levels`() {
        `when`(instrumentRepository.getByFigi("BTC/USD")).thenReturn(
            InstrumentEntity(figi = "BTC/USD", ticker = "BTC/USD", lot = 1, currency = "usdt", name = "BTC"))

        levelsDetectionService.detectLevels("BTC/USD", CandleInterval.CANDLE_INTERVAL_DAY, CandleInterval.CANDLE_INTERVAL_DAY, ItemType.CRYPTO)

        Thread.sleep(5000)

        val levels = levelsRepository.getLevelsByFigi("BTC/USD")
        assertTrue { levels.isNotEmpty() }

        val levelStatistics = levelStatisticsRepository.getByFigi("BTC/USD")
        assertTrue { levelStatistics.isNotEmpty() }
    }

}