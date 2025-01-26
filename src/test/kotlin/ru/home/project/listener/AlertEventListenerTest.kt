package ru.home.project.listener

import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.any
import org.mockito.Mockito.`when`
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import ru.home.project.dto.AlertType
import ru.home.project.entity.ActiveTradeEntity
import ru.home.project.entity.LevelEntity
import ru.home.project.entity.MergedLevelEntity
import ru.home.project.model.AlertEvent
import ru.home.project.model.ItemType
import ru.home.project.properties.TelegramBotProperties
import ru.home.project.repository.ActiveTradeRepository
import ru.home.project.repository.LevelsRepository
import ru.home.project.repository.MergedLevelsRepository
import ru.home.project.service.TelegramBotGrpcService
import ru.home.project.service.TradingBotService
import ru.home.project.utils.cryptoStopLossPercentage
import ru.home.project.utils.cryptoTakeProfitPercentage
import ru.home.project.utils.stockStopLossPercentage
import ru.home.project.utils.stockTakeProfitPercentage
import java.time.ZonedDateTime
import java.util.*

/**
 * @author rlagay
 */
class AlertEventListenerTest {

    val mergedLevelsRepository: MergedLevelsRepository = Mockito.mock(MergedLevelsRepository::class.java)
    val activeTradeRepository: ActiveTradeRepository = Mockito.mock(ActiveTradeRepository::class.java)
    val levelsRepository: LevelsRepository = Mockito.mock(LevelsRepository::class.java)
    val tradingBotService: TradingBotService = Mockito.mock(TradingBotService::class.java)
    val telegramBotGrpcService: TelegramBotGrpcService = Mockito.mock(TelegramBotGrpcService::class.java)
    val telegramBotProperties: TelegramBotProperties = Mockito.mock()

    private val alertEventListener = AlertEventListener(mergedLevelsRepository, activeTradeRepository, levelsRepository,
        tradingBotService, telegramBotGrpcService, telegramBotProperties)

    @Test
    fun `test single level`() {
        `when`(activeTradeRepository.getByTickerAndClosedFalse("figi")).thenReturn(
            ActiveTradeEntity(alertType = AlertType.BUY, closed = false, mergedLevelId = "1", ticker = "figi"))
        `when`(mergedLevelsRepository.findById("1")).thenReturn(Optional.of(
            MergedLevelEntity(figi = "figi", maxLevel = 100.5, maxLevelDate = ZonedDateTime.now(), minLevel = 99.5,
                minLevelDate = ZonedDateTime.now())))
        `when`(telegramBotProperties.accounts).thenReturn(listOf("11", "22"))


        val alertEvent = AlertEvent(price = 100.0, figi = "figi", type = ItemType.CRYPTO)
        alertEventListener.processCryptoPricesEvent(alertEvent)

        verify(telegramBotGrpcService, times(0)).sendMessage(any(), any(), any(), any())
    }

    @Test
    fun `test level breaking less 1 percent`() {
        `when`(activeTradeRepository.getByTickerAndClosedFalse("figi")).thenReturn(
            ActiveTradeEntity(alertType = AlertType.BUY, closed = false, mergedLevelId = "1", ticker = "figi"))
        `when`(mergedLevelsRepository.findById("1")).thenReturn(Optional.of(
            MergedLevelEntity(figi = "figi", maxLevel = 101.0, maxLevelDate = ZonedDateTime.now(), minLevel = 99.0,
                minLevelDate = ZonedDateTime.now())))
        `when`(telegramBotProperties.accounts).thenReturn(listOf("11", "22"))


        val alertEvent = AlertEvent(price = 100.0, figi = "figi", type = ItemType.CRYPTO)
        alertEventListener.processCryptoPricesEvent(alertEvent)

        verify(telegramBotGrpcService, times(0)).sendMessage(any(), any(), any(), any())
    }

