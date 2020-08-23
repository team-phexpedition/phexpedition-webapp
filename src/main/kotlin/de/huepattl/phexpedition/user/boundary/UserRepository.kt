package de.huepattl.phexpedition.user.boundary

import de.huepattl.phexpedition.user.entity.User
import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import io.quarkus.panache.common.Sort
import java.time.Instant
import javax.enterprise.context.ApplicationScoped

enum class SortColumn { Login, DisplayName, ValidFrom, ValidUntil }

enum class SortDirection { Descending, Ascending }

@ApplicationScoped
class UserRepository : PanacheRepositoryBase<User, String> {

    fun findByLogin(login: String): User? {
        return find("login", login).firstResult()
    }

    fun findByLoginAndName(login: String, displayName: String, sortColumn: SortColumn = SortColumn.Login,
                           sortDirection: SortDirection = SortDirection.Ascending): List<User> {
        val params = mutableMapOf(
                Pair("login", "%${login.toLowerCase()}%"),
                Pair("displayName", "%${displayName.toLowerCase()}%")
        )

        return find(
                """
                    lower(login) like :login and
                    lower(displayName) like :displayName
                """.trimIndent(),
                Sort.by(sortColumn.name, panacheSortDirection(sortDirection)),
                params)
                .list()
    }

    private fun panacheSortDirection(dir: SortDirection): Sort.Direction {
        return if (dir == SortDirection.Ascending) {
            Sort.Direction.Ascending
        } else {
            Sort.Direction.Descending
        }
    }
}
