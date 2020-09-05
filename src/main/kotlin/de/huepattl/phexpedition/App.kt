package de.huepattl.phexpedition

import de.huepattl.phexpedition.user.User
import de.huepattl.phexpedition.user.UserRepository
import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import io.quarkus.runtime.StartupEvent
import org.eclipse.microprofile.config.inject.ConfigProperty
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

@Singleton
class App(@Inject val userRepository: UserRepository) {

    private val log = Logger.getLogger(App::class.java)

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

@Path("/")
@Produces(MediaType.TEXT_HTML)
class Home(val home: Template) {

    @GET
    fun show(): TemplateInstance {
        return home.data("breadCrumbs", linkedMapOf(
                Pair("Home", "/")))
    }

}

class Converters {
    companion object {
        // Format YYYY-MM-DDTHH:mm, e.g. 2020-12-31T23:59
        fun parseLocalDateTime(string: String, default: Instant): Instant {
            if (string == null) {
                return default
            }
            val (date, time) = string.split("T")
            val (year, month, day) = date.split("-")
            val (hour, minute) = time.split(":")

            val localDateTime = LocalDateTime.of(year.toInt(), month.toInt(), day.toInt(), hour.toInt(), minute.toInt())
            val zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"))

            val instant = Instant.from(zonedDateTime)

            return instant
        }

        fun toString(instant: Instant): String {
            val ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
            val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            val str =  fmt.format(ldt).replace(' ', 'T')//"${ldt.year}-${ldt.monthValue}-${ldt.dayOfMonth}T${ldt.hour}:${ldt.minute}"

            return str
        }
    }
}
