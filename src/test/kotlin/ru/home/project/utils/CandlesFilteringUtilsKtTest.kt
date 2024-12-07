package ru.home.project.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import ru.home.project.entity.CandleEntity
import ru.home.project.entity.MergedLevelEntity
import ru.home.project.util.getCandles
import ru.home.project.util.readValueAsString
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.function.Predicate

class CandlesFilteringUtilsKtTest {

    private val mapper = ObjectMapper()

    @BeforeEach
    fun init () {
        mapper.registerModules(JavaTimeModule())
    }

    @Test
    fun `test filtering daily candles`() {
        val candles = getCandles("candles/vkco-daily-candles.json")
            .candlesList.map { CandleEntity(figi = "TCS00A106YF0", low = priceToDouble(it.low), high = priceToDouble(it.high),
                open = priceToDouble(it.open), close = priceToDouble(it.close), volume = it.volume, dateTime = timestampToDate( it.time),
                ticker = "VKCO", interval = CandleInterval.CANDLE_INTERVAL_DAY) }
        val level = MergedLevelEntity(figi = "TCS00A106YF0", ticker = "VKCO", minLevelDate = ZonedDateTime.now().minusYears(5), level = 652.0,
            minLevel = 652.0, maxLevel = 652.0, maxLevelDate = ZonedDateTime.now().minusYears(5))

        val result = retrieveDatesAroundLevel(candles, level)

        assertEquals(15, result.size)
        assertAll(
            { assertEquals(LocalDate.of(2023,6,5), result[0].start) },
            { assertEquals(LocalDate.of(2023,6,9), result[0].end) }
        )
        assertAll(
            { assertEquals(LocalDate.of(2023,6,13), result[1].start) },
            { assertEquals(LocalDate.of(2023,6,14), result[1].end) }
        )
        assertAll(
            { assertEquals(LocalDate.of(2023,6,21), result[2].start) },
            { assertEquals(LocalDate.of(2023,6,26), result[2].end) }
        )
        assertAll(
            { assertEquals(LocalDate.of(2023,6,28), result[3].start) },
            { assertEquals(LocalDate.of(2023,6,28), result[3].end) }
        )
        assertAll(
            { assertEquals(LocalDate.of(2023,7,13), result[4].start) },
            { assertEquals(LocalDate.of(2023,7,17), result[4].end) }
        )
        assertAll(
            { assertEquals(LocalDate.of(2023,7,20), result[5].start) },
            { assertEquals(LocalDate.of(2023,7,24), result[5].end) }
        )
        assertAll(
            { assertEquals(LocalDate.of(2023,8,4), result[6].start) },
            { assertEquals(LocalDate.of(2023,8,4), result[6].end) }
        )
        assertAll(
            { assertEquals(LocalDate.of(2023,9,11), result[7].start) },
            { assertEquals(LocalDate.of(2023,9,15), result[7].end) }
        )
        assertAll(
            { assertEquals(LocalDate.of(2023,9,18), result[8].start) },
            { assertEquals(LocalDate.of(2023,9,18), result[8].end) }
        )
        assertAll(
            { assertEquals(LocalDate.of(2023,10,20), result[9].start) },
            { assertEquals(LocalDate.of(2023,10,23), result[9].end) }
        )
        assertAll(
            { assertEquals(LocalDate.of(2023,11,6), result[10].start) },
            { assertEquals(LocalDate.of(2023,11,8), result[10].end) }
        )
        assertAll(
            { assertEquals(LocalDate.of(2023,11,21), result[11].start) },
            { assertEquals(LocalDate.of(2023,11,23), result[11].end) }
        )
        assertAll(
            { assertEquals(LocalDate.of(2023,11,29), result[12].start) },
            { assertEquals(LocalDate.of(2023,11,29), result[12].end) }
        )
    }

    @Test
    fun `test split candles to interval - bug fix`() {
        val candles = getCandles("candles/gmkn-hourly-candles-bugfix.json")
            .candlesList.map { CandleEntity(figi = "BBG00QPYJ5H0", low = priceToDouble(it.low), high = priceToDouble(it.high),
                open = priceToDouble(it.open), close = priceToDouble(it.close), volume = it.volume, dateTime = timestampToDate( it.time),
                ticker = "GMKN", interval = CandleInterval.CANDLE_INTERVAL_DAY) }
        val level = MergedLevelEntity(figi = "BBG00QPYJ5H0", ticker = "GMKN", minLevel = 17126.0, maxLevel = 17360.0,
            minLevelDate = ZonedDateTime.now(), maxLevelDate = ZonedDateTime.now())
        val predicate = Predicate<CandleEntity> { candle -> candle.low <= level.minLevel && candle.high >= level.minLevel }
        val result = getInterval(candles, predicate, false, level)

        assertEquals(1, result.size)
        assertEquals(ZonedDateTime.of(2023, 12, 8, 7, 0, 0, 0, ZoneId.of("UTC")), result.keys.first())
    }

    @Test
    fun `test split candles to support interval - full candles GMKN`() {
        val typeRef: TypeReference<ArrayList<CandleEntity>> = object : TypeReference<ArrayList<CandleEntity>>() {}
        val content: String = readValueAsString("candles/gmkn-hourly-candles-full.json")
        val candles: ArrayList<CandleEntity> = mapper.readValue(content, typeRef)
        val level = MergedLevelEntity(figi = "BBG00QPYJ5H0", ticker = "GMKN", minLevel = 17126.0, maxLevel = 17360.0,
            minLevelDate = ZonedDateTime.now(), maxLevelDate = ZonedDateTime.now())
        val predicate = Predicate<CandleEntity> { candle -> candle.low <= level.maxLevel && candle.high >= level.maxLevel
                || candle.low <= level.minLevel && candle.high >= level.minLevel }
        // candles near the level
        val sorted = candles
            .filter { candle -> getCloseCandlesPredicate(level).test(candle) }
            .sortedWith(Comparator.comparing(CandleEntity::dateTime))

        val support = getInterval(sorted, predicate, true, level)
        val resistance = getInterval(sorted, predicate, false, level)

        removeCloseRetests(support, resistance, predicate)

        assertEquals(3, support.size)
        assertEquals(3, resistance.size)
    }

}