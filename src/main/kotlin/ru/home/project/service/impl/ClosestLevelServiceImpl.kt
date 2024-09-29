package ru.home.project.service.impl

import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.home.project.entity.MergedLevelEntity
import ru.home.project.repository.MergedLevelsRepository
import ru.home.project.service.ClosestLevelService
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * Сервис для получения ближайшего уровня для дальнейшего анализа
 *
 * @author rlagay
 */
@Service
class ClosestLevelServiceImpl(
    val mergedLevelsRepository: MergedLevelsRepository,

) : ClosestLevelService {

    private val log: Logger = LoggerFactory.getLogger(ClosestLevelServiceImpl::class.java)
    private val levels: ConcurrentHashMap<String, ArrayList<MergedLevelEntity>> = ConcurrentHashMap()

    @PostConstruct
    fun init() {
        val savedLevels = mergedLevelsRepository.findAll().groupBy { it.figi }
        savedLevels.forEach {
            levels[it.key] = ArrayList(it.value)
        }
    }

    override fun getClosestLevel(figi: String, price: Double): MergedLevelEntity? {
        val levels = levels[figi]
        if (levels.isNullOrEmpty()) {
            log.warn("No levels for {}", figi)
            return null
        }

        val closestLevel = levels.filter {
            if (it.minLevel < price && it.maxLevel < price) {
                abs(it.maxLevel - price) / price < 0.01
            } else if (it.minLevel > price && it.maxLevel > price) {
                abs(it.minLevel - price) / price < 0.01
            } else {
                false
            }
        }.minByOrNull { abs(((it.maxLevel + it.minLevel) / 2) - price) }

        if (closestLevel == null) {
            log.debug("No levels found for {}, price {}", figi, price)
            return null
        }

        val isSupportLevel = closestLevel.maxLevel < price && closestLevel.minLevel < price

        val closestLevelValue =  if (isSupportLevel) closestLevel.maxLevel else closestLevel.minLevel

        return MergedLevelEntity(price = price, level = closestLevelValue, minLevel = closestLevel.minLevel,
            maxLevel = closestLevel.maxLevel, maxLevelDate = closestLevel.maxLevelDate, minLevelDate = closestLevel.minLevelDate,
            figi = figi)
    }

    override fun addLevel(figi: String, level: MergedLevelEntity) {
        synchronized(levels) {
            if (levels.containsKey(figi)) {
                levels[figi]?.add(level)
            } else {
                val list = ArrayList<MergedLevelEntity>()
                list.add(level)
                levels.put(figi, list)
            }
        }
    }
}