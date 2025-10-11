package ru.home.project.listener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import ru.home.project.dto.AlertType
import ru.home.project.entity.LevelEntity
import ru.home.project.model.events.AlertEvent
import ru.home.project.model.ItemType
import ru.home.project.properties.TelegramBotProperties
import ru.home.project.repository.ActiveTradeRepository
import ru.home.project.repository.LevelsRepository
import ru.home.project.repository.MergedLevelsRepository
import ru.home.project.service.TelegramBotGrpcService
import ru.home.project.service.TradingBotService
import ru.home.project.utils.*
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.function.BiFunction
import java.util.function.Predicate
import kotlin.math.abs

/**
 * @author rlagay
 */
@Component
class AlertEventListener(
    val mergedLevelsRepository: MergedLevelsRepository,
    val activeTradeRepository: ActiveTradeRepository,
    val levelsRepository: LevelsRepository,
    val tradingBotService: TradingBotService,
    val telegramBotGrpcService: TelegramBotGrpcService,
    val telegramBotProperties: TelegramBotProperties) {

    private val log: Logger = LoggerFactory.getLogger(AlertEventListener::class.java)
    private val removePassedLevelsMap: Map<AlertType, BiFunction<Double, LevelEntity, Boolean>> = mapOf(
        Pair(AlertType.BUY, BiFunction { price, level -> supportLevelPredicate(price).test(level) }),
        Pair(AlertType.SELL, BiFunction { price, level -> resistanceLevelPredicate(price).test(level) })
    )

    private val processedAlerts = ConcurrentHashMap<String, Pair<Type, LocalDateTime>>()

    @Async("alertEventExecutor")
    @EventListener
    fun processCryptoPricesEvent(alertEvent: AlertEvent) {
        kotlin.runCatching {
            val activeTrade = activeTradeRepository.getByTickerAndClosedFalse(alertEvent.figi)
            mergedLevelsRepository.findById(activeTrade.mergedLevelId).ifPresentOrElse({ level ->

                val levelsDifference = (level.maxLevel - level.minLevel) / level.maxLevel
                if (levelsDifference < 0.019) {
                    log.debug("Difference between max level & min level < 1.9%")
                    return@ifPresentOrElse
                }

                val accounts = telegramBotProperties.accounts
                val levelType = activeTrade.alertType
                val medianValue = (level.maxLevel + level.minLevel) / 2
                val levelValue = if (levelType == AlertType.BUY) { level.maxLevel } else { level.minLevel }
                val extremumValue = if (levelType == AlertType.BUY) { level.minLevel } else { level.maxLevel }

                if (abs(levelValue - alertEvent.price) / maxOf(levelValue, alertEvent.price) < 0.01) {
                    log.info("Пробитие менее 1% {}", alertEvent.figi)
                    return@ifPresentOrElse
                }

                val levels = ArrayList(levelsRepository.getLevelEntityByLevelBetween(level.minLevel, level.maxLevel))

                levels.removeIf { removePassedLevelsMap[levelType]!!.apply(alertEvent.price, it) }
                levels.removeIf { it.level == level.maxLevel || it.level == level.minLevel }
                val nearestToMedian = levels.minByOrNull { abs(it.level - medianValue) }

                var processedSign = processedAlerts[alertEvent.figi]
                if (processedSign != null && processedSign.second.isBefore(LocalDateTime.now().minusDays(5))) {
                    processedSign = null
                    processedAlerts.remove(alertEvent.figi)
                }

                val takeProfitPercentage = if (alertEvent.type == ItemType.CRYPTO) cryptoTakeProfitPercentage else stockTakeProfitPercentage
                val stopLossPercentage = if (alertEvent.type == ItemType.CRYPTO) cryptoStopLossPercentage else stockStopLossPercentage

                val takeProfit = if (levelType == AlertType.BUY) level.maxLevel + level.maxLevel * takeProfitPercentage else level.minLevel - level.minLevel * takeProfitPercentage
                val stopLoss = if (levelType == AlertType.BUY) level.minLevel - level.minLevel * stopLossPercentage else level.maxLevel + level.maxLevel * stopLossPercentage

                if (processedSign == null) {
                    if (nearestToMedian == null) {
                        log.warn("No nearest to median level, figi {}, median {}", alertEvent.figi, medianValue)
                        return@ifPresentOrElse
                    }
                    if (abs(alertEvent.price - nearestToMedian.level) / maxOf(alertEvent.price, nearestToMedian.level) < 0.003) {
                        accounts.forEach {
                            val message = "Цена подошла к медианному уровню, усреднение по цене ${medianValue}, тикер ${alertEvent.figi}"
                            telegramBotGrpcService.sendMessage(figi = alertEvent.figi, ticker = alertEvent.figi, text = message, accountId = it)
                            tradingBotService.sendSignal(ticker = alertEvent.figi, alertType = levelType,
                                accountId = it, isPrimaryTrade = false, level = null, takeProfit = takeProfit, stopLoss = stopLoss, price = alertEvent.price)
                            processedAlerts[alertEvent.figi] = Pair(Type.medion, LocalDateTime.now())
                        }
                    }
                } else if (processedSign.first == Type.medion) {
                    log.info("Nearest to median level has been passed, ticker {}, price {}, median level {}",
                        alertEvent.figi, alertEvent.price, medianValue)
                    if (abs(alertEvent.price - extremumValue) / maxOf(alertEvent.price, extremumValue) < 0.003) {
                        accounts.forEach {
                            val message = "Цена подошла к последнему уровню, усреднение по цене ${alertEvent.price}, тикер ${alertEvent.figi}"
                            telegramBotGrpcService.sendMessage(figi = alertEvent.figi, ticker = alertEvent.figi, text = message, accountId = it)
                            tradingBotService.sendSignal(ticker = alertEvent.figi, alertType = levelType,
                                accountId = it, isPrimaryTrade = false, level = null, takeProfit = takeProfit, stopLoss = stopLoss, price = alertEvent.price)
                            processedAlerts[alertEvent.figi] = Pair(Type.extremum, LocalDateTime.now())
                        }
                    }
                } },
                { log.warn("No merged level found to tracked alert, ticker {}", alertEvent.figi) }
            )
        }
    }

    private fun supportLevelPredicate(price: Double) = Predicate<LevelEntity> {
        it?.let { it.level > price } ?: true
    }

    private fun resistanceLevelPredicate(price: Double) = Predicate<LevelEntity> {
        it?.let { it.level < price } ?: true
    }

    private enum class Type {
        medion, extremum
    }
}