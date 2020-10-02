package de.huepattl.phexpedition.user.web

import de.huepattl.phexpedition.*
import de.huepattl.phexpedition.user.UserEntity
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
 * Controller for viewing, creating or editing a [UserEntity] entity.
 */
@Path("/user/{id}")
@Produces(MediaType.TEXT_HTML)
@RequestScoped
@Transactional
@RolesAllowed(Role.Administrator, Role.User)
class UserEdit(@Inject val userRepository: UserRepository, @Inject val userEdit: Template) {

    private val log = Logger.getLogger(UserEdit::class.java)

    /**
     * Model used for presenting a single [UserEntity] in view/edit/create view, used by
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
             * Create model directly form [UserEntity] entity.
             */
            fun from(entity: UserEntity): UserEditModel {
                var model = UserEditModel()
                model.id = entity.id
                model.login = entity.login
                model.displayName = entity.displayName
                model.validFrom = toString(entity.validFrom)
                model.validUntil = toString(entity.validUntil)
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

        transactionStart(whoAmI(securityContext, userRepository))

        val loginUser = userRepository.findByLogin(securityContext?.userPrincipal?.name ?: "")
        if (!loginUser?.roles?.contains(Role.Administrator)!! && id != loginUser.id ?: "") {
            throw IllegalAccessException("You are not authorized to edit this user!")
        }

        val user = if (id == "_new") {
            log.info("Edit: showing nothing since we want to create a user")
            UserEntity(login = "")
        } else {
            log.info("Edit: showing information for user with ID '$id'")
            userRepository.findById(id)
        }

        val template = userEdit
                .data("breadCrumbs", breadCrumbs(user))
                .data("messages", emptyList<UiMessage>())
                .data("user", UserEditModel.from(user!!))
                .data("me", whoAmI(securityContext, userRepository))

        transactionStop()

        return template
    }

    /**
     * Creates or updates the user passed.
     */
    @POST
    fun update(
            @BeanParam userEditModel: UserEditModel,
            @Context securityContext: SecurityContext
    ): TemplateInstance {
        transactionStart(whoAmI(securityContext, userRepository))
        log.info("Creating/updating user $userEditModel.login $$userEditModel.displayName $userEditModel.validFrom")

        val me = whoAmI(securityContext, userRepository)

        val messages = mutableListOf<UiMessage>()

        var user = userRepository.findById(userEditModel.id)

        if (user == null) {
            log.info("User not found, creating '${userEditModel.login}' with ID '${userEditModel.id}' instead...")
            user = UserEntity(id = userEditModel.id, login = userEditModel.login, displayName = userEditModel.displayName)
            userRepository.persist(user)
            messages.add(UiMessage(
                    type = MessageType.Information,
                    title = "User created",
                    text = "User '${userEditModel.displayName}' with login '${userEditModel.login}' has been created."))
        } else {
            log.info("Updating user '${userEditModel.login}' with ID '${userEditModel.id}'")
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
        user.validFrom = parseLocalDateTime(userEditModel.validFrom, user.validFrom)
        user.validUntil = parseLocalDateTime(userEditModel.validUntil, user.validUntil)

        log.info("Persisting user $user")

        val template = userEdit
                .data("breadCrumbs", breadCrumbs(user))
                .data("messages", messages)
                .data("user", UserEditModel.from(user))
                .data("me", me)

        transactionStop()

        return template
    }

    fun breadCrumbs(user: UserEntity?): LinkedHashMap<String, String> {
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
