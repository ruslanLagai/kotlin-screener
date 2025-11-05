package ru.home.project.model

import java.time.LocalDateTime
import ru.tinkoff.piapi.contract.v1.CandleInterval

/**
 * @author rlagay
 */
data class Pattern(
    val patternType: PatternType,
    val figi: String,
    val ticker: String?,
    val startDate: LocalDateTime,
    val price: Double?,
    val interval: CandleInterval,
    val aMax: Double,
    val bMax: Double,
    val aMin: Double,
    val bMin: Double
) 