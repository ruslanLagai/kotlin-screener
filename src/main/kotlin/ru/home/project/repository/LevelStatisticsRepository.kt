package ru.home.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.home.project.entity.LevelEntity
import ru.home.project.entity.LevelStatisticsEntity

/**
 * @author rlagay
 */
@Repository
interface LevelStatisticsRepository : JpaRepository<LevelStatisticsEntity, String> {

    fun getByFigi(figi: String): Set<LevelStatisticsEntity>
}