package com.tweener.passage.gatekeeper.apple

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIROAuthProvider
import com.tweener.kmpkit.contract.requireNotNullOrThrow
import com.tweener.kmpkit.thread.resumeIfActive
import com.tweener.kmpkit.thread.resumeWithExceptionIfActive
import com.tweener.kmpkit.thread.suspendCatching
import com.tweener.passage.error.PassageGatekeeperUnknownEntrantException
import com.tweener.passage.gatekeeper.apple.error.PassageAppleGatekeeperException
import com.tweener.passage.mapper.toEntrant
import com.tweener.passage.model.Entrant
import com.tweener.passage.model.PassageAppleCredential
import dev.gitlive.firebase.auth.FirebaseAuth
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.AuthenticationServices.ASAuthorization
import platform.AuthenticationServices.ASAuthorizationAppleIDCredential
import platform.AuthenticationServices.ASAuthorizationAppleIDProvider
import platform.AuthenticationServices.ASAuthorizationController
import platform.AuthenticationServices.ASAuthorizationControllerDelegateProtocol
import platform.AuthenticationServices.ASAuthorizationControllerPresentationContextProvidingProtocol
import platform.AuthenticationServices.ASAuthorizationScopeEmail
import platform.AuthenticationServices.ASAuthorizationScopeFullName
import platform.AuthenticationServices.ASPresentationAnchor
import platform.Foundation.NSError
import platform.Foundation.NSPersonNameComponents
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import kotlin.coroutines.cancellation.CancellationException

/**
 * An iOS-specific implementation of the [PassageAppleGatekeeper].
 *
 * This class handles authentication using Apple Sign-In on iOS devices. It integrates with
 * Firebase for user management and uses the `ASAuthorizationAppleIDProvider` to initiate
 * the Apple Sign-In process. The `signIn` method facilitates the authentication flow, while
 * the `signOut` method is a no-op since Apple Sign-In does not require explicit sign-out
 * operations.
 *
 * Responsibilities:
 * - Initiating the Apple Sign-In process using the Apple ID provider.
 * - Generating and securely hashing a nonce for secure communication with Firebase.
 * - Managing callbacks and responses via the `AuthorizationControllerDelegate`.
 *
 * @param firebaseAuth The Firebase authentication instance used for managing authenticated users.
 *
 * @author Vivien Mahe
 * @since 02/12/2024
 */
internal class PassageAppleGatekeeperIos(
    private val firebaseAuth: FirebaseAuth,
) : PassageAppleGatekeeper() {

    private lateinit var delegate: AuthorizationControllerDelegate
    private val presentationContextProvider = PresentationContextProvider()

    /**
     * Authenticates a user using Apple Sign-In on iOS.
     *
     * This method initiates the Apple Sign-In flow using the `ASAuthorizationAppleIDProvider`,
     * generates a secure nonce, and integrates with Firebase to complete the authentication process.
     *
     * @param params Unused, as no parameters are required for Apple Sign-In.
     * @return A [Result] containing the authenticated [Entrant] if successful, or an error if the process fails.
     */
    override suspend fun signIn(params: Unit): Result<Entrant> = suspendCatching {
        val rawCredential = performAppleAuthorization().getOrThrow()
        signInToFirebase(rawCredential).getOrThrow()
    }.onFailure { throwable ->
        println("Couldn't sign in the user with Apple provider: $throwable")
    }

    override suspend fun retrieveCredential(): Result<PassageAppleCredential> = suspendCatching {
        val rawCredential = performAppleAuthorization().getOrThrow()

        PassageAppleCredential(
            identityToken = rawCredential.identityToken,
            name = rawCredential.formattedName,
            email = rawCredential.email,
            rawNonce = rawCredential.rawNonce,
        )
    }.onFailure { throwable ->
        println("Couldn't retrieve the credential with Apple provider: $throwable")
    }

    /**
     * Signs out the current user for Apple Sign-In on iOS.
     *
     * Since Apple Sign-In does not require explicit session management on iOS, this method performs no actions.
     */
    override suspend fun signOut() {
        // Nothing to do here
    }

    /**
     * Runs the native Apple Sign-In UI and resumes with the raw [AppleRawCredential]. Shared by [signIn] and [retrieveCredential].
     */
    private suspend fun performAppleAuthorization(): Result<AppleRawCredential> = suspendCancellableCoroutine { continuation ->
        try {
            val rawNonce = AppleNonceFactory.createRandomNonceString()
            delegate = AuthorizationControllerDelegate(nonce = rawNonce) { result -> continuation.resumeIfActive(result) }

            continuation.invokeOnCancellation {
                println("Canceled Apple Sign In on iOS")

                delegate.onResponse = {}

                continuation.resumeWithExceptionIfActive(CancellationException())
            }

            val request = ASAuthorizationAppleIDProvider().createRequest().apply {
                requestedScopes = listOf(ASAuthorizationScopeEmail, ASAuthorizationScopeFullName)
                nonce = AppleNonceFactory.sha256(rawNonce)
            }

            ASAuthorizationController(authorizationRequests = listOf(request)).apply {
                delegate = this@PassageAppleGatekeeperIos.delegate
                presentationContextProvider = this@PassageAppleGatekeeperIos.presentationContextProvider
                performRequests()
            }
        } catch (throwable: Throwable) {
            println("An error occurred while signing in with Apple provider: $throwable")
            continuation.resumeWithExceptionIfActive(throwable)
        }
    }

    /**
     * Exchanges the raw Apple credential for a Firebase session.
     */
    @OptIn(ExperimentalForeignApi::class)
    private suspend fun signInToFirebase(rawCredential: AppleRawCredential): Result<Entrant> = suspendCancellableCoroutine { continuation ->
        val credential = FIROAuthProvider.appleCredentialWithIDToken(idToken = rawCredential.identityToken, rawNonce = rawCredential.rawNonce, fullName = rawCredential.fullName)

        FIRAuth.auth().signInWithCredential(credential) { authResult, error ->
            error?.let { println("Couldn't sign in with Apple on iOS! $error") }

            when {
                error != null || authResult == null -> continuation.resumeIfActive(Result.failure(PassageAppleGatekeeperException(message = "FIRAuthDataResult is null")))

                else -> {
                    firebaseAuth.currentUser?.toEntrant()
                        ?.let { user -> continuation.resumeIfActive(Result.success(user)) }
                        ?: continuation.resumeIfActive(Result.failure(PassageGatekeeperUnknownEntrantException()))
                }
            }
        }
    }
}

