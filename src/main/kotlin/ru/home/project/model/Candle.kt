package ru.home.project.model

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import ru.home.project.config.deserializer.TwelveDataCandleDateDeserializer
import java.time.LocalDateTime

/**
 * @author rlagay
 */
data class Candle(
    @JsonProperty("open") val open: Double,
    @JsonProperty("close") val close: Double,
    @JsonProperty("high") val high: Double,
    @JsonProperty("low") val low: Double,
    @JsonProperty("datetime") @JsonDeserialize(using = TwelveDataCandleDateDeserializer::class)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", shape = JsonFormat.Shape.STRING) val datetime: LocalDateTime
)
