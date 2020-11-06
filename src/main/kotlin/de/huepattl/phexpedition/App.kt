package de.huepattl.phexpedition

import de.huepattl.phexpedition.user.UserEntity
import de.huepattl.phexpedition.user.UserRepository
import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import io.quarkus.runtime.StartupEvent
import io.quarkus.runtime.configuration.ProfileManager
import org.jboss.logging.Logger
import org.jboss.logging.MDC
import java.net.URI
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.*
import javax.annotation.security.RolesAllowed
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.SecurityContext

/**
 * Available role definitions. Since we use them in annotations, we use costants because
 * enums cannot be used in annotations such as [javax.annotation.security.RolesAllowed].
 */
class Role {
    companion object {
        const val Administrator = "administrator"
        const val User = "user"
        const val Guest = "guest"
        const val Api = "api"
        const val System = "system"
    }

}

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
            val randomPassword = if (ProfileManager.getActiveProfile() == "dev") {
                "admin"
            } else {
                UUID.randomUUID().toString()
            }
            userRepository.persist(UserEntity(login = "admin", displayName = "Admin User",
                    password = BcryptUtil.bcryptHash(randomPassword), roles = Role.Administrator))
            log.info("+++ !!! GENERATED ADMIN PASSWORD IS: '$randomPassword' !!!+++")
            log.info("+++ !!! Please change it immediately after loggin in.  !!!+++")
        }
    }

    @Transactional
    fun initDefaultUser(@Observes event: StartupEvent) {
        if (userRepository.findByLogin("user") == null) {
            val user = UserEntity(login = "user", displayName = "Default User",
                    password = BcryptUtil.bcryptHash("user"), roles = Role.User)
            userRepository.persist(user)
            log.info("+++ !!! GENERATED DEFAULT USER PASSWORD IS: 'user' !!!+++")
            log.info("+++ !!! Please change it immediately after loggin in.  !!!+++")
        }
    }

}

/**
 * Home page is rendered here.
 */
@Path("/")
@Produces(MediaType.TEXT_HTML)
class Home(@Inject val userRepository: UserRepository, @Inject val home: Template) {

    private val log = Logger.getLogger(Home::class.java)

    @GET
    fun show(@Context securityContext: SecurityContext): TemplateInstance {

        transactionStart(whoAmI(securityContext, userRepository))

        log.info("Show home page")
        val template = home
                .data("title", "Phexpedition")
                .data("breadCrumbs", linkedMapOf(
                        Pair("Home", "/")))
                .data("me", whoAmI(securityContext, userRepository))

        transactionStop()

        return template
    }

}

@Path("/login")
@Produces(MediaType.TEXT_HTML)
@RolesAllowed(Role.User, Role.Administrator)
class Login {

    @GET
    fun show(): Response {
        return Response.temporaryRedirect(URI.create("/")).build()
    }

}

fun transactionStart(me: UserEntity?): Unit {
    MDC.put("transaction", UUID.randomUUID().toString())
    if (me != null) {
        MDC.put("user", me.id)
    }
}

fun transactionStop(): Unit {
    MDC.remove("transaction")
    MDC.remove("user")
}

