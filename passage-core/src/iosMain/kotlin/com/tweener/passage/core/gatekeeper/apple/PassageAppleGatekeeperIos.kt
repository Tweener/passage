package com.tweener.passage.core.gatekeeper.apple

import com.tweener.kmpkit.contract.requireNotNullOrThrow
import com.tweener.kmpkit.thread.resumeIfActive
import com.tweener.kmpkit.thread.resumeWithExceptionIfActive
import com.tweener.passage.core.authplugin.AuthPlugin
import com.tweener.passage.core.gatekeeper.apple.error.PassageAppleGatekeeperException
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.AuthResult
import com.tweener.passage.core.model.EntrantInterface
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
 * An iOS-specific implementation of the [PassageAppleGatekeeper].
 *
 * This class handles authentication using Apple Sign-In on iOS devices. It integrates with
 * Backend Adapter for user management and uses the `ASAuthorizationAppleIDProvider` to initiate
 * the Apple Sign-In process. The `signIn` method facilitates the authentication flow, while
 * the `signOut` method is a no-op since Apple Sign-In does not require explicit sign-out
 * operations.
 *
 * Responsibilities:
 * - Initiating the Apple Sign-In process using the Apple ID provider.
 * - Generating and securely hashing a nonce for secure communication with Backend adapter.
 * - Managing callbacks and responses via the `AuthorizationControllerDelegate`.
 *
 * @param authPlugin The Backend authentication adapter instance used for managing authenticated users.
 *
 * @author Vivien Mahe
 * @since 02/12/2024
 */
internal class PassageAppleGatekeeperIos<T : EntrantInterface>(
    private val authPlugin: AuthPlugin<T>,
) : PassageAppleGatekeeper<T>() {

    private lateinit var delegate: AuthorizationControllerDelegate<T>
    private val presentationContextProvider = PresentationContextProvider()

    /**
     * Authenticates a user using Apple Sign-In on iOS.
     *
     * This method initiates the Apple Sign-In flow using the `ASAuthorizationAppleIDProvider`,
     * generates a secure nonce, and integrates with Backend adapter to complete the authentication process.
     *
     * @param params Unused, as no parameters are required for Apple Sign-In.
     * @return A [Result] containing the authenticated [Entrant] if successful, or an error if the process fails.
     */
    override suspend fun signIn(params: Unit): Result<T> = suspendCancellableCoroutine { continuation ->
        try {
            val rawNonce = AppleNonceFactory.createRandomNonceString()
            delegate = AuthorizationControllerDelegate(authPlugin = authPlugin, nonce = rawNonce) { result -> continuation.resumeIfActive(result) }

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
     * Signs out the current user for Apple Sign-In on iOS.
     *
     * Since Apple Sign-In does not require explicit session management on iOS, this method performs no actions.
     */
    override suspend fun signOut() {
        // Nothing to do here
    }
}

private class AuthorizationControllerDelegate<T : EntrantInterface>(
    private val authPlugin: AuthPlugin<T>,
    private val nonce: String,
    var onResponse: (Result<T>) -> Unit,
) : ASAuthorizationControllerDelegateProtocol, NSObject() {

    @OptIn(BetaInteropApi::class, ExperimentalForeignApi::class)
    override fun authorizationController(
        controller: ASAuthorizationController,
        didCompleteWithAuthorization: ASAuthorization
    ) {
        try {
            val appleIDCredential = didCompleteWithAuthorization.credential as ASAuthorizationAppleIDCredential

            val appleIDToken = appleIDCredential.identityToken
            requireNotNullOrThrow(appleIDToken) {
                PassageAppleGatekeeperException(message = "Unable to fetch identity token")
            }

            val idTokenString = NSString.create(
                data = appleIDToken,
                encoding = NSUTF8StringEncoding
            )?.toString()

            requireNotNullOrThrow(idTokenString) {
                PassageAppleGatekeeperException(
                    message = "Unable to serialize token string from data: ${appleIDToken.debugDescription}"
                )
            }

            CoroutineScope(Dispatchers.Main).launch {

                when (
                    val authResult = authPlugin.signIn(AuthCredential.AppleCredential(
                        idToken = idTokenString,
                        rawNonce = nonce,
                        fullName = appleIDCredential.fullName
                    ))
                ) {

                    is AuthResult.Success -> {
                        onResponse(Result.success(authResult.data))
                    }

                    is AuthResult.Error -> {
                        println("Couldn't sign in with Apple on iOS! ${authResult.throwable}")
                        onResponse(
                            Result.failure(
                                authPlugin.mapPluginAuthError(authResult.throwable)
                            )
                        )
                    }
                }
            }

        } catch (throwable: Throwable) {
            onResponse(Result.failure(throwable))
        }
    }

    override fun authorizationController(controller: ASAuthorizationController, didCompleteWithError: NSError) {
        println("Didn't get authorization to sign in with Apple: $didCompleteWithError")
        onResponse(Result.failure(PassageAppleGatekeeperException(message = didCompleteWithError.localizedFailureReason)))
    }
}

private class PresentationContextProvider : ASAuthorizationControllerPresentationContextProvidingProtocol, NSObject() {

    override fun presentationAnchorForAuthorizationController(controller: ASAuthorizationController): ASPresentationAnchor =
        UIApplication.sharedApplication.keyWindow?.rootViewController?.view?.window
}