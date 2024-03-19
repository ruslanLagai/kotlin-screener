package ru.home.project.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
@Entity(name = "instrument_entity")
data class InstrumentEntity(
    @Id @GeneratedValue(strategy = GenerationType.AUTO) val id: Long? = null,
    @Column(unique = true) val figi: String,
    @Column(unique = true) val ticker: String,
    val lot: Int,
    val currency: String,
    val name: String,
    val first1dayCandleDate: ZonedDateTime? = null
)