package de.huepattl.phexpedition

import org.jboss.logging.Logger
import java.lang.IllegalArgumentException
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class DateConverter {

    companion object {

        private val log = Logger.getLogger(DateConverter::class.java)

        /**
         * Returns a Java [Instant] for UTC based on a given local date time as passed
         * by the client. Browsers with `<input type="datetime-local" ...>` useprovide format
         * yyyy-MM-ddTHH:mm (e.g. 2020-12-31T23:59) while java is more precise and
         * [Instant.parse] does not understand that pattern.
         */
        fun parseLocalDateTimeAsUtc(string: String? = null, default: Instant = Instant.now(),
                                    userTimeZone: ZoneId = ZoneId.of("UTC")): Instant {

            try {
                if (string == null) {
                    return default
                }

                // split e.g. "2020-12-31T23:59" into separate date and time variables
                val (date, time) = string.split("T")

                // extract date fields from e.g. "2020-12-31"
                val (year, month, day) = date.split("-")

                // extract time fields from e.g. "23:59"
                val (hour, minute) = time.split(":")

                val localDateTime = LocalDateTime.of(year.toInt(), month.toInt(), day.toInt(),
                        hour.toInt(), minute.toInt())
                val originalZoned = ZonedDateTime.of(localDateTime, userTimeZone)
                val utcZoned = originalZoned.withZoneSameInstant(ZoneId.of("UTC"))

                return utcZoned.toInstant()
            } catch (exception: Exception) {
                throw IllegalArgumentException(
                        "The date passed was no valid date/time in form of YYYY-MM-DDTh24:mm", exception)
            }
        }

        /**
         * Returns datetime in required format of `<input type="datetime-local" ...>`
         */
        fun toString(instant: Instant, timeZone: ZoneId = ZoneOffset.UTC): String {
            val ldt = LocalDateTime.ofInstant(instant, timeZone)
            val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            val res = fmt.format(ldt).replace(' ', 'T')
            println("-toString of $instant for $timeZone: $res")
            return res
        }

    }
}

