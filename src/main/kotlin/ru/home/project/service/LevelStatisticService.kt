package ru.home.project.service

import ru.home.project.entity.CandleEntity
import ru.home.project.entity.MergedLevelEntity
import ru.tinkoff.piapi.contract.v1.CandleInterval

/**
 * @author rlagay
 */
interface LevelStatisticService {

    /**
     * Analyze level and save to DB
     */
    fun analyzeStock(figi: String, ticker: String?, level: MergedLevelEntity,  interval: CandleInterval,
                     candles: ArrayList<CandleEntity>)
}