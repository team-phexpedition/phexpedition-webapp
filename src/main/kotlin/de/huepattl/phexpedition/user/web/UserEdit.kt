package de.huepattl.phexpedition.user.web

import de.huepattl.phexpedition.Converters
import de.huepattl.phexpedition.user.UserRepository
import de.huepattl.phexpedition.user.User
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import org.jboss.logging.Logger
import org.jboss.logging.MDC
import java.time.Instant
import java.util.*
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import kotlin.collections.LinkedHashMap

data class UserEditModel(val id: String, val login: String, val displayName: String,
                         val validFrom: String, val validUntil: String,
                         val hidden: Boolean, val suspended: Boolean, val roles: String) {

    companion object {

        fun from(entity: User): UserEditModel {
            return UserEditModel(entity.id, entity.login, entity.displayName,
                    Converters.toString(entity.validFrom),
                    Converters.toString(entity.validUntil),
                    entity.hidden, entity.suspended,
                    entity.roles)
        }

    }

}

/**
 * FIXME: Somehow constructor injection throws `RESTEASY003190: Could not find constructor for class: de.huepattl.phexpedition.user.web.UserEdit`
 * while it is not thrown in [UserList]
 */
@Path("/user/{id}")
@Produces(MediaType.TEXT_HTML)
@Transactional
class UserEdit {

    private val log = Logger.getLogger(UserEdit::class.java)

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var userEdit: Template

    @GET
    fun showUser(@PathParam("id") id: String): TemplateInstance {
        val user = if (id == "_new") {
            User(login = "")
        } else {
            userRepository.findById(id)
        }

        return userEdit
                .data("breadCrumbs", breadCrumbs(user))
                .data("user", UserEditModel.from(user!!))
    }

    @POST
    fun update(
            @PathParam("id") id: String,
            @FormParam("login") login: String,
            @FormParam("displayName") displayName: String,
            @FormParam("switches") switches: List<String?>?,
            @FormParam("roles") roles: String,
            @FormParam("validFrom") validFrom: String,
            @FormParam("validUntil") validUntil: String
    ): TemplateInstance {
        MDC.put("transaction", UUID.randomUUID())
        log.info("Creating/updating user $login $displayName $validFrom")

        var user = userRepository.findById(id)
        if (user == null) {
            log.info("User not found, creating one instead...")
            user = User()
            userRepository.persist(user)
        }

        user.login = login
        user.displayName = displayName
        user.suspended = switches?.contains("suspended") ?: false
        user.hidden = switches?.contains("hidden") ?: false
        user.roles = roles
        user.validFrom = Converters.parseLocalDateTime(validFrom, user.validFrom)
        user.validUntil = Converters.parseLocalDateTime(validUntil, user.validUntil)

        return userEdit
                .data("breadCrumbs", breadCrumbs(user))
                .data("user", UserEditModel.from(user))
    }

    private fun breadCrumbs(user: User?): LinkedHashMap<String, String> {
        return linkedMapOf(
                Pair("Home", "/"),
                Pair("Users", "/user/_all"),
                Pair(user?.displayName ?: "new", "")
        )
    }

}
