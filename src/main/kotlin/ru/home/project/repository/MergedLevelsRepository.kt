package ru.home.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.home.project.entity.MergedLevelEntity

/**
 * @author rlagay
 */
@Repository
interface MergedLevelsRepository : JpaRepository<MergedLevelEntity, String> {

    fun getLevelsByFigi(figi: String): Set<MergedLevelEntity>
}