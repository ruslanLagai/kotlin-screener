package ru.home.project.processor

import ru.home.project.entity.CandleEntity
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
interface LevelProcessor {

    fun processStock(figi: String, candles: List<CandleEntity>): Set<Pair<ZonedDateTime, Double>>
}