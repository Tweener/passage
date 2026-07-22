package com.tweener.passage.model

/**
 * Raw Apple credential returned by [com.tweener.passage.Passage.retrieveAppleCredential], without a Firebase session.
 *
 * @property identityToken The Apple identity token (a JWT), with `aud` set to the app's bundle id.
 * @property name The user's full name. Delivered ONLY on the first authorization; null afterwards.
 * @property email The user's email (possibly a Hide-My-Email relay). Also only on first authorization.
 * @property rawNonce The un-hashed nonce (the token's `nonce` claim is `sha256(rawNonce)`). Null if none was used.
 *
 * @author Vivien Mahe
 * @since 22/07/2026
 */
data class PassageAppleCredential(
    val identityToken: String,
    val name: String? = null,
    val email: String? = null,
    val rawNonce: String? = null,
)
