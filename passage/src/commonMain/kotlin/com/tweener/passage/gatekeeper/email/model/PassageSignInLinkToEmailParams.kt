package com.tweener.passage.gatekeeper.email.model

/**
 * Parameters required for signing in link to email email.
 * @author Vivien Mahe
 * @since 14/05/2025
 */
data class PassageSignInLinkToEmailParams(
    val email: String,
    val url: String,
    val iosParams: PassageSignInLinkToEmailIosParams? = null,
    val androidParams: PassageSignInLinkToEmailAndroidParams? = null,
    val canHandleCodeInApp: Boolean,
)

/**
 * Specific iOS parameters required for signing in link to email email.
 *
 * @param bundleId The iOS bundle ID for the app. Default is null.
 */
data class PassageSignInLinkToEmailIosParams(
    val bundleId: String,
)

/**
 * Specific Android parameters required for signing in link to email email.
 *
 * @param packageName The Android package name for the app. Default is null.
 * @param installIfNotAvailable Whether to install the Android app if not available. Default is true.
 * @param minimumVersion The minimum Android version of the app required. Default is null.
 */
data class PassageSignInLinkToEmailAndroidParams(
    val packageName: String,
    val installIfNotAvailable: Boolean = true,
    val minimumVersion: String? = null,
)
