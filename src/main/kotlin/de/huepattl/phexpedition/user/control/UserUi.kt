package de.huepattl.phexpedition.user.control

import de.huepattl.phexpedition.user.boundary.SortColumn
import de.huepattl.phexpedition.user.boundary.SortDirection
import de.huepattl.phexpedition.user.boundary.UserRepository
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.core.MediaType


@Path("/user")
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
            @QueryParam("filter") filter: String?,
            @QueryParam("sortColumn") sortColumn: String?,
            @QueryParam("sortDirection") sortDirection: String?
    ): TemplateInstance {
        val allUsers = userRepository.findAny(
                filter ?: "",
                SortColumn.valueOf(sortColumn ?: "Login"),
                SortDirection.valueOf(sortDirection ?: "Ascending"))

        return userUiSingle
                .data("userList", allUsers)
                .data("filter", filter)
                .data("sortColumn", sortColumn)
                .data("sortDirection", sortDirection)
    }

}