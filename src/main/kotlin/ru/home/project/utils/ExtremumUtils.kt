package ru.home.project.utils

import org.slf4j.LoggerFactory
import ru.home.project.entity.CandleEntity

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
 * @return A [Pair] where the first element is a list of maximum [CandleEntity] objects and
 *         the second element is a list of minimum [CandleEntity] objects.
 */
fun getExtremums(candles: List<CandleEntity>): Pair<List<CandleEntity>, List<CandleEntity>> {
    val maximums = ArrayList<CandleEntity>()
    val minimums = ArrayList<CandleEntity>()

    val trends = splitToTrends(candles)
    if (trends.isEmpty()) {
        return Pair(listOf(), listOf())
    }

    trends.forEach {
        if (it.first == Trend.ASCENDING) {
            maximums.add(it.second.maxBy { candle -> candle.high })
        } else {
            minimums.add(it.second.minBy { candle -> candle.low })
        }
    }


    val lastMinIndex = candles.indexOf(minimums.last())
    val lastMaxIndex = candles.indexOf(maximums.last())

    if (candles.size < lastMaxIndex + countOfCandles || candles.size < lastMinIndex + countOfCandles) {
        return Pair(listOf(), listOf())
    }

    return Pair(maximums, minimums)
}


fun checkExtremumsAreBetweenLines(maximums: List<CandleEntity>, minimums: List<CandleEntity>,
                                  candles: List<CandleEntity>): Boolean {
    val firstExtremum = listOf(maximums.minBy { it.dateTime }, minimums.minBy { it.dateTime }).minBy { it.dateTime }
    val firstExtremumIndex = candles.indexOf(firstExtremum)
    val maxLine = getExtremumLineParams(candles, maximums, CandleEntity::high)
    val minLine = getExtremumLineParams(candles, minimums, CandleEntity::low)

    if (maxLine == null || minLine == null) {
        log.debug("Not enough data to check if values are between lines")
        return false
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
            return false
        }
    }

    return true
}