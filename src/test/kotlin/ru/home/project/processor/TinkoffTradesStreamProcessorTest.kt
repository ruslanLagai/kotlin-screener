package ru.home.project.processor

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.springframework.context.ApplicationEventPublisher
import ru.home.project.model.TradeEvent
import ru.tinkoff.piapi.contract.v1.MarketDataResponse
import ru.tinkoff.piapi.contract.v1.Quotation
import ru.tinkoff.piapi.contract.v1.Trade

/**
 * @author rlagay
 */
@ExtendWith(MockitoExtension::class)
class TinkoffTradesStreamProcessorTest {

    private val testVal = "FIGI"

    private val eventPublisher = Mockito.mock(ApplicationEventPublisher::class.java)

    private val tinkoffTradesStreamProcessor = TinkoffTradesStreamProcessor(eventPublisher)

    @Test
    fun `test event publishing`() {
        val marketDataResponse = MarketDataResponse.newBuilder().apply {
            trade = Trade.newBuilder().apply {
                figi = testVal
                price = Quotation.newBuilder().apply {
                    nano = 15
                    units = 15
                }.build()
            }.build()
        }.build()
        tinkoffTradesStreamProcessor.process(marketDataResponse)

        verify(eventPublisher).publishEvent(TradeEvent(15.15, testVal))
    }

}