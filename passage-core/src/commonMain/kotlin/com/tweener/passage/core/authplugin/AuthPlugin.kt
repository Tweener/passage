package com.tweener.passage.core.authplugin

import com.tweener.passage.core.gatekeeper.email.model.PassageEmailVerificationParams
import com.tweener.passage.core.gatekeeper.email.model.PassageForgotPasswordParams
import com.tweener.passage.core.gatekeeper.email.model.PassageSignInLinkToEmailParams
import com.tweener.passage.core.model.ActionCodeType
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.AuthResult
import com.tweener.passage.core.model.EntrantInterface
import kotlinx.coroutines.flow.Flow

/**
 * @author Chirag Redij
 * @since 29/03/2026
 */

interface AuthPlugin<T : EntrantInterface> {

    val currentUser: T?
    val authStateChanged: Flow<T?>

    suspend fun getCurrentUser(): AuthResult<T?>

    suspend fun signIn(
        credential: AuthCredential
    ): AuthResult<T>

    suspend fun signUp(
        credential: AuthCredential
    ): AuthResult<T>

    suspend fun reauthenticate(
        credential: AuthCredential
    ): AuthResult<Unit>

    suspend fun sendPasswordResetEmail(
        email: String,
        params: PassageForgotPasswordParams
    ): AuthResult<Unit>

    suspend fun verifyPasswordResetCode(
        oobCode: String
    ): AuthResult<String>

    suspend fun confirmPasswordReset(
        oobCode: String,
        newPassword: String
    ): AuthResult<Unit>

    suspend fun sendEmailVerification(
        params: PassageEmailVerificationParams
    ): AuthResult<Unit>

    suspend fun handleOobCode(
        oobCode: String,
        type: ActionCodeType
    ): AuthResult<Unit>

    suspend fun sendSignInLinkToEmail(
        email: String,
        params: PassageSignInLinkToEmailParams
    ): AuthResult<Unit>

    suspend fun isSignInWithEmailLink(
        link: String
    ): AuthResult<Boolean>

    suspend fun signInWithEmailLink(
        email: String,
        link: String
    ): AuthResult<T>

    suspend fun signOut(): AuthResult<Unit>

    suspend fun deleteCurrentUser(): AuthResult<Unit>

    fun mapPluginAuthError(throwable: Throwable): Throwable
}