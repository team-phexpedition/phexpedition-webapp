package de.huepattl.phexpedition.user.web

import de.huepattl.phexpedition.*
import de.huepattl.phexpedition.user.SortColumn
import de.huepattl.phexpedition.user.SortDirection
import de.huepattl.phexpedition.user.UserEntity
import de.huepattl.phexpedition.user.UserRepository
import io.quarkus.qute.Template
import io.quarkus.qute.TemplateInstance
import org.jboss.logging.Logger
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
import javax.ws.rs.core.Context
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.SecurityContext


/**
 * Model used for presenting the [UserEntity] in the list, used by [UserList] controller.
 */
data class UserListModel(val id: String, val login: String, val displayName: String,
                         val validFrom: String, val validUntil: String) {

    companion object {

        /**
         * Create a list user from the basing entity [UserEntity].
         */
        fun from(entity: UserEntity, timeZoneId: String): UserListModel {
            return UserListModel(entity.id, entity.login, entity.displayName,
                    formatDate(entity.validFrom, timeZoneId), formatDate(entity.validUntil, timeZoneId))
        }

        /**
         * Used internally for showing validity dates with date only as YYYY-MM-DD.
         */
        private fun formatDate(date: Instant, timeZoneId: String): String {
            val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd")
                    .withLocale(Locale.GERMANY)
                    .withZone(ZoneId.of(timeZoneId))
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

    private val log = Logger.getLogger(UserList::class.java)

    /**
     * Show a filterable list of users found.
     */
    @GET
    fun list(
            @QueryParam("filter") filter: String?,
            @QueryParam("sortColumn") sortColumn: String?,
            @QueryParam("sortDirection") sortDirection: String?,
            @Context securityContext: SecurityContext
    ): TemplateInstance {

        transactionStart(whoAmI(securityContext, userRepository))

        log.info("Finding users for '$filter'")

        val me = whoAmI(securityContext, userRepository)

        val allUsers = userRepository.findAny(
                filter ?: "",
                SortColumn.valueOf(sortColumn ?: "Login"),
                SortDirection.valueOf(sortDirection ?: "Ascending")).map { UserListModel.from(it, me!!.timeZone) }

        log.info("Found ${allUsers.size} entries")

        val breadCrumbs = linkedMapOf(
                Pair("Home", "/"),
                Pair("Users", "")
        )

        val template = userList
                .data("breadCrumbs", breadCrumbs)
                .data("userList", allUsers)
                .data("filter", filter)
                .data("sortColumn", sortColumn)
                .data("sortDirection", sortDirection)
                .data("me", whoAmI(securityContext, userRepository))

        transactionStop()

        return template
    }

}
