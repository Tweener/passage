package com.tweener.passage.core

import androidx.compose.runtime.Composable
import com.tweener.passage.core.authplugin.AuthPlugin
import com.tweener.passage.core.error.PassageGatekeeperNotConfiguredException
import com.tweener.passage.core.gatekeeper.apple.PassageAppleGatekeeper
import com.tweener.passage.core.gatekeeper.email.EmailAddress
import com.tweener.passage.core.gatekeeper.email.PassageEmailGatekeeper
import com.tweener.passage.core.gatekeeper.email.model.PassageEmailAuthParams
import com.tweener.passage.core.gatekeeper.email.model.PassageEmailVerificationParams
import com.tweener.passage.core.gatekeeper.email.model.PassageForgotPasswordParams
import com.tweener.passage.core.gatekeeper.email.model.PassageResetPasswordParams
import com.tweener.passage.core.gatekeeper.email.model.PassageSignInLinkToEmailParams
import com.tweener.passage.core.gatekeeper.google.PassageGoogleGatekeeper
import com.tweener.passage.core.model.AppleGatekeeperConfiguration
import com.tweener.passage.core.model.AuthResult
import com.tweener.passage.core.model.EmailPasswordGatekeeperConfiguration
import com.tweener.passage.core.model.EntrantInterface
import com.tweener.passage.core.model.GatekeeperType
import com.tweener.passage.core.model.GoogleGatekeeperConfiguration
import com.tweener.passage.core.model.PassageGatekeeperConfiguration
import com.tweener.passage.core.model.PassageUniversalLink
import com.tweener.passage.core.universallink.PassageUniversalLinkHandler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Handles authentication operations through a pluggable [AuthPlugin] backend.
 *
 * The **Passage** represents the authentication entry point—a secure gateway through which users (Entrants)
 * must pass to access the system. To navigate the Passage, Entrants are verified by Gatekeepers,
 * which act as authentication providers (e.g., Google, Apple, or Email/Password).
 *
 * ### Key concepts
 * - **Passage**: The entry point for authentication. It facilitates interactions between Entrants and Gatekeepers.
 * - **Gatekeeper**: An authentication provider that verifies an Entrant's identity (e.g., Google, Apple, Email/Password).
 * - **Entrant**: A user who has been successfully authenticated and granted access to the system.
 * - **AuthPlugin**: The backend adapter (e.g., Firebase, Supabase) that performs the actual authentication calls.
 *
 * ### Usage
 * 1. Initialize the service with the necessary configuration for each Gatekeeper and an [AuthPlugin] instance.
 * 2. Use methods like `authenticateWithGoogle`, `authenticateWithApple`, or `authenticateWithEmailAndPassword` to allow Entrants to pass through the Passage.
 * 3. Retrieve the current Entrant or observe authentication state changes as needed.
 *
 * @author Chirag Redij
 * @since 29/03/2026
 */
abstract class Passage<T : EntrantInterface> {
    private val universalLinkHandler = PassageUniversalLinkHandler()

    val universalLinkToHandle: StateFlow<PassageUniversalLink?> = universalLinkHandler.linkToHandle

    protected lateinit var authPlugin: AuthPlugin<T>

    /**
     * Returns `true` if the [authPlugin] has been initialized via [initialize].
     */
    private val isInitialized: Boolean
        get() = ::authPlugin.isInitialized

    private var googleGatekeeper: PassageGoogleGatekeeper<T>? = null
    private var appleGatekeeper: PassageAppleGatekeeper<T>? = null
    private var emailGatekeeper: PassageEmailGatekeeper<T>? = null

    /**
     * Initializes the Passage library with the specified list of Gatekeeper configurations and [AuthPlugin] instance.
     *
     * This method sets up the required Gatekeepers (e.g., Google, Apple, Email/Password) using the provided configurations
     * and the specified [AuthPlugin] backend.
     *
     * @param gatekeeperConfigurations A list of Gatekeeper configurations implementing [PassageGatekeeperConfiguration].
     * @param authPlugin The authentication plugin used for backend operations.
     *
     * @see PassageGatekeeperConfiguration
     */
    fun initialize(gatekeeperConfigurations: List<PassageGatekeeperConfiguration>, authPlugin: AuthPlugin<T>) {
        this.authPlugin = authPlugin

        gatekeeperConfigurations.forEach { configuration ->
            if (configuration is GoogleGatekeeperConfiguration) googleGatekeeper = createGoogleGatekeeper(configuration = configuration, authPlugin = authPlugin)
            if (configuration is AppleGatekeeperConfiguration) appleGatekeeper = createAppleGatekeeper(configuration = configuration, authPlugin = authPlugin)
            if (configuration is EmailPasswordGatekeeperConfiguration) emailGatekeeper = createEmailGatekeeper(configuration = configuration, authPlugin = authPlugin)
        }

        println("Passage is initialized.")
    }

