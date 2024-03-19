package ru.home.project.properties

import jakarta.validation.constraints.NotEmpty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated

/**
 * @author rlagay
 */
@Validated
@ConfigurationProperties(value = "service.telegram.bot")
data class TelegramBotProperties(
    @NotEmpty val url: String,
    @NotEmpty val adminAccount: String,
    val accounts: List<String>
)
