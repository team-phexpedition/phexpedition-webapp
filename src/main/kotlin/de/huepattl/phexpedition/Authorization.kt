package de.huepattl.phexpedition

import de.huepattl.phexpedition.user.UserEntity
import de.huepattl.phexpedition.user.UserRepository
import javax.ws.rs.core.SecurityContext

fun whoAmI(securityContext: SecurityContext, userRepository: UserRepository): UserEntity? {
    securityContext?.userPrincipal?.name?.let { login ->
        userRepository?.let { repo ->
            return repo.findByLogin(login)
        }
    }
    return null
}

fun myselfOrAdmin(me: UserEntity, id: String): Boolean {
    if (me.isAdmin()) {
        return true
    }
    if (me.id == id) {
        return true
    }
    return false
}

fun myselfOrAdmin(me: UserEntity, id: String, function: Function<Unit>) {
    if (me.isAdmin() && me.id == id) {
        function.apply {  }
    }
}
