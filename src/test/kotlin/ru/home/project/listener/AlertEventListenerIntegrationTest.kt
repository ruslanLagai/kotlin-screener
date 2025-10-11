package ru.home.project.listener

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.home.project.AbstractIntegrationTest
import ru.home.project.dto.AlertType
import ru.home.project.entity.ActiveTradeEntity
import ru.home.project.entity.LevelEntity
import ru.home.project.entity.MergedLevelEntity
import ru.home.project.model.ItemType
import ru.home.project.model.events.AlertEvent
import ru.home.project.repository.ActiveTradeRepository
import ru.home.project.repository.LevelsRepository
import ru.home.project.repository.MergedLevelsRepository
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
class AlertEventListenerIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var alertEventListener: AlertEventListener

    @Autowired
    private lateinit var activeTradeRepository: ActiveTradeRepository

    @Autowired
    private lateinit var mergedLevelsRepository: MergedLevelsRepository

    @Autowired
    private lateinit var levelsRepository: LevelsRepository

    @Test
    fun processCryptoPricesEvent() {
        activeTradeRepository.save(ActiveTradeEntity(ticker = "BTC/USD", closed = false, mergedLevelId = "1", alertType = AlertType.SELL))
        mergedLevelsRepository.save(MergedLevelEntity(maxLevel = 102.0, minLevel = 98.0, maxLevelDate = ZonedDateTime.now(),
            minLevelDate = ZonedDateTime.now()))
        levelsRepository.save(LevelEntity(figi = "BTC/USD", ticker = "BTC/USD", level = 102.0, levelDate = ZonedDateTime.now()))
        levelsRepository.save(LevelEntity(figi = "BTC/USD", ticker = "BTC/USD", level = 98.0, levelDate = ZonedDateTime.now()))
        levelsRepository.save(LevelEntity(figi = "BTC/USD", ticker = "BTC/USD", level = 101.0, levelDate = ZonedDateTime.now()))
        levelsRepository.save(LevelEntity(figi = "BTC/USD", ticker = "BTC/USD", level = 99.8, levelDate = ZonedDateTime.now()))
        levelsRepository.save(LevelEntity(figi = "BTC/USD", ticker = "BTC/USD", level = 99.2, levelDate = ZonedDateTime.now()))


        val alertEvent = AlertEvent(price = 100.0, figi = "BTC/USD", type = ItemType.CRYPTO)
        alertEventListener.processCryptoPricesEvent(alertEvent)
    }
}