package com.tweener.passage.core.model

/**
 * Represents authentication credentials for different sign-in providers.
 *
 * Each subclass encapsulates the specific parameters required by its respective provider,
 * enabling type-safe credential handling across the Passage plugin system.
 *
 * @author Chirag Redij
 * @since 29/03/2026
 */
sealed interface AuthCredential {

    /**
     * Credentials for email/password authentication.
     *
     * @property email The user's email address.
     * @property password The user's password.
     */
    data class EmailCredential(val email: String, val password: String) : AuthCredential

    /**
     * Credentials for Google Sign-In authentication.
     *
     * @property idToken The Google ID token obtained from the sign-in flow.
     * @property accessToken The optional Google access token.
     */
    data class GoogleCredential(val idToken: String, val accessToken: String?) : AuthCredential

    /**
     * Credentials for Apple Sign-In authentication.
     *
     * @property idToken The Apple identity token obtained from the sign-in flow.
     * @property rawNonce The raw nonce used for validation during Apple Sign-In.
     * @property fullName The user's full name as provided by Apple, if available.
     *   On iOS this is a platform-specific `NSPersonNameComponents` instance; on other platforms it may be `null`.
     */
    data class AppleCredential(val idToken: String, val rawNonce: String, val fullName: Any?) : AuthCredential
}