package ru.home.project.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.home.project.entity.CandleEntity
import ru.home.project.model.IntervalForScan
import ru.home.project.repository.CandlesRepository
import ru.home.project.service.CandlesService
import ru.home.project.utils.priceToDouble
import ru.home.project.utils.timestampToDate
import ru.home.project.utils.weeksToScan
import ru.home.project.utils.yearsToScan
import ru.tinkoff.piapi.contract.v1.CandleInterval
import ru.tinkoff.piapi.contract.v1.HistoricCandle
import ru.tinkoff.piapi.core.InvestApi
import java.time.Instant
import java.time.LocalDateTime
import java.time.Period
import java.time.ZoneOffset
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author rlagay
 */
@Service
class CandlesServiceImpl(
    val investApi: InvestApi,
    val candlesRepository: CandlesRepository
) : CandlesService {

    val executors: ExecutorService = Executors.newFixedThreadPool(5)
    val log: Logger = LoggerFactory.getLogger(CandlesServiceImpl::class.java)
    private final val processDailyInterval: (LocalDateTime, String, String?, ArrayList<CandleEntity>) -> CompletableFuture<MutableList<HistoricCandle>> =
        { from, figi, ticker, candles ->
            this.getDailyCandles(from, figi, ticker, candles)
            CompletableFuture.completedFuture(mutableListOf())
        }
    private final val processHourlyInterval: (LocalDateTime, String, String?, ArrayList<CandleEntity>) -> CompletableFuture<MutableList<HistoricCandle>> =
        { from, figi, ticker, candles ->
            this.getHourlyCandles(from, figi, ticker, candles)
            CompletableFuture.completedFuture(mutableListOf())
        }

    private final val noProcessor: (LocalDateTime, String, String?, ArrayList<CandleEntity>) -> CompletableFuture<MutableList<HistoricCandle>> =
        { _, _, _, _ ->
            log.warn("Only daily period is allowed for now")
            CompletableFuture.failedFuture(RuntimeException("Only daily period is allowed for now"))
        }

    private val intervalProcessor = mapOf(
        Pair(CandleInterval.CANDLE_INTERVAL_DAY, processDailyInterval),
        Pair(CandleInterval.CANDLE_INTERVAL_HOUR, processHourlyInterval)
    )

    override fun getHistoricalCandles(figi: String, ticker: String?, interval: CandleInterval,
                                      allCandles: ArrayList<CandleEntity>
    ) : CompletableFuture<MutableList<HistoricCandle>> {
        val saved = candlesRepository.findByFigiAndIntervalOrderByDateTimeDesc(figi, interval)
        if (saved.isNotEmpty()) {
            allCandles.addAll(saved)
            return processDbCandles(figi, ticker, interval, allCandles)
        } else {
            return getAllFromRest(figi, ticker, interval, allCandles)
        }
    }

    override fun getCandlesForIntervals(figi: String, ticker: String?, interval: CandleInterval, intervals: List<IntervalForScan>
    ): ArrayList<CandleEntity> {
        val candles = ArrayList<CandleEntity>()
        intervals.forEach {
            val from = it.start.atStartOfDay().toInstant(ZoneOffset.UTC)
            val to = it.end.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
            retrieveCandles(figi = figi, ticker = ticker, fromDate = from, toDate = to, interval = interval, allCandles = candles, saveData = false)
        }
        return candles
    }

    override fun getLastCandlesFromDb(figi: String, interval: CandleInterval): List<CandleEntity> {
        return candlesRepository.findTop14ByFigiAndIntervalOrderByDateTimeDesc(figi, interval)
    }

    private fun processDbCandles(figi: String, ticker: String?, interval: CandleInterval, saved: ArrayList<CandleEntity>):
            CompletableFuture<MutableList<HistoricCandle>> {
        val from = saved[0].dateTime.toLocalDateTime()
        return intervalProcessor.getOrDefault(interval, noProcessor).invoke(from, figi, ticker, saved)
    }

    // sync - выполняется разово при заполнении БД
    private fun getAllFromRest(
        figi: String, ticker: String?, interval: CandleInterval, allCandles: ArrayList<CandleEntity>
    ): CompletableFuture<MutableList<HistoricCandle>> {
        val from = LocalDateTime.now().minus(Period.ofYears(yearsToScan).plus(Period.ofDays(1)))
        return intervalProcessor.getOrDefault(interval, noProcessor).invoke(from, figi, ticker, allCandles)
    }

    private fun getDailyCandles(from: LocalDateTime, figi: String, ticker: String?, allCandles: ArrayList<CandleEntity>) {
        for (i in 0..<yearsToScan) {
            val fromDate = from.plus(Period.ofYears(i)).toInstant(ZoneOffset.UTC)
            val toDate = from.plus(Period.ofYears(i + 1)).toInstant(ZoneOffset.UTC)
            retrieveCandles(figi, fromDate, toDate, ticker, CandleInterval.CANDLE_INTERVAL_DAY, allCandles, true)
        }
    }

    // шаг 7 -> можно не получить последние 5 свечей, сейчас они не нужны
    private fun getHourlyCandles(from: LocalDateTime, figi: String, ticker: String?, allCandles: ArrayList<CandleEntity>) {
        var initHour = from.hour.toLong() + 1
        for (i in 0..<weeksToScan * yearsToScan) {
            val fromDate = from.plus(Period.ofDays(i)).plusHours(initHour).toInstant(ZoneOffset.UTC)
            val toDate = from.plus(Period.ofDays(i + 7)).toInstant(ZoneOffset.UTC)
            retrieveCandles(figi, fromDate, toDate, ticker, CandleInterval.CANDLE_INTERVAL_HOUR, allCandles, true)
            if (i == 0) {
                initHour = 0
            }
            Thread.sleep(100)
        }
    }

    private fun retrieveCandles(figi: String, fromDate: Instant, toDate: Instant, ticker: String?, interval: CandleInterval,
                                allCandles: ArrayList<CandleEntity>, saveData: Boolean) {
        investApi.marketDataService
            .getCandles(figi, fromDate, toDate, interval)
            .whenComplete { candles, ex ->
                if (ex != null) {
                    log.error("Error getting candles for tinkoff {}", figi, ex)
                    throw ex
                } else {
                    val converted = saveCandles(candles, figi, ticker, interval, saveData)
                    allCandles.addAll(converted)
                }
            }
            .get()
    }


    private fun saveCandles(candles: MutableList<HistoricCandle>, figi: String, ticker: String?,
                            interval: CandleInterval, saveData: Boolean
    ): List<CandleEntity> {
        val converted = candles.map {
            val date = timestampToDate(it.time)
            CandleEntity(
                figi = figi, ticker = ticker, open = priceToDouble(it.open),
                close = priceToDouble(it.close), high = priceToDouble(it.high), low = priceToDouble(it.low),
                volume = it.volume, interval = interval, dateTime = date
            )
        }
        if (saveData) {
            executors.execute { candlesRepository.saveAll(converted) }
        }
        return converted
    }
}