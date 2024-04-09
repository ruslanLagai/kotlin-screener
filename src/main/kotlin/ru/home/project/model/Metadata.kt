package ru.home.project.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonProperty

data class Metadata(
    @JsonProperty("1: Symbol") @JsonAlias("symbol") val symbol: String,
    @JsonProperty("4: Interval") @JsonAlias("interval") val interval: String,
    @JsonProperty("currency_base") val currencyBase: String?,
    @JsonProperty("currency_quote") val currencyQuote: String?,
    @JsonProperty("exchange") val exchange: String?,
    @JsonProperty("type") val type: String?
)