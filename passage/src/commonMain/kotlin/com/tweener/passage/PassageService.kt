package com.tweener.passage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tweener.passage.error.PassageGatekeeperNotConfiguredException
import com.tweener.passage.gatekeeper.apple.PassageAppleGatekeeper
import com.tweener.passage.gatekeeper.email.PassageEmailGatekeeper
import com.tweener.passage.gatekeeper.email.model.PassageEmailAuthParams
import com.tweener.passage.gatekeeper.email.model.PassageEmailVerificationParams
import com.tweener.passage.gatekeeper.email.model.PassageForgotPasswordParams
import com.tweener.passage.gatekeeper.google.PassageGoogleGatekeeper
import com.tweener.passage.mapper.toEntrant
import com.tweener.passage.model.AppleGatekeeperConfiguration
import com.tweener.passage.model.EmailPasswordGatekeeperConfiguration
import com.tweener.passage.model.Entrant
import com.tweener.passage.model.GatekeeperType
import com.tweener.passage.model.GoogleGatekeeperConfiguration
import com.tweener.passage.model.PassageServiceConfiguration
import com.tweener.passage.universallink.FirebaseUniversalLink
import com.tweener.passage.universallink.PassageUniversalLinkHandler
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map

/**
 * Creates and [remember] the [PassageService].
 */
@Composable
expect fun rememberPassageService(universalLinkHandler: PassageUniversalLinkHandler = PassageUniversalLinkHandler()): PassageService

/**
 * Handles Firebase authentication operations.
 *
 * The **Passage** represents the authentication entry point—a secure gateway through which users (Entrants)
 * must pass to access the system. To navigate the Passage, Entrants are verified by Gatekeepers,
 * which act as authentication providers (e.g., Google, Apple, or Email/Password).
 *
 * ### Key concepts
 * - **Passage**: The entry point for authentication. It facilitates interactions between Entrants and Gatekeepers.
 * - **Gatekeeper**: An authentication provider that verifies an Entrant's identity (e.g., Google, Apple, Email/Password).
 * - **Entrant**: A user who has been successfully authenticated and granted access to the system.
 *
 * ### Usage
 * 1. Initialize the service with the necessary configuration for each Gatekeeper.
 * 2. Use methods like `authenticateWithGoogle`, `authenticateWithApple`, or `authenticateWithEmailAndPassword` to allow Entrants to pass through the Passage.
 * 3. Retrieve the current Entrant or observe authentication state changes as needed.
 *
 * @author Vivien Mahe
 * @since 30/11/2024
 */
abstract class PassageService(
    universalLinkHandler: PassageUniversalLinkHandler,
) {
    val universalLinkToHandle: StateFlow<FirebaseUniversalLink?> = universalLinkHandler.linkToHandle

    protected lateinit var firebaseAuth: FirebaseAuth

    private var googleGatekeeper: PassageGoogleGatekeeper? = null
    private var appleGatekeeper: PassageAppleGatekeeper? = null
    private var emailGatekeeper: PassageEmailGatekeeper? = null

    /**
     * Initializes the Passage with the provided configuration.
     *
     * @param configuration The configuration for setting up Gatekeepers (Google, Apple, Email/Password).
     */
    fun initialize(configuration: PassageServiceConfiguration) {
        initializeFirebase()

        firebaseAuth = Firebase.auth

        configuration.google?.let { googleGatekeeper = createGoogleGatekeeper(configuration = it, firebaseAuth = firebaseAuth) }
        configuration.apple?.let { appleGatekeeper = createAppleGatekeeper(configuration = it) }
        configuration.emailPassword?.let { emailGatekeeper = createEmailGatekeeper(configuration = it) }
    }

    /**
     * Retrieves the currently authenticated user as an [Entrant], or `null` if no user is authenticated.
     *
     * @return The current [Entrant], or `null` if no user is logged in.
     */
    fun getCurrentUser(): Entrant? =
        firebaseAuth.currentUser?.toEntrant()

    /**
     * Observes changes to the authenticated user as a [Flow] of [Entrant].
     *
     * @return A [Flow] that emits the current [Entrant], or `null` if no user is logged in.
     */
    fun getCurrentUserAsFlow(): Flow<Entrant?> =
        firebaseAuth.authStateChanged.map { it?.toEntrant() }

    /**
     * Observes whether a user is logged in as a [Flow] of [Boolean].
     *
     * @return A [Flow] that emits `true` if a user is logged in, or `false` otherwise.
     */
    fun isUserLoggedIn(): Flow<Boolean> =
        getCurrentUserAsFlow().map { it != null }

    /**
     * Signs out the current user from all configured Gatekeepers and Firebase.
     */
    suspend fun signOut() {
        googleGatekeeper?.signOut()
        appleGatekeeper?.signOut()
        emailGatekeeper?.signOut()

        firebaseAuth.signOut()
    }

    /**
     * Deletes the currently authenticated user from Firebase.
     */
    suspend fun deleteCurrentUser() {
        firebaseAuth.currentUser?.delete()
    }

    /**
     * Platform-specific initialization for Firebase.
     */
    protected abstract fun initializeFirebase()

    // region Google gatekeeper

    /**
     * Authenticates a user with Google.
     *
     * @return A [Result] containing the authenticated [Entrant] or an error if the operation fails.
     */
    suspend fun authenticateWithGoogle(): Result<Entrant> =
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
     * @param firebaseAuth The Firebase authentication instance.
     * @return The created [PassageGoogleGatekeeper].
     */
    internal abstract fun createGoogleGatekeeper(configuration: GoogleGatekeeperConfiguration, firebaseAuth: FirebaseAuth): PassageGoogleGatekeeper

    // endregion Google gatekeeper

    // region Apple gatekeeper

    /**
     * Authenticates a user with Apple.
     *
     * @return A [Result] containing the authenticated [Entrant] or an error if the operation fails.
     */
    suspend fun authenticateWithApple(): Result<Entrant> =
        appleGatekeeper
            ?.signIn(Unit)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.APPLE))

    /**
     * Creates the Apple Gatekeeper for handling authentication.
     *
     * @param configuration The configuration for the Apple Gatekeeper.
     * @return The created [PassageAppleGatekeeper].
     */
    internal abstract fun createAppleGatekeeper(configuration: AppleGatekeeperConfiguration): PassageAppleGatekeeper

    // endregion Apple gatekeeper

    // region Email & Password gatekeeper

    /**
     * Creates the Email/Password Gatekeeper for handling authentication.
     *
     * @param configuration The configuration for the Email/Password Gatekeeper.
     * @return The created [PassageEmailGatekeeper].
     */
    private fun createEmailGatekeeper(configuration: EmailPasswordGatekeeperConfiguration): PassageEmailGatekeeper = PassageEmailGatekeeper(firebaseAuth = firebaseAuth)

    /**
     * Authenticates a user with an email and password.
     *
     * @param params TThe parameters required for authentication.
     * @return The authenticated entrant, or null if authentication fails.
     */
    suspend fun authenticateWithEmailAndPassword(params: PassageEmailAuthParams): Result<Entrant> =
        emailGatekeeper
            ?.signIn(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = GatekeeperType.EMAIL_PASSWORD))

    /**
     * Creates a new user with the given email and password.
     *
     * @param params TThe parameters required for authentication.
     * @return The created entrant, or null if creation fails.
     */
    suspend fun createUserWithEmailAndPassword(params: PassageEmailAuthParams): Result<Entrant> =
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
     * @return A [Result] containing the success or failure of the password reset process.
     */
    suspend fun handlePasswordResetCode(oobCode: String): Result<Unit> =
        emailGatekeeper
            ?.handlePasswordResetCode(oobCode = oobCode)
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
}
