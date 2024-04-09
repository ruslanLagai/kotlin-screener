package ru.home.project.service.impl

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.home.project.properties.TinkoffTradingProperties
import ru.home.project.service.CoinMarketCapService

/**
 * @author rlagay
 */
@SpringBootTest
@Testcontainers
class CoinMarketCapServiceImplTest {

    @Autowired
    private lateinit var coinMarketCapService: CoinMarketCapService

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
            registry.add("spring.flyway.schemas", container::getDatabaseName)
        }

    }

    @Test
    fun `test get crypto prices`() {
        val prices = coinMarketCapService.getCurrentPrices()
        assertTrue(prices.size > 50)
    }
}