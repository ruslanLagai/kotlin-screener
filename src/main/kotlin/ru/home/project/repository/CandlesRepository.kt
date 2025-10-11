package ru.home.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import ru.home.project.entity.CandleEntity
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
@Repository
interface CandlesRepository : JpaRepository<CandleEntity, String> {

    fun findByFigiAndIntervalOrderByDateTimeDesc(figi: String, interval: CandleInterval): List<CandleEntity>

    @Query("SELECT c FROM candle_entity c WHERE c.figi = :figi AND c.interval = :interval AND c.dateTime > :from")
    fun findCandlesForPeriod(figi: String, interval: CandleInterval, from: ZonedDateTime): List<CandleEntity>

}