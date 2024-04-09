package ru.home.project.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * @author rlagay
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class Quote(
    @JsonProperty("USD") val usd: USD?
)