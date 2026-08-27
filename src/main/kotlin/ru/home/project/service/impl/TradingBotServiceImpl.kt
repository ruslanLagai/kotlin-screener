package ru.home.project.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.home.project.client.TradingServiceClient
import ru.home.project.dto.AlertDto
import ru.home.project.dto.AlertType
import ru.home.project.entity.ActiveTradeEntity
import ru.home.project.entity.MergedLevelEntity
import ru.home.project.repository.ActiveTradeRepository
import ru.home.project.service.TelegramBotGrpcService
import ru.home.project.service.TradingBotService

/**
 * @author rlagay
 */
@Service
class TradingBotServiceImpl(
    val tradingServiceClient: TradingServiceClient,
    val telegramBotGrpcService: TelegramBotGrpcService,
    val activeTradeRepository: ActiveTradeRepository
) : TradingBotService {

    private val log: Logger = LoggerFactory.getLogger(TradingBotServiceImpl::class.java)

    override fun sendSignal(ticker: String, alertType: AlertType, accountId: String, level: MergedLevelEntity?,
                            isPrimaryTrade: Boolean, takeProfit: Double, stopLoss: Double, price: Double) {
        try {
            log.info("Sending alert to trading bot: ticker {}, alertType {}", ticker, alertType)
            val alert = AlertDto(ticker = ticker, alertType = alertType, takeProfit = takeProfit.toString(),
                stopLoss = stopLoss.toString(), price = price.toString())
            tradingServiceClient.sendAlert(alert)
            if (isPrimaryTrade) {
                level?.id?.let {
                    activeTradeRepository.save(ActiveTradeEntity(ticker = ticker, closed = false, alertType = alertType, mergedLevelId = level.id!!))
                }
            }
        } catch (e: Exception) {
            log.error("Failed to send alert to trading bot: ticker {}, alertType {}", ticker, alertType, e)
            val text = "Не удалось отправить запрос в на обработку сигнала по ${ticker}, $alertType"
            telegramBotGrpcService.sendMessage(ticker = ticker, figi = "", text = text, accountId = accountId)
        }
    }

}