package ru.home.project.processor

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import ru.home.project.entity.CandleEntity
import java.time.ZonedDateTime
import java.util.*

/**
 * @author rlagay
 */
abstract class AbstractLevelProcessor: LevelProcessor {

    private val log: Logger = LoggerFactory.getLogger(AbstractLevelProcessor::class.java)
    protected var periodToDetect: Int = 0

    override fun processStock(figi: String, candles: List<CandleEntity>): Set<Pair<ZonedDateTime, Double>> {
        log.debug("Starting weekly levels processing for {}", figi)

        val levels = Collections.synchronizedSet(HashSet<Pair<ZonedDateTime, Double>>())
        levels.addAll(detectSupportLevels(candles))
        levels.addAll(detectResistanceLevels(candles))
        return levels
    }

    /**
     * 1. candles are ordered
     * 2. get first 10 candles, detect min
     * 3. check that following 10 values are >= min && prev 10 values are >= min
     *      a. it is level -> go to step 2, starting from min_index + 10
     *      b. not level -> go to step 2, starting from min_index
     *
     * @param candles   - list of candles ordered by date
     * @return          - List of support levels
     */
    private fun detectSupportLevels(candles: List<CandleEntity>): Set<Pair<ZonedDateTime, Double>> {
        val levels = Collections.synchronizedSet(HashSet<Pair<ZonedDateTime, Double>>())
        var startIndex: Int = periodToDetect
        while (startIndex < candles.size - periodToDetect) {
            val sublist = candles.subList(startIndex, startIndex + periodToDetect)
            val minCandle = sublist.minBy { it.low }
            val followingSublist = getFollowingSublist(candles, candles.indexOf(minCandle))
            val previousSublist = getPreviousSublist(candles, candles.indexOf(minCandle))
            if (isCandlesLarger(followingSublist, minCandle.low) && isCandlesLarger(previousSublist, minCandle.low)) {
                levels.add(Pair(minCandle.dateTime, minCandle.low))
                startIndex = candles.indexOf(minCandle) + periodToDetect
                log.trace("Detected level {}", minCandle.low)
            } else {
                startIndex++
            }
        }
        log.debug("Detected {} levels", levels.size)
        return levels
    }

    /**
     * 1. candles are ordered
     * 2. get first 10 candles, detect max
     * 3. check that following 10 values are <= max && prev 10 values are <= max
     *      a. it is level -> go to step 2, starting from max_index + 10
     *      b. not level -> go to step 2, starting from max_index
     *
     * @param candles   - list of candles ordered by date
     * @return          - List of resistance levels
     */
    private fun detectResistanceLevels(candles: List<CandleEntity>): Set<Pair<ZonedDateTime, Double>> {
        val levels = Collections.synchronizedSet(HashSet<Pair<ZonedDateTime, Double>>())
        var startIndex = 0
        while (startIndex < candles.size - periodToDetect) {
            val sublist: List<CandleEntity> = candles.subList(startIndex, startIndex + periodToDetect)
            val maxCandle: CandleEntity = sublist.maxBy { it.high }
            val followingSublist = getFollowingSublist(candles, candles.indexOf(maxCandle))
            val previousSublist = getPreviousSublist(candles, candles.indexOf(maxCandle))
            if (isCandlesLess(followingSublist, maxCandle.high) && isCandlesLess(previousSublist, maxCandle.high)) {
                levels.add(Pair(maxCandle.dateTime, maxCandle.high))
                startIndex = candles.indexOf(maxCandle) + periodToDetect
                log.trace("Detected level {}", maxCandle.high)
            } else {
                startIndex++
            }
        }
        log.debug("Detected {} levels", levels.size)
        return levels
    }

    private fun getFollowingSublist(candles: List<CandleEntity>, index: Int): List<CandleEntity>? {
        return if (index + periodToDetect <= candles.size) candles.subList(index + 1, index + periodToDetect) else null
    }

    private fun getPreviousSublist(candles: List<CandleEntity>, index: Int): List<CandleEntity>? {
        return if (index - periodToDetect >= 0) candles.subList(index - periodToDetect, index) else null
    }

    /**
     * Check if prev/next candles are larger
     * @param list          - candles to check
     * @param minimum       - min val
     * @return              - tru/false
     */
    private fun isCandlesLarger(list: List<CandleEntity>?, minimum: Double): Boolean {
        return list != null && minimum < list.minOf { it.low }
    }

    private fun isCandlesLess(list: List<CandleEntity>?, max: Double): Boolean {
        return list != null && max > list.maxOf { it.high }
    }
}