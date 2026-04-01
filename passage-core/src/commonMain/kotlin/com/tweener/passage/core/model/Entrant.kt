package com.tweener.passage.core.model

/**
 * Represents a successfully authenticated user.
 *
 * @property uid The unique identifier of the user, assigned by the authentication backend.
 * @property email The email address associated with the user, if available.
 * @property displayName The display name of the user, if set.
 * @property phoneNumber The phone number associated with the user, if available.
 * @property photoUrl The URL to the user's profile photo, if set.
 * @property isAnonymous Indicates whether the user is authenticated anonymously.
 * @property isEmailVerified Indicates whether the user's email address has been verified.
 * @property metadata Additional provider-specific metadata about the user.
 *
 * @author Chirag Redij
 * @since 29/03/2026
 */
interface EntrantInterface {
    val uid: String
    val email: String?
    val displayName: String?
    val phoneNumber: String?
    val photoUrl: String?
    val isAnonymous: Boolean
    val isEmailVerified: Boolean
    val metadata: Map<String, Any?>
}

/**
 * Default implementation of [EntrantInterface] backed by a data class.
 *
 * @author Chirag Redij
 * @since 29/03/2026
 */
data class DefaultEntrant(
    override val uid: String,
    override val email: String? = null,
    override val displayName: String? = null,
    override val phoneNumber: String? = null,
    override val photoUrl: String? = null,
    override val isAnonymous: Boolean = false,
    override val isEmailVerified: Boolean = false,
    override val metadata: Map<String, Any?> = emptyMap(),
) : EntrantInterface