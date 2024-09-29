package ru.home.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.home.project.entity.CandleEntity
import ru.tinkoff.piapi.contract.v1.CandleInterval

/**
 * @author rlagay
 */
@Repository
interface CandlesRepository : JpaRepository<CandleEntity, String> {

    fun findByFigiAndIntervalOrderByDateTimeDesc(figi: String, interval: CandleInterval): List<CandleEntity>

    fun findTop14ByFigiAndIntervalOrderByDateTimeDesc(figi: String, interval: CandleInterval): List<CandleEntity>

}