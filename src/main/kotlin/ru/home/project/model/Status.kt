package ru.home.project.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class Status(
    @JsonProperty("total_count") val totalCount: Int,
    @JsonProperty("credit_count") val creditCount: Int?,
    @JsonProperty("elapsed") val elapsed: Int?,
    @JsonProperty("error_message") val errorMessage: String?,
    @JsonProperty("error_code") val errorCode: Int,
    @JsonProperty("timestamp") val timestamp: ZonedDateTime?
)