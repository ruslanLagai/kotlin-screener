package ru.home.project.properties

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * @author rlagay
 */
@ConfigurationProperties("ru.twelve-data")
@Validated
data class TwelveDataProperties(
    @NotBlank val url: String,
    @NotBlank val token: List<String>
)