package ru.home.project.utils

import ru.home.project.entity.LevelEntity
import ru.home.project.entity.MergedLevelEntity
import kotlin.math.abs
import kotlin.math.max

fun mergeLevels(levels: List<LevelEntity>): Set<MergedLevelEntity> {
    val mergedLevels = HashSet<MergedLevelEntity>()
    val levelsToSkip = ArrayList<LevelEntity>()

    for (level in levels) {
        if (levelsToSkip.contains(level)) {
            continue
        }

        val closeLevels = getNearByLevels(level, levels)
        val nearByLevels = HashSet<LevelEntity>(closeLevels)

        if (closeLevels.size > 1) {
            val buffer = ArrayList<LevelEntity>(closeLevels)
            for (i in 0..< buffer.size) {
                val closeToNearBy = getNearByLevels(buffer[i], levels)
                levelsToSkip.addAll(closeToNearBy)
                buffer.addAll(closeToNearBy)
            }
            nearByLevels.addAll(buffer)

        }

        val minLevel = nearByLevels.minBy { it.level }
        val maxLevel = nearByLevels.maxBy { it.level }

        mergedLevels.add(MergedLevelEntity(minLevel = minLevel.level, maxLevel = maxLevel.level, maxLevelDate = maxLevel.levelDate,
            minLevelDate = minLevel.levelDate, figi = maxLevel.figi, ticker = maxLevel.ticker))
    }
    return mergedLevels
}

private fun getNearByLevels(levelEntity: LevelEntity, levels: List<LevelEntity>): List<LevelEntity> {
    return levels.filter { abs(it.level - levelEntity.level) / max(it.level, levelEntity.level) < 0.01 }
}