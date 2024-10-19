package ru.home.project.client

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import ru.home.project.dto.AlertDto

/**
 * @author rlagay
 */
@Component
class TradingServiceClient(
    private val tradingWebClient: WebClient
) {

    fun sendAlert(request: AlertDto): Void? {
        return tradingWebClient.post()
            .uri { uriBuilder: UriBuilder -> uriBuilder.path("/alert").build() }
            .body(BodyInserters.fromValue(request))
            .retrieve()
            .bodyToMono(Void::class.java)
            .block()

    }
}