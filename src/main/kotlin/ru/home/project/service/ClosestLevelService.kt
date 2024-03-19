package ru.home.project.service

import ru.home.project.entity.MergedLevelEntity

/**
 * @author rlagay
 */
interface ClosestLevelService {

    /**
     * Получение ближайших уровней: возвращаются те, что ближе 2%
     */
    fun getClosestLevel(figi: String, price: Double): MergedLevelEntity?

    /**
     * Кэширование уровней
     */
    fun addLevel(figi: String, level: MergedLevelEntity)
}