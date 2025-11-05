package ru.home.project.utils

import ru.home.project.entity.CandleEntity

const val numberOfCandlesOnHill = 75
const val numberOfCandlesOnHillFor7Days = 15
const val numberOfCandlesToEstimateLocalTrend = 10

val numberOfCandlesToCandleInTrendMap = mapOf(
    Pair(7, numberOfCandlesOnHillFor7Days),
    Pair(30, numberOfCandlesOnHill)
)

val numberOfCandlesToCandleToEstimateTrend = mapOf(
    Pair(7, 5),
    Pair(30, 15)
)

/**
 * Splits a list of candle entities into trends based on their average high and low values.
 *
 * @param candles the list of candle entities to split into trends
 * @param days number of days in candles list
 * @return a list of pairs where each pair consists of a trend and the corresponding list of candle entities
 */
fun splitToTrends(candles: List<CandleEntity>, days: Int): List<Pair<Trend, List<CandleEntity>>> {
    val trends = ArrayList<Pair<Trend, List<CandleEntity>>>()
    val candlesInCurrentTrend = ArrayList<CandleEntity>()
    var prevTrend: Trend? = null
    var currentTrend: Trend?
    val numberOfCandles = numberOfCandlesToCandleInTrendMap[days] ?: numberOfCandlesOnHill

    if (candles.size < numberOfCandles) {
        return trends
    }
    for (i in 0 until candles.size step numberOfCandles) {

        val candle = candles[i]
        val index = candles.indexOf(candle)
        if (prevTrend != null && index >= candles.size - numberOfCandles) {
            if (candlesInCurrentTrend.size > numberOfCandles) {
                trends.add(Pair(prevTrend, ArrayList(candlesInCurrentTrend)))
            }
            break
        }
        val numberOfCandlesToEstimateLocalTrend = numberOfCandlesToCandleToEstimateTrend[days] ?: 15
        val nextCandlesToDetectLocalTrend = candles.subList(index, index + numberOfCandles)
        val isAscending = nextCandlesToDetectLocalTrend.subList(0, numberOfCandlesToEstimateLocalTrend).map { it.close }.average() <
                nextCandlesToDetectLocalTrend.subList(numberOfCandles - numberOfCandlesToEstimateLocalTrend, numberOfCandles).map { it.close }.average()
        val isDescending = nextCandlesToDetectLocalTrend.subList(0, numberOfCandlesToEstimateLocalTrend).map { it.close }.average() >
                nextCandlesToDetectLocalTrend.subList(numberOfCandles - numberOfCandlesToEstimateLocalTrend, numberOfCandles).map { it.close }.average()

        currentTrend = if (isAscending) { Trend.ASCENDING }
            else if (isDescending) { Trend.DESCENDING }
            else { Trend.NEUTRAL }
        candlesInCurrentTrend.addAll(nextCandlesToDetectLocalTrend)
        if (prevTrend != null && prevTrend != currentTrend) {
            trends.add(Pair(prevTrend, ArrayList(candlesInCurrentTrend)))
            candlesInCurrentTrend.clear()
            candlesInCurrentTrend.addAll(nextCandlesToDetectLocalTrend)
            prevTrend = currentTrend
        } else {
            prevTrend = currentTrend
        }

    }
    return trends.filter { it.first != Trend.NEUTRAL }
}