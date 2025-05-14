package com.tweener.passage.gatekeeper.email

import com.tweener.kmpkit.thread.suspendCatching
import com.tweener.passage.error.PassageEmailAddressAlreadyExistsException
import com.tweener.passage.error.PassageGatekeeperUnknownEntrantException
import com.tweener.passage.error.PassageInvalidCredentialsException
import com.tweener.passage.error.PassageNoUserMatchingEmailException
import com.tweener.passage.error.PassageSignInLinkToEmailException
import com.tweener.passage.error.PassageTooManyRequestsException
import com.tweener.passage.error.PassageWeakPasswordException
import com.tweener.passage.gatekeeper.PassageGatekeeper
import com.tweener.passage.gatekeeper.email.model.PassageEmailAuthParams
import com.tweener.passage.gatekeeper.email.model.PassageEmailVerificationParams
import com.tweener.passage.gatekeeper.email.model.PassageForgotPasswordParams
import com.tweener.passage.gatekeeper.email.model.PassageSignInLinkToEmailParams
import com.tweener.passage.mapper.toEntrant
import com.tweener.passage.model.Entrant
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseTooManyRequestsException
import dev.gitlive.firebase.auth.ActionCodeResult
import dev.gitlive.firebase.auth.ActionCodeSettings
import dev.gitlive.firebase.auth.AndroidPackageName
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.FirebaseAuthInvalidCredentialsException
import dev.gitlive.firebase.auth.FirebaseAuthInvalidUserException
import dev.gitlive.firebase.auth.FirebaseAuthUserCollisionException
import dev.gitlive.firebase.auth.FirebaseAuthWeakPasswordException
import dev.gitlive.firebase.auth.auth
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
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't sign in the user: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

    override suspend fun signOut() {
        // Nothing to do here
    }

    suspend fun signUp(params: PassageEmailAuthParams): Result<Entrant> = suspendCatching {
        firebaseAuth.createUserWithEmailAndPassword(email = params.email, password = params.password).user?.toEntrant()
            ?: throw PassageGatekeeperUnknownEntrantException()
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't sign up the user: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

    suspend fun reauthenticate(params: PassageEmailAuthParams): Result<Unit> = suspendCatching {
        val firebaseCredential = EmailAuthProvider.credential(email = params.email, password = params.password)
        firebaseAuth.currentUser?.reauthenticate(credential = firebaseCredential)
            ?: throw PassageGatekeeperUnknownEntrantException()
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't reauthenticate the user: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

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
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't send reset password email: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

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
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't verify the oobCode ($oobCode) from the password reset email: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

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
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't confirm the password reset: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

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
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't send email address verification email: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

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
        handleOobCode<ActionCodeResult.VerifyEmail>(oobCode = oobCode).fold(
            onSuccess = { Result.success(it) },
            onFailure = { throwable ->
                println("Couldn't verify the oobCode ($oobCode) from the email verification email: $throwable")
                Result.failure(mapFirebaseAuthError(throwable))
            }
        )

    /**
     * Sends a sign-in link to the specified email address with the provided parameters.
     *
     * @param params The parameters required for sending the sign-in link to email.
     */
    suspend fun sendSignInLinkToEmail(params: PassageSignInLinkToEmailParams): Result<Unit> = suspendCatching {
        val actionCodeSettings = buildActionCodeSettings(
            url = params.url,
            iOSBundleId = params.iosParams?.bundleId,
            androidPackageName = params.androidParams?.packageName,
            installIfNotAvailable = params.androidParams?.installIfNotAvailable ?: true,
            minimumVersion = params.androidParams?.minimumVersion,
            canHandleCodeInApp = params.canHandleCodeInApp,
        )

        firebaseAuth.sendSignInLinkToEmail(email = params.email, actionCodeSettings = actionCodeSettings)
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't send sign-in link to email: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

    /**
     * Handles the sign-in process using the provided email and sign-in link.
     *
     * @param email The email address of the user.
     * @param emailLink The sign-in link sent to the user's email.
     * @return A [Result] containing the signed-in user or an error if the sign-in fails.
     */
    suspend fun handleSignInLinkToEmail(email: String, emailLink: String): Result<Entrant> = suspendCatching {
        if (firebaseAuth.isSignInWithEmailLink(link = emailLink).not()) throw PassageSignInLinkToEmailException()

        firebaseAuth.signInWithEmailLink(email = email, link = emailLink).user?.toEntrant()
            ?: throw PassageGatekeeperUnknownEntrantException()
    }.fold(
        onSuccess = { Result.success(it) },
        onFailure = { throwable ->
            println("Couldn't sign in the user with the email link: $throwable")
            Result.failure(mapFirebaseAuthError(throwable))
        }
    )

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

    private fun mapFirebaseAuthError(throwable: Throwable): Throwable =
        when (throwable) {
            is FirebaseAuthInvalidUserException -> PassageNoUserMatchingEmailException()
            is FirebaseAuthInvalidCredentialsException -> PassageInvalidCredentialsException()
            is FirebaseAuthUserCollisionException -> PassageEmailAddressAlreadyExistsException()
            is FirebaseAuthWeakPasswordException -> PassageWeakPasswordException()
            is FirebaseTooManyRequestsException -> PassageTooManyRequestsException()
            else -> throwable
        }
}
