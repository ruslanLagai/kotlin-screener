package ru.home.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.home.project.entity.LevelEntity

/**
 * @author rlagay
 */
@Repository
interface LevelsRepository : JpaRepository<LevelEntity, String> {

    fun getLevelsByFigi(figi: String): Set<LevelEntity>

    fun getLevelEntityByLevelBetween(min: Double, max: Double): Set<LevelEntity>
}