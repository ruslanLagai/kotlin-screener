package ru.home.project.properties

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * @author rlagay
 */
@ConfigurationProperties("ru.coin-market-cap")
@Validated
data class CoinMarketCapProperties(
    @NotBlank val url: String,
    @NotBlank val token: String
)