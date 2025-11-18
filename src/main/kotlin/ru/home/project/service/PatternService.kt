package ru.home.project.service

import ru.home.project.entity.PatternEntity
import ru.home.project.model.ItemType
import ru.tinkoff.piapi.contract.v1.CandleInterval

/**
 * @author rlagay
 */
interface PatternService {

    fun detectPattern(figi: String, ticker: String, instrumentUid: String?, interval: CandleInterval, days: Int, type: ItemType): PatternEntity?

}
