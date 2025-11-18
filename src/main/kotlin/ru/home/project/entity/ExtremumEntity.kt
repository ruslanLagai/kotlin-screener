package ru.home.project.entity

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * @author rlagay
 */
@Entity(name = "extremum_entity")
open class ExtremumEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) open val id: String? = null,
    open val date: LocalDateTime,
    @Column(name = "value") open val value: Double,
    @ManyToOne @JoinColumn(name = "pattern_id", nullable = false)
    open val patternEntity: PatternEntity
)