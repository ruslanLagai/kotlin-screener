package ru.home.project.service

import ru.home.project.dto.AlertType
import ru.home.project.entity.MergedLevelEntity

/**
 * @author rlagay
 */
interface TradingBotService {

    fun sendSignal(ticker: String, alertType: AlertType, accountId: String, level: MergedLevelEntity?,
                   isPrimaryTrade: Boolean, takeProfit: Double, stopLoss: Double, price: Double)
}