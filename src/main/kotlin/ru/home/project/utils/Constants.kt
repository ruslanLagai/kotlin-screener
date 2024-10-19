package ru.home.project.utils

const val yearsToScan = 4
const val weeksToScan = yearsToScan * 53
const val daysToDetectLevel = 20
const val fourHoursToDetectLevel = 20
const val hoursToDetectCloseRetest = 120     // 5 days
const val percentageForLong = 1.02
const val percentageForGoodSignalLong = 1.01
const val percentageForGoodSignalShort = 0.99
const val percentageForShort = 0.98
const val percentageForCandlesLowBorder = 0.98
const val percentageForCandlesHighBorder = 1.02
const val telegramMessage = "%s приближается к уровню %s.\nВход %s, тейк %s, стоп %s\n%s"