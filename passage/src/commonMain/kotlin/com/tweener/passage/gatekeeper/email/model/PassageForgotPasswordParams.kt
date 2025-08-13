package com.tweener.passage.gatekeeper.email.model

/**
 * Parameters required for sending a password reset email.
 *
 * @param email The email address to send the password reset email to.
 * @param url The URL for the password reset page.
 * @param iosParams The specific parameters for iOS platform.
 * @param androidParams The specific parameters for Android platform.
 * @param canHandleCodeInApp Whether the app can handle the code in app. Default is false.
 * @param hostingDomain Optional custom hosting domain for the email links. Default is null.
 *
 * @author Vivien Mahe
 * @since 02/12/2024
 */
data class PassageForgotPasswordParams(
    val email: String,
    val url: String,
    val iosParams: PassageForgotPasswordIosParams? = null,
    val androidParams: PassageForgotPasswordAndroidParams? = null,
    val canHandleCodeInApp: Boolean,
    val hostingDomain: String? = null,
)

/**
 * Specific iOS parameters required for sending a password reset email.
 *
 * @param bundleId The iOS bundle ID for the app. Default is null.
 */
data class PassageForgotPasswordIosParams(
    val bundleId: String,
)

/**
 * Specific Android parameters required for sending a password reset email.
 *
 * @param packageName The Android package name for the app. Default is null.
 * @param installIfNotAvailable Whether to install the Android app if not available. Default is true.
 * @param minimumVersion The minimum Android version of the app required. Default is null.
 */
data class PassageForgotPasswordAndroidParams(
    val packageName: String,
    val installIfNotAvailable: Boolean = true,
    val minimumVersion: String? = null,
)
