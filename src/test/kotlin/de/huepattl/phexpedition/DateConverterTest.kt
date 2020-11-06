package de.huepattl.phexpedition

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

internal class DateConverterTest {

    @Test
    fun parseLocalDateTimeDefaultValue() {
        // given
        val input = Instant.now()

        // when
        val output = DateConverter.parseLocalDateTimeAsUtc(default = input)

        // then
        assertEquals(input, output)
    }

    @Test
    fun parseLocalDateTime() {
        // given
        val input = "2020-12-31T23:59"

        // when
        val output = DateConverter.parseLocalDateTimeAsUtc(string = input,
                userTimeZone = ZoneId.of("Europe/Berlin"))

        // then
        val expected = LocalDateTime.of(2020, 12, 31, 22, 59)
        assertEquals(Instant.from(ZonedDateTime.of(expected, ZoneId.of("UTC"))), output)
    }

    @Test
    fun parseLocalDateTimeError() {
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            DateConverter.parseLocalDateTimeAsUtc(string = "foo bar no date",
                    userTimeZone = ZoneId.of("Europe/Berlin"))
        }
    }

}
