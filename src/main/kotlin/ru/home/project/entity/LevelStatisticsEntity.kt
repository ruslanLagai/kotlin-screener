package ru.home.project.entity

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.*
import ru.home.project.model.LevelType
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
@Entity(name = "level_statistic_entity")
data class LevelStatisticsEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) val id: String? = null,
    val figi: String,
    val ticker: String?,
    val maxLevel: Double,
    val minLevel: Double,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ssXXX") val minLevelDate: ZonedDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ssXXX") val maxLevelDate: ZonedDateTime,
    var successRate: Double = 0.0,
    var averageBreaking: Double = 0.0,
    var averageRebound: Double = 0.0,
    var totalCrosses: Int = 0,
    var goodSignals: Int = 0,
    @Enumerated(value = EnumType.STRING) var levelType: LevelType
)
