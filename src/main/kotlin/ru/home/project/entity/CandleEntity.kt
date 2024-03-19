package ru.home.project.entity

import jakarta.persistence.*
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
@Entity(name = "candle_entity")
data class CandleEntity(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,
    val figi: String,
    val ticker: String? = null,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val dateTime: ZonedDateTime,
    @Enumerated(value = EnumType.STRING) @Column(name = "interval_column") val interval: CandleInterval
)
