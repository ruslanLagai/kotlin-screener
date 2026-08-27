package ru.home.project.repository

import org.springframework.cache.annotation.CachePut
import org.springframework.cache.annotation.Cacheable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.home.project.entity.InstrumentEntity

/**
 * @author rlagay
 */
@Repository
interface InstrumentRepository : JpaRepository<InstrumentEntity, Long> {

    @Cacheable(cacheNames = ["instruments"], key = "#figi", unless = "#result == null")
    fun getByFigi(figi: String): InstrumentEntity?

    @CachePut(cacheNames = ["instruments"], key = "#instrument.figi", unless = "#result == null")
    fun save(instrument: InstrumentEntity): InstrumentEntity
}