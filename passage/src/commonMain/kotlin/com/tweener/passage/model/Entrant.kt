package com.tweener.passage.model

/**
 * Represents a successfully authenticated user against Firebase.
 *
 * @property uid The unique identifier of the user, assigned by Firebase.
 * @property email The email address associated with the user, if available.
 * @property displayName The display name of the user, if set.
 * @property phoneNumber The phone number associated with the user, if available.
 * @property photoUrl The URL to the user's profile photo, if set.
 * @property isAnonymous Indicates whether the user is authenticated anonymously.
 * @property isEmailVerified Indicates whether the user's email address has been verified.
 *
 * @author Vivien Mahe
 * @since 30/11/2024
 */
data class Entrant(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val phoneNumber: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean,
    val isEmailVerified: Boolean,
)
