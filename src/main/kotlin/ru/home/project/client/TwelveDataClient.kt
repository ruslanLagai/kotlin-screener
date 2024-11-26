package ru.home.project.client

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.util.UriBuilder
import ru.home.project.exceptions.TwelveDataException
import ru.home.project.model.Candle
import ru.home.project.model.TwelveDataCandles
import ru.home.project.properties.TwelveDataProperties

/**
 * @author rlagay
 */
@Component
class TwelveDataClient(
    private val twelveDataProperties: TwelveDataProperties,
    private val twelveDataWebClient: WebClient
) {

    fun getCryptoCandles(symbol: String, interval: String, size: Int) : List<Candle>? {
        return twelveDataWebClient
            .mutate().codecs {configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024)}.build()
            .get()
            .uri { uriBuilder: UriBuilder ->
                uriBuilder
                    .path("/time_series")
                    .queryParam("symbol", symbol)
                    .queryParam("interval", interval)
                    .queryParam("apikey", twelveDataProperties.token.random())
                    .queryParam("outputsize", size)
                    .build()
            }
            .retrieve()
            .bodyToMono(TwelveDataCandles::class.java)
            .mapNotNull { resp ->
                if ("error" == resp.status) {
                    throw TwelveDataException("Error for '${symbol}'\n code: '${resp.code}', message: '${resp.message}'")
                }
                resp.values
            }
            .block()

    }
}