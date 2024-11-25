package ru.home.project.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
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

    private val log: Logger = LoggerFactory.getLogger(TradingBotServiceImpl::class.java)

    override fun sendSignal(ticker: String, alertType: AlertType, accountId: String) {
        try {
            log.info("Sending alert to trading bot: ticker {}, alertType {}", ticker, alertType)
            tradingServiceClient.sendAlert(AlertDto(ticker = ticker, alertType = alertType))
        } catch (e: Exception) {
            log.error("Failed to send alert to trading bot: ticker {}, alertType {}", ticker, alertType, e)
            val text = "Не удалось отправить запрос в на обработку сигнала по ${ticker}, ${alertType}"
            telegramBotGrpcService.sendMessage(ticker = ticker, figi = "", text = text, accountId = accountId)
        }
    }

}