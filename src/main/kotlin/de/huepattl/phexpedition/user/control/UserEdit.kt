package de.huepattl.phexpedition.user.control

import de.huepattl.phexpedition.user.boundary.UserRepository
import de.huepattl.phexpedition.user.entity.User
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import org.jboss.logging.Logger
import org.jboss.logging.MDC
import java.util.*
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import kotlin.collections.LinkedHashMap

/**
 * FIXME: Somehow constructor injection throws `RESTEASY003190: Could not find constructor for class: de.huepattl.phexpedition.user.control.UserEdit`
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
                .data("user", user)
    }

    @POST
    fun update(
            @PathParam("id") id: String,
            @FormParam("login") login: String,
            @FormParam("displayName") displayName: String
    ): TemplateInstance {
        MDC.put("transaction", UUID.randomUUID())
        log.info("Creating user $login $displayName")

        val user = userRepository.findById(id) ?: User(login = "")


        return userEdit
                .data("breadCrumbs", breadCrumbs(user))
    }

    private fun breadCrumbs(user: User?): LinkedHashMap<String, String> {
        return linkedMapOf(
                Pair("Home", "/"),
                Pair("Users", "/user/_all"),
                Pair(user?.displayName ?: "new", "")
        )
    }

}
