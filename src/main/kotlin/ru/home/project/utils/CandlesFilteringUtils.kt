package ru.home.project.utils

import ru.home.project.entity.CandleEntity
import ru.home.project.entity.MergedLevelEntity
import ru.home.project.model.IntervalForScan
import java.time.Duration
import java.time.LocalDate
import java.time.ZonedDateTime
import java.util.function.Predicate

fun retrieveDatesAroundLevel(candles: List<CandleEntity>, level: MergedLevelEntity) : List<IntervalForScan> {
    val levelMinDate = minOf(level.minLevelDate, level.maxLevelDate)
    val dates = candles.asSequence()
        .filter { it.dateTime == levelMinDate || it.dateTime.isAfter(levelMinDate) }
        .filter { getCloseCandlesPredicate(level).test(it) }
        .map { it.dateTime }
        .map { it.toLocalDate() }
        .toCollection(HashSet<LocalDate>())
        .sorted()
    val result = ArrayList<IntervalForScan>()
    if (dates.isEmpty()) {
        return result
    }
    var start: LocalDate = dates.first()
    var prev: LocalDate = dates.first()
    val bufferList = ArrayList<LocalDate>()
    for (i in dates.indices) {
        if (start.plusDays(6).isAfter(dates[i])) {
            prev = dates[i]
            bufferList.add(dates[i])
            continue
        } else if (start.plusDays(7).isBefore(dates[i]) || start.plusDays(7).equals(dates[i])) {
            val startDate = if (bufferList.isEmpty()) prev else start
            val interval = IntervalForScan(start = startDate, end = prev)
            result.add(interval)
            start = dates[i]
            prev = dates[i]
            bufferList.clear()
        }
    }
    return result
}

fun getCloseCandlesPredicate(level: MergedLevelEntity) : Predicate<CandleEntity> {
    return Predicate { candle -> candle.low / level.maxLevel in 0.97..1.03 || level.minLevel / candle.high in 0.97..1.03 }
}

/**
 * Split candles to intervals near the level
 * - get candle crossed the level
 * - collect next NUMBER_OF_CANDLES candles to collect statistics
 * - ignoring close retest (< CLOSE_RETEST_DAYS days)
 *
 * @param candles sorted list of candles near the level
 * @param crossingPredicate filter to detect crossing
 * @return candles split to intervals
 */
fun getInterval(candles: List<CandleEntity>, crossingPredicate: Predicate<CandleEntity>, isSupport: Boolean,
                level: MergedLevelEntity
): MutableMap<ZonedDateTime, List<CandleEntity>> {
    var dateTime: ZonedDateTime? = null
    val interval: MutableList<CandleEntity> = ArrayList()
    val intervalsByDate: MutableMap<ZonedDateTime, List<CandleEntity>> = HashMap()
    var isNextInterval = false
    var crossed: CandleEntity? = null
    var prev: CandleEntity?
    var isConsistentInterval = false
    for (candle in candles) {
        if (crossed == null && crossingPredicate.test(candle)) {
            crossed = candle
            prev = if (candles.indexOf(candle) != 0) candles[candles.indexOf(candle) - 1] else candles[0]
            isConsistentInterval = if (isSupport) {
                prev.low > level.maxLevel
            } else {
                prev.high < level.minLevel
            }
        }
        if (crossed == null || !isConsistentInterval) {
            crossed = null
            continue
        }
        if (dateTime == null) {
            dateTime = candle.dateTime
            interval.add(candle)
            continue
        }
        if (candle.dateTime.isAfter(dateTime.plusHours(hoursToDetectCloseRetest.toLong()))) {
            isNextInterval = true
            crossed = null
        }
        if (!isNextInterval && candle.dateTime.isBefore(dateTime.plusHours(hoursToDetectCloseRetest.toLong()))) {
            interval.add(candle)
            continue
        }
        if (isNextInterval) {
            intervalsByDate[dateTime] = ArrayList(interval)
            interval.clear()
            interval.add(candle)
            dateTime = candle.dateTime
            isNextInterval = false
            continue
        }
        if (interval.isNotEmpty()) {
            intervalsByDate[dateTime] = ArrayList(interval)
        }
    }
    return intervalsByDate
}

/**
 * remove empty list && close retests
 *
 * @param predicate - crossing date
 * @param support support intervals
 * @param resistance resistance intervals
 */
fun removeCloseRetests(support: MutableMap<ZonedDateTime, List<CandleEntity>>,
                               resistance: MutableMap<ZonedDateTime, List<CandleEntity>>,
                               predicate: Predicate<CandleEntity>) {
    val supportMinDate = support.minOfOrNull { it.key }
    val resistanceMinDate = resistance.minOfOrNull { it.key }
    var minDate = if (supportMinDate != null && resistanceMinDate != null) {
        minOf(supportMinDate, resistanceMinDate)
    } else supportMinDate ?: resistanceMinDate

    val keysToRemove = ArrayList<ZonedDateTime>()
    val keys = HashMap<ZonedDateTime, List<CandleEntity>>()
    var isFirstPass = true
    keys.putAll(support)
    keys.putAll(resistance)

    keys.toSortedMap().forEach {
        val sorted = it.value.sortedBy { item -> item.dateTime }
        val lastCrossing = sorted.findLast { item -> predicate.test(item) }
        val firstCrossing = sorted.firstOrNull()
        if (lastCrossing == null || firstCrossing == null) {
            keysToRemove.add(it.key)
        } else {
            val isCloseRetest = !isFirstPass
                    && Duration.between(minDate, firstCrossing.dateTime).toHours() < hoursToDetectCloseRetest
            if (isCloseRetest) {
                keysToRemove.add(it.key)
            }
            minDate = lastCrossing.dateTime
            isFirstPass = false
        }
    }

    keysToRemove.forEach {
        support.remove(it)
        resistance.remove(it)
    }
}