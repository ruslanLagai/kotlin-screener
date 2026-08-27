package ru.home.project.entity

import com.fasterxml.jackson.annotation.JsonFormat
import jakarta.persistence.*
import ru.home.project.model.LevelType
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
@Entity(name = "level_statistic_entity")
class LevelStatisticsEntity(
    @Id @GeneratedValue(strategy = GenerationType.UUID) var id: String? = null,
    var figi: String?,
    var ticker: String?,
    var maxLevel: Double,
    var minLevel: Double,
    @param:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ssXXX") var minLevelDate: ZonedDateTime,
    @param:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ssXXX") var maxLevelDate: ZonedDateTime,
    var successRate: Double = 0.0,
    var averageBreaking: Double = 0.0,
    var averageRebound: Double = 0.0,
    var totalCrosses: Int = 0,
    var goodSignals: Int = 0,
    @Enumerated(value = EnumType.STRING) var levelType: LevelType
)
