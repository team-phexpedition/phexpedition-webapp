package de.huepattl.phexpedition

import de.huepattl.phexpedition.user.entity.User
import de.huepattl.phexpedition.user.boundary.UserRepository
import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.runtime.StartupEvent
import javax.enterprise.event.Observes
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@Singleton
class App(@Inject val userRepository: UserRepository) {

    @Transactional
    fun initAdminUser(@Observes event: StartupEvent) {
        userRepository.persist(User(login = "admin", displayName = "Admin User", password = BcryptUtil.bcryptHash("admin")))
        userRepository.persist(User(login = "guest", displayName = "Guest User", password = BcryptUtil.bcryptHash("guest")))

        repeat(999) { index ->
            userRepository.persist(User.random().copy(login = "user-$index"))
        }

    }

}
