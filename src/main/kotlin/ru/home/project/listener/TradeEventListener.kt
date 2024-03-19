package ru.home.project.listener

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import ru.home.project.entity.MergedLevelEntity
import ru.home.project.model.LevelType
import ru.home.project.model.TradeEvent
import ru.home.project.properties.TelegramBotProperties
import ru.home.project.repository.InstrumentRepository
import ru.home.project.repository.LevelStatisticsRepository
import ru.home.project.service.CandlesService
import ru.home.project.service.ClosestLevelService
import ru.home.project.service.TelegramBotGrpcService
import ru.home.project.service.impl.LevelStatisticServiceImpl
import ru.home.project.telegram.bot.TelegramMessageServiceGrpc.TelegramMessageServiceStub
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.util.concurrent.ConcurrentHashMap

/**
 * @author rlagay
 */
@Component
class TradeEventListener(
    val closestLevelService: ClosestLevelService,
    val telegramBotGrpcService: TelegramBotGrpcService,
    val candlesService: CandlesService,
    val instrumentRepository: InstrumentRepository,
    val telegramBotProperties: TelegramBotProperties
) {

    private val log: Logger = LoggerFactory.getLogger(TradeEventListener::class.java)

    /**
     * Проверка близости к уровням
     * Если ближе, чем 2%, считаем статистику и шлем оповещение
     */
    @Async("tradeEventExecutor")
    @EventListener
    fun processTradeEvent(event: TradeEvent) {
        val level = closestLevelService.getClosestLevel(event.figi, event.price)
        if (level == null) {
            log.debug("Skipping trade event for {}, no close levels", event.figi)
            return
        }

        val lastCandles = candlesService.getLastCandlesFromDb(event.figi, CandleInterval.CANDLE_INTERVAL_DAY)
        val levelType = if (level.minLevel > event.price) LevelType.Support else LevelType.Resistance
        val isFarRetest = if (LevelType.Support == levelType) {
            lastCandles.all { it.low > level.maxLevel }
        } else {
            lastCandles.all { it.high < level.minLevel }
        }
        if (!isFarRetest) {
            log.debug("Close retest for level {}", level.level)
            return
        }

        val instrument = instrumentRepository.getByFigi(event.figi)
        if (instrument == null) {
            log.warn("No record in instrument table for {}", event.figi)
            return
        }

        telegramBotProperties.accounts.forEach {
            telegramBotGrpcService.sendMessage(figi = event.figi, ticker = instrument.ticker, text = "", accountId = it)
        }
    }
}