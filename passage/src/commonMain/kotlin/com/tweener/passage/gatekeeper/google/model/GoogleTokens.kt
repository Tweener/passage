package com.tweener.passage.gatekeeper.google.model

/**
 * @author Vivien Mahe
 * @since 02/12/2024
 */
data class GoogleTokens(
    val idToken: String,
    val accessToken: String? = null,
)
