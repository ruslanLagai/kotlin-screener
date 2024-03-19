package ru.home.project.properties

import jakarta.validation.constraints.NotEmpty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * @author rlagay
 */
@ConfigurationProperties("service.tinkoff")
@Validated
data class TinkoffTradingProperties(
    @NotEmpty val instruments: List<String>,
    val dailyCron: String
)
