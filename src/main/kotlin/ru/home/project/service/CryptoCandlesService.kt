package ru.home.project.service

import ru.home.project.model.Candle
import java.time.LocalDate
import java.time.LocalDateTime

/**
 * @author rlagay
 */
interface CryptoCandlesService {

    fun getFiveMinCandles(symbol: String, from: LocalDate) : List<Candle>

    fun getDailyCandles(symbol: String, from: LocalDate) : List<Candle>

    fun getHourlyCandles(symbol: String, from: LocalDateTime) : List<Candle>

    fun getDailyCandles(symbol: String) : List<Candle>
}