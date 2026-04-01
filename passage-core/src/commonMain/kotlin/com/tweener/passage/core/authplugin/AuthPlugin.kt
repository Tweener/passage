package com.tweener.passage.core.authplugin

import com.tweener.passage.core.gatekeeper.email.model.PassageEmailVerificationParams
import com.tweener.passage.core.gatekeeper.email.model.PassageForgotPasswordParams
import com.tweener.passage.core.gatekeeper.email.model.PassageSignInLinkToEmailParams
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.AuthResult
import com.tweener.passage.core.model.EntrantInterface
import com.tweener.passage.core.model.PassageUniversalLinkMode
import kotlinx.coroutines.flow.Flow

/**
 * Defines the contract for a pluggable authentication backend in the Passage library.
 *
 * An **AuthPlugin** encapsulates all interactions with a specific authentication provider
 * (e.g., Firebase, Supabase). It handles sign-in, sign-up, session management, email flows,
 * and error mapping so that the rest of the library remains provider-agnostic.
 *
 * Implementations must map provider-specific user representations to the domain model [T]
 * via an appropriate mapper (e.g., `FirebaseUserMapper`, `SupabaseUserMapper`).
 *
 * @param T The domain user type, constrained to [EntrantInterface].
 *
 * @author Chirag Redij
 * @since 29/03/2026
 */
interface AuthPlugin<T : EntrantInterface> {

    /**
     * The currently authenticated user, or `null` if no user is signed in.
     */
    val currentUser: T?

    /**
     * A [Flow] that emits the current authenticated user whenever the auth state changes,
     * or `null` when the user signs out.
     */
    val authStateChanged: Flow<T?>

    /**
     * Retrieves the currently authenticated user.
     *
     * @return [AuthResult.Success] containing the user, or [AuthResult.Error] if no user is signed in.
     */
    suspend fun getCurrentUser(): AuthResult<T?>

    /**
     * Signs in a user with the given [credential].
     *
     * @param credential The authentication credential (email, Google, or Apple).
     * @return [AuthResult.Success] containing the authenticated user, or [AuthResult.Error] on failure.
     */
    suspend fun signIn(
        credential: AuthCredential
    ): AuthResult<T>

    /**
     * Creates a new user account with the given [credential].
     *
     * @param credential The authentication credential (typically email/password).
     * @return [AuthResult.Success] containing the newly created user, or [AuthResult.Error] on failure.
     */
    suspend fun signUp(
        credential: AuthCredential
    ): AuthResult<T>

    /**
     * Re-authenticates the currently signed-in user with the given [credential].
     *
     * @param credential The authentication credential used for re-authentication.
     * @return [AuthResult.Success] on success, or [AuthResult.Error] on failure.
     */
    suspend fun reauthenticate(
        credential: AuthCredential
    ): AuthResult<Unit>

    /**
     * Sends a password reset email to the specified [email] address.
     *
     * @param email The email address to send the reset link to.
     * @param params Additional parameters controlling the reset email.
     * @return [AuthResult.Success] on success, or [AuthResult.Error] on failure.
     */
    suspend fun sendPasswordResetEmail(
        email: String,
        params: PassageForgotPasswordParams
    ): AuthResult<Unit>

    /**
     * Verifies a password reset out-of-band code.
     *
     * @param oobCode The out-of-band code from the reset email link.
     * @return [AuthResult.Success] containing the associated email address, or [AuthResult.Error] on failure.
     */
    suspend fun verifyPasswordResetCode(
        oobCode: String
    ): AuthResult<String>

    /**
     * Confirms a password reset using the out-of-band code and the user's new password.
     *
     * @param oobCode The out-of-band code from the reset email link.
     * @param newPassword The user's new password.
     * @return [AuthResult.Success] on success, or [AuthResult.Error] on failure.
     */
    suspend fun confirmPasswordReset(
        oobCode: String,
        newPassword: String
    ): AuthResult<Unit>

    /**
     * Sends an email verification to the currently authenticated user.
     *
     * @param params Parameters controlling the verification email (e.g., redirect URLs).
     * @return [AuthResult.Success] on success, or [AuthResult.Error] on failure.
     */
    suspend fun sendEmailVerification(
        params: PassageEmailVerificationParams
    ): AuthResult<Unit>

    /**
     * Processes an out-of-band action code of the given [mode].
     *
     * @param oobCode The out-of-band code to process.
     * @param mode The type of action this code represents (e.g., email verification, password reset).
     * @return [AuthResult.Success] on success, or [AuthResult.Error] on failure.
     */
    suspend fun handleOobCode(
        oobCode: String,
        mode: PassageUniversalLinkMode
    ): AuthResult<Unit>

    /**
     * Sends a sign-in link (magic link) to the specified email address.
     *
     * @param email The email address to send the sign-in link to.
     * @param params Additional parameters controlling the sign-in link email.
     * @return [AuthResult.Success] on success, or [AuthResult.Error] on failure.
     */
    suspend fun sendSignInLinkToEmail(
        email: String,
        params: PassageSignInLinkToEmailParams
    ): AuthResult<Unit>

    /**
     * Checks whether the given [link] is a valid sign-in-with-email link.
     *
     * @param link The URL to check.
     * @return [AuthResult.Success] containing `true` if valid, `false` otherwise.
     */
    suspend fun isSignInWithEmailLink(
        link: String
    ): AuthResult<Boolean>

    /**
     * Signs in a user using an email link (magic link).
     *
     * @param email The user's email address.
     * @param link The sign-in link received via email.
     * @return [AuthResult.Success] containing the authenticated user, or [AuthResult.Error] on failure.
     */
    suspend fun signInWithEmailLink(
        email: String,
        link: String
    ): AuthResult<T>

    /**
     * Signs out the currently authenticated user.
     *
     * @return [AuthResult.Success] on success, or [AuthResult.Error] on failure.
     */
    suspend fun signOut(): AuthResult<Unit>

    /**
     * Deletes the currently authenticated user's account.
     *
     * @return [AuthResult.Success] on success, or [AuthResult.Error] on failure.
     */
    suspend fun deleteCurrentUser(): AuthResult<Unit>

    /**
     * Maps a provider-specific [throwable] to a domain-level exception.
     *
     * Implementations should translate backend-specific errors (e.g., Firebase or Supabase exceptions)
     * into Passage domain exceptions such as [com.tweener.passage.core.error.PassageInvalidCredentialsException].
     *
     * @param throwable The original provider-specific error.
     * @return A mapped domain exception, or the original [throwable] if no mapping applies.
     */
    fun mapPluginAuthError(throwable: Throwable): Throwable
}