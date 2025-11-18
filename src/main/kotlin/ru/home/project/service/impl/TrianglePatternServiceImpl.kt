package ru.home.project.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.home.project.entity.CandleEntity
import ru.home.project.entity.ExtremumEntity
import ru.home.project.entity.PatternEntity
import ru.home.project.exceptions.InvalidTypeException
import ru.home.project.model.ItemType
import ru.home.project.model.PatternType
import ru.home.project.service.CandlesService
import ru.home.project.service.CryptoCandlesService
import ru.home.project.service.PatternService
import ru.home.project.utils.checkExtremumsAreBetweenLines
import ru.home.project.utils.getExtremums
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.Set

/**
 * @author rlagay
 */
@Service
final class TrianglePatternServiceImpl(
    val candlesService: CandlesService,
    val cryptoCandlesService: CryptoCandlesService
) : PatternService {

    private final val log: Logger = LoggerFactory.getLogger(TrianglePatternServiceImpl::class.java)

    private val tinkoffCandlesProvider: (String, String?, CandleInterval, Int) -> List<CandleEntity> =
        {
            figi, intrumentUid, interval, from ->
            candlesService.getLastCandles(figi = figi, instrumentUid = intrumentUid, interval = interval, dayFrom = from)
        }

    private val cryptoCandlesProvider: (String, String?, CandleInterval, Int) -> List<CandleEntity> =
        {
            symbol, _, interval, from ->
            cryptoCandlesService.getHourlyCandles(symbol = symbol, LocalDateTime.now().minusDays(from.toLong()))
                .map {
                    val instant = it.datetime.toInstant(ZoneOffset.UTC)
                    CandleEntity(figi = symbol, open = it.open, close = it.close, low = it.low, high = it.high, volume = 0,
                        dateTime = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC), interval = interval)
                }.sortedBy { it.dateTime }
        }

    private val noCandlesProvider: (String, String?, CandleInterval, Int) -> List<CandleEntity> =
        {
                _, _, _, _ -> throw InvalidTypeException("Unknown type")
        }

    private final val candlesProvider: Map<ItemType, (String, String?, CandleInterval, Int) -> List<CandleEntity>> = mapOf(
        ItemType.CRYPTO to cryptoCandlesProvider,
        ItemType.STOCK to tinkoffCandlesProvider
    )

    // for crypto candles - candles sorting should be added
    override fun detectPattern(figi: String, ticker: String, intrumentUid: String?, interval: CandleInterval,
                               days: Int, type: ItemType): PatternEntity? {

        val candles = candlesProvider.getOrDefault(type, noCandlesProvider).invoke(figi, intrumentUid, interval, days)

        val extremums = getExtremums(candles, days, type)
        val maximums = extremums.first
        val minimums = extremums.second

        if (maximums.isEmpty() || minimums.isEmpty()) {
            return null
        }

        val patternDto = checkExtremumsAreBetweenLines(maximums, minimums, candles);
        if (patternDto == null) {
            log.debug("Extremums are not between lines.")
            return null
        }

        log.info("Maximums: $maximums")
        log.info("Minimums: $minimums")
        if (patternDto.patternType == PatternType.TRIANGLE && !checkExtremumsOrdered(maximums, minimums)) {
            log.debug("Extremums are not ordered")
            return null
        }

        val startDate = listOf(maximums, minimums).flatten().minBy { it.dateTime }.dateTime
        val emptySet: Set<ExtremumEntity> = setOf<ExtremumEntity>() as Set<ExtremumEntity>
        val patternEntity = PatternEntity(
            figi = figi,
            ticker = ticker,
            patternType = patternDto.patternType,
            interval = interval,
            startDate = startDate.toLocalDateTime(),
            maxA = patternDto.maxA,
            maxB = patternDto.maxB,
            minA = patternDto.minA,
            minB = patternDto.minB,
            instrumentUid = intrumentUid!!,
            maximums = emptySet,
            minimums = emptySet
        )
        patternEntity.minimums = minimums.map {
            ExtremumEntity(value = it.low, date = it.dateTime.toLocalDateTime(), patternEntity = patternEntity)
        }.toSet() as Set<ExtremumEntity>
        patternEntity.maximums = maximums.map {
            ExtremumEntity(value = it.high, date = it.dateTime.toLocalDateTime(), patternEntity = patternEntity)
        }.toSet() as Set<ExtremumEntity>

        return patternEntity
    }

    /**
     * Checks if the given maximums and minimums are ordered in time and value.
     * Maximums should be in descending order by value and minimums in ascending order.
     *
     * The check is suitable only for TRIANGLE pattern.
     *
     * @param maximums List of candle entities representing maximums.
     * @param minimums List of candle entities representing minimums.
     * @return True if both maximums and minimums are ordered correctly, false otherwise.
     */
    private final fun checkExtremumsOrdered(maximums: List<CandleEntity>, minimums: List<CandleEntity>): Boolean {
        val isMaxOrdered = maximums.sortedBy { it.dateTime }.map { it.high }.zipWithNext().all { it.first > it.second }
        val isMinOrdered = minimums.sortedBy { it.dateTime }.map { it.low }.zipWithNext().all { it.first < it.second }

        return isMaxOrdered && isMinOrdered
    }
}