package com.tweener.passage.gatekeeper.apple.error

/**
 * @author Vivien Mahe
 * @since 30/11/2024
 */
class PassageAppleGatekeeperException(message: String? = null) : Throwable("An error occurred during Apple Sign In process! ${message?.let { "\n$it" }}")
