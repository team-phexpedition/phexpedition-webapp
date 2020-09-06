package de.huepattl.phexpedition

import de.huepattl.phexpedition.user.User
import de.huepattl.phexpedition.user.UserRepository
import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import io.quarkus.runtime.StartupEvent
import org.jboss.logging.Logger
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.ext.ExceptionMapper
import javax.ws.rs.ext.Provider

@Singleton
class App(@Inject val userRepository: UserRepository) {

    private val log = Logger.getLogger(App::class.java)

    /**
     * Create an admin user upon start if we cannot find one. The initial password
     * then is logged to be used for the first login.
     */
    @Transactional
    fun initAdminUser(@Observes event: StartupEvent) {
        if (userRepository.findByLogin("admin") == null) {
            val randomPassword = UUID.randomUUID().toString()
            userRepository.persist(User(login = "admin", displayName = "Admin User",
                    password = BcryptUtil.bcryptHash(randomPassword), roles = "admin"))
            log.info("+++ !!! GENERATED ADMIN PASSWORD IS: '$randomPassword' !!!+++")
            log.info("+++ !!! Please change it immediately after loggin in.  !!!+++")
        }
    }

}

/**
 * Home page is rendered here.
 */
@Path("/")
@Produces(MediaType.TEXT_HTML)
class Home(val home: Template) {

    @GET
    fun show(): TemplateInstance {
        return home.data("breadCrumbs", linkedMapOf(
                Pair("Home", "/")))
    }

}

/**
 * Turned out that still datetime formats differ between web and Java, thus we
 * handle conversion here until we found a better place.
 */
class Converters {

    companion object {

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
            val str =  fmt.format(ldt).replace(' ', 'T')//"${ldt.year}-${ldt.monthValue}-${ldt.dayOfMonth}T${ldt.hour}:${ldt.minute}"

            return str
        }

    }
}
