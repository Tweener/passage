package com.tweener.passage.model

/**
 * Defines the configuration of a Gatekeeper in the Passage library.
 *
 * This interface ensures that each Gatekeeper (e.g., Google, Apple, Email/Password) provides its
 * specific configuration details required for proper initialization and operation.
 *
 * Responsibilities:
 * - Define the required configuration for a specific Gatekeeper.
 * - Ensure consistency in how Gatekeeper configurations are handled within Passage.
 *
 * @author Vivien Mahe
 * @since 02/12/2024
 */
interface PassageGatekeeperConfiguration

// region Google

/**
 * Configuration for the Google Gatekeeper.
 *
 * This class provides the necessary settings for Google Sign-In, including platform-specific
 * configurations for Android and iOS.
 *
 * @property serverClientId The server client ID associated with the Google Sign-In configuration.
 * @property android Platform-specific configuration for Android.
 * @property ios Platform-specific configuration for iOS.
 */
data class GoogleGatekeeperConfiguration(
    val serverClientId: String,
    val android: GoogleGatekeeperAndroidConfiguration,
    val ios: GoogleGatekeeperIosConfiguration = GoogleGatekeeperIosConfiguration,
) : PassageGatekeeperConfiguration

/**
 * Android-specific configuration for the Google Gatekeeper.
 *
 * This class provides optional settings to customize Google Sign-In behavior on Android,
 * such as filtering by authorized accounts, enabling auto-selection, and retrying failed sign-ins.
 *
 * @property useGoogleButtonFlow If true, uses the [Google button flow](https://developer.android.com/identity/sign-in/credential-manager-siwg#trigger-siwg). Otherwise, use the [Google sign-in request](https://developer.android.com/identity/sign-in/credential-manager-siwg#instantiate-google).
 * @property filterByAuthorizedAccounts If true, filters credentials by authorized accounts for the app.
 * @property autoSelectEnabled If true, enables automatic credential selection when possible.
 * @property maxRetries The maximum number of retries for authentication attempts.
 */
data class GoogleGatekeeperAndroidConfiguration(
    val useGoogleButtonFlow: Boolean = true,
    val filterByAuthorizedAccounts: Boolean = false,
    val autoSelectEnabled: Boolean = true,
    val maxRetries: Int = 3,
)

/**
 * iOS-specific configuration for the Google Gatekeeper.
 *
 * This object serves as a placeholder for future iOS-specific configurations related to Google Sign-In.
 */
data object GoogleGatekeeperIosConfiguration

// endregion Google

// region Apple

/**
 * Configuration for the Apple Gatekeeper.
 *
 * This class provides platform-specific settings for Apple Sign-In on Android and iOS.
 *
 * @property android Platform-specific configuration for Android.
 * @property ios Platform-specific configuration for iOS.
 */
data class AppleGatekeeperConfiguration(
    val android: AppleGatekeeperAndroidConfiguration = AppleGatekeeperAndroidConfiguration,
    val ios: AppleGatekeeperIosConfiguration = AppleGatekeeperIosConfiguration,
) : PassageGatekeeperConfiguration

/**
 * Android-specific configuration for the Apple Gatekeeper.
 *
 * This object serves as a placeholder for future configurations related to Apple Sign-In on Android.
 */
data object AppleGatekeeperAndroidConfiguration

/**
 * iOS-specific configuration for the Apple Gatekeeper.
 *
 * This object serves as a placeholder for future configurations related to Apple Sign-In on iOS.
 */
data object AppleGatekeeperIosConfiguration

// endregion Apple

// region Email/password

/**
 * Configuration for the Email/Password Gatekeeper.
 *
 * This object serves as a placeholder for future configurations related to Email/Password authentication.
 */
data object EmailPasswordGatekeeperConfiguration : PassageGatekeeperConfiguration

// endregion Email/password