    @Test
    fun `test price coming to median level`() {
        `when`(activeTradeRepository.getByTickerAndClosedFalse("figi")).thenReturn(
            ActiveTradeEntity(alertType = AlertType.BUY, closed = false, mergedLevelId = "1", ticker = "figi"))
        `when`(mergedLevelsRepository.findById("1")).thenReturn(Optional.of(
            MergedLevelEntity(figi = "figi", maxLevel = 102.0, maxLevelDate = ZonedDateTime.now(), minLevel = 98.0,
                minLevelDate = ZonedDateTime.now())))
        `when`(telegramBotProperties.accounts).thenReturn(listOf("11"))
        `when`(levelsRepository.getLevelEntityByLevelBetween(min = 98.0, max = 102.0)).thenReturn(
            setOf(
                LevelEntity(figi = "figi", level = 98.0, levelDate = ZonedDateTime.now(), ticker = "figi"),
                LevelEntity(figi = "figi", level = 99.7, levelDate = ZonedDateTime.now(), ticker = "figi"),
                LevelEntity(figi = "figi", level = 100.5, levelDate = ZonedDateTime.now(), ticker = "figi"),
                LevelEntity(figi = "figi", level = 100.1, levelDate = ZonedDateTime.now(), ticker = "figi")
            )
        )

        val alertEvent = AlertEvent(price = 100.0, figi = "figi", type = ItemType.STOCK)
        alertEventListener.processCryptoPricesEvent(alertEvent)

        verify(telegramBotGrpcService).sendMessage(figi = "figi", ticker = "figi",
            text = "Цена подошла к медианному уровню, усреднение по цене 100.0, тикер figi", accountId = "11")
        verify(tradingBotService).sendSignal(ticker = "figi", alertType = AlertType.BUY,
            accountId = "11", isPrimaryTrade = false, level = null,
            stopLoss = 98.0 - 98.0 * stockStopLossPercentage,
            takeProfit = 102.0 + 102.0 * stockTakeProfitPercentage,
            price = 100.0)
    }

    @Test
    fun `test price coming to extremum level`() {
        `when`(activeTradeRepository.getByTickerAndClosedFalse("figi")).thenReturn(
            ActiveTradeEntity(alertType = AlertType.BUY, closed = false, mergedLevelId = "1", ticker = "figi"))
        `when`(mergedLevelsRepository.findById("1")).thenReturn(Optional.of(
            MergedLevelEntity(figi = "figi", maxLevel = 102.0, maxLevelDate = ZonedDateTime.now(), minLevel = 98.0,
                minLevelDate = ZonedDateTime.now())))
        `when`(telegramBotProperties.accounts).thenReturn(listOf("11"))
        `when`(levelsRepository.getLevelEntityByLevelBetween(min = 98.0, max = 102.0)).thenReturn(
            setOf(
                LevelEntity(figi = "figi", level = 98.0, levelDate = ZonedDateTime.now(), ticker = "figi"),
                LevelEntity(figi = "figi", level = 98.4, levelDate = ZonedDateTime.now(), ticker = "figi"),
                LevelEntity(figi = "figi", level = 99.7, levelDate = ZonedDateTime.now(), ticker = "figi"),
                LevelEntity(figi = "figi", level = 100.5, levelDate = ZonedDateTime.now(), ticker = "figi"),
                LevelEntity(figi = "figi", level = 100.1, levelDate = ZonedDateTime.now(), ticker = "figi")
            )
        )

        //Сначала отрабатывает медианный уровень
        var alertEvent = AlertEvent(price = 100.0, figi = "figi", type = ItemType.CRYPTO)
        alertEventListener.processCryptoPricesEvent(alertEvent)

        //Не должен отработать, есть уровень 98
        alertEvent = AlertEvent(price = 98.5, figi = "figi", type = ItemType.CRYPTO)
        alertEventListener.processCryptoPricesEvent(alertEvent)

        //Отрабатывает экстремальный уровень
        alertEvent = AlertEvent(price = 98.1, figi = "figi", type = ItemType.CRYPTO)
        alertEventListener.processCryptoPricesEvent(alertEvent)

        //Проверка, что больше не отрабатывает
        alertEvent = AlertEvent(price = 98.095, figi = "figi", type = ItemType.CRYPTO)
        alertEventListener.processCryptoPricesEvent(alertEvent)

        verify(telegramBotGrpcService).sendMessage(figi = "figi", ticker = "figi",
            text = "Цена подошла к медианному уровню, усреднение по цене 100.0, тикер figi", accountId = "11")
        verify(tradingBotService, times(1)).sendSignal(ticker = "figi", alertType = AlertType.BUY,
            accountId = "11", isPrimaryTrade = false, level = null,
            takeProfit = 102.0 + 102.0 * cryptoTakeProfitPercentage,
            stopLoss = 98.0 - 98.0 * cryptoStopLossPercentage,
            price = 100.0)
        verify(tradingBotService, times(1)).sendSignal(ticker = "figi", alertType = AlertType.BUY,
            accountId = "11", isPrimaryTrade = false, level = null,
            takeProfit = 102.0 + 102.0 * cryptoTakeProfitPercentage,
            stopLoss = 98.0 - 98.0 * cryptoStopLossPercentage,
            price = 98.1)
        verify(telegramBotGrpcService).sendMessage(figi = "figi", ticker = "figi",
            text = "Цена подошла к последнему уровню, усреднение по цене 98.1, тикер figi", accountId = "11")
    }
}