package ru.home.project.service.impl

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.home.project.entity.InstrumentEntity
import ru.home.project.entity.LevelEntity
import ru.home.project.properties.TinkoffTradingProperties
import ru.home.project.repository.InstrumentRepository
import ru.home.project.repository.LevelStatisticsRepository
import ru.home.project.repository.LevelsRepository
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
    private lateinit var levelStatisticsRepository: LevelStatisticsRepository

    @Autowired
    private lateinit var instrumentRepository: InstrumentRepository

    @MockBean
    private lateinit var tinkoffTradingProperties: TinkoffTradingProperties

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
        }

    }

    @Test
    fun `test gmkn daily levels - no data in DB`() {
        instrumentRepository.save(InstrumentEntity(figi = "BBG004731489", ticker = "GMKN", lot = 1, currency = "rub",
            name = "Норильский никель"))

        levelsDetectionService.detectLevels("BBG004731489", CandleInterval.CANDLE_INTERVAL_DAY, CandleInterval.CANDLE_INTERVAL_HOUR)

        val levels = levelsRepository.getLevelsByFigi("BBG004731489")
        assertTrue { levels.isNotEmpty() }

        val levelStatistics = levelStatisticsRepository.getByFigi("BBG004731489")
        assertTrue { levelStatistics.isNotEmpty() }
    }

    @Test
    fun `test ozon daily levels - data in DB`() {
        instrumentRepository.save(InstrumentEntity(figi = "BBG00Y91R9T3", ticker = "OZON", lot = 1, currency = "rub", name = "Ozon"))
        levelsRepository.save(LevelEntity(figi = "BBG00Y91R9T3", ticker = "OZON", level = 3012.5,
            levelDate = ZonedDateTime.of(2023, 11, 22, 0, 0, 0, 0, ZoneId.of("UTC"))))

        levelsDetectionService.detectLevels("BBG00Y91R9T3", CandleInterval.CANDLE_INTERVAL_DAY, CandleInterval.CANDLE_INTERVAL_HOUR)

        val levels = levelsRepository.getLevelsByFigi("BBG00Y91R9T3")
        assertTrue { levels.isNotEmpty() }

        val levelStatistics = levelStatisticsRepository.getByFigi("BBG00Y91R9T3")
        assertTrue { levelStatistics.isNotEmpty() }
    }

}