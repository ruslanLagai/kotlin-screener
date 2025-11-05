package ru.home.project.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Version
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
@Entity(name = "instrument_entity")
open class InstrumentEntity(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,
    @Column(unique = true) val figi: String,
    @Column(unique = true) val ticker: String,
    @Version val version: Long? = null,
    val lot: Int,
    val currency: String,
    val name: String,
    var instrumentUid: String? = null,
    val first1dayCandleDate: ZonedDateTime? = null
)