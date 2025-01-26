package ru.home.project.utils

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ru.home.project.entity.LevelEntity
import ru.home.project.entity.MergedLevelEntity
import ru.home.project.util.readValueAsString
import java.time.ZonedDateTime

/**
 * @author rlagay
 */
class LevelUtilsKtTest {

    private val mapper = ObjectMapper()

    @BeforeEach
    fun init () {
        mapper.registerModules(JavaTimeModule())
    }

    @Test
    fun `test merge levels`() {
        val dateTime = ZonedDateTime.now()
        val levels = listOf(
            LevelEntity(figi = "figi", ticker = "ticker", levelDate = dateTime, level = 100.0),
            LevelEntity(figi = "figi", ticker = "ticker", levelDate = dateTime.minusDays(1), level = 99.1),
            LevelEntity(figi = "figi", ticker = "ticker", levelDate = dateTime.minusDays(2), level = 100.5),
            LevelEntity(figi = "figi", ticker = "ticker", levelDate = dateTime.minusDays(3), level = 102.0),
            LevelEntity(figi = "figi", ticker = "ticker", levelDate = dateTime.minusDays(4), level = 96.9),

            LevelEntity(figi = "figi", ticker = "ticker", levelDate = dateTime.minusDays(5), level = 150.0),
            LevelEntity(figi = "figi", ticker = "ticker", levelDate = dateTime.minusDays(6), level = 149.1),
            LevelEntity(figi = "figi", ticker = "ticker", levelDate = dateTime.minusDays(7), level = 150.9),
            LevelEntity(figi = "figi", ticker = "ticker", levelDate = dateTime.minusDays(8), level = 152.0),
            LevelEntity(figi = "figi", ticker = "ticker", levelDate = dateTime.minusDays(9), level = 140.9)
        )

        val result = ArrayList<MergedLevelEntity>(mergeLevels(levels)).sortedWith(Comparator.comparing { it.minLevel })

        assertEquals(3, result.size)
        result.forEach {
            assertEquals("figi", it.figi)
            assertEquals("ticker", it.ticker)
        }
        assertEquals(96.9, result[0].minLevel)
        assertEquals(102.0, result[0].maxLevel)

        assertEquals(140.9, result[1].minLevel)
        assertEquals(140.9, result[1].maxLevel)

        assertEquals(149.1, result[2].minLevel)
        assertEquals(152.0, result[2].maxLevel)
    }

    @Test
    fun `test merge levels - bugfix`() {
        val typeRef: TypeReference<ArrayList<LevelEntity>> = object : TypeReference<ArrayList<LevelEntity>>() {}
        val content: String = readValueAsString("levels/detected-levels-bugfix.json")
        val levels: ArrayList<LevelEntity> = mapper.readValue(content, typeRef)

        val result = ArrayList<MergedLevelEntity>(mergeLevels(levels)).sortedWith(Comparator.comparing { it.minLevel })

        assertEquals(11, result.size)

    }

    @Test
    fun `test POSI levels - merge 2 percent`() {
        val content: String = readValueAsString("levels/posi-levels.json")
        val levels: ArrayList<LevelEntity> = mapper.readValue(content, object : TypeReference<ArrayList<LevelEntity>>() {})

        val result = mergeLevels(levels)
        assertEquals(15, result.size)

    }
}