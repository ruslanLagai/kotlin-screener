package ru.home.project.entity

import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
@Entity(name = "candle_entity")
open class CandleEntity(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) open val id: Long? = null,
    open val figi: String,
    open val ticker: String? = null,
    open val open: Double,
    open val high: Double,
    open val low: Double,
    open val close: Double,
    open val volume: Long,
    open val dateTime: ZonedDateTime,
    open val instrumentUid: String? = null,
    @CreationTimestamp open val createdAt: ZonedDateTime = ZonedDateTime.now(),
    @Version open val version: Long? = null,
    @Enumerated(value = EnumType.STRING) @Column(name = "interval_column") open val interval: CandleInterval
)
