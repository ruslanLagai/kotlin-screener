package ru.home.project.service.impl

import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import ru.home.project.entity.CandleEntity
import ru.home.project.properties.TinkoffTradingProperties
import ru.home.project.repository.CandlesRepository
import ru.home.project.service.CandlesService
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ExecutionException

/**
 * Real connection with tinkoff - testing historical candles interaction
 *
 * @author rlagay
 */
@SpringBootTest
@Testcontainers
class CandlesServiceImplTest {

    @Autowired
    private lateinit var candlesService: CandlesService

    @MockBean
    private lateinit var candlesRepository: CandlesRepository

    @MockBean
    private lateinit var tinkoffTradingProperties: TinkoffTradingProperties

    companion object {

        @JvmStatic
        @Container
        protected var container = MySQLContainer("mysql:8")

        @Container
        protected var redisContainer: GenericContainer<Nothing> = GenericContainer<Nothing>(DockerImageName.parse("redis:5.0.3-alpine"))
            .withExposedPorts(6379)

        @JvmStatic
        @DynamicPropertySource
        fun mysqlProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", container::getJdbcUrl)
            registry.add("spring.datasource.username", container::getUsername)
            registry.add("spring.datasource.password", container::getPassword)
            registry.add("spring.datasource.driver-class-name", container::getDriverClassName)
            registry.add("spring.flyway.schemas", container::getDatabaseName)
            registry.add("spring.data.redis.host", redisContainer::getHost)
            registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort)

        }

    }
    @Test
    fun `test getting candles from grpc only`() {

        `when`(candlesRepository.findByFigiAndIntervalOrderByDateTimeDesc(any(), any())).thenReturn(emptyList())
        val candles = ArrayList<CandleEntity>()

        val future = candlesService.getHistoricalCandles("BBG00QPYJ5H0", "TCSG", CandleInterval.CANDLE_INTERVAL_DAY, candles)

        future.get()
        assertFalse(candles.isEmpty())
        assertTrue(candles.size > 900)
    }

    @Test
    fun `test getting candles from db + grpc`() {
        `when`(candlesRepository.findByFigiAndIntervalOrderByDateTimeDesc(any(), any())).thenReturn(listOf(
            CandleEntity(id = 1, figi = "BBG00QPYJ5H0", ticker = "TCSG", open = 1000.0, close = 1001.0, high = 1001.0,
                low = 999.0, volume = 1000, interval = CandleInterval.CANDLE_INTERVAL_DAY,
                dateTime = ZonedDateTime.of(2023, 11, 11, 1, 1, 1, 1, ZoneId.of("UTC")))
        ))
        val candles = ArrayList<CandleEntity>()

        val future = candlesService.getHistoricalCandles("BBG00QPYJ5H0", "TCSG", CandleInterval.CANDLE_INTERVAL_DAY, candles)

        future.get()
        assertFalse(candles.isEmpty())
        assertTrue(candles.size > 40)
    }

    @Test
    fun `test getting candles from db + grpc - hour interval`() {
        `when`(candlesRepository.findByFigiAndIntervalOrderByDateTimeDesc(any(), any())).thenReturn(listOf(
            CandleEntity(id = 1, figi = "BBG00QPYJ5H0", ticker = "TCSG", open = 1000.0, close = 1001.0, high = 1001.0,
                low = 999.0, volume = 1000, interval = CandleInterval.CANDLE_INTERVAL_HOUR,
                dateTime = ZonedDateTime.now().minusDays(10))
        ))
        val candles = ArrayList<CandleEntity>()

        val future = candlesService.getHistoricalCandles("BBG00QPYJ5H0", "TCSG", CandleInterval.CANDLE_INTERVAL_HOUR, candles)

        future.get()
        assertFalse(candles.isEmpty())
        assertTrue(candles.size > 250)
    }

    @Test
    fun `test getting candles only from grpc - hour interval`() {
        `when`(candlesRepository.findByFigiAndIntervalOrderByDateTimeDesc(any(), any())).thenReturn(emptyList())
        val candles = ArrayList<CandleEntity>()

        val future = candlesService.getHistoricalCandles("BBG00QPYJ5H0", "TCSG", CandleInterval.CANDLE_INTERVAL_HOUR, candles)

        future.get()
        assertFalse(candles.isEmpty())
        assertTrue(candles.size > 1500)
    }

    @Test
    fun `test getting candles from db + grpc - exception`() {
        `when`(candlesRepository.findByFigiAndIntervalOrderByDateTimeDesc(any(), any())).thenReturn(listOf(
            CandleEntity(id = 1, figi = "BBG00QPYJ5H0", ticker = "TCSG", open = 1000.0, close = 1001.0, high = 1001.0,
                low = 999.0, volume = 1000, interval = CandleInterval.CANDLE_INTERVAL_1_MIN,
                dateTime = ZonedDateTime.of(2023, 10, 11, 1, 1, 1, 1, ZoneId.of("UTC")))
        ))
        val candles = ArrayList<CandleEntity>()

        val future = candlesService.getHistoricalCandles("BBG00QPYJ5H0", "TCSG", CandleInterval.CANDLE_INTERVAL_1_MIN, candles)

        assertThrows<ExecutionException> { future.get() }
        assertTrue(candles.isEmpty())
    }
}