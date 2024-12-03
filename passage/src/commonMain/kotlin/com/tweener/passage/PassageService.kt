package com.tweener.passage

import androidx.compose.runtime.Composable
import com.tweener.passage.error.PassageGatekeeperNotConfiguredException
import com.tweener.passage.gatekeeper.apple.PassageAppleGatekeeper
import com.tweener.passage.gatekeeper.email.PassageEmailGatekeeper
import com.tweener.passage.gatekeeper.email.model.PassageEmailAuthParams
import com.tweener.passage.gatekeeper.email.model.PassageEmailVerificationParams
import com.tweener.passage.gatekeeper.email.model.PassageForgotPasswordParams
import com.tweener.passage.gatekeeper.google.PassageGoogleGatekeeper
import com.tweener.passage.mapper.toEntrant
import com.tweener.passage.model.Entrant
import com.tweener.passage.model.AppleGatekeeperConfiguration
import com.tweener.passage.model.EmailPasswordGatekeeperConfiguration
import com.tweener.passage.model.GoogleGatekeeperConfiguration
import com.tweener.passage.model.PassageServiceConfiguration
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Composable
expect fun rememberPassageService(): PassageService

/**
 * Handles Firebase authentication operations.
 *
 * @author Vivien Mahe
 * @since 30/11/2024
 */
abstract class PassageService {

    protected lateinit var firebaseAuth: FirebaseAuth

    private var googleGatekeeper: PassageGoogleGatekeeper? = null
    private var appleGatekeeper: PassageAppleGatekeeper? = null
    private var emailGatekeeper: PassageEmailGatekeeper? = null

    fun initialize(configuration: PassageServiceConfiguration) {
        initializeFirebase()

        firebaseAuth = Firebase.auth

        configuration.google?.let { googleGatekeeper = createGoogleGatekeeper(configuration = it, firebaseAuth = firebaseAuth) }
        configuration.apple?.let { appleGatekeeper = createAppleGatekeeper(configuration = it) }
        configuration.emailPassword?.let { emailGatekeeper = createEmailGatekeeper(configuration = it) }
    }

    fun getCurrentUser(): Entrant? =
        firebaseAuth.currentUser?.toEntrant()

    fun getCurrentUserAsFlow(): Flow<Entrant?> =
        firebaseAuth.authStateChanged.map { it?.toEntrant() }

    fun isUserLoggedIn(): Flow<Boolean> =
        getCurrentUserAsFlow().map { it != null }

    suspend fun signOut() {
        googleGatekeeper?.signOut()
        appleGatekeeper?.signOut()
        emailGatekeeper?.signOut()

        firebaseAuth.signOut()
    }

    suspend fun deleteCurrentUser() {
        firebaseAuth.currentUser?.delete()
    }

    protected abstract fun initializeFirebase()

    // region Google gatekeeper

    /**
     * Authenticates a user against Google gatekeeper.
     *
     * @return The authenticated entrant, or null if authentication fails.
     */
    suspend fun authenticateWithGoogle(): Result<Entrant> =
        googleGatekeeper
            ?.signIn(Unit)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = "Google"))

    /**
     * Re-authenticates the currently logged-in user against Google gatekeeper.
     */
    suspend fun reauthenticateWithGoogle(): Result<Unit> =
        googleGatekeeper
            ?.reauthenticate()
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = "Google"))

    internal abstract fun createGoogleGatekeeper(configuration: GoogleGatekeeperConfiguration, firebaseAuth: FirebaseAuth): PassageGoogleGatekeeper

    // endregion Google gatekeeper

    // region Apple gatekeeper

    /**
     * Authenticates a user against Apple gatekeeper.
     *
     * @return The authenticated entrant, or null if authentication fails.
     */
    suspend fun authenticateWithApple(): Result<Entrant> =
        appleGatekeeper
            ?.signIn(Unit)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = "Apple"))

    internal abstract fun createAppleGatekeeper(configuration: AppleGatekeeperConfiguration): PassageAppleGatekeeper

    // endregion Apple gatekeeper

    // region Email & Password gatekeeper

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
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = "Email/Password"))

    /**
     * Creates a new user with the given email and password.
     *
     * @param params TThe parameters required for authentication.
     * @return The created entrant, or null if creation fails.
     */
    suspend fun createUserWithEmailAndPassword(params: PassageEmailAuthParams): Result<Entrant> =
        emailGatekeeper
            ?.signUp(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = "Email/Password"))

    /**
     * Re-authenticates the currently logged-in user using email and password credentials.
     *
     * @param params The parameters required for re-authentication.
     */
    suspend fun reauthenticateWithEmailAndPassword(params: PassageEmailAuthParams): Result<Unit> =
        emailGatekeeper
            ?.reauthenticate(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = "Email/Password"))

    /**
     * Sends a password reset email with specified settings.
     *
     * @param params The parameters required for sending a password reset email.
     */
    suspend fun sendPasswordResetEmail(params: PassageForgotPasswordParams): Result<Unit> =
        emailGatekeeper
            ?.sendPasswordResetEmail(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = "Email/Password"))

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
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = "Email/Password"))

    /**
     * Sends an email address verification email with specified settings.
     *
     * @param params The parameters required for sending an email address verification email.
     */
    suspend fun sendEmailVerification(params: PassageEmailVerificationParams): Result<Unit> =
        emailGatekeeper
            ?.sendEmailVerification(params = params)
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = "Email/Password"))

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
            ?: Result.failure(PassageGatekeeperNotConfiguredException(gatekeeper = "Email/Password"))

    // endregion Email & Password gatekeeper
}
