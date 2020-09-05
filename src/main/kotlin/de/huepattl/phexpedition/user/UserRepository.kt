package de.huepattl.phexpedition.user

import io.quarkus.hibernate.orm.panache.kotlin.PanacheRepositoryBase
import io.quarkus.panache.common.Sort
import javax.enterprise.context.ApplicationScoped

enum class SortColumn { Login, DisplayName, ValidFrom, ValidUntil }

enum class SortDirection { Descending, Ascending }

@ApplicationScoped
class UserRepository : PanacheRepositoryBase<User, String> {

    fun findByLogin(login: String): User? {
        return find("login", login).firstResult()
    }

    fun findAny(filter: String, sortColumn: SortColumn = SortColumn.Login,
                sortDirection: SortDirection = SortDirection.Ascending): List<User> {

        val params = mutableMapOf(
                Pair("filter", "%${filter.toLowerCase()}%")
        )

        return find(
                """
                    lower(login) like :filter or
                    lower(displayName) like :filter
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