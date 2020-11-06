package de.huepattl.phexpedition

import java.time.ZoneId
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

data class TimeZoneWithName(val tz: TimeZone, val name: String, val id: String)

class TimeZones {

    companion object {

        /**
         * @see https://mkyong.com/java/java-display-list-of-timezone-with-gmt/
         * @see https://stackoverflow.com/questions/57468423/java-8-time-zone-zonerulesexception-unknown-time-zone-id-est
         */
        fun list(): List<TimeZoneWithName> {
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
            return result.toList().sortedBy { it.id }
        }

    }

}
