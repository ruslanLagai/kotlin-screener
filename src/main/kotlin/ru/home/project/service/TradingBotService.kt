package ru.home.project.service

import ru.home.project.dto.AlertType

/**
 * @author rlagay
 */
interface TradingBotService {

    fun sendSignal(ticker: String, alertType: AlertType, accountId: String)
}