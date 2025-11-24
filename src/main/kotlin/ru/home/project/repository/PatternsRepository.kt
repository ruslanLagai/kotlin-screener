package ru.home.project.repository

import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.home.project.entity.PatternEntity
import java.util.Optional

/**
 * @author rlagay
 */
@Repository
interface PatternsRepository : JpaRepository<PatternEntity, String> {

    @Query("SELECT p FROM pattern_entity p WHERE p.figi = :figi AND p.maxA = :maxA AND p.maxB = :maxB " +
            "AND p.minA = :minA AND p.minB = :minB")
    fun findPattern(figi: String, maxA: Double, maxB: Double, minA: Double, minB: Double): Optional<PatternEntity>

    @Cacheable(cacheNames = ["patterns"], key = "#figi")
    fun findPatternByFigiAndFinishedIsTrue(figi: String): List<PatternEntity>

    fun findPatternByInstrumentUidAndFinishedIsTrue(figi: String): List<PatternEntity>
}