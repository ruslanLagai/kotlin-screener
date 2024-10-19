package ru.home.project.service.impl

import org.springframework.stereotype.Service
import ru.home.project.client.TradingServiceClient
import ru.home.project.dto.AlertDto
import ru.home.project.dto.AlertType
import ru.home.project.service.TelegramBotGrpcService
import ru.home.project.service.TradingBotService

/**
 * @author rlagay
 */
@Service
class TradingBotServiceImpl(
    val tradingServiceClient: TradingServiceClient,
    val telegramBotGrpcService: TelegramBotGrpcService,
) : TradingBotService {

    override fun sendSignal(ticker: String, alertType: AlertType, accountId: String) {
        kotlin.runCatching {
            tradingServiceClient.sendAlert(AlertDto(ticker = ticker, alertType = alertType))
        }.onFailure {
            val text = "Не удалось отправить запрос в на обработку сигнала по ${ticker}, ${alertType}"
            telegramBotGrpcService.sendMessage(ticker = ticker, figi = "", text = text, accountId = accountId)
        }
    }

}