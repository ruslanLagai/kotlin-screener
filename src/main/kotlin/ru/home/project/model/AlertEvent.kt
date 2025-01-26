package ru.home.project.model

/**
 * @author rlagay
 */
data class AlertEvent(
    val price: Double,
    val figi: String,
    val type: ItemType
)
