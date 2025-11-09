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
    @Id @GeneratedValue(strategy = GenerationType.AUTO) open val id: Long? = null,
    @Column(unique = true) open val figi: String,
    @Column(unique = true) open val ticker: String,
    @Version open val version: Long? = null,
    open val lot: Int,
    open val currency: String,
    open val name: String,
    open var instrumentUid: String? = null,
    open val first1dayCandleDate: ZonedDateTime? = null
)