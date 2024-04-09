package ru.home.project.model

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.DateDeserializers.DateDeserializer
import java.util.*

/**
 * @author rlagay
 */
data class Candle(
    @JsonProperty("open") val open: Double,
    @JsonProperty("close") val close: Double,
    @JsonProperty("high") val high: Double,
    @JsonProperty("low") val low: Double,
    @JsonProperty("datetime") @JsonDeserialize(using = DateDeserializer::class) val datetime: Date
)
