package ru.home.project.util

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.google.protobuf.util.JsonFormat
import ru.home.project.processor.DailyLevelProcessorTest
import ru.tinkoff.piapi.contract.v1.GetCandlesResponse
import java.io.File
import java.nio.file.Files
import kotlin.io.path.Path

/**
 * @author rlagay
 */

fun readValueAsString(resourcePath: String?): String {
    val path = TestUtils::class.java.getClassLoader().getResource(resourcePath).path
    return Files.readString(Path(path))
}
fun <T> readValue(resourcePath: String?, clazz: Class<T>?): T {
    val path = TestUtils::class.java.getClassLoader().getResource(resourcePath).path
    val input = Files.readString(Path(path))

    val mapper = ObjectMapper()
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    mapper.disable(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)
    mapper.registerModule(JavaTimeModule())
    mapper.registerModule(KotlinModule.Builder().build())
    return mapper.readValue(input, clazz)
}

fun getCandles(file: String): GetCandlesResponse {
    val path = TestUtils::class.java.getClassLoader().getResource(file).path
    val input = Files.readString(Path(path))
    val builder = GetCandlesResponse.newBuilder()
    JsonFormat.parser().ignoringUnknownFields().merge(input, builder)
    return builder.build()
}

class TestUtils