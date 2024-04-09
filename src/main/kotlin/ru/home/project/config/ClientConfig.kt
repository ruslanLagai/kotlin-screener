package ru.home.project.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatusCode
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.ClientResponse
import org.springframework.web.reactive.function.client.WebClient
import ru.home.project.exceptions.CoinMarketCapException
import ru.home.project.exceptions.TwelveDataException
import ru.home.project.properties.CoinMarketCapProperties
import ru.home.project.properties.TinkoffProperties
import ru.home.project.properties.TwelveDataProperties
import ru.tinkoff.piapi.core.InvestApi

/**
 * @author rlagay
 */
@Configuration
class ClientConfig(
    val tinkoffProperties: TinkoffProperties,
    val twelveDataProperties: TwelveDataProperties,
    val coinMarketCapProperties: CoinMarketCapProperties
) {

    private val log: Logger = LoggerFactory.getLogger(ClientConfig::class.java)

    @Bean
    fun investApi(): InvestApi {
        return InvestApi.create(tinkoffProperties.token)
    }

    @Bean
    fun twelveDataWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl(twelveDataProperties.url)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultStatusHandler({ status: HttpStatusCode -> status.isError },
                { resp: ClientResponse ->
                    log.info("Status code {}, body: {}", resp.statusCode(), resp.bodyToMono(String::class.java))
                    throw TwelveDataException("log prefix '${resp.logPrefix()}'")
                })
            .build()
    }

    @Bean
    fun coinMarketCapWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl(coinMarketCapProperties.url)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("X-CMC_PRO_API_KEY", coinMarketCapProperties.token)
            .defaultStatusHandler({ status: HttpStatusCode -> status.isError },
                { resp: ClientResponse ->
                    log.info("Status code {}, body: {}", resp.statusCode(), resp.bodyToMono(String::class.java))
                    throw CoinMarketCapException("log prefix '${resp.logPrefix()}'")
                })
            .build()
    }

}