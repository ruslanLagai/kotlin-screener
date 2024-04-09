package ru.home.project.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.home.project.client.CoinMarketCapClient
import ru.home.project.exceptions.CoinMarketCapException
import ru.home.project.model.CryptoPrice
import ru.home.project.service.CoinMarketCapService

/**
 * @author rlagay
 */
@Service
class CoinMarketCapServiceImpl(
    val coinMarketCapClient: CoinMarketCapClient
): CoinMarketCapService {

    private val log: Logger = LoggerFactory.getLogger(CoinMarketCapServiceImpl::class.java)

    override fun getCurrentPrices() : List<CryptoPrice> {
        var prices: List<CryptoPrice> = ArrayList()
        kotlin.runCatching {
            val response = coinMarketCapClient.getCryptoCandles()
            if (response.isNullOrEmpty()) {
                throw CoinMarketCapException("No current prices for crypto")
            }

            prices = response
                .filter { it.quote != null }
                .filter { it.quote?.usd != null }
                .map {
                    val usd = it.quote?.usd
                    val symbol = it.symbol + "/" + usd?.javaClass?.simpleName
                    val price = it.quote?.usd?.price ?: 0.0
                    val dateTime = it.lastUpdated
                    val name = it.name
                    CryptoPrice(symbol, price, dateTime, name)
                }
        }.onFailure {
            log.error("Failed to retrieve crypto data", it)
        }
        return prices
    }
}