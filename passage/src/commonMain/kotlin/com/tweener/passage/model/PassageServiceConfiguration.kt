package com.tweener.passage.model

/**
 * @author Vivien Mahe
 * @since 02/12/2024
 */
data class PassageServiceConfiguration(
    val google: GoogleGatekeeperConfiguration? = null,
    val apple: AppleGatekeeperConfiguration? = null,
    val emailPassword: EmailPasswordGatekeeperConfiguration? = null,
)

// region Google

data class GoogleGatekeeperConfiguration(
    val serverClientId: String,
    val android: GoogleGatekeeperAndroidConfiguration,
    val ios: GoogleGatekeeperIosConfiguration = GoogleGatekeeperIosConfiguration,
)

data class GoogleGatekeeperAndroidConfiguration(
    val filterByAuthorizedAccounts: Boolean = false,
    val autoSelectEnabled: Boolean = true,
    val maxRetries: Int = 3,
)

data object GoogleGatekeeperIosConfiguration

// endregion Google

// region Apple

data class AppleGatekeeperConfiguration(
    val android: AppleGatekeeperAndroidConfiguration = AppleGatekeeperAndroidConfiguration,
    val ios: AppleGatekeeperIosConfiguration = AppleGatekeeperIosConfiguration,
)

data object AppleGatekeeperAndroidConfiguration

data object AppleGatekeeperIosConfiguration

// endregion Apple

// region Email/password

data object EmailPasswordGatekeeperConfiguration

// endregion Email/password
