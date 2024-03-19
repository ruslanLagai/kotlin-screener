package ru.home.project.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
@Entity(name = "level_entity")
data class LevelEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) val id: String? = null,
    val figi: String,
    val ticker: String,
    val level: Double,
    val levelDate: ZonedDateTime
)
