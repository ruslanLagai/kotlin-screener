package ru.home.project.config.deserializer

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

/**
 * @author rlagay
 */
class TwelveDataCandleDateDeserializer : JsonDeserializer<LocalDateTime>() {

    private val pattern = Pattern.compile("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")
    private val dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    override fun deserialize(jsonParser: JsonParser?, p1: DeserializationContext?): LocalDateTime {

        val dateAsString: String = jsonParser!!.text

        val isDateTime = pattern.matcher(dateAsString).matches()

        if (isDateTime) {
            return LocalDateTime.parse(dateAsString, dateTimeFormatter)
        }

        return LocalDate.parse(dateAsString, dateFormatter).atStartOfDay()
    }


}