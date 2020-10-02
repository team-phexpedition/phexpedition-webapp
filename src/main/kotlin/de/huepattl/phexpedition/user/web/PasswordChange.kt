package de.huepattl.phexpedition.user.web

import de.huepattl.phexpedition.*
import de.huepattl.phexpedition.user.UserRepository
import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.jboss.logging.Logger
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.*
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.SecurityContext

@Path("/user/{id}/password")
@Produces(MediaType.TEXT_HTML)
@RequestScoped
@Transactional
class PasswordChange(
        @Inject val userRepository: UserRepository,
        @Inject val passwordChange: Template,
        @Inject val userEdit: UserEdit,
        @ConfigProperty(name = "phexpedition.auth.minPasswordLength", defaultValue = "10") val minPasswordLength: String) {

    private val log = Logger.getLogger(PasswordChange::class.java)

    @GET
    fun show(
            @PathParam("id") id: String,
            @Context securityContext: SecurityContext
    ): TemplateInstance {
        transactionStart(whoAmI(securityContext, userRepository))

        val user = userRepository.findById(id)

        val messages = mutableListOf<UiMessage>()

        val template = passwordChange
                .data("user", user)
                .data("breadCrumbs", userEdit.breadCrumbs(user))
                .data("messages", messages)
                .data("me", whoAmI(securityContext, userRepository))

        transactionStop()

        return template
    }

    @POST
    fun changePassword(
            @PathParam("id") id: String,
            @FormParam("newPassword") newPassword: String,
            @FormParam("newPasswordRepeated") newPasswordRepeated: String,
            @Context securityContext: SecurityContext
    ): TemplateInstance {
        transactionStart(whoAmI(securityContext, userRepository))

        val messages = mutableListOf<UiMessage>()
        val me = whoAmI(securityContext, userRepository)
        val user = userRepository.findById(id)

        if (myselfOrAdmin(me!!, id)) {
            if (newPassword.length < minPasswordLength.toInt()) {
                messages.add(UiMessage(
                        type = MessageType.Error,
                        title = "Password change failed",
                        text = "Your new password must have $minPasswordLength characters or more."))
                log.error("Password change for user ID '$id' failed: password provided was too short - required: $minPasswordLength, passed ${newPassword.length}")
            }
            if (newPassword != newPasswordRepeated) {
                messages.add(UiMessage(
                        type = MessageType.Error,
                        title = "Password change failed",
                        text = "Your new password did not match the retyped one, please try again."))
                log.error("Password change for user ID '$id' failed: password and retyped one did not match")
            }

            if (!messages.any { it.type == MessageType.Error }) {
                user!!.password = BcryptUtil.bcryptHash(newPassword)
                messages.add(UiMessage(
                        type = MessageType.Information,
                        title = "Password changed",
                        text = "Successfully updated password!"))
                log.info("Password was successfully changed for user with ID '$id'")
            }
        } else {
            messages.add(UiMessage(
                    type = MessageType.Error,
                    title = "Foo",
                    text = "You cannot change password for this user!"))
            log.error("Changing password for user with ID '$id' was not authorized")
        }

        val template = passwordChange
                .data("me", me)
                .data("user", user)
                .data("breadCrumbs", userEdit.breadCrumbs(user))
                .data("messages", messages)

        transactionStop()

        return template
    }

}
