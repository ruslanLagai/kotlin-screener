package ru.home.project.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import ru.home.project.entity.ActiveTradeEntity
import java.util.*

/**
 * @author rlagay
 */
@Repository
interface ActiveTradeRepository : JpaRepository<ActiveTradeEntity, UUID> {

    fun getByTickerAndClosedFalse(ticker: String) : ActiveTradeEntity
}