package de.huepattl.phexpedition.user.control

import de.huepattl.phexpedition.user.boundary.SortColumn
import de.huepattl.phexpedition.user.boundary.SortDirection
import de.huepattl.phexpedition.user.boundary.UserRepository
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.*
import javax.ws.rs.core.MediaType


@Path("/user/_all")
@Produces(MediaType.TEXT_HTML)
@RequestScoped
@Transactional
class UserList(@Inject val userRepository: UserRepository, @Inject val userList: Template) {

    @GET
    fun list(
            @QueryParam("filter") filter: String?,
            @QueryParam("sortColumn") sortColumn: String?,
            @QueryParam("sortDirection") sortDirection: String?
    ): TemplateInstance {
        val allUsers = userRepository.findAny(
                filter ?: "",
                SortColumn.valueOf(sortColumn ?: "Login"),
                SortDirection.valueOf(sortDirection ?: "Ascending"))

        val breadCrumbs = linkedMapOf(
                Pair("Home", "/"),
                Pair("Users", "")
        )

        return userList
                .data("breadCrumbs", breadCrumbs)
                .data("userList", allUsers)
                .data("filter", filter)
                .data("sortColumn", sortColumn)
                .data("sortDirection", sortDirection)
    }

}
