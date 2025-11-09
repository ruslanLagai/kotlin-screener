package ru.home.project.config

import com.bybit.api.client.config.BybitApiConfig
import com.bybit.api.client.restApi.BybitApiAsyncMarketDataRestClient
import com.bybit.api.client.service.BybitApiClientFactory
import io.grpc.Channel
import io.grpc.Metadata
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import io.grpc.stub.MetadataUtils
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
import ru.home.project.exceptions.TradingSignalException
import ru.home.project.exceptions.TwelveDataException
import ru.home.project.properties.CoinMarketCapProperties
import ru.home.project.properties.TinkoffProperties
import ru.home.project.properties.TradingProperties
import ru.home.project.properties.TwelveDataProperties
import ru.tinkoff.piapi.contract.v1.InstrumentsServiceGrpc
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc
import ru.ttech.piapi.core.connector.ServiceStubFactory
import ru.ttech.piapi.core.connector.internal.LoggingDebugInterceptor
import java.util.concurrent.TimeUnit

/**
 * @author rlagay
 */
@Configuration
class ClientConfig(
    val tinkoffProperties: TinkoffProperties,
    val twelveDataProperties: TwelveDataProperties,
    val coinMarketCapProperties: CoinMarketCapProperties,
    val tradingProperties: TradingProperties,
    val serviceStubFactory: ServiceStubFactory
) {

    private val log: Logger = LoggerFactory.getLogger(ClientConfig::class.java)

    @Bean
    fun marketDataServiceBlockingStub(): MarketDataServiceGrpc.MarketDataServiceBlockingStub {
        return serviceStubFactory.newSyncService {
            MarketDataServiceGrpc.newBlockingStub(it)
        }.stub
    }

    @Bean
    fun instrumentsServiceBlockingStub(): InstrumentsServiceGrpc.InstrumentsServiceBlockingV2Stub {
        return serviceStubFactory.newSyncService { InstrumentsServiceGrpc.newBlockingV2Stub(it) }.stub
    }

    private fun channel(): Channel {
        val headers = Metadata()
        headers.put(Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer ${tinkoffProperties.token}")
        return NettyChannelBuilder.forTarget(tinkoffProperties.target)
            .intercept(MetadataUtils.newAttachHeadersInterceptor(headers))
            .intercept(LoggingDebugInterceptor())
            .useTransportSecurity()
            .idleTimeout(240, TimeUnit.SECONDS)
            .keepAliveTime(120, TimeUnit.SECONDS)
            .keepAliveTimeout(120, TimeUnit.SECONDS)
            .keepAliveWithoutCalls(true)
            .build()
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

    @Bean
    fun tradingWebClient(): WebClient {
        return WebClient.builder()
            .baseUrl(tradingProperties.url)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultStatusHandler({ status: HttpStatusCode -> status.isError },
                { resp: ClientResponse ->
                    log.error("Status code {}, body: {}", resp.statusCode(), resp.bodyToMono(String::class.java))
                    throw TradingSignalException("Error from trading bot service '${resp.statusCode()}'")
                })
            .build()
    }

    @Bean
    fun bybitApiAsyncMarketDataRestClient() : BybitApiAsyncMarketDataRestClient {
        return BybitApiClientFactory.newInstance("GGlC2mwRe4rhePVa1D", "UJHvckIQoHP4B2wJyg70HiDUH9R3JRvbG4VQ", BybitApiConfig.MAINNET_DOMAIN, true)
            .newAsyncMarketDataRestClient()
    }

}