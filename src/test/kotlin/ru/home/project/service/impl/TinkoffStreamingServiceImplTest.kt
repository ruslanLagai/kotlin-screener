package ru.home.project.service.impl

import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.kotlin.any
import org.mockito.kotlin.anyVararg
import org.mockito.kotlin.atLeast
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.bean.override.mockito.MockitoBean
import ru.home.project.AbstractIntegrationTest
import ru.home.project.listener.ContextRefreshedEventListener
import ru.home.project.listener.TradeEventListener
import ru.home.project.model.events.TradeEvent
import ru.home.project.service.TinkoffStreamingService
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlin.test.assertTrue


/**
 * @author rlagay
 */
class TinkoffStreamingServiceImplTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var tinkoffStreamingService: TinkoffStreamingService

    @MockitoBean
    private lateinit var contextRefreshedEventListener: ContextRefreshedEventListener

    @MockitoBean
    private lateinit var tradeEventListener: TradeEventListener

    @Test
    fun `test getting trades stream - works only with working stock exchange`() {

        val lock = CountDownLatch(1)
        tinkoffStreamingService.subscribeTradingStream()

        Mockito.`when`(tradeEventListener.processTradeEvent(any())).thenAnswer {
            lock.countDown()
        }

        assertTrue { lock.await(30000, TimeUnit.MILLISECONDS) }
        verify(tradeEventListener, atLeast(1)).processTradeEvent(anyVararg(TradeEvent::class))
    }

}