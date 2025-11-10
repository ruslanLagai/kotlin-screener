package ru.home.project.utils

import org.slf4j.LoggerFactory
import ru.home.project.dto.PatternDto
import ru.home.project.entity.CandleEntity
import ru.home.project.model.ItemType
import ru.home.project.model.PatternType
import java.time.Duration.between
import java.time.LocalDateTime

private const val countOfCandles = 5
private const val tolerance = 1.01

private val log = LoggerFactory.getLogger(object{}::class.java.`package`.name)

/**
 * Identifies and returns the local maximums and minimums (extremums) from a list of candle entities.
 *
 * The function splits the input candles into trends and determines the maximums in ascending trends
 * and minimums in descending trends. It then checks if the last identified extremums are far enough
 * from the end of the list based on a predefined count of candles. If not, it returns empty lists.
 *
 * @param candles A list of [CandleEntity] objects representing the price data.
 * @param days number of days in candles list.
 * @return A [Pair] where the first element is a list of maximum [CandleEntity] objects and
 *         the second element is a list of minimum [CandleEntity] objects.
 */
fun getExtremums(candles: List<CandleEntity>, days: Int, type: ItemType = ItemType.STOCK): Pair<List<CandleEntity>, List<CandleEntity>> {
    val maximums = ArrayList<CandleEntity>()
    val minimums = ArrayList<CandleEntity>()

    val trends = splitToTrends(candles, days, type)
    if (trends.isEmpty()) {
        return Pair(listOf(), listOf())
    }

    log.info("Trends size: ${trends.size} for figi ${candles.first().figi} during $days days")

    trends.forEach {
        if (it.first == Trend.ASCENDING) {
            maximums.add(it.second.maxBy { candle -> candle.high })
        } else {
            minimums.add(it.second.minBy { candle -> candle.low })
        }
    }

    if (maximums.isEmpty() || minimums.isEmpty()) {
        return Pair(listOf(), listOf())
    }

    val lastMinIndex = candles.indexOf(minimums.last())
    val lastMaxIndex = candles.indexOf(maximums.last())

    if (candles.size < lastMaxIndex + countOfCandles || candles.size < lastMinIndex + countOfCandles) {
        return Pair(listOf(), listOf())
    }

    return Pair(maximums, minimums)
}


fun checkExtremumsAreBetweenLines(maximums: List<CandleEntity>, minimums: List<CandleEntity>,
                             candles: List<CandleEntity>): PatternDto? {
    val firstExtremum = listOf(maximums.minBy { it.dateTime }, minimums.minBy { it.dateTime }).minBy { it.dateTime }
    val firstExtremumIndex = candles.indexOf(firstExtremum)
    val maxLine = getExtremumLineParams(candles, maximums, CandleEntity::high)
    val minLine = getExtremumLineParams(candles, minimums, CandleEntity::low)

    if (maxLine == null || minLine == null) {
        log.debug("Not enough data to check if values are between lines")
        return null
    }
    log.debug("Max line: {}", maxLine)
    log.debug("Min line: {}", minLine)

    for (index in firstExtremumIndex until candles.size) {
        val candle = candles[index]
        val isAboveMin = candle.low * tolerance >= minLine.first * index + minLine.second
        val isUnderMax = candle.high <= (maxLine.first * index + maxLine.second) * tolerance

        if (!isAboveMin || !isUnderMax) {
            log.debug("Candle: {}, min: {}, max: {}, above min: {}, under max {}",
                candle, minLine.first * index + minLine.second, maxLine.first * index + maxLine.second, isAboveMin, isUnderMax)
            return null
        }
    }

    val aMax = maxLine.first
    val aMin = minLine.first
    val patternType = if (aMax * aMin < 0) PatternType.TRIANGLE else PatternType.CHANNEL
    return PatternDto(aMax, maxLine.second, aMin, minLine.second, patternType)
}

/**
 * Checks if the price is between the lines.
 * Candles are assumed to be at UTC
 *
 * @param maxLine The maximum line.
 * @param minLine The minimum line.
 * @param price The price to check.
 * @param startDate The start date of the pattern UTC.
 * @return True if the price is between the lines, false otherwise.
 */
fun checkPriceIsBetweenLines(maxLine: Pair<Double, Double>, minLine: Pair<Double, Double>, price: Double,
                             startDate: LocalDateTime
): Pair<Boolean, Double> {
    val current = LocalDateTime.now()
    val from = startDate.plusDays(1)
    val workingDays = between(from, current).toDays()

    val firstDayCandles = candlesPerWorkingDayForStock - startDate.hour - 5 // UTC
    val lastDayCandles = LocalDateTime.now().hour - 8 // UTC +3

    val numberOfCandles = workingDays * candlesPerWorkingDayForStock + firstDayCandles + lastDayCandles
    val index = numberOfCandles + 1
    val isAboveMin = price >= minLine.first * index + minLine.second
    val isUnderMax = price <= maxLine.first * index + maxLine.second
    val minVal = price - minLine.first * index + minLine.second
    val maxVal = maxLine.first * index + maxLine.second - price
    val nearestValue = minOf(minVal, maxVal)
    return Pair(isAboveMin && isUnderMax, nearestValue)
}