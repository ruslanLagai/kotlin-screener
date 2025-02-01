package ru.home.project.listener

import com.bybit.api.client.domain.market.response.tickers.TickerEntry
import com.google.common.util.concurrent.AtomicDouble
import org.apache.commons.lang3.function.TriFunction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import ru.home.project.dto.AlertType
import ru.home.project.entity.MergedLevelEntity
import ru.home.project.model.*
import ru.home.project.properties.TelegramBotProperties
import ru.home.project.repository.InstrumentRepository
import ru.home.project.repository.LevelStatisticsRepository
import ru.home.project.service.*
import ru.home.project.utils.*
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author rlagay
 */
@Component
class TradeEventListener(
    val closestLevelService: ClosestLevelService,
    val telegramBotGrpcService: TelegramBotGrpcService,
    val candlesService: CandlesService,
    val cryptoCandlesService: CryptoCandlesService,
    val instrumentRepository: InstrumentRepository,
    val telegramBotProperties: TelegramBotProperties,
    val levelStatisticsRepository: LevelStatisticsRepository,
    val tradingBotService: TradingBotService,
    val applicationEventPublisher: ApplicationEventPublisher
) {

    private val log: Logger = LoggerFactory.getLogger(TradeEventListener::class.java)
    private val sentMessages = ConcurrentHashMap<String, LocalDateTime>()
    private val executors: ExecutorService = Executors.newFixedThreadPool(2)

    private val farRetestProcessor = mapOf(
        Pair(ItemType.STOCK, checkTinkoffStock()),
        Pair(ItemType.CRYPTO, checkCrypto())
    )
    
    /**
     * Проверка близости к уровням
     * Если ближе, чем 2%, считаем статистику и шлем оповещение
     */
    @Async("tradeEventExecutor")
    @EventListener
    fun processTradeEvent(event: TradeEvent) {
        processEvent(event, ItemType.STOCK)
    }

    @EventListener
    fun processCryptoPricesEvent(price: TickerEntry) {
        val twelveDataFormatSymbol = price.symbol.replace("USDT", "/USD")
        val event = TradeEvent(price.lastPrice.toDouble(), twelveDataFormatSymbol)
        processEvent(event, ItemType.CRYPTO)
    }
    
    /**
     * Проверка близости к уровням
     * Если ближе, чем 1%, считаем статистику и шлем оповещение
     */

    private fun processEvent(event: TradeEvent, type: ItemType) {
        try {
            val prevMessageDate = sentMessages[event.figi]
            if (prevMessageDate != null) {
                applicationEventPublisher.publishEvent(AlertEvent(event.price, event.figi, type))
                if (prevMessageDate.plusDays(5).isAfter(LocalDateTime.now())) {
                    return
                }
            }

            val level = closestLevelService.getClosestLevel(event.figi, event.price)
            if (level == null) {
                log.debug("Skipping trade event for {}, no close levels", event.figi)
                return
            }
            val levelValue = AtomicDouble()

            val isFarRetest = farRetestProcessor[type]?.apply(event, level, levelValue)
            if (isFarRetest == null || !isFarRetest) {
                log.debug("Close retest for level {}", level.level)
                return
            }

            val instrument = instrumentRepository.getByFigi(event.figi)
            if (instrument == null) {
                log.warn("No record in instrument table for {}", event.figi)
                return
            }

            val levelStatistics = levelStatisticsRepository.getByFigiAndMaxLevel(event.figi, level.maxLevel)
            val statistics = getStatistics(levelStatistics)

            val levelType = levelType(level, event.price)
            val takeProfitPercentage = if (type == ItemType.CRYPTO) cryptoTakeProfitPercentage else stockTakeProfitPercentage
            val stopLossPercentage = if (type == ItemType.CRYPTO) cryptoStopLossPercentage else stockStopLossPercentage

            val takeProfit = if (levelType == LevelType.Support) level.maxLevel + level.maxLevel * takeProfitPercentage else level.minLevel - level.minLevel * takeProfitPercentage
            val stopLoss = if (levelType == LevelType.Support) level.minLevel - level.minLevel * stopLossPercentage else level.maxLevel + level.maxLevel * stopLossPercentage

            val message = String.format(telegramMessage, instrument.ticker, levelValue.get(), levelValue.get(),
                takeProfit, stopLoss, statistics)

            log.info("Detected signal for {}", event.figi)

            executors.execute {
                for (account in telegramBotProperties.accounts) {
                    log.info("Sending alert to trading bot")
                    val alertType = if (levelType == LevelType.Support) AlertType.BUY else AlertType.SELL
                    tradingBotService.sendSignal(ticker = instrument.ticker, accountId = account, alertType = alertType, level = level,
                        isPrimaryTrade = true, takeProfit = takeProfit, stopLoss = stopLoss, price = event.price)
                }
            }
            executors.execute {
                for (account in telegramBotProperties.accounts) {
                    log.info("Sending message on {} to telegram user {}", instrument.ticker, account)
                    telegramBotGrpcService.sendMessage(figi = event.figi, ticker = instrument.ticker, text = message, accountId = account)
                }
            }
            sentMessages[event.figi] = LocalDateTime.now()
        } catch (e: Exception) {
            log.error("Error occurred in trade event", e)
        }

    }
    
    private fun checkTinkoffStock(): TriFunction<TradeEvent, MergedLevelEntity, AtomicDouble, Boolean> {
        return TriFunction { event, level, levelValue ->
            val lastCandles = candlesService.getLastCandles(event.figi, CandleInterval.CANDLE_INTERVAL_DAY, 14)
            val levelType = levelType(level, event.price)
            levelValue.set(if (levelType == LevelType.Support) level.maxLevel else level.minLevel)
            if (LevelType.Support == levelType) {
                lastCandles.all { it.low > level.maxLevel * 1.01 }
            } else {
                lastCandles.all { it.high < level.minLevel * 0.99 }
            }
        }
    }

    private fun checkCrypto(): TriFunction<TradeEvent, MergedLevelEntity, AtomicDouble, Boolean> {
        return TriFunction { event, level, levelValue ->
            // изменил с 10 до 7
            val lastCandles = cryptoCandlesService.getDailyCandles(event.figi, LocalDate.now().minusDays(7))
            val withoutLastCandle = ArrayList<Candle>(lastCandles)
            withoutLastCandle.removeAt(0)

            val levelType = levelType(level, event.price)
            levelValue.set(if (levelType == LevelType.Support) level.maxLevel else level.minLevel)
            if (LevelType.Support == levelType) {
                lastCandles.all { it.low > level.maxLevel * 1.01 }
            } else {
                lastCandles.all { it.high < level.minLevel * 0.99 }
            }
        }
    }
}