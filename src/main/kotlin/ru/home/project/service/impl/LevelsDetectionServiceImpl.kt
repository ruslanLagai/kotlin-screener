package ru.home.project.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.home.project.entity.CandleEntity
import ru.home.project.entity.LevelEntity
import ru.home.project.processor.DailyLevelProcessor
import ru.home.project.repository.InstrumentRepository
import ru.home.project.repository.LevelsRepository
import ru.home.project.repository.MergedLevelsRepository
import ru.home.project.service.CandlesService
import ru.home.project.service.ClosestLevelService
import ru.home.project.service.LevelStatisticService
import ru.home.project.service.LevelsDetectionService
import ru.home.project.utils.mergeLevels
import ru.home.project.utils.retrieveDatesAroundLevel
import ru.tinkoff.piapi.contract.v1.CandleInterval

/**
 * @author rlagay
 */
@Service
class LevelsDetectionServiceImpl(
    val candlesService: CandlesService,
    val levelsRepository: LevelsRepository,
    val mergedLevelsRepository: MergedLevelsRepository,
    val instrumentRepository: InstrumentRepository,
    val levelProcessor: DailyLevelProcessor,
    val levelStatisticService: LevelStatisticService,
    val closestLevelService: ClosestLevelService
): LevelsDetectionService {

    val log: Logger = LoggerFactory.getLogger(LevelsDetectionServiceImpl::class.java)

    /**
     * Detect levels for selected interval
     *
     * @param figi - figi
     * @param interval - interval to detect levels
     * @param intervalForStatistics - interval to collect statistics
     */
    override fun detectLevels(figi: String, interval: CandleInterval, intervalForStatistics: CandleInterval) {
        val instrument = instrumentRepository.getByFigi(figi)
        if (instrument == null) {
            log.warn("Instrument is not in instrument_entity")
            return
        }

        val candles = ArrayList<CandleEntity>()
        val future = candlesService.getHistoricalCandles(figi = figi, ticker = instrument.ticker, interval = interval, allCandles = candles)
        val levels = levelsRepository.getLevelsByFigi(figi)
        val latestLevel = levels.maxOfOrNull { it.levelDate }

        future.get()
        if (candles.isEmpty()) {
            log.error("No candles for {}", figi)
            return
        }

        if (latestLevel != null) {
            candles.removeIf { it.dateTime.isBefore(latestLevel) }
        }
        val detectedLevels = levelProcessor.processStock(figi, candles)
            .map { LevelEntity(figi = figi, ticker = instrument.ticker, level = it.second, levelDate = it.first) }
        levelsRepository.saveAll(detectedLevels)

        val mergedLevels = mergeLevels(detectedLevels)
        mergedLevelsRepository.saveAll(mergedLevels)
        mergedLevels.forEach {
            // отфильтровать свечи возле уровня, взять даты свечей и по ним запрашивать часовые свечи
            val datesToRetrieveHourlyCandles = retrieveDatesAroundLevel(candles, it)
            val hourlyCandles = candlesService.getCandlesForIntervals(figi, instrument.ticker, intervalForStatistics, datesToRetrieveHourlyCandles)
            levelStatisticService.analyzeStock(figi, instrument.ticker, it, intervalForStatistics, hourlyCandles)
            closestLevelService.addLevel(figi, it)
        }
    }
}