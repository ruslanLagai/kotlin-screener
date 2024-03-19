package ru.home.project.utils

import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

fun timestampToDate(timestamp: Timestamp): ZonedDateTime {
    return LocalDateTime.ofEpochSecond(timestamp.seconds, timestamp.nanos, ZoneOffset.UTC)
        .atZone(ZoneId.of("UTC"))
}