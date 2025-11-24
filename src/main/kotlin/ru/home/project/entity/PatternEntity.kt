package ru.home.project.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import ru.home.project.model.PatternType
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.LocalDateTime
import java.util.Set

/**
 * @author rlagay
 */
@Entity(name = "pattern_entity")
open class PatternEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) open val id: String? = null,
    open val figi: String,
    open val instrumentUid: String,
    open val ticker: String,
    @Enumerated(value = EnumType.STRING) @Column(name = "pattern_type") open val patternType: PatternType,
    @Enumerated(value = EnumType.STRING) @Column(name = "interval_column") open val interval: CandleInterval,
    open val startDate: LocalDateTime,
    open val maxA: Double,
    open val maxB: Double,
    open val minA: Double,
    open val minB: Double,
    open var finished: Boolean = false,
    @JsonIgnore @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "patternEntity")
    open var minimums: Set<ExtremumEntity> = Set.of<ExtremumEntity>() as Set<ExtremumEntity>,
    @JsonIgnore @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "patternEntity")
    open var maximums: Set<ExtremumEntity> = Set.of<ExtremumEntity>() as Set<ExtremumEntity>
)