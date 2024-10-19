package ru.home.project.properties

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * @author rlagay
 */
@ConfigurationProperties("ru.trading.service")
@Validated
data class TradingProperties(
    @NotBlank val url: String
)