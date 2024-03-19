package ru.home.project.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author rlagay
 */
@ConfigurationProperties("service.tinkoff")
data class TinkoffTradingProperties(
    val instruments: List<String>,
    val dailyCron: String
)
