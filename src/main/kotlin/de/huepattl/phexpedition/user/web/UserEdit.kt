package de.huepattl.phexpedition.user.web

import de.huepattl.phexpedition.Converters
import de.huepattl.phexpedition.user.User
import de.huepattl.phexpedition.user.UserRepository
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import org.jboss.logging.Logger
import org.jboss.logging.MDC
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

/**
 * Model used for presenting a single [User] in view/edit/create view, used by
 * [UserEdit] controller.
 */
data class UserEditModel(val id: String, val login: String, val displayName: String,
                         val validFrom: String, val validUntil: String,
                         val hidden: Boolean, val suspended: Boolean, val roles: String) {

    companion object {

        /**
         * Create model directly form [User] entity.
         */
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
 * Controller for viewing, creating or editing a [User] entity.
 */
@Path("/user/{id}")
@Produces(MediaType.TEXT_HTML)
@RequestScoped
@Transactional
class UserEdit(@Inject val userRepository: UserRepository, @Inject val userEdit: Template) {

    private val log = Logger.getLogger(UserEdit::class.java)

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
