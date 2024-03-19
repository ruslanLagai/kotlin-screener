package ru.home.project.service.impl

import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.home.project.listener.TradeEventListener
import ru.home.project.model.TradeEvent
import ru.home.project.processor.TinkoffTradesStreamProcessor
import ru.home.project.properties.TinkoffTradingProperties
import ru.home.project.service.TinkoffStreamingService
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue


/**
 * @author rlagay
 */
@SpringBootTest
@Testcontainers
class TinkoffStreamingServiceImplTest {

    @Autowired
    private lateinit var tinkoffStreamingService: TinkoffStreamingService

    @Autowired
    private lateinit var tradesStreamProcessor: TinkoffTradesStreamProcessor

    @MockBean
    private lateinit var tinkoffTradingProperties: TinkoffTradingProperties

    @MockBean
    private lateinit var tradeEventListener: TradeEventListener

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
    fun `test getting trades stream - works only with working stock exchange`() {

        val lock = CountDownLatch(1)
        Mockito.`when`(tinkoffTradingProperties.instruments).thenReturn(listOf("TCS009029540", "BBG00QPYJ5H0"))

        tinkoffStreamingService.subscribeTradingStream()

        Mockito.`when`(tradeEventListener.processTradeEvent(any())).thenAnswer {
            lock.countDown()
        }

        assertTrue { lock.await(30000, TimeUnit.MILLISECONDS) }
        verify(tradeEventListener, atLeast(1)).processTradeEvent(anyVararg(TradeEvent::class))
    }

    @Test
    fun `test getting candles stream`() {
    }
}