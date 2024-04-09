package ru.home.project.properties

import jakarta.validation.constraints.NotEmpty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * @author rlagay
 */
@ConfigurationProperties("service.crypto")
@Validated
data class TwelveDataTradingProperties(
    @NotEmpty val instruments: List<String>,
    val dailyCron: String
)
