package ru.home.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.home.project.entity.InstrumentEntity
import ru.home.project.entity.LevelEntity

/**
 * @author rlagay
 */
@Repository
interface InstrumentRepository : JpaRepository<InstrumentEntity, Long> {

    fun getByFigi(figi: String): InstrumentEntity?
}