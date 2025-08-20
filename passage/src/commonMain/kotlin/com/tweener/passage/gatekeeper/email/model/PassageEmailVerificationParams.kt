package com.tweener.passage.gatekeeper.email.model

/**
 * Parameters required for sending an email address verification email.
 *
 * @param url The URL for the email address verification page.
 * @param hostingDomain The hosting domain for the app. This is used to ensure the email link is valid for the app's domain.
 * @param iosParams The specific parameters for iOS platform.
 * @param androidParams The specific parameters for Android platform.
 * @param canHandleCodeInApp Whether the app can handle the code in app. Default is true.
 *
 * @author Vivien Mahe
 * @since 02/12/2024
 */
data class PassageEmailVerificationParams(
    val url: String,
    val hostingDomain: String,
    val iosParams: PassageEmailVerificationIosParams? = null,
    val androidParams: PassageEmailVerificationAndroidParams? = null,
    val canHandleCodeInApp: Boolean = true,
)

/**
 * Specific iOS parameters required for sending an email address verification email.
 *
 * @param bundleId The iOS bundle ID for the app. Default is null.
 */
data class PassageEmailVerificationIosParams(
    val bundleId: String,
)

/**
 * Specific Android parameters required for sending an email address verification email.
 *
 * @param packageName The Android package name for the app. Default is null.
 * @param installIfNotAvailable Whether to install the Android app if not available. Default is true.
 * @param minimumVersion The minimum Android version of the app required. Default is null.
 */
data class PassageEmailVerificationAndroidParams(
    val packageName: String,
    val installIfNotAvailable: Boolean = true,
    val minimumVersion: String? = null,
)
