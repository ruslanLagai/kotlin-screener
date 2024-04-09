package ru.home.project.service.impl

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.PropertySource
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.web.reactive.function.client.WebClient
import ru.home.project.client.TwelveDataClient
import ru.home.project.config.ClientConfig
import ru.home.project.exceptions.TwelveDataException
import ru.home.project.properties.TinkoffProperties
import ru.home.project.properties.TwelveDataProperties
import ru.home.project.service.CryptoCandlesService
import ru.home.project.util.YamlPropertySourceFactory
import java.time.LocalDate
import kotlin.test.assertEquals

/**
 * @author rlagay
 */
@ContextConfiguration(classes = [ CryptoCandlesServiceImplTest.TestConfig::class ])
@ExtendWith(SpringExtension::class)
class CryptoCandlesServiceImplTest {

    @Autowired
    private lateinit var candlesService: CryptoCandlesService

    @Test
    fun `test getting crypto candles`() {
        val result = candlesService.getDailyCandles("BTC/USD")

        assertTrue(result.isNotEmpty())
    }

    @Test
    fun `test getting crypto candles - from instant`() {
        val result = candlesService.getDailyCandles("BTC/USD", LocalDate.now().minusDays(7))

        assertEquals(7, result.size)
    }

    @Test
    fun `test invalid symbol`() {
        assertThrows<TwelveDataException> { candlesService.getDailyCandles("BTC/USDT") }
    }

    @TestConfiguration
    @ConfigurationPropertiesScan(basePackages = ["ru.home.project.properties"])
    @PropertySource(value = ["classpath:application.yml"], factory = YamlPropertySourceFactory::class)
    @Import(ClientConfig::class)
    class TestConfig {

        @MockBean
        private lateinit var tinkoffProperties: TinkoffProperties

        @Bean
        fun clientConfig(twelveDataProperties: TwelveDataProperties, twelveDataWebClient: WebClient) : TwelveDataClient {
            return TwelveDataClient(twelveDataProperties, twelveDataWebClient)
        }

        @Bean
        fun cryptoCandlesService(twelveDataClient: TwelveDataClient): CryptoCandlesService {
            return CryptoCandlesServiceImpl(twelveDataClient)
        }

    }
}