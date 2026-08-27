package ru.home.project.repository

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager
import ru.home.project.entity.PatternEntity
import ru.home.project.model.PatternType
import ru.tinkoff.piapi.contract.v1.CandleInterval
import java.time.LocalDateTime

@DataJpaTest(properties = [
    "spring.h2.console.enabled=true",
    "spring.h2.console.path=/h2-console"
])
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class PatternsRepositoryTest {

    @Autowired
    private lateinit var patternsRepository: PatternsRepository

    @Autowired
    private lateinit var entityManager: TestEntityManager

    @Test
    fun `findPatternByFigiAndFinishedIsFalse should return patterns with finished false`() {
        // Given
        val figi = "TEST_FIGI"
        val pattern1 = PatternEntity(
            figi = figi,
            maxA = 1.0,
            maxB = 2.0,
            minA = 0.5,
            minB = 1.5,
            finished = false,
            instrumentUid = "1",
            ticker = "1",
            patternType = PatternType.CHANNEL,
            interval = CandleInterval.CANDLE_INTERVAL_DAY,
            startDate = LocalDateTime.now()
        )

        entityManager.persist(pattern1)
        entityManager.flush()

        // When
        val result = patternsRepository.findPatternByFigiAndFinishedIsFalse(figi)

        // Then
        assertEquals(1, result.size)
        assertTrue(result.all { !it.finished })
    }

    @Test
    fun `findPatternByFigiAndFinishedIsFalse should return empty list when no patterns found`() {
        // Given
        val figi = "NON_EXISTENT_FIGI"

        // When
        val result = patternsRepository.findPatternByFigiAndFinishedIsFalse(figi)

        // Then
        assertTrue(result.isEmpty())
    }

}
