package ru.home.project.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import ru.home.project.exceptions.CoinMarketCapException
import ru.home.project.model.CoinMarketCapLatestPrices
import ru.home.project.model.CryptoData

/**
 * @author rlagay
 */
@Component
class CoinMarketCapClient(
    private val coinMarketCapWebClient: WebClient
) {

    private val log: Logger = LoggerFactory.getLogger(CoinMarketCapClient::class.java)

    fun getCryptoCandles() : List<CryptoData>? {
        return coinMarketCapWebClient.get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/listings/latest")
                    .build()
            }
            .retrieve()
            .bodyToMono(CoinMarketCapLatestPrices::class.java)
            .mapNotNull { resp ->
                if (resp.status.errorMessage != null) {
                    throw CoinMarketCapException("Error code: '${resp.status.errorCode}', message: '${resp.status.errorMessage}'")
                }
                resp.data
            }
            .block()

    }
}