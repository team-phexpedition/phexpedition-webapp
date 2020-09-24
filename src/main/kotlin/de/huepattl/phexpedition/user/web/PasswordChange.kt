package de.huepattl.phexpedition.user.web

import de.huepattl.phexpedition.App
import de.huepattl.phexpedition.MessageType
import de.huepattl.phexpedition.UiMessage
import de.huepattl.phexpedition.user.User
import de.huepattl.phexpedition.user.UserRepository
import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import org.eclipse.microprofile.config.inject.ConfigProperty
import java.util.LinkedHashMap
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
        @ConfigProperty(name = "phexpedition.auth.minPasswordLength", defaultValue = "10") val minPasswordLength: String) {

    @GET
    fun show(
            @PathParam("id") id: String,
            @Context securityContext: SecurityContext
    ): TemplateInstance {
        val user = userRepository.findById(id)

        val messages = mutableListOf<UiMessage>()

        return passwordChange
                .data("user", user)
                .data("breadCrumbs", breadCrumbs(user))
                .data("messages", messages)
                .data("me", App.whoAmI(securityContext, userRepository))
    }

    @POST
    fun setPassword(
            @PathParam("id") id: String,
            @FormParam("newPassword") newPassword: String,
            @FormParam("newPasswordRepeated") newPasswordRepeated: String,
            @Context securityContext: SecurityContext
    ): TemplateInstance {
        val messages = mutableListOf<UiMessage>()
        val me = App.whoAmI(securityContext, userRepository)
        val user = userRepository.findById(id)

        if (App.myselfOrAdmin(me!!, id)) {
            if (newPassword.length < minPasswordLength.toInt()) {
                messages.add(UiMessage(
                        type = MessageType.Error,
                        title = "Password change failed",
                        text = "Your new password must have $minPasswordLength characters or more."))
            }
            if (newPassword != newPasswordRepeated) {
                messages.add(UiMessage(
                        type = MessageType.Error,
                        title = "Password change failed",
                        text = "Your new password did not match the retyped one, please try again."))
            }

            if (!messages.any { it.type == MessageType.Error }) {
                user!!.password = BcryptUtil.bcryptHash(newPassword)
                messages.add(UiMessage(
                        type = MessageType.Information,
                        title = "Password changed",
                        text = "Successfully updated password!"))
            }
        } else {
            messages.add(UiMessage(
                    type = MessageType.Error,
                    title = "Foo",
                    text = "You cannot change password for this user!"))
        }

        return passwordChange
                .data("me", me)
                .data("user", user)
                .data("breadCrumbs", breadCrumbs(user))
                .data("messages", messages)
    }

    private fun breadCrumbs(user: User?): LinkedHashMap<String, String> {
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
