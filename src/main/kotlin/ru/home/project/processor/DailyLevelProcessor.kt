package ru.home.project.processor

import org.springframework.stereotype.Component
import ru.home.project.utils.daysToDetectLevel

/**
 * @author rlagay
 */
@Component
class DailyLevelProcessor: AbstractLevelProcessor() {

    init {
        super.periodToDetect = daysToDetectLevel
    }
}