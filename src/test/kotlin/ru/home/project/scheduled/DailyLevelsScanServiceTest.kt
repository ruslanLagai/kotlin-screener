package ru.home.project.scheduled

import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import ru.home.project.AbstractIntegrationTest
import ru.home.project.repository.PatternsRepository
import java.time.Duration

class DailyLevelsScanServiceTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var dailyLevelsScanService: DailyLevelsScanService

    @Autowired
    private lateinit var patternsRepository: PatternsRepository

    @Test
    @Disabled("only for debugging")
    fun `test patterns`() {
        dailyLevelsScanService.scanPatterns()

        val res = patternsRepository.findAll()
        await().atMost(Duration.ofMinutes(2)).untilAsserted {
            assertThat(res).isNotEmpty()
        }
    }

}