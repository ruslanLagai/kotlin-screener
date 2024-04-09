package ru.home.project.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.home.project.client.TwelveDataClient
import ru.home.project.exceptions.TwelveDataException
import ru.home.project.model.Candle
import ru.home.project.service.CryptoCandlesService
import java.time.LocalDate

/**
 * @author rlagay
 */
@Service
class CryptoCandlesServiceImpl(
    val twelveDataClient: TwelveDataClient
) : CryptoCandlesService {

    private val log: Logger = LoggerFactory.getLogger(CryptoCandlesServiceImpl::class.java)


    override fun getDailyCandles(symbol: String, from: LocalDate) : List<Candle> {
        val size = LocalDate.now().dayOfYear - from.dayOfYear
        return getCandles(symbol, size)
    }

    override fun getDailyCandles(symbol: String) : List<Candle> {
       return getCandles(symbol, 2000)
    }

    private fun getCandles(symbol: String, size: Int) : List<Candle> {
        val result = kotlin.runCatching {
            twelveDataClient.getCryptoCandles(symbol, "1day", size)
        }.onFailure {
            log.error("Failed to retrieve crypto candles for {}", symbol, it)
            throw it
        }.onSuccess {
            log.debug("Retrieved crypto candles for {}", symbol)
        }.getOrNull()

        if (result.isNullOrEmpty()) {
            throw TwelveDataException("No candles for '${symbol}'")
        }
        return result
    }
}