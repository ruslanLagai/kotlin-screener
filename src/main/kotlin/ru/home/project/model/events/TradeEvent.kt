package ru.home.project.model.events

/**
 * @author rlagay
 */
data class TradeEvent(
    val price: Double,
    val figi: String,
    val uuid: String? = null,
    val type: Type
)

enum class Type(val value: String) {
    LEVEL("stock"),
    PATTERN("crypto")
}