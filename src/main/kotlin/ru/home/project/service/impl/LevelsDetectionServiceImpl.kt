package ru.home.project.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import ru.home.project.entity.CandleEntity
import ru.home.project.entity.InstrumentEntity
import ru.home.project.entity.LevelEntity
import ru.home.project.entity.MergedLevelEntity
import ru.home.project.exceptions.InvalidTypeException
import ru.home.project.model.ItemType
import ru.home.project.processor.DailyLevelProcessor
import ru.home.project.repository.InstrumentRepository
import ru.home.project.repository.LevelsRepository
import ru.home.project.repository.MergedLevelsRepository
import ru.home.project.service.*
import ru.home.project.utils.mergeLevels
import ru.home.project.utils.retrieveDatesAroundLevel
import ru.tinkoff.piapi.contract.v1.CandleInterval
import ru.tinkoff.piapi.contract.v1.HistoricCandle
import java.time.Duration
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.concurrent.CompletableFuture

/**
 * @author rlagay
 */
@Service
class LevelsDetectionServiceImpl(
    val candlesService: CandlesService,
    val cryptoCandlesService: CryptoCandlesService,
    val levelsRepository: LevelsRepository,
    val mergedLevelsRepository: MergedLevelsRepository,
    val instrumentRepository: InstrumentRepository,
    val levelProcessor: DailyLevelProcessor,
    val levelStatisticService: LevelStatisticService,
    val closestLevelService: ClosestLevelService
): LevelsDetectionService {

    val log: Logger = LoggerFactory.getLogger(LevelsDetectionServiceImpl::class.java)

    private val tinkoffCandlesProvider: (String, InstrumentEntity, CandleInterval, ArrayList<CandleEntity>) -> CompletableFuture<MutableList<HistoricCandle>> =
        {
            figi, instrument, interval, candles ->
            candlesService.getHistoricalCandles(figi = figi, ticker = instrument.ticker, interval = interval, allCandles = candles)
        }

    private val cryptoCandlesProvider: (String, InstrumentEntity, CandleInterval, ArrayList<CandleEntity>) -> CompletableFuture<MutableList<HistoricCandle>> =
        {
            symbol, _, _, candles ->
            val result = cryptoCandlesService.getDailyCandles(symbol = symbol)
                .map {
                    val instant = it.datetime.toInstant(ZoneOffset.UTC)
                    CandleEntity(figi = symbol, open = it.open, close = it.close, low = it.low, high = it.high, volume = 0,
                        dateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC), interval = CandleInterval.CANDLE_INTERVAL_DAY)
                }
            candles.addAll(result)
            CompletableFuture.completedFuture(mutableListOf())
        }

    private val noCandlesProvider: (String, InstrumentEntity, CandleInterval, ArrayList<CandleEntity>) -> CompletableFuture<MutableList<HistoricCandle>> =
        {
            _, _, _, _ -> throw InvalidTypeException("Unknown type")
        }

    private val analyzeHourlyInterval: (ArrayList<CandleEntity>, MergedLevelEntity, String, InstrumentEntity) -> Unit =
        {
            candles, level, figi, instrument ->
                val datesToRetrieveHourlyCandles = retrieveDatesAroundLevel(candles, level)
                val hourlyCandles = candlesService.getCandlesForIntervals(figi, instrument.ticker,
                    CandleInterval.CANDLE_INTERVAL_HOUR, datesToRetrieveHourlyCandles)
                levelStatisticService.analyzeStock(figi, instrument.ticker, level, hourlyCandles)
                closestLevelService.addLevel(figi, level)
        }

    private val analyzeDailyInterval: (ArrayList<CandleEntity>, MergedLevelEntity, String, InstrumentEntity) -> Unit =
        {
            candles, level, symbol, instrument ->
            levelStatisticService.analyzeStock(symbol, instrument.ticker, level, candles)
            closestLevelService.addLevel(symbol, level)
        }

    private val candlesProviders = mapOf(
        Pair(ItemType.STOCK, tinkoffCandlesProvider),
        Pair(ItemType.CRYPTO, cryptoCandlesProvider)
    )

    private val statisticsProcessor = mapOf(
        Pair(CandleInterval.CANDLE_INTERVAL_DAY, analyzeDailyInterval),
        Pair(CandleInterval.CANDLE_INTERVAL_HOUR, analyzeHourlyInterval)
    )

    /**
     * Detect levels for selected interval
     *
     * @param figi - figi
     * @param interval - interval to detect levels
     * @param intervalForStatistics - interval to collect statistics
     */
    @Transactional
    override fun detectLevels(figi: String, interval: CandleInterval, intervalForStatistics: CandleInterval, type: ItemType) {
        val instrument = instrumentRepository.getByFigi(figi)
        if (instrument == null) {
            log.warn("Instrument is not in instrument_entity {}", figi )
            return
        }

        val candles = ArrayList<CandleEntity>()
        val future = candlesProviders.getOrDefault(type, noCandlesProvider).invoke(figi, instrument, interval, candles)
        future.get()

        val levels = levelsRepository.getLevelsByFigi(figi)
        val latestLevel = levels.maxOfOrNull { it.levelDate }

        if (candles.isEmpty()) {
            log.error("No candles for {}", figi)
            return
        }

        if (latestLevel != null) {
            candles.removeIf { it.dateTime.isBefore(latestLevel) }
        }
        candles.sortBy { it.dateTime }
        val detectedLevels = levelProcessor.processStock(figi, candles)
            .map { LevelEntity(figi = figi, ticker = instrument.ticker, level = it.second, levelDate = it.first) }
        levelsRepository.saveAll(detectedLevels)

        log.info("Detected {} levels for {}", detectedLevels.size, figi)

        val mergedLevels = mergeLevels(detectedLevels, type)
        mergedLevelsRepository.saveAll(mergedLevels)
        mergedLevels.forEach { level ->
            statisticsProcessor[intervalForStatistics]?.invoke(candles, level, figi, instrument)
        }
        // to decrease load on twelvedata
        Thread.sleep(Duration.ofSeconds(10))
    }
}