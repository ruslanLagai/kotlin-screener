package ru.home.project.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class CoinMarketCapLatestPrices(
    val data: List<CryptoData>,
    val status: Status
)

