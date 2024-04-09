package ru.home.project.service

import ru.home.project.model.CryptoPrice

/**
 * @author rlagay
 */
interface CoinMarketCapService {
    fun getCurrentPrices() : List<CryptoPrice>
}