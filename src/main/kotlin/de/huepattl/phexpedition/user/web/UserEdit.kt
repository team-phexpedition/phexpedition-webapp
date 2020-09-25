package de.huepattl.phexpedition.user.web

import de.huepattl.phexpedition.*
import de.huepattl.phexpedition.user.User
import de.huepattl.phexpedition.user.UserRepository
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import org.jboss.logging.Logger
import org.jboss.logging.MDC
import java.util.*
import javax.annotation.security.RolesAllowed
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.SecurityContext


/**
 * Controller for viewing, creating or editing a [User] entity.
 */
@Path("/user/{id}")
@Produces(MediaType.TEXT_HTML)
@RequestScoped
@Transactional
@RolesAllowed(Role.Administrator, Role.User)
class UserEdit(@Inject val userRepository: UserRepository, @Inject val userEdit: Template) {

    private val log = Logger.getLogger(UserEdit::class.java)

    /**
     * Model used for presenting a single [User] in view/edit/create view, used by
     * [UserEdit] controller. Using a data class did not work with RESTEasy with Kotlin.
     */
    class UserEditModel {
        @PathParam("id")
        lateinit var id: String

        @FormParam("login")
        lateinit var login: String

        @FormParam("displayName")
        lateinit var displayName: String

        @FormParam("validFrom")
        lateinit var validFrom: String

        @FormParam("validUntil")
        lateinit var validUntil: String
        var hidden: Boolean = false
        var suspended: Boolean = false

        @FormParam("roles")
        lateinit var roles: String

        @FormParam("switches")
        var switches: List<String?>? = null

        companion object {

            /**
             * Create model directly form [User] entity.
             */
            fun from(entity: User): UserEditModel {
                var model = UserEditModel()
                model.id = entity.id
                model.login = entity.login
                model.displayName = entity.displayName
                model.validFrom = Converters.toString(entity.validFrom)
                model.validUntil = Converters.toString(entity.validUntil)
                model.hidden = entity.hidden
                model.suspended = entity.suspended
                model.roles = entity.roles

                return model
            }
        }
    }

    /**
     * Retrieve and show user by given uniqie identifier.
     */
    @GET
    fun showUser(
            @PathParam("id") id: String,
            @Context securityContext: SecurityContext
    ): TemplateInstance {
        val loginUser = userRepository.findByLogin(securityContext?.userPrincipal?.name ?: "")
        if (!loginUser?.roles?.contains(Role.Administrator)!! && id != loginUser.id ?: "") {
            throw IllegalAccessException("You are not authorized to edit this user!")
        }

        val user = if (id == "_new") {
            User(login = "")
        } else {
            userRepository.findById(id)
        }

        return userEdit
                .data("breadCrumbs", breadCrumbs(user))
                .data("messages", emptyList<UiMessage>())
                .data("user", UserEditModel.from(user!!))
                .data("me", App.whoAmI(securityContext, userRepository))
    }

    /**
     * Creates or updates the user passed.
     */
    @POST
    fun update(
            @BeanParam userEditModel: UserEditModel,
            @Context securityContext: SecurityContext
    ): TemplateInstance {
        MDC.put("transaction", UUID.randomUUID())
        log.info("Creating/updating user $userEditModel.login $$userEditModel.displayName $userEditModel.validFrom")

        val me = App.whoAmI(securityContext, userRepository)

        val messages = mutableListOf<UiMessage>()

        var user = userRepository.findById(userEditModel.id)

        if (user == null) {
            log.info("User not found, creating one instead...")
            user = User(id = userEditModel.id, login = userEditModel.login, displayName = userEditModel.displayName)
            userRepository.persist(user)
            messages.add(UiMessage(
                    type = MessageType.Information,
                    title = "User created",
                    text = "User '${userEditModel.displayName}' with login '${userEditModel.login}' has been created."))
        } else {
            messages.add(UiMessage(
                    type = MessageType.Information,
                    title = "User updated",
                    text = "User '${userEditModel.displayName}' with login '${userEditModel.login}' has been updated."))
        }

        user.login = userEditModel.login
        user.displayName = userEditModel.displayName
        user.suspended = userEditModel.switches?.contains("suspended") ?: false
        user.hidden = userEditModel.switches?.contains("hidden") ?: false
        user.roles = userEditModel.roles
        user.validFrom = Converters.parseLocalDateTime(userEditModel.validFrom, user.validFrom)
        user.validUntil = Converters.parseLocalDateTime(userEditModel.validUntil, user.validUntil)

        return userEdit
                .data("breadCrumbs", breadCrumbs(user))
                .data("messages", messages)
                .data("user", UserEditModel.from(user))
                .data("me", me)
    }

    fun breadCrumbs(user: User?): LinkedHashMap<String, String> {
        return if (user!!.isAdmin()) {
            linkedMapOf(
                    Pair("Home", "/"),
                    Pair("Users", "/user/_all"),
                    Pair(user?.displayName ?: "new", "")
            )
        } else {
            linkedMapOf(
                    Pair("Home", "/"),
                    Pair(user?.displayName ?: "new", ""))
        }
    }

}
