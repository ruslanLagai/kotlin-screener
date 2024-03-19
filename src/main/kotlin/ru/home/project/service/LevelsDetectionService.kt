package ru.home.project.service

import ru.tinkoff.piapi.contract.v1.CandleInterval

/**
 * Находит уровни (раз в сутки)
 *
 * @author rlagay
 */
interface LevelsDetectionService {

    fun detectLevels(figi: String, interval: CandleInterval, intervalForStatistics: CandleInterval)
}