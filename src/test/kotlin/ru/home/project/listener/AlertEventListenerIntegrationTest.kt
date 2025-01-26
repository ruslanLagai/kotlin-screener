package ru.home.project.listener

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import ru.home.project.dto.AlertType
import ru.home.project.entity.ActiveTradeEntity
import ru.home.project.entity.LevelEntity
import ru.home.project.entity.MergedLevelEntity
import ru.home.project.model.AlertEvent
import ru.home.project.model.ItemType
import ru.home.project.repository.ActiveTradeRepository
import ru.home.project.repository.LevelsRepository
import ru.home.project.repository.MergedLevelsRepository
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
@SpringBootTest
@Testcontainers
class AlertEventListenerIntegrationTest {

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
            registry.add("service.crypto.instruments") { "BTCUSDT,ETHUSDT,TONUSDT" }
            registry.add("spring.data.redis.host", redisContainer::getHost)
            registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort)
        }
    }

    @Autowired
    private lateinit var alertEventListener: AlertEventListener

    @Autowired
    private lateinit var activeTradeRepository: ActiveTradeRepository

    @Autowired
    private lateinit var mergedLevelsRepository: MergedLevelsRepository

    @Autowired
    private lateinit var levelsRepository: LevelsRepository

    @Test
    fun processCryptoPricesEvent() {
        activeTradeRepository.save(ActiveTradeEntity(ticker = "BTC/USD", closed = false, mergedLevelId = "1", alertType = AlertType.SELL))
        mergedLevelsRepository.save(MergedLevelEntity(maxLevel = 102.0, minLevel = 98.0, maxLevelDate = ZonedDateTime.now(),
            minLevelDate = ZonedDateTime.now()))
        levelsRepository.save(LevelEntity(figi = "BTC/USD", ticker = "BTC/USD", level = 102.0, levelDate = ZonedDateTime.now()))
        levelsRepository.save(LevelEntity(figi = "BTC/USD", ticker = "BTC/USD", level = 98.0, levelDate = ZonedDateTime.now()))
        levelsRepository.save(LevelEntity(figi = "BTC/USD", ticker = "BTC/USD", level = 101.0, levelDate = ZonedDateTime.now()))
        levelsRepository.save(LevelEntity(figi = "BTC/USD", ticker = "BTC/USD", level = 99.8, levelDate = ZonedDateTime.now()))
        levelsRepository.save(LevelEntity(figi = "BTC/USD", ticker = "BTC/USD", level = 99.2, levelDate = ZonedDateTime.now()))


        val alertEvent = AlertEvent(price = 100.0, figi = "BTC/USD", type = ItemType.CRYPTO)
        alertEventListener.processCryptoPricesEvent(alertEvent)
    }
}