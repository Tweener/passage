package com.tweener.passage.core.model

/**
 * @author Chirag Redij
 * @since 29/03/2026
 */

sealed interface AuthCredential {
    data class EmailCredential(val email: String, val password: String) : AuthCredential
    data class GoogleCredential(val idToken: String, val accessToken: String?) : AuthCredential
    data class AppleCredential(val idToken: String, val rawNonce: String, val fullName: Any?) : AuthCredential
}