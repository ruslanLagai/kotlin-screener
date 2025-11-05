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
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,
    val figi: String,
    val ticker: String? = null,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Long,
    val dateTime: ZonedDateTime,
    val instrumentUid: String? = null,
    @CreationTimestamp val createdAt: ZonedDateTime = ZonedDateTime.now(),
    @Version val version: Long? = null,
    @Enumerated(value = EnumType.STRING) @Column(name = "interval_column") val interval: CandleInterval
)
