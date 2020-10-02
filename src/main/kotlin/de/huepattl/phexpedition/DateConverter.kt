package de.huepattl.phexpedition

import java.time.*
import java.time.format.DateTimeFormatter

/*
 * Turned out that still datetime formats differ between web and Java, thus we
 * handle conversion here until we found a better place.
 */

/**
 * Returns a Java [Instant] for UTC based on a given local date time as passed
 * by the client. Browsers with `<input type="datetime-local" ...>` useprovide format
 * yyyy-MM-ddTHH:mm (e.g. 2020-12-31T23:59) while java is more precise and
 * [Instant.parse] does not understand that pattern.
 *
 * TODO: allow for passing client time zone
 */
fun parseLocalDateTime(string: String, default: Instant): Instant {
    if (string == null) {
        return default
    }
    val (date, time) = string.split("T")
    val (year, month, day) = date.split("-")
    val (hour, minute) = time.split(":")

    val localDateTime = LocalDateTime.of(year.toInt(), month.toInt(), day.toInt(), hour.toInt(), minute.toInt())
    val zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"))

    return Instant.from(zonedDateTime)
}

/**
 * Returns datetime in required format of `<input type="datetime-local" ...>`
 *
 * TODO: allow for passing client time zone
 */
fun toString(instant: Instant): String {
    val ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
    val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    val str = fmt.format(ldt).replace(' ', 'T')//"${ldt.year}-${ldt.monthValue}-${ldt.dayOfMonth}T${ldt.hour}:${ldt.minute}"

    return str
}
