package de.huepattl.phexpedition.user.control

import de.huepattl.phexpedition.user.boundary.SortColumn
import de.huepattl.phexpedition.user.boundary.SortDirection
import de.huepattl.phexpedition.user.boundary.UserRepository
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType


@Path("/ui/user")
@Produces(MediaType.TEXT_HTML)
@RequestScoped
class UserUi(@Inject val userRepository: UserRepository, @Inject val userUiSingle: Template) {

    @Path("/{login}")
    @GET
    fun user(@PathParam("login") login: String): TemplateInstance {
        val user = userRepository.findByLogin(login)
        return userUiSingle.data("user", user)
    }

    @GET
    @Path("/_all")
    fun list(
            @QueryParam("login") login: String?,
            @QueryParam("displayName") displayName: String?,
            @QueryParam("validFrom") validFrom: String?,
            @QueryParam("validUntil") validUntil: String?,
            @QueryParam("sortColumn") sortColumn: String?,
            @QueryParam("sortDirection") sortDirection: String?
    ): TemplateInstance {
        val allUsers = userRepository.findByLoginAndName(
                login ?: "",
                displayName ?: "",
                SortColumn.valueOf(sortColumn ?: "Login"),
                SortDirection.valueOf(sortDirection ?: "Ascending"))
        val x = Instant.now()
        println(x.dateOnly())
        return userUiSingle
                .data("userList", allUsers)
                .data("filter-login", login)
                .data("filter-displayName", displayName)
                .data("sortColumn", sortColumn)
                .data("sortDirection", sortDirection)
    }

    fun Instant.dateOnly(): String {
        return DateTimeFormatter.ISO_LOCAL_DATE
                .withZone(ZoneId.systemDefault()).format(this)
    }

}
