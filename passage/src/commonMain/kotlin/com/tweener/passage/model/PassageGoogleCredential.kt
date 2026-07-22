package com.tweener.passage.model

/**
 * Raw Google credential returned by [com.tweener.passage.Passage.retrieveGoogleCredential], without a Firebase session.
 *
 * @property idToken The Google ID token (a JWT), with `aud` set to the configured `serverClientId`.
 * @property accessToken The OAuth access token, when available.
 * @property email The account email, when available.
 * @property displayName The display name, when available.
 *
 * @author Vivien Mahe
 * @since 22/07/2026
 */
data class PassageGoogleCredential(
    val idToken: String,
    val accessToken: String? = null,
    val email: String? = null,
    val displayName: String? = null,
)
