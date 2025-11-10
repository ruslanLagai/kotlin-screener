package ru.home.project.service.impl

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import ru.home.project.entity.InstrumentEntity
import ru.home.project.entity.PatternEntity
import ru.home.project.model.ItemType
import ru.home.project.repository.PatternsRepository
import ru.home.project.service.PatternService
import ru.home.project.service.PatternsDetectionService
import ru.tinkoff.piapi.contract.v1.CandleInterval

/**
 * @author rlagay
 */
@Service
class PatternsDetectionServiceImpl(
    val trianglePatternServiceImpl: PatternService,
    val patternsRepository: PatternsRepository,
): PatternsDetectionService {

    val log: Logger = LoggerFactory.getLogger(PatternsDetectionServiceImpl::class.java)

    /**
     * Detect patterns (channel & triangle) for selected interval
     *
     * @param figi - figi
     * @param interval - interval to detect levels
     * @param type - item type
     */
    override fun detectPatterns(
        instrument: InstrumentEntity,
        interval: CandleInterval,
        type: ItemType
    ) {

        val pattern = trianglePatternServiceImpl.detectPattern(
            figi = instrument.figi,
            ticker = instrument.ticker,
            instrumentUid = instrument.instrumentUid,
            interval = interval,
            days = 30,
            type = type
        )
        if (pattern == null) {
            log.debug("Pattern is not detected for instrument {}", instrument.figi)
            return
        }
        log.info("Pattern detected for instrument {}", instrument.figi)

        val optional = patternsRepository.findPattern(
            figi = instrument.figi,
            maxA = pattern.aMax,
            maxB = pattern.bMax,
            minA = pattern.aMin,
            minB = pattern.bMin
        )
        if (optional.isPresent) {
            log.debug("Pattern is already in pattern_entity {}", instrument.figi)
            return
        }

        optional.orElseGet {
            patternsRepository.save(PatternEntity(
                figi = instrument.figi,
                ticker = instrument.ticker,
                patternType = pattern.patternType,
                interval = interval,
                startDate = pattern.startDate,
                maxA = pattern.aMax,
                maxB = pattern.bMax,
                minA = pattern.aMin,
                minB = pattern.bMin,
                instrumentUid = instrument.instrumentUid!!)
            )
        }
    }
}