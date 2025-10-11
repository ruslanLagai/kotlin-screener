package ru.home.project.model.events

import ru.home.project.model.ItemType

/**
 * @author rlagay
 */
data class AlertEvent(
    val price: Double,
    val figi: String,
    val type: ItemType
)