    /**
     * Binds Passage to the current view associated with this Composable.
     * This method is necessary when using the Google gatekeeper on Android, as Google Sign-In requires access to the current Activity-based context.
     */
    @Composable
    abstract fun bindToView()

    /**
     * Retrieves the currently authenticated user as an [Entrant], or `null` if no user is authenticated.
     *
     * @return The current [Entrant], or `null` if no user is logged in.
     */
    suspend fun getCurrentUser(): T? {
        check(isInitialized) { "Passage must be initialized before calling getCurrentUser()." }

        return when (val result = authPlugin.getCurrentUser()) {
            is AuthResult.Success -> result.data
            is AuthResult.Error -> null
        }
    }

    /**
     * Observes changes to the authenticated user as a [Flow] of [Entrant].
     *
     * @return A [Flow] that emits the current [Entrant], or `null` if no user is logged in.
     */
    fun getCurrentUserAsFlow(): Flow<T?> {
        check(isInitialized) { "Passage must be initialized before calling getCurrentUserAsFlow()." }
        return authPlugin.authStateChanged
    }

    /**
     * Indicates whether a user is currently logged in.
     */
    suspend fun isUserLoggedIn(): Boolean = getCurrentUser() != null

    /**
     * Observes whether a user is logged in as a [Flow] of [Boolean].
     *
     * @return A [Flow] that emits `true` if a user is logged in, or `false` otherwise.
     */
    fun isUserLoggedInAsFlow(): Flow<Boolean> =
        getCurrentUserAsFlow().map { it != null }

    /**
     * Signs out the current user from all configured Gatekeepers and the authentication backend.
     */
    suspend fun signOut() {
        googleGatekeeper?.signOut()
        appleGatekeeper?.signOut()
        emailGatekeeper?.signOut()

        authPlugin.signOut()
    }

    /**
     * Deletes the currently authenticated user from the authentication backend.
     */
    suspend fun deleteCurrentUser() {
        authPlugin.deleteCurrentUser()
    }

    /**
     * Retrieves the ID token for the currently authenticated user.
     *
     * @return A [Result] containing the ID token as a [String], or an error if no user is authenticated.
     */
    suspend fun getIdToken(): Result<String> {
        return when (val result = authPlugin.getCurrentUser()) {
            is AuthResult.Success -> {
                result.data?.uid?.let {
                    Result.success(it)
                } ?: Result.failure(
                    IllegalStateException("No authenticated user found to retrieve ID token.")
                )
            }

            is AuthResult.Error -> {
                Result.failure(result.throwable)
            }
        }
    }
    // region Google gatekeeper

    /**
     * Authenticates a user with Google.
     *
     * @return A [Result] containing the authenticated [Entrant] or an error if the operation fails.
     */
    suspend fun authenticateWithGoogle(): Result<T> =
        googleGatekeeper
            ?.signIn(Unit)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.GOOGLE))

    /**
     * Re-authenticates the currently authenticated user with Google.
     *
     * @return A [Result] indicating success or failure.
     */
    suspend fun reauthenticateWithGoogle(): Result<Unit> =
        googleGatekeeper
            ?.reauthenticate()
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.GOOGLE))

    /**
     * Creates the Google Gatekeeper for handling authentication.
     *
     * @param configuration The configuration for the Google Gatekeeper.
     * @param authPlugin The Backend Adapter authentication instance.
     * @return The created [PassageGoogleGatekeeper].
     */
    internal abstract fun createGoogleGatekeeper(configuration: GoogleGatekeeperConfiguration, authPlugin: AuthPlugin<T>): PassageGoogleGatekeeper<T>

    // endregion Google gatekeeper

    // region Apple gatekeeper

    /**
     * Authenticates a user with Apple.
     *
     * @return A [Result] containing the authenticated [Entrant] or an error if the operation fails.
     */
    suspend fun authenticateWithApple(): Result<T> =
        appleGatekeeper
            ?.signIn(Unit)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.APPLE))

    /**
     * Creates the Apple Gatekeeper for handling authentication.
     *
     * @param configuration The configuration for the Apple Gatekeeper.
     * @param authPlugin The Backend Adapter authentication instance.
     * @return The created [PassageAppleGatekeeper].
     */
    internal abstract fun createAppleGatekeeper(configuration: AppleGatekeeperConfiguration, authPlugin: AuthPlugin<T>): PassageAppleGatekeeper<T>

    // endregion Apple gatekeeper

    // region Email & Password gatekeeper

    /**
     * Creates the Email/Password Gatekeeper for handling authentication.
     *
     * @param configuration The configuration for the Email/Password Gatekeeper.
     * @param authPlugin The Backend Adapter authentication instance.
     * @return The created [PassageEmailGatekeeper].
     */
    internal fun createEmailGatekeeper(
        configuration: EmailPasswordGatekeeperConfiguration, authPlugin: AuthPlugin<T>
    ): PassageEmailGatekeeper<T> = PassageEmailGatekeeper( authPlugin = authPlugin)

    /**
     * Authenticates a user with an email and password.
     *
     * @param params The parameters required for authentication.
     * @return The authenticated entrant.
     */
    suspend fun authenticateWithEmailAndPassword(params: PassageEmailAuthParams): Result<T> =
        emailGatekeeper
            ?.signIn(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    /**
     * Creates a new user with the given email and password.
     *
     * @param params The parameters required for account creation.
     * @return The created entrant.
     */
    suspend fun createUserWithEmailAndPassword(params: PassageEmailAuthParams): Result<T> =
        emailGatekeeper
            ?.signUp(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    /**
     * Re-authenticates the currently logged-in user using email and password credentials.
     *
     * @param params The parameters required for re-authentication.
     */
    suspend fun reauthenticateWithEmailAndPassword(params: PassageEmailAuthParams): Result<Unit> =
        emailGatekeeper
            ?.reauthenticate(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    /**
     * Sends a password reset email with specified settings.
     *
     * @param params The parameters required for sending a password reset email.
     */
    suspend fun sendPasswordResetEmail(params: PassageForgotPasswordParams): Result<Unit> =
        emailGatekeeper
            ?.sendPasswordResetEmail(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    /**
     * Handles the password reset process using the provided out-of-band (OOB) code.
     *
     * This method verifies the password reset action code, applies it to confirm the password reset,
     * and reloads the current user to reflect the updated password.
     *
     * @param oobCode The out-of-band code received from the password reset link.
     * @return The user's [EmailAddress] for the password reset process.
     */
    suspend fun handlePasswordResetCode(oobCode: String): Result<EmailAddress> =
        emailGatekeeper
            ?.handlePasswordResetCode(oobCode = oobCode)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    /**
     * Confirms a password reset operation using the provided reset parameters.
     *
     * This method completes the password reset flow by validating the provided parameters
     * and updating the user's password to the specified value.
     *
     * @param params The parameters required for resetting the password.
     */
    suspend fun confirmResetPassword(params: PassageResetPasswordParams): Result<Unit> =
        emailGatekeeper
            ?.confirmResetPassword(oobCode = params.oobCode, newPassword = params.newPassword)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    /**
     * Sends an email address verification email with specified settings.
     *
     * @param params The parameters required for sending an email address verification email.
     */
    suspend fun sendEmailVerification(params: PassageEmailVerificationParams): Result<Unit> =
        emailGatekeeper
            ?.sendEmailVerification(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    /**
     * Handles the email verification process using the provided out-of-band (OOB) code.
     *
     * This method verifies the email verification action code, applies it to confirm the user's email,
     * and reloads the current user to reflect the updated email verification status.
     * If the email is successfully verified, the [Entrant.isEmailVerified] property will be set to `true`.
     *
     * @param oobCode The out-of-band code received from the email verification link.
     * @return A [Result] containing the success or failure of the email verification process.
     */
    suspend fun handleEmailVerificationCode(oobCode: String): Result<Unit> =
        emailGatekeeper
            ?.handleEmailVerificationCode(oobCode = oobCode)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    // endregion Email & Password gatekeeper

    // region Sign in link to email

    /**
     * Sends a sign-in link to the specified email address.
     *
     * @param params The parameters required for sending the sign-in link to email.
     */
    suspend fun sendSignInLinkToEmail(params: PassageSignInLinkToEmailParams): Result<Unit> =
        emailGatekeeper
            ?.sendSignInLinkToEmail(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    /**
     * Handles the sign-in process using the provided email and sign-in link.
     *
     * @param email The email address of the user.
     * @param emailLink The sign-in link sent to the user's email.
     * @return A [Result] containing the signed-in user or an error if the sign-in fails.
     */
    suspend fun handleSignInLinkToEmail(email: String, emailLink: String): Result<T> =
        emailGatekeeper
            ?.handleSignInLinkToEmail(email = email, emailLink = emailLink)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    // region Universal Links

    /**
     * Handles a Universal Link by passing the provided URL to the link handler.
     *
     * Call this method whenever your app receives a universal link (iOS) or App Link (Android) to allow Passage to process it.
     * This is typically used to handle deep linking for authentication flows, such as email verification or password reset links.
     *
     * @param url The URL from the universal or App Link.
     */
    fun handleLink(url: String): Boolean =
        universalLinkHandler.handle(url = url)

    /**
     * Notifies Passage that a universal link has been handled.
     *
     * Call this method whenever a universal link or App Link is successfully handled in your app.
     * This ensures that Passage processes the link appropriately, allowing it to update the
     * authentication state or perform other related tasks.
     */
    fun onLinkHandled() {
        universalLinkHandler.onLinkHandled()
    }

    // endregion Universal Links
}