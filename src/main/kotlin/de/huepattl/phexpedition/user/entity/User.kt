package de.huepattl.phexpedition.user.entity

import io.quarkus.elytron.security.common.BcryptUtil
import io.quarkus.security.jpa.*
import java.time.Instant
import java.util.*
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Index
import javax.persistence.Table

/**
 * User entity containing the most vital information such as unique identifier, display name, login name, password etc.
 *
 * Example:
 *
 * `val newUser = User(login = "jdoe", displayName = "Jane Doe", roles = "contributor, moderator")`
 *
 * @since 2020-08-21
 */
@Entity
@Table(name = "LOCAL_USER")
@UserDefinition
data class User(

        /**
         * Unique user idnetifier, remains the same over time.
         *
         * @see [login]
         */
        @Id
        var id: String = UUID.randomUUID().toString(),

        /**
         * Login used by the user, may change over time while [id] will not.
         */
        @Username
        var login: String = "admin",

        @Password(PasswordType.CLEAR)
        var password: String = "",

        /**
         * User name as seen by other users, may be changed over time by user.
         *
         */
        var displayName: String = login,

        /**
         * Comma separated list of roles/user groups assigned to user.
         */
        @Roles
        var roles: String = "admin",

        /**
         * Valid from date, must be in the past in order to work for the user.
         *
         * @see [validUntil]
         * @see [suspended]
         */
        var validFrom: Instant = Instant.now(),

        /**
         * Valid until date, must be in the future in order to work for the user.
         *
         * @see [validFrom]
         * @see [suspended]
         */
        var validUntil: Instant = Instant.parse("2099-12-31T23:59:59.000000Z"),

        /**
         * Flag to indicate a user is suspended. This can be used to temporarily or permanently
         * disable a user but keeping validity dates intact. If a user and her/his contributions
         * (photos, comments, likes etc.) should not be visible as well, use [hidden] in addition.
         *
         * @see [validFrom]
         * @see [validUntil]
         * @see [hidden]
         */
        var suspended: Boolean = false,

        /**
         * If a user and her/his contributions (photos, comments, likes etc.) should not be visible,
         * use this flag. It does not depend on [suspended] or validity dates ([validFrom] and [validUntil]).
         * Thus, you may create a 'silent' user, which still can login (given it is not suspended and
         * validity dates are in range).
         */
        var hidden: Boolean = false

) {
    companion object {
        fun random(): User {
            return User(login = "rnd-${System.currentTimeMillis()}", displayName = "name-${UUID.randomUUID().toString()}")
        }
    }
}
