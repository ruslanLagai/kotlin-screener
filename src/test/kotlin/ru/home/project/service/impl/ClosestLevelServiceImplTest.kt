package ru.home.project.service.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.mock
import ru.home.project.entity.MergedLevelEntity
import ru.home.project.repository.MergedLevelsRepository
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
@ExtendWith(MockitoExtension::class)
class ClosestLevelServiceImplTest {

    private val levelsRepository = mock<MergedLevelsRepository>()
    private val closestLevelService = ClosestLevelServiceImpl(levelsRepository)

    @Test
    fun `test get support level`() {
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 90.0,
            minLevel = 90.0, maxLevel = 90.5, maxLevelDate = ZonedDateTime.now().minusDays(20)))
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 130.0,
            minLevel = 130.0, maxLevel = 130.0, maxLevelDate = ZonedDateTime.now().minusDays(10)
        ))
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 98.4,
            minLevel = 98.3, maxLevel = 98.5, maxLevelDate = ZonedDateTime.now().minusDays(20)
        ))
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 98.5,
            minLevel = 98.5, maxLevel = 98.6, maxLevelDate = ZonedDateTime.now().minusDays(20)
        ))
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 98.0,
            minLevel = 98.0, maxLevel = 98.0, maxLevelDate = ZonedDateTime.now().minusDays(20)
        ))
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 0.0,
            minLevel = 0.0, maxLevel = 0.0, maxLevelDate = ZonedDateTime.now().minusDays(20)
        ))
        val result = closestLevelService.getClosestLevel("figi", 100.0)

        assertEquals(98.6, result?.level ?: 0.0)
        assertEquals(98.6, result?.maxLevel ?: 0.0)
        assertEquals(98.5, result?.minLevel ?: 0.0)
        assertEquals(100.0, result?.price ?: 0.0)
    }

    @Test
    fun `test get resistance level`() {
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 90.0,
            minLevel = 90.0, maxLevel = 90.5, maxLevelDate = ZonedDateTime.now().minusDays(20)
        ))
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 130.0,
            minLevel = 130.0, maxLevel = 130.0, maxLevelDate = ZonedDateTime.now().minusDays(10)
        ))
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 98.4,
            minLevel = 98.4, maxLevel = 98.5, maxLevelDate = ZonedDateTime.now().minusDays(20)
        ))
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 98.5,
            minLevel = 98.5, maxLevel = 98.6, maxLevelDate = ZonedDateTime.now().minusDays(20)
        ))
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 98.0,
            minLevel = 98.0, maxLevel = 98.0, maxLevelDate = ZonedDateTime.now().minusDays(20)
        ))
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 0.0,
            minLevel = 0.0, maxLevel = 0.0, maxLevelDate = ZonedDateTime.now().minusDays(20)
        ))

        val result = closestLevelService.getClosestLevel("figi", 98.0)

        assertEquals(98.4, result?.level ?: 0.0)
        assertEquals(98.5, result?.maxLevel ?: 0.0)
        assertEquals(98.4, result?.minLevel ?: 0.0)
        assertEquals(98.0, result?.price ?: 0.0)
    }

    @Test
    fun `test no levels`() {
        closestLevelService.addLevel("figi",MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 90.0,
            minLevel = 90.0, maxLevel = 90.5, maxLevelDate = ZonedDateTime.now().minusDays(20)
        ))
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 130.0,
            minLevel = 130.0, maxLevel = 130.0, maxLevelDate = ZonedDateTime.now().minusDays(10)
        ))
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 98.4,
            minLevel = 98.4, maxLevel = 98.5, maxLevelDate = ZonedDateTime.now().minusDays(20)
        ))
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 98.5,
            minLevel = 98.5, maxLevel = 98.6, maxLevelDate = ZonedDateTime.now().minusDays(20)
        ))
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 98.0,
            minLevel = 98.0, maxLevel = 98.0, maxLevelDate = ZonedDateTime.now().minusDays(20)
        ))
        closestLevelService.addLevel("figi", MergedLevelEntity(figi = "figi", ticker = "ticker", minLevelDate = ZonedDateTime.now().minusDays(10), level = 0.0,
            minLevel = 0.0, maxLevel = 0.0, maxLevelDate = ZonedDateTime.now().minusDays(20)
        ))

        val result = closestLevelService.getClosestLevel("figi", 94.0)

        assertNull(result)
    }
}