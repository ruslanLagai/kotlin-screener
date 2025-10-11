package ru.home.project.service

import ru.home.project.entity.CandleEntity
import ru.home.project.model.IntervalForScan
import ru.tinkoff.piapi.contract.v1.CandleInterval
import ru.tinkoff.piapi.contract.v1.HistoricCandle
import java.util.concurrent.CompletableFuture

/**
 * @author rlagay
 */
interface CandlesService {

    fun getHistoricalCandles(
        figi: String, ticker: String?, instrumentUid: String? = null, interval: CandleInterval, allCandles: ArrayList<CandleEntity>
    ): CompletableFuture<MutableList<HistoricCandle>>

    fun getCandlesForIntervals(
        figi: String, ticker: String?, interval: CandleInterval, intervals: List<IntervalForScan>
    ): ArrayList<CandleEntity>

    fun getLastCandles(figi: String, interval: CandleInterval, instrumentUid: String? = null, dayFrom: Int): List<CandleEntity>
}