package ru.home.project.scheduled

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import ru.home.project.properties.TinkoffTradingProperties
import ru.home.project.service.LevelsDetectionService
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.Duration

/**
 * @author rlagay
 */
@Service
class DailyLevelsScanService(
    val levelsDetectionService: LevelsDetectionService,
    val tinkoffTradingProperties: TinkoffTradingProperties
) {

    private val log: Logger = LoggerFactory.getLogger(DailyLevelsScanService::class.java)

    @Scheduled(cron = "\${service.tinkoff.daily-cron}")
    fun scanLevels() {
        kotlin.runCatching {
            tinkoffTradingProperties.instruments.forEach {
                levelsDetectionService.detectLevels(it, CandleInterval.CANDLE_INTERVAL_DAY, CandleInterval.CANDLE_INTERVAL_HOUR)
                Thread.sleep(Duration.ofSeconds(120))
            }
        }.onFailure {
            log.error("Failed to detect levels", it)
        }
    }
}