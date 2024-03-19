package ru.home.project.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import ru.home.project.properties.TinkoffProperties
import ru.tinkoff.piapi.core.InvestApi
import ru.tinkoff.piapi.core.MarketDataService

/**
 * @author rlagay
 */
@Configuration
class ClientConfig(
    val tinkoffProperties: TinkoffProperties
) {

    @Bean
    fun investApi(): InvestApi {
        return InvestApi.create(tinkoffProperties.token)
    }

}