package ru.home.project.model

import java.time.ZonedDateTime

/**
 * @author rlagay
 */
data class CryptoPrice(
    val symbol: String,
    val price: Double,
    val dateTime: ZonedDateTime,
    val name: String
)