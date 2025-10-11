package ru.home.project

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName

/**
 * @author rlagay
 */
@SpringBootTest
@Testcontainers
open class AbstractIntegrationTest {

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
            registry.add("service.tinkoff.instruments", { "8e2b0325-0292-4654-8a18-4f63ed3b0e09,e6123145-9665-43e0-8413-cd61b8aa9b13" })

        }
    }
}