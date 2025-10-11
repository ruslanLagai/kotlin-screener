package ru.home.project.service.impl

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.testcontainers.junit.jupiter.Testcontainers
import ru.home.project.AbstractIntegrationTest
import ru.home.project.service.CoinMarketCapService

/**
 * @author rlagay
 */
@SpringBootTest
@Testcontainers
@Disabled
class CoinMarketCapServiceImplTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var coinMarketCapService: CoinMarketCapService

    @Test
    fun `test get crypto prices`() {
        val prices = coinMarketCapService.getCurrentPrices()
        assertTrue(prices.size > 50)
    }
}