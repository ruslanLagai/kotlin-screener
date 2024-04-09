package ru.home.project.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class USD(
    val price: Double,
    @JsonProperty("volume_24h") val volume: Double?,
    @JsonProperty("volume_change_24h") val volumeChange24: Double?,
    @JsonProperty("percent_change_1h") val percentChange1H: Double?,
    @JsonProperty("percent_change_24h") val percentChange24H: Double?,
    @JsonProperty("percent_change_7d") val percentChange7D: Double?,
    @JsonProperty("percent_change_30d") val percentChange30D: Double?,
    @JsonProperty("percent_change_60d") val percentChange60D: Double?,
    @JsonProperty("percent_change_90d") val percentChange90D: Double?,
    @JsonProperty("market_cap") val marketCap: Double,
    @JsonProperty("last_updated") val updated: ZonedDateTime?
)
