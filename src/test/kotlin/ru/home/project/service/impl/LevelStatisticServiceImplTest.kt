package ru.home.project.service.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import ru.home.project.entity.CandleEntity
import ru.home.project.entity.LevelStatisticsEntity
import ru.home.project.entity.MergedLevelEntity
import ru.home.project.model.LevelType
import ru.home.project.repository.LevelStatisticsRepository
import ru.home.project.util.readValueAsString
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.ZoneId
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
@ExtendWith(MockitoExtension::class)
class LevelStatisticServiceImplTest {

    private val mapper = ObjectMapper()
    private val levelStatisticsRepository = mock<LevelStatisticsRepository>()
    private val levelStatisticService = LevelStatisticServiceImpl(levelStatisticsRepository)

    @BeforeEach
    fun init () {
        mapper.registerModules(JavaTimeModule())
    }

    @Test
    fun `test analyze level`() {
        val typeRef: TypeReference<ArrayList<CandleEntity>> = object : TypeReference<ArrayList<CandleEntity>>() {}
        val content: String = readValueAsString("candles/tcsg-hourly-candles.json")
        val candles: ArrayList<CandleEntity> = mapper.readValue(content, typeRef)
        val levelDate = ZonedDateTime.of(2022, 11, 21, 0, 0, 0, 0, ZoneId.of("UTC"))
        val level = MergedLevelEntity(figi = "BBG00QPYJ5H0", ticker = "TCSG", minLevel = 2855.5, maxLevel = 2855.5,
            minLevelDate = levelDate, maxLevelDate = levelDate)

        val test = ArrayList<CandleEntity>(listOf(
            CandleEntity(figi = "", open = 2782.5, close = 2789.0, high = 2839.0, low = 2700.0, volume = 10, dateTime = ZonedDateTime.now(),
                interval = CandleInterval.CANDLE_INTERVAL_HOUR)
        ))
        levelStatisticService.analyzeStock("BBG00QPYJ5H0", "TCSG", level, CandleInterval.CANDLE_INTERVAL_HOUR, candles)

        val levelResistance = LevelStatisticsEntity(figi = "BBG00QPYJ5H0", ticker = "TCSG", maxLevel = 2855.5, minLevel = 2855.5,
            minLevelDate = levelDate, maxLevelDate = levelDate, successRate = 0.0, averageBreaking = 0.02, averageRebound = 0.08,
            totalCrosses = 1, goodSignals = 0, levelType = LevelType.Resistance)

        verify(levelStatisticsRepository).saveAll(ArrayList<LevelStatisticsEntity>(listOf(levelResistance)))
    }
}