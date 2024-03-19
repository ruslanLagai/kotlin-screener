package ru.home.project.properties

import org.springframework.boot.context.properties.ConfigurationProperties

/**
 * @author rlagay
 */
@ConfigurationProperties("ru.tinkoff.piapi.core")
data class TinkoffProperties(
    val token: String,
    val target: String,
    val connectionTimeout: Double,
    val requestTimeout: Double
)
