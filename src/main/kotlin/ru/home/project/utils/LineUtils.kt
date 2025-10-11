package ru.home.project.utils

import ru.home.project.entity.CandleEntity
import java.util.function.Function
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Calculates the parameters of the line based on the given candles and extremums.
 *
 * @param candles the list of candles to analyze
 * @param extremums the list of extremum points from the candles
 * @param valueSupplier the function to extract the value from a candle entity
 * @return a Pair containing the slope (a1) and y-intercept (b1) of the extremum line
 */
fun getExtremumLineParams(
    candles: List<CandleEntity>,
    extremums: List<CandleEntity>,
    valueSupplier: Function<CandleEntity, Double>
): Pair<Double, Double>? {
    if (extremums.size < 2) {
        return null
    }

    val firstIndex = candles.indexOf(extremums.first()).toDouble()
    val secondIndex = candles.indexOf(extremums.last()).toDouble()

    val first = extremums.first()
    val second = extremums.last()
    val firstExtremum = valueSupplier.apply(first)
    val secondExtremum = valueSupplier.apply(second)

    val a = (secondExtremum - firstExtremum) / (secondIndex - firstIndex)
    val b = firstExtremum - a * firstIndex

    return Pair(a, b)
}

private fun distance(x1: Double, y1: Double, x2: Double, y2: Double): Double {
    return sqrt((x2 - x1).pow(2.0) + (y2 - y1).pow(2.0))
}
