package ru.home.project.service.impl

import jakarta.annotation.PreDestroy
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.home.project.processor.TinkoffTradesStreamProcessor
import ru.home.project.properties.TinkoffTradingProperties
import ru.home.project.service.TinkoffStreamingService
import ru.tinkoff.piapi.core.InvestApi

/**
 * @author rlagay
 */
@Service
class TinkoffStreamingServiceImpl(
    val investApi: InvestApi,
    val tradesStreamProcessor: TinkoffTradesStreamProcessor,
    val tinkoffTradingProperties: TinkoffTradingProperties
) : TinkoffStreamingService {

    val log: Logger = LoggerFactory.getLogger(TinkoffStreamingServiceImpl::class.java)

    override fun subscribeTradingStream() {
        kotlin
            .runCatching { investApi.marketDataStreamService
                .newStream("1", tradesStreamProcessor) { e -> log.error("Error on trading stream", e) }
                .subscribeTrades(tinkoffTradingProperties.instruments) }
            .onFailure {
                log.error("Error on creating trading stream", it)
                investApi.marketDataStreamService.getStreamById("1").cancel()
            }
            .onSuccess {
                log.info("Successfully subscribed to trades stream")
            }
    }

    @PreDestroy
    fun close() {
        if (investApi.marketDataStreamService.streamCount() != 0) {
            investApi.marketDataStreamService.getStreamById("1").cancel()
        }

    }
}