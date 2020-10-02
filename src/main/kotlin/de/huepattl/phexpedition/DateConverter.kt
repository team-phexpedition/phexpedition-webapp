package de.huepattl.phexpedition

import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

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
fun parseLocalDateTime(string: String, default: Instant, timeZoneId: String): Instant {
    if (string == null) {
        return default
    }
    val (date, time) = string.split("T")
    val (year, month, day) = date.split("-")
    val (hour, minute) = time.split(":")

    val localDateTime = LocalDateTime.of(year.toInt(), month.toInt(), day.toInt(), hour.toInt(), minute.toInt())
    val zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of(timeZoneId))

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

data class TimeZoneWithName(val tz: TimeZone, val name: String, val id: String)

/**
 * @see https://mkyong.com/java/java-display-list-of-timezone-with-gmt/
 * @see https://stackoverflow.com/questions/57468423/java-8-time-zone-zonerulesexception-unknown-time-zone-id-est
 */
fun timeZones(): List<TimeZoneWithName> {
    var result = mutableListOf<TimeZoneWithName>()
    ZoneId.getAvailableZoneIds().forEach {
        val tz = TimeZone.getTimeZone(it)
        val hours = TimeUnit.MILLISECONDS.toHours(tz.rawOffset.toLong())
        var minutes = TimeUnit.MILLISECONDS.toMinutes(tz.rawOffset.toLong()) - TimeUnit.HOURS.toMinutes(hours)
        // avoid -4:-30 issue
        minutes = abs(minutes)

        val tzString = if (hours > 0) {
            String.format("(GMT+%d:%02d) %s", hours, minutes, tz.id)
        } else {
            String.format("(GMT%d:%02d) %s", hours, minutes, tz.id)
        }

        result.add(TimeZoneWithName(id = tz.id, name = tzString, tz = tz))
    }
    return result
}

