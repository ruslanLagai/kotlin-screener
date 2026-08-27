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
class LevelEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: String? = null,
    var figi: String,
    var ticker: String,
    var level: Double,
    var levelDate: ZonedDateTime
)
