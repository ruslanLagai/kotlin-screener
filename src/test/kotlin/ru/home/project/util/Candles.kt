package ru.home.project.util

import ru.tinkoff.piapi.contract.v1.HistoricCandle

/**
 * @author rlagay
 */
data class Candles(
    val candles: List<HistoricCandle>
)