/**
 * Raw data from an [ASAuthorizationAppleIDCredential]: the native [fullName] for the Firebase credential and a
 * [formattedName] string for [PassageAppleCredential].
 */
@OptIn(ExperimentalForeignApi::class)
private class AppleRawCredential(
    val identityToken: String,
    val fullName: NSPersonNameComponents?,
    val formattedName: String?,
    val email: String?,
    val rawNonce: String,
)

private class AuthorizationControllerDelegate(
    private val nonce: String,
    var onResponse: (Result<AppleRawCredential>) -> Unit,
) : ASAuthorizationControllerDelegateProtocol, NSObject() {

    @OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
    override fun authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization: ASAuthorization) {
        try {
            val appleIDCredential = didCompleteWithAuthorization.credential as ASAuthorizationAppleIDCredential

            val appleIDToken = appleIDCredential.identityToken
            requireNotNullOrThrow(appleIDToken) { PassageAppleGatekeeperException(message = "Unable to fetch identity token") }

            val idTokenString = NSString.create(data = appleIDToken, encoding = NSUTF8StringEncoding)?.toString()
            requireNotNullOrThrow(idTokenString) { PassageAppleGatekeeperException(message = "Unable to serialize token string from data: ${appleIDToken.debugDescription}") }

            val fullName = appleIDCredential.fullName

            onResponse(
                Result.success(
                    AppleRawCredential(
                        identityToken = idTokenString,
                        fullName = fullName,
                        formattedName = fullName?.let { formatPersonName(it) },
                        email = appleIDCredential.email,
                        rawNonce = nonce,
                    )
                )
            )
        } catch (throwable: Throwable) {
            onResponse(Result.failure(throwable))
        }
    }

    override fun authorizationController(controller: ASAuthorizationController, didCompleteWithError: NSError) {
        println("Didn't get authorization to sign in with Apple: $didCompleteWithError")
        onResponse(Result.failure(PassageAppleGatekeeperException(message = didCompleteWithError.localizedFailureReason)))
    }

    /**
     * Joins the given and family name components, or returns null when both are absent.
     */
    @OptIn(ExperimentalForeignApi::class)
    private fun formatPersonName(components: NSPersonNameComponents): String? =
        listOfNotNull(components.givenName, components.familyName)
            .joinToString(" ")
            .ifBlank { null }
}

private class PresentationContextProvider : ASAuthorizationControllerPresentationContextProvidingProtocol, NSObject() {

    override fun presentationAnchorForAuthorizationController(controller: ASAuthorizationController): ASPresentationAnchor =
        UIApplication.sharedApplication.keyWindow?.rootViewController?.view?.window
}
