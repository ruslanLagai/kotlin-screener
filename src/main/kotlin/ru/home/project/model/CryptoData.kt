package ru.home.project.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class CryptoData(
    val id: Int,
    val name: String,
    val symbol: String,
    val slug: String,
    @JsonProperty("date_added") val dateAdded: ZonedDateTime,
    val tags: List<String>?,
    @JsonProperty("last_updated") val lastUpdated: ZonedDateTime,
    val quote: Quote?
)

