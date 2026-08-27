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
    @Id @GeneratedValue(strategy = GenerationType.UUID) open var id: String? = null,
    open var figi: String,
    open var instrumentUid: String,
    open var ticker: String,
    @Enumerated(value = EnumType.STRING) @Column(name = "pattern_type") open var patternType: PatternType,
    @Enumerated(value = EnumType.STRING) @Column(name = "interval_column") open var interval: CandleInterval,
    open var startDate: LocalDateTime,
    open var maxA: Double,
    open var maxB: Double,
    open var minA: Double,
    open var minB: Double,
    open var finished: Boolean = false,
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "patternEntity")
    open var minimums: Set<ExtremumEntity> = Set.of<ExtremumEntity>() as Set<ExtremumEntity>,
    @JsonIgnore
    @OneToMany(fetch = FetchType.LAZY, cascade = [CascadeType.ALL], mappedBy = "patternEntity")
    open var maximums: Set<ExtremumEntity> = Set.of<ExtremumEntity>() as Set<ExtremumEntity>
)