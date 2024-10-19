package ru.home.project.dto;

/**
 * @author rlagay
 */
data class AlertDto (
    val ticker: String,
    val alertType: AlertType
)

enum class AlertType {
    BUY, SELL, TAKE_PROFIT, STOP_LOSS
}