package com.tweener.passage.gatekeeper.apple

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIROAuthProvider
import com.tweener.common._internal.contract.requireNotNullOrThrow
import com.tweener.common._internal.thread.resumeIfActive
import com.tweener.common._internal.thread.resumeWithExceptionIfActive
import com.tweener.passage.error.PassageGatekeeperUnknownAdmitteeException
import com.tweener.passage.gatekeeper.apple.error.PassageAppleGatekeeperException
import com.tweener.passage.mapper.toAdmittee
import com.tweener.passage.model.Admittee
import dev.gitlive.firebase.auth.FirebaseAuth
import io.github.aakira.napier.Napier
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
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.UIKit.UIApplication
import platform.darwin.NSObject
import kotlin.coroutines.cancellation.CancellationException

/**
 * @author Vivien Mahe
 * @since 02/12/2024
 */
internal class PassageAppleGatekeeperIos(
    private val firebaseAuth: FirebaseAuth,
) : PassageAppleGatekeeper() {

    private lateinit var delegate: AuthorizationControllerDelegate
    private val presentationContextProvider = PresentationContextProvider()

    override suspend fun signIn(params: Unit): Result<Admittee> = suspendCancellableCoroutine { continuation ->
        try {
            val rawNonce = AppleNonceFactory.createRandomNonceString()
            delegate = AuthorizationControllerDelegate(firebaseAuth = firebaseAuth, nonce = rawNonce) { result -> continuation.resumeIfActive(result) }

            continuation.invokeOnCancellation {
                Napier.d { "Canceled Apple Sign In on iOS" }

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
            Napier.e(throwable) { "An error occurred while signing in with Apple provider" }
            continuation.resumeWithExceptionIfActive(throwable)
        }
    }

    override suspend fun signOut() {
        // Nothing to do here
    }
}

private class AuthorizationControllerDelegate(
    private val firebaseAuth: FirebaseAuth,
    private val nonce: String,
    var onResponse: (Result<Admittee>) -> Unit,
) : ASAuthorizationControllerDelegateProtocol, NSObject() {

    @OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
    override fun authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization: ASAuthorization) {
        try {
            val appleIDCredential = didCompleteWithAuthorization.credential as ASAuthorizationAppleIDCredential

            val appleIDToken = appleIDCredential.identityToken
            requireNotNullOrThrow(appleIDToken) { PassageAppleGatekeeperException(message = "Unable to fetch identity token") }

            val idTokenString = NSString.create(data = appleIDToken, encoding = NSUTF8StringEncoding)?.toString()
            requireNotNullOrThrow(idTokenString) { PassageAppleGatekeeperException(message = "Unable to serialize token string from data: ${appleIDToken.debugDescription}") }

            val credential = FIROAuthProvider.appleCredentialWithIDToken(idToken = idTokenString, rawNonce = nonce, fullName = appleIDCredential.fullName)

            FIRAuth.auth().signInWithCredential(credential) { authResult, error ->
                error?.let { Napier.e { "Couldn't sign in with Apple on iOS! $error" } }

                when {
                    error != null || authResult == null -> onResponse(Result.failure(PassageAppleGatekeeperException(message = "FIRAuthDataResult is null")))

                    else -> {
                        firebaseAuth.currentUser?.toAdmittee()
                            ?.let { user -> onResponse(Result.success(user)) }
                            ?: onResponse(Result.failure(PassageGatekeeperUnknownAdmitteeException()))
                    }
                }
            }
        } catch (throwable: Throwable) {
            onResponse(Result.failure(throwable))
        }
    }

    override fun authorizationController(controller: ASAuthorizationController, didCompleteWithError: NSError) {
        Napier.e { "Didn't get authorization to sign in with Apple: $didCompleteWithError" }
        onResponse(Result.failure(PassageAppleGatekeeperException(message = didCompleteWithError.localizedFailureReason)))
    }
}

private class PresentationContextProvider : ASAuthorizationControllerPresentationContextProvidingProtocol, NSObject() {

    override fun presentationAnchorForAuthorizationController(controller: ASAuthorizationController): ASPresentationAnchor =
        UIApplication.sharedApplication.keyWindow?.rootViewController?.view?.window
}
