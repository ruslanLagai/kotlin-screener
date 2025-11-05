package ru.home.project.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import ru.home.project.model.PatternType
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.LocalDateTime

/**
 * @author rlagay
 */
@Entity(name = "pattern_entity")
open class PatternEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) val id: String? = null,
    val figi: String,
    val instrumentUid: String,
    val ticker: String,
    val patternType: PatternType,
    val interval: CandleInterval,
    val startDate: LocalDateTime,
    val maxA: Double,
    val maxB: Double,
    val minA: Double,
    val minB: Double,
    var finished: Boolean = false
)