package ru.home.project.processor

import org.springframework.stereotype.Component
import ru.home.project.utils.fourHoursToDetectLevel

/**
 * @author rlagay
 */
@Component
class FourHourLevelProcessor: AbstractLevelProcessor() {
    init {
        super.periodToDetect = fourHoursToDetectLevel
    }
}