package ru.home.project.scheduled

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.home.project.model.ItemType
import ru.home.project.properties.TinkoffTradingProperties
import ru.home.project.properties.TwelveDataTradingProperties
import ru.home.project.service.LevelsDetectionService
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.Duration

/**
 * @author rlagay
 */
@Service
class DailyLevelsScanService(
    val levelsDetectionService: LevelsDetectionService,
    val tinkoffTradingProperties: TinkoffTradingProperties,
    val twelveDataTradingProperties: TwelveDataTradingProperties
) {

    private val log: Logger = LoggerFactory.getLogger(DailyLevelsScanService::class.java)

    @Scheduled(cron = "\${service.tinkoff.daily-cron}")
    fun scanLevels() {
        tinkoffTradingProperties.instruments.forEach {
            kotlin.runCatching {
                log.info("Starting levels scan for {}", it)
                levelsDetectionService.detectLevels(it, CandleInterval.CANDLE_INTERVAL_DAY, CandleInterval.CANDLE_INTERVAL_HOUR, ItemType.STOCK)
                Thread.sleep(Duration.ofSeconds(120))
            }.onFailure {
                log.error("Failed to detect levels", it)
            }
        }
    }

    @Scheduled(cron = "\${service.crypto.daily-cron}")
    fun scanCryptoLevels() {
        twelveDataTradingProperties.instruments.forEach {
            kotlin.runCatching {
                log.info("Starting crypto levels scan for {}", it)
                levelsDetectionService.detectLevels(
                    it,
                    CandleInterval.CANDLE_INTERVAL_DAY,
                    CandleInterval.CANDLE_INTERVAL_DAY,
                    ItemType.CRYPTO
                )
                Thread.sleep(Duration.ofSeconds(20))
            }.onFailure {
                log.error("Failed to detect crypto levels", it)
            }
        }
    }
}