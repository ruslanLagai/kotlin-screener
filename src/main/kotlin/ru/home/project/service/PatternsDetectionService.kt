package ru.home.project.service

import ru.home.project.entity.InstrumentEntity
import ru.home.project.model.ItemType
import ru.tinkoff.piapi.contract.v1.CandleInterval

/**
 * Находит паттерны
 *
 * @author rlagay
 */
interface PatternsDetectionService {

    fun detectPatterns(instrumentEntity: InstrumentEntity, interval: CandleInterval, type: ItemType)

}