package ru.home.project.utils

import ru.home.project.entity.CandleEntity

const val numberOfCandlesOnHill = 75
const val numberOfCandlesToEstimateLocalTrend = 10

/**
 * Splits a list of candle entities into trends based on their average high and low values.
 *
 * @param candles the list of candle entities to split into trends
 * @return a list of pairs where each pair consists of a trend and the corresponding list of candle entities
 */
fun splitToTrends(candles: List<CandleEntity>): List<Pair<Trend, List<CandleEntity>>> {
    val trends = ArrayList<Pair<Trend, List<CandleEntity>>>()
    val candlesInCurrentTrend = ArrayList<CandleEntity>()
    var prevTrend: Trend? = null
    var currentTrend: Trend?

    if (candles.size < numberOfCandlesOnHill) {
        return trends
    }
    for (i in 0 until candles.size step numberOfCandlesOnHill) {

        val candle = candles[i]
        val index = candles.indexOf(candle)
        if (prevTrend != null && index >= candles.size - numberOfCandlesOnHill) {
            if (candlesInCurrentTrend.size > numberOfCandlesOnHill) {
                trends.add(Pair(prevTrend, ArrayList(candlesInCurrentTrend)))
            }
            break
        }
        val nextCandlesToDetectLocalTrend = candles.subList(index, index + numberOfCandlesOnHill)
        val isAscending = nextCandlesToDetectLocalTrend.subList(0, 15).map { it.close }.average() <
                nextCandlesToDetectLocalTrend.subList(numberOfCandlesOnHill - 15, numberOfCandlesOnHill).map { it.close }.average()
        val isDescending = nextCandlesToDetectLocalTrend.subList(0, 15).map { it.close }.average() >
                nextCandlesToDetectLocalTrend.subList(numberOfCandlesOnHill - 15, numberOfCandlesOnHill).map { it.close }.average()

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