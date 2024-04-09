package ru.home.project.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class TwelveDataCandles(
    val meta: Metadata,
    val values: List<Candle>?,
    val status: String,
    val code: Int?,
    val message: String?
)

