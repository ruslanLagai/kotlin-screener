package ru.home.project.service.impl

import com.google.protobuf.Timestamp
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.dao.OptimisticLockingFailureException
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
import ru.tinkoff.piapi.contract.v1.GetCandlesRequest
import ru.tinkoff.piapi.contract.v1.HistoricCandle
import ru.tinkoff.piapi.contract.v1.MarketDataServiceGrpc
import java.time.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * @author rlagay
 */
@Service
class CandlesServiceImpl(
    val marketDataServiceBlockingStub: MarketDataServiceGrpc.MarketDataServiceBlockingStub,
    val candlesRepository: CandlesRepository
) : CandlesService {

    val executors: ExecutorService = Executors.newFixedThreadPool(10)
    val log: Logger = LoggerFactory.getLogger(CandlesServiceImpl::class.java)

    private final val processDailyInterval: (LocalDateTime, String, String?, String?, ArrayList<CandleEntity>) ->
    CompletableFuture<MutableList<HistoricCandle>> = { from, figi, ticker, instrumentUid, candles ->
            this.getDailyCandles(from, figi, ticker, instrumentUid, candles)
            CompletableFuture.completedFuture(mutableListOf())
        }
    private final val processHourlyInterval: (LocalDateTime, String, String?, String?, ArrayList<CandleEntity>) ->
    CompletableFuture<MutableList<HistoricCandle>> = { from, figi, ticker, instrumentUid, candles ->
            this.getHourlyCandles(from, figi, ticker, instrumentUid ,candles)
            CompletableFuture.completedFuture(mutableListOf())
        }

    private final val noProcessor: (LocalDateTime, String, String?, String?, ArrayList<CandleEntity>) ->
    CompletableFuture<MutableList<HistoricCandle>> = { _, _, _, _, _ ->
            log.warn("Only daily period is allowed for now")
            CompletableFuture.failedFuture(RuntimeException("Only daily period is allowed for now"))
        }

    private val intervalProcessor = mapOf(
        Pair(CandleInterval.CANDLE_INTERVAL_DAY, processDailyInterval),
        Pair(CandleInterval.CANDLE_INTERVAL_HOUR, processHourlyInterval)
    )

    override fun getHistoricalCandles(figi: String, ticker: String?, instrumentUid: String?, interval: CandleInterval,
                                      allCandles: ArrayList<CandleEntity>
    ) : CompletableFuture<MutableList<HistoricCandle>> {
        val saved = candlesRepository.findByFigiAndIntervalOrderByDateTimeDesc(figi, interval)
        if (saved.isNotEmpty()) {
            allCandles.addAll(saved)
            return processDbCandles(figi, ticker, instrumentUid, interval, allCandles)
        } else {
            return getAllFromRest(figi, ticker, instrumentUid, interval, allCandles)
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

    @Cacheable(cacheNames = [ "daily-tinkoff" ], key = "#figi + #interval + #dayFrom", condition = "@checkRedis.get()")
    override fun getLastCandles(figi: String, interval: CandleInterval, instrumentUid: String?, dayFrom: Int): List<CandleEntity> {
        val fromDate = LocalDateTime.now().minus(Period.ofDays(dayFrom)).toInstant(ZoneOffset.UTC)
        val toDate = LocalDate.now().minusDays(1).atTime(23, 50).toInstant(ZoneOffset.ofHours(3))
        val allCandles = ArrayList<CandleEntity>()
        retrieveCandles(figi = figi, fromDate = fromDate, toDate = toDate, ticker = null, instrumentUid = instrumentUid,
            interval = CandleInterval.CANDLE_INTERVAL_HOUR, allCandles = allCandles, saveData = false)
        return allCandles
    }

    private fun processDbCandles(figi: String, ticker: String?, instrumentUid: String?, interval: CandleInterval,
                                 saved: ArrayList<CandleEntity>): CompletableFuture<MutableList<HistoricCandle>> {
        val from = saved[0].dateTime.toLocalDateTime()
        return intervalProcessor.getOrDefault(interval, noProcessor).invoke(from, figi, ticker, instrumentUid, saved)
    }

    // sync - выполняется разово при заполнении БД
    private fun getAllFromRest(figi: String, ticker: String?, instrumentUid: String?, interval: CandleInterval,
                               allCandles: ArrayList<CandleEntity>
    ): CompletableFuture<MutableList<HistoricCandle>> {
        val from = LocalDateTime.now().minus(Period.ofYears(yearsToScan).plus(Period.ofDays(1)))
        return intervalProcessor.getOrDefault(interval, noProcessor).invoke(from, figi, ticker, instrumentUid, allCandles)
    }

    private fun getDailyCandles(from: LocalDateTime, figi: String, ticker: String?, instrumentUid: String?,
                                allCandles: ArrayList<CandleEntity>) {
        for (i in 0..<yearsToScan) {
            val fromDate = from.plus(Period.ofYears(i)).toInstant(ZoneOffset.UTC)
            val toDate = from.plus(Period.ofYears(i + 1)).toInstant(ZoneOffset.UTC)
            if (fromDate.isAfter(ZonedDateTime.now(ZoneOffset.UTC).toInstant())) {
                break
            }
            retrieveCandles(figi = figi, instrumentUid = instrumentUid, fromDate = fromDate, toDate = toDate,
                ticker = ticker, interval = CandleInterval.CANDLE_INTERVAL_DAY, allCandles = allCandles, saveData = true)
        }
    }

    // шаг 7 -> можно не получить последние 5 свечей, сейчас они не нужны
    private fun getHourlyCandles(from: LocalDateTime, figi: String, ticker: String?, instrumentUid: String?,
                                 allCandles: ArrayList<CandleEntity>) {
        var initHour = from.hour.toLong() + 1
        for (i in 0..<weeksToScan * yearsToScan step 7) {
            val fromDate = from.plus(Period.ofDays(i)).plusHours(initHour).toInstant(ZoneOffset.UTC)
            val toDate = from.plus(Period.ofDays(i + 7)).toInstant(ZoneOffset.UTC)
            if (fromDate.isAfter(ZonedDateTime.now(ZoneOffset.UTC).toInstant())) {
                break
            }
            retrieveCandles(figi = figi, instrumentUid = instrumentUid, fromDate = fromDate, toDate = toDate, ticker = ticker,
                interval = CandleInterval.CANDLE_INTERVAL_HOUR, allCandles =  allCandles, saveData = true)
            if (i == 0) {
                initHour = 0
            }
        }
    }

    private fun retrieveCandles(figi: String, instrumentUid: String? = null, fromDate: Instant, toDate: Instant,
                                ticker: String?, interval: CandleInterval, allCandles: ArrayList<CandleEntity>, saveData: Boolean) {
        val request = if (instrumentUid != null)
            GetCandlesRequest.newBuilder()
                .setFrom(Timestamp.newBuilder().setSeconds(fromDate.epochSecond))
                .setTo(Timestamp.newBuilder().setSeconds(toDate.epochSecond))
                .setInterval(interval)
                .setInstrumentId(instrumentUid)
                .build()
        else {
            GetCandlesRequest.newBuilder()
                .setFrom(Timestamp.newBuilder().setSeconds(fromDate.epochSecond))
                .setTo(Timestamp.newBuilder().setSeconds(toDate.epochSecond))
                .setInterval(interval)
                .setFigi(figi)
                .build()
        }
        val candles = marketDataServiceBlockingStub.getCandles(request).candlesList
        val converted = saveCandles(candles, figi, ticker, interval, saveData)
        allCandles.addAll(converted)
    }


    private fun saveCandles(candles: MutableList<HistoricCandle>, figi: String, ticker: String?,
                            interval: CandleInterval, saveData: Boolean
    ): List<CandleEntity> {
        val converted = candles
            .filter { it.isComplete }
            .map {
            val date = timestampToDate(it.time)
            CandleEntity(
                figi = figi, ticker = ticker, open = priceToDouble(it.open),
                close = priceToDouble(it.close), high = priceToDouble(it.high), low = priceToDouble(it.low),
                volume = it.volume, interval = interval, dateTime = date
            )
        }
        if (saveData) {
            executors.execute {
                runCatching {
                    candlesRepository.saveAll(converted)
                }.onFailure {
                    log.error("Error saving candles: {}", it.message, it)
                }
            }
        }
        return converted
    }
}