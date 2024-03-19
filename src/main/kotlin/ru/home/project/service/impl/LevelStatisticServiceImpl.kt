package ru.home.project.service.impl

import org.apache.commons.math3.util.Precision
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.home.project.entity.CandleEntity
import ru.home.project.entity.LevelStatisticsEntity
import ru.home.project.entity.MergedLevelEntity
import ru.home.project.model.LevelType
import ru.home.project.repository.LevelStatisticsRepository
import ru.home.project.service.LevelStatisticService
import ru.home.project.utils.*
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.ZonedDateTime
import java.util.function.Predicate
import kotlin.math.abs

/**
 * @author rlagay
 */
@Service
class LevelStatisticServiceImpl(
    val levelStatisticsRepository: LevelStatisticsRepository
): LevelStatisticService {

    private val log: Logger = LoggerFactory.getLogger(LevelStatisticServiceImpl::class.java)

    override fun analyzeStock(figi: String, ticker: String?, level: MergedLevelEntity, interval: CandleInterval,
                              candles: ArrayList<CandleEntity>) {
        try {
            // candles near the level
            val sorted = candles
                .filter { candle -> getCloseCandlesPredicate(level).test(candle) }
                .sortedWith(Comparator.comparing(CandleEntity::dateTime))

            val supportIntervals = getInterval(sorted, crossedLevelPredicate(level), true, level)
            val resistanceIntervals = getInterval(sorted, crossedLevelPredicate(level), false, level)

            removeCloseRetests(supportIntervals, resistanceIntervals, crossedLevelPredicate(level))

            val levelAsSupport = if (supportIntervals.isNotEmpty()) processSupport(supportIntervals, level) else null
            val levelAsResistance = if (resistanceIntervals.isNotEmpty()) processResistance(resistanceIntervals, level) else null

            val listToSave = ArrayList<LevelStatisticsEntity>()
            if (levelAsSupport != null) {
                listToSave.add(levelAsSupport)
            }
            if (levelAsResistance != null) {
                listToSave.add(levelAsResistance)
            }
            if (listToSave.isNotEmpty()) {
                levelStatisticsRepository.saveAll(listToSave)
            }

        } catch (e: Exception) {
            log.error("Failed to analyze level statistic, ticker {}", level.ticker, e)
        }
    }

    /**
     * Collect statistics for support level
     * @param intervals - map of interval, testing the level
     * @param level - level
     */
    private fun processSupport(intervals: Map<ZonedDateTime, List<CandleEntity>>, level: MergedLevelEntity) : LevelStatisticsEntity? {
        //support
        var goodSignals = 0
        val maxPrices = ArrayList<Double>()
        val minPrices = ArrayList<Double>()
        var crosses = 0

        for ((_, candles) in intervals) {
            val crossingDate = getCrossingDateSupport(candles, level)
            if (crossingDate != null) {
                crosses++
                val candlesAfterCrossing = candles.filter { it.dateTime.isAfter(crossingDate) || it.dateTime == crossingDate }
                if (candlesAfterCrossing.isEmpty()) {
                    continue
                }
                val min = candlesAfterCrossing.minByOrNull { it.low }
                val max = candlesAfterCrossing.maxByOrNull { it.high }
                if (min == null || max == null) {
                    continue
                }
                maxPrices.add(max.high)
                minPrices.add(min.low)

                // take profit before stop loss
                val isStopLoss = min.low <= level.minLevel * percentageForShort
                if (isStopLoss) {
                    if (max.dateTime.isBefore(min.dateTime) && max.high >= level.maxLevel * percentageForLong) {
                        goodSignals++
                    }
                } else {
                    if (max.high >= level.maxLevel * percentageForGoodSignalLong) {
                        goodSignals++
                    }
                }
            }
        }

        return if (crosses != 0) toLevelStatisticsEntity(goodSignals, level, minPrices, maxPrices, LevelType.Support, crosses) else null
    }

    /**
     * Collect statistics for resistance level
     * @param intervals - map of interval, testing the level
     * @param level - level
     */
    private fun processResistance(intervals: Map<ZonedDateTime, List<CandleEntity>>, level: MergedLevelEntity) : LevelStatisticsEntity? {
        //support
        var goodSignals = 0
        val maxPrices = ArrayList<Double>()
        val minPrices = ArrayList<Double>()
        var crosses = 0

        for ((_, candles) in intervals) {
            val crossingDate = getCrossingDateResistance(candles, level)
            if (crossingDate != null) {
                val candlesAfterCrossing = candles.filter { it.dateTime.isAfter(crossingDate) || it.dateTime == crossingDate}
                crosses++
                val min = candlesAfterCrossing.minBy { it.low }
                val max = candlesAfterCrossing.maxBy { it.high }
                minPrices.add(min.low)
                maxPrices.add(max.high)

                // take profit before stop loss
                val isStopLoss = max.high >= level.maxLevel * percentageForLong
                if (isStopLoss) {
                    if (min.dateTime.isBefore(max.dateTime) && min.low <= level.minLevel * percentageForShort) {
                        goodSignals++
                    }
                } else {
                    if (min.low <= level.minLevel * percentageForGoodSignalShort) {
                        goodSignals++
                    }
                }
            }
        }

        return if (crosses != 0) toLevelStatisticsEntity(goodSignals, level, minPrices, maxPrices, LevelType.Resistance, crosses) else null
    }

    private fun getCrossingDateSupport(candles: List<CandleEntity>, level: MergedLevelEntity) : ZonedDateTime? {
        return candles.filter { it.low <= level.maxLevel && it.high >= level.maxLevel }
            .map { it.dateTime }
            .minByOrNull { it }
    }

    private fun getCrossingDateResistance(candles: List<CandleEntity>, level: MergedLevelEntity) : ZonedDateTime? {
        return candles.filter { it.low <= level.minLevel && it.high >= level.minLevel }
            .map { it.dateTime }
            .minByOrNull { it }
    }

    private fun toLevelStatisticsEntity(goodSignals: Int, level: MergedLevelEntity, minPrices: ArrayList<Double>,
                                        maxPrices: ArrayList<Double>, levelType: LevelType, crosses: Int): LevelStatisticsEntity {
        val winPercentage = Precision.round(goodSignals.toDouble() / crosses.toDouble(), 2)
        val averageBreaking = Precision.round(abs(level.minLevel - minPrices.average()) / level.minLevel, 2)
        val averageRebound = Precision.round(abs(maxPrices.average() - level.maxLevel) / level.maxLevel, 2)

        return LevelStatisticsEntity(
            figi = level.figi, ticker = level.ticker, maxLevel = level.maxLevel, minLevel = level.minLevel,
            minLevelDate = level.minLevelDate, maxLevelDate = level.maxLevelDate, successRate = winPercentage,
            averageBreaking = averageBreaking, averageRebound = averageRebound, goodSignals = goodSignals,
            totalCrosses = crosses, levelType = levelType
        )
    }

//    // Пропускаем:
//    // - если 10 свечей до текущей были меньше уровня
//    // - если предыдущая свеча пробивает уровень, а текущая полностью выше уровня
//    // - если свеча пробивает уровень
//    // - если свеча в промежутке +/- 3%
//    private fun supportPredicate(level: MergedLevelEntity, candles: List<CandleEntity>): Predicate<CandleEntity> {
//        return Predicate { candle ->
//            if (candle.low >= level.maxLevel) {
//                val initIndex = getInitialIndex(candles, candle)
//                val subList = candles.subList(initIndex, candles.indexOf(candle))
//                subList.all { it.low > level.maxLevel }
//            } else {
//                val prev = if (candles.indexOf(candle) != 0) { candles[candles.indexOf(candle) - 1] } else { candles.first() }
//                (level.maxLevel * percentageForCandlesHighBorder >= candle.low && level.minLevel * percentageForCandlesLowBorder <= candle.high)
//                        || level.maxLevel < candle.high && level.maxLevel > candle.low
//                        || (level.maxLevel < prev.high && level.maxLevel > prev.low && candle.high < level.maxLevel)
//            }
//        }
//    }

//    // Пропускаем:
//    // - если 10 свечей до текущей были больше уровня
//    // - если предыдущая свеча пробивает уровень, а текущая полностью ниже уровня
//    // - если свеча пробивает уровень
//    // - если свеча в промежутке +/- 3%
//    private fun resistancePredicate(level: MergedLevelEntity, candles: List<CandleEntity>): Predicate<CandleEntity> {
//        return Predicate { candle ->
//            if (candle.high <= level.minLevel) {
//                val initIndex = getInitialIndex(candles, candle)
//                val subList = candles.subList(initIndex, candles.indexOf(candle))
//                subList.all { it.high > level.minLevel }
//            } else {
//                val prev = if (candles.indexOf(candle) != 0) { candles[candles.indexOf(candle) - 1] } else { candles.first() }
//                level.maxLevel * percentageForLong
//                (level.maxLevel * percentageForCandlesHighBorder >= candle.low && level.minLevel * percentageForCandlesLowBorder <= candle.high)
//                        || level.minLevel < candle.high && level.minLevel > candle.low
//                        || (level.minLevel < prev.high && level.minLevel > prev.low && candle.low > level.minLevel)
//
//            }
//        }
//    }

    private fun crossedLevelPredicate(level: MergedLevelEntity) : Predicate<CandleEntity> {
        return Predicate<CandleEntity> { candle -> candle.low <= level.maxLevel && candle.high >= level.maxLevel
                || candle.low <= level.minLevel && candle.high >= level.minLevel
        }
    }
}