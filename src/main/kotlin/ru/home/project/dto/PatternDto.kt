package ru.home.project.dto

import ru.home.project.model.PatternType

data class PatternDto(
    val maxA: Double,
    val maxB: Double,
    val minA: Double,
    val minB: Double,
    val patternType: PatternType
)
