package de.huepattl.phexpedition.user.web

import de.huepattl.phexpedition.Role
import de.huepattl.phexpedition.user.SortColumn
import de.huepattl.phexpedition.user.SortDirection
import de.huepattl.phexpedition.user.User
import de.huepattl.phexpedition.user.UserRepository
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import javax.annotation.security.RolesAllowed
import javax.enterprise.context.RequestScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType


/**
 * Model used for presenting the [User] in the list, used by [UserList] controller.
 */
data class UserListModel(val id: String, val login: String, val displayName: String,
                         val validFrom: String, val validUntil: String) {

    companion object {

        /**
         * Create a list user from the basing entity [User].
         */
        fun from(entity: User): UserListModel {
            return UserListModel(entity.id, entity.login, entity.displayName,
                    formatDate(entity.validFrom), formatDate(entity.validUntil))
        }

        /**
         * Used internally for showing validity dates with date only as YYYY-MM-DD.
         */
        private fun formatDate(date: Instant): String {
            val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd")
                    .withLocale(Locale.GERMANY)
                    .withZone(ZoneId.systemDefault())
            return formatter.format(date)
        }
    }
}


/**
 * Controller for finding and displaying users in a list.
 */
@Path("/user/_all")
@Produces(MediaType.TEXT_HTML)
@RequestScoped
@Transactional
@RolesAllowed(Role.Administrator)
class UserList(@Inject val userRepository: UserRepository, @Inject val userList: Template) {

    /**
     * Show a filterable list of users found.
     */
    @GET
    fun list(
            @QueryParam("filter") filter: String?,
            @QueryParam("sortColumn") sortColumn: String?,
            @QueryParam("sortDirection") sortDirection: String?
    ): TemplateInstance {
        val allUsers = userRepository.findAny(
                filter ?: "",
                SortColumn.valueOf(sortColumn ?: "Login"),
                SortDirection.valueOf(sortDirection ?: "Ascending")).map { UserListModel.from(it) }

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
