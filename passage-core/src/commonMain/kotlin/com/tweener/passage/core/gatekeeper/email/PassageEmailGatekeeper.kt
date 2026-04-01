package com.tweener.passage.core.gatekeeper.email

import com.tweener.passage.core.authplugin.AuthPlugin
import com.tweener.passage.core.error.PassageSignInLinkToEmailException
import com.tweener.passage.core.gatekeeper.PassageGatekeeper
import com.tweener.passage.core.gatekeeper.email.model.PassageEmailAuthParams
import com.tweener.passage.core.gatekeeper.email.model.PassageEmailVerificationParams
import com.tweener.passage.core.gatekeeper.email.model.PassageForgotPasswordParams
import com.tweener.passage.core.gatekeeper.email.model.PassageSignInLinkToEmailParams
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.AuthResult
import com.tweener.passage.core.model.EntrantInterface
import com.tweener.passage.core.model.PassageUniversalLinkMode
import kotlin.jvm.JvmInline

@JvmInline
value class EmailAddress(val email: String)

/**
 * Handles authentication with Backend Adapter via email.
 *
 * This class provides functionality for signing in users using their email and password.
 * It also provides methods to create a new user with email and password, send a password reset email and send an email address verification email.
 *
 * @author Chirag Redij
 * @since 29/03/2026
 */
internal class PassageEmailGatekeeper<T : EntrantInterface>(
    private val authPlugin: AuthPlugin<T>,
) : PassageGatekeeper<PassageEmailAuthParams, T>() {

    override suspend fun signIn(params: PassageEmailAuthParams): Result<T> {
        return when (val result = authPlugin.signIn(AuthCredential.EmailCredential(
            email = params.email,
            password = params.password
        ))) {
            is AuthResult.Success -> Result.success(result.data)
            is AuthResult.Error -> {
                println("Couldn't sign in the user: ${result.throwable}")
                Result.failure(authPlugin.mapPluginAuthError(result.throwable))
            }
        }
    }

    override suspend fun signOut() {
        // Nothing to do here (same behavior)
    }

    suspend fun signUp(params: PassageEmailAuthParams): Result<T> {
        return when (val result = authPlugin.signUp(AuthCredential.EmailCredential(
            email = params.email,
            password = params.password
        ))) {
            is AuthResult.Success -> Result.success(result.data)
            is AuthResult.Error -> {
                println("Couldn't sign up the user: ${result.throwable}")
                Result.failure(authPlugin.mapPluginAuthError(result.throwable))
            }
        }
    }

    suspend fun reauthenticate(params: PassageEmailAuthParams): Result<Unit> {
        return when (val result = authPlugin.reauthenticate(AuthCredential.EmailCredential(
            email = params.email,
            password = params.password
        ))) {
            is AuthResult.Success -> Result.success(Unit)
            is AuthResult.Error -> {
                println("Couldn't reauthenticate the user: ${result.throwable}")
                Result.failure(authPlugin.mapPluginAuthError(result.throwable))
            }
        }
    }

    suspend fun sendPasswordResetEmail(params: PassageForgotPasswordParams): Result<Unit> {
        return when (val result = authPlugin.sendPasswordResetEmail(
            email = params.email,
            params = params
        )) {
            is AuthResult.Success -> Result.success(Unit)
            is AuthResult.Error -> {
                println("Couldn't send reset password email: ${result.throwable}")
                Result.failure(authPlugin.mapPluginAuthError(result.throwable))
            }
        }
    }

    suspend fun handlePasswordResetCode(oobCode: String): Result<EmailAddress> {
        return when (val result = authPlugin.verifyPasswordResetCode(oobCode)) {
            is AuthResult.Success -> Result.success(EmailAddress(result.data))
            is AuthResult.Error -> {
                println("Couldn't verify the oobCode ($oobCode): ${result.throwable}")
                Result.failure(authPlugin.mapPluginAuthError(result.throwable))
            }
        }
    }

    suspend fun confirmResetPassword(oobCode: String, newPassword: String): Result<Unit> {
        return when (val result = authPlugin.confirmPasswordReset(
            oobCode = oobCode,
            newPassword = newPassword
        )) {
            is AuthResult.Success -> Result.success(Unit)
            is AuthResult.Error -> {
                println("Couldn't confirm password reset: ${result.throwable}")
                Result.failure(authPlugin.mapPluginAuthError(result.throwable))
            }
        }
    }

    suspend fun sendEmailVerification(params: PassageEmailVerificationParams): Result<Unit> {
        return when (val result = authPlugin.sendEmailVerification(params)) {
            is AuthResult.Success -> Result.success(Unit)
            is AuthResult.Error -> {
                println("Couldn't send email verification: ${result.throwable}")
                Result.failure(authPlugin.mapPluginAuthError(result.throwable))
            }
        }
    }

    suspend fun handleEmailVerificationCode(oobCode: String): Result<Unit> {
        return when (val result = authPlugin.handleOobCode(
            oobCode = oobCode,
            mode = PassageUniversalLinkMode.VERIFY_EMAIL
        )) {
            is AuthResult.Success -> Result.success(Unit)
            is AuthResult.Error -> {
                println("Couldn't verify email ($oobCode): ${result.throwable}")
                Result.failure(authPlugin.mapPluginAuthError(result.throwable))
            }
        }
    }

    suspend fun sendSignInLinkToEmail(params: PassageSignInLinkToEmailParams): Result<Unit> {
        return when (val result = authPlugin.sendSignInLinkToEmail(
            email = params.email,
            params = params
        )) {
            is AuthResult.Success -> Result.success(Unit)
            is AuthResult.Error -> {
                println("Couldn't send sign-in link: ${result.throwable}")
                Result.failure(authPlugin.mapPluginAuthError(result.throwable))
            }
        }
    }

    suspend fun handleSignInLinkToEmail(
        email: String,
        emailLink: String
    ): Result<T> {

        when (val check = authPlugin.isSignInWithEmailLink(emailLink)) {
            is AuthResult.Success -> {
                if (!check.data) {
                    return Result.failure(PassageSignInLinkToEmailException())
                }
            }
            is AuthResult.Error -> {
                return Result.failure(authPlugin.mapPluginAuthError(check.throwable))
            }
        }

        return when (val result = authPlugin.signInWithEmailLink(email, emailLink)) {
            is AuthResult.Success -> Result.success(result.data)
            is AuthResult.Error -> {
                println("Couldn't sign in with email link: ${result.throwable}")
                Result.failure(authPlugin.mapPluginAuthError(result.throwable))
            }
        }
    }
}