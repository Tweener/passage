package com.tweener.passage.gatekeeper.email

import com.tweener.common._internal.thread.suspendCatching
import com.tweener.passage.error.PassageGatekeeperUnknownEntrantException
import com.tweener.passage.gatekeeper.PassageGatekeeper
import com.tweener.passage.gatekeeper.email.model.PassageEmailAuthParams
import com.tweener.passage.gatekeeper.email.model.PassageEmailVerificationParams
import com.tweener.passage.gatekeeper.email.model.PassageForgotPasswordParams
import com.tweener.passage.mapper.toEntrant
import com.tweener.passage.model.Entrant
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.ActionCodeResult
import dev.gitlive.firebase.auth.ActionCodeSettings
import dev.gitlive.firebase.auth.AndroidPackageName
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.auth
import io.github.aakira.napier.Napier
import kotlin.jvm.JvmInline

@JvmInline
value class EmailAddress(val email: String)

/**
 * Handles authentication with Firebase via email.
 *
 * This class provides functionality for signing in users using their email and password.
 * It also provides methods to create a new user with email and password, send a password reset email and send an email address verification email.
 *
 * @author Vivien Mahe
 * @since 02/12/2024
 */
internal class PassageEmailGatekeeper(
    private val firebaseAuth: FirebaseAuth,
) : PassageGatekeeper<PassageEmailAuthParams>() {

    override suspend fun signIn(params: PassageEmailAuthParams): Result<Entrant> = suspendCatching {
        firebaseAuth.signInWithEmailAndPassword(email = params.email, password = params.password).user?.toEntrant()
            ?: throw PassageGatekeeperUnknownEntrantException()
    }.onFailure { throwable ->
        Napier.e(throwable) { "Couldn't sign in the user." }
    }

    override suspend fun signOut() {
        // Nothing to do here
    }

    suspend fun signUp(params: PassageEmailAuthParams): Result<Entrant> = suspendCatching {
        firebaseAuth.createUserWithEmailAndPassword(email = params.email, password = params.password).user?.toEntrant()
            ?: throw PassageGatekeeperUnknownEntrantException()
    }.onFailure { throwable ->
        Napier.e(throwable) { "Couldn't sign in the user." }
    }

    suspend fun reauthenticate(params: PassageEmailAuthParams): Result<Unit> = suspendCatching {
        val firebaseCredential = EmailAuthProvider.credential(email = params.email, password = params.password)
        firebaseAuth.currentUser?.reauthenticate(credential = firebaseCredential)
            ?: throw PassageGatekeeperUnknownEntrantException()
    }.onFailure { throwable ->
        Napier.e(throwable) { "Couldn't sign in the user." }
    }

    /**
     * Sends a password reset email with specified parameters.
     *
     * @param params The parameters required for sending the password reset email.
     */
    suspend fun sendPasswordResetEmail(params: PassageForgotPasswordParams): Result<Unit> = suspendCatching {
        val actionCodeSettings = buildActionCodeSettings(
            url = params.url,
            iOSBundleId = params.iosParams?.bundleId,
            androidPackageName = params.androidParams?.packageName,
            installIfNotAvailable = params.androidParams?.installIfNotAvailable ?: true,
            minimumVersion = params.androidParams?.minimumVersion,
            canHandleCodeInApp = params.canHandleCodeInApp,
        )

        firebaseAuth.sendPasswordResetEmail(email = params.email, actionCodeSettings = actionCodeSettings)
    }.onFailure { throwable ->
        Napier.e(throwable) { "Couldn't send reset password email." }
    }

    /**
     * Handles the password reset process using the provided out-of-band (OOB) code.
     *
     * This method verifies the password reset action code, applies it to confirm the password reset,
     * and reloads the current user to reflect the updated password.
     *
     * @param oobCode The out-of-band code received from the password reset link.
     * @return A [Result] containing the success or failure of the password reset process.
     */
    suspend fun handlePasswordResetCode(oobCode: String): Result<EmailAddress> = suspendCatching {
        val email = firebaseAuth.verifyPasswordResetCode(code = oobCode)
        EmailAddress(email = email)
    }.onFailure { throwable ->
        Napier.e(throwable) { "Couldn't verify the oobCode ($oobCode) from the password reset email." }
    }

    /**
     * Confirms a password reset operation using the provided out-of-band code and new password.
     *
     * This method completes the password reset flow by validating the provided `oobCode`
     * (out-of-band code) and updating the user's password to the specified `newPassword`.
     *
     * @param oobCode The out-of-band code sent to the user's email for password reset.
     * @param newPassword The new password to set for the user.
     * @return A [Result] indicating the success or failure of the operation.
     */
    suspend fun confirmResetPassword(oobCode: String, newPassword: String): Result<Unit> = suspendCatching {
        firebaseAuth.confirmPasswordReset(code = oobCode, newPassword = newPassword)
    }.onFailure { throwable ->
        Napier.e(throwable) { "Couldn't confirm the password reset." }
    }

    /**
     * Sends an email address verification email with specified parameters.
     *
     * @param params The parameters required for sending the email address verification email.
     */
    suspend fun sendEmailVerification(params: PassageEmailVerificationParams): Result<Unit> = suspendCatching {
        val actionCodeSettings = buildActionCodeSettings(
            url = params.url,
            iOSBundleId = params.iosParams?.bundleId,
            androidPackageName = params.androidParams?.packageName,
            installIfNotAvailable = params.androidParams?.installIfNotAvailable ?: true,
            minimumVersion = params.androidParams?.minimumVersion,
            canHandleCodeInApp = params.canHandleCodeInApp,
        )

        firebaseAuth.currentUser?.sendEmailVerification(actionCodeSettings = actionCodeSettings)
            ?: throw PassageGatekeeperUnknownEntrantException()
    }.onFailure { throwable ->
        Napier.e(throwable) { "Couldn't send email address verification email." }
    }

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
    suspend fun handleEmailVerificationCode(oobCode: String) =
        handleOobCode<ActionCodeResult.VerifyEmail>(oobCode = oobCode).onFailure { throwable ->
            Napier.e(throwable) { "Couldn't verify the oobCode ($oobCode) from the email verification email." }
        }

    private fun buildActionCodeSettings(
        url: String,
        iOSBundleId: String?,
        androidPackageName: String?,
        installIfNotAvailable: Boolean,
        minimumVersion: String?,
        canHandleCodeInApp: Boolean,
    ): ActionCodeSettings =
        ActionCodeSettings(
            url = url,
            androidPackageName = androidPackageName?.let { AndroidPackageName(packageName = it, installIfNotAvailable = installIfNotAvailable, minimumVersion = minimumVersion) },
            iOSBundleId = iOSBundleId,
            canHandleCodeInApp = canHandleCodeInApp,
        )

    private suspend fun <T : ActionCodeResult> handleOobCode(oobCode: String) = suspendCatching {
        Firebase.auth.checkActionCode<T>(code = oobCode)
        Firebase.auth.applyActionCode(code = oobCode)
        Firebase.auth.currentUser?.reload() ?: Unit
    }
}
