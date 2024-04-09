package ru.home.project.service

import ru.home.project.model.Candle
import java.time.LocalDate

/**
 * @author rlagay
 */
interface CryptoCandlesService {

    fun getDailyCandles(symbol: String, from: LocalDate) : List<Candle>

    fun getDailyCandles(symbol: String) : List<Candle>
}