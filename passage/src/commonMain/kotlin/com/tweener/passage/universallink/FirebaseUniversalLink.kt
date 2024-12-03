package com.tweener.passage.universallink

/**
 * @author Vivien Mahe
 * @since 03/12/2024
 */
data class FirebaseUniversalLink(
    val link: String,
    val mode: String,
    val oobCode: String,
)
