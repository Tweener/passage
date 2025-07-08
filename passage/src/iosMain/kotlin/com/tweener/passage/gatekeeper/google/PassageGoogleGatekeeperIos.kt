package com.tweener.passage.gatekeeper.google

import cocoapods.GoogleSignIn.GIDSignIn
import com.tweener.kmpkit.utils.safeLet
import com.tweener.kmpkit.thread.suspendCatching
import com.tweener.passage.error.PassageGatekeeperUnknownEntrantException
import com.tweener.passage.gatekeeper.google.error.PassageGoogleGatekeeperException
import com.tweener.passage.gatekeeper.google.model.GoogleTokens
import com.tweener.passage.mapper.toEntrant
import com.tweener.passage.model.Entrant
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.GoogleAuthProvider
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * An iOS-specific implementation of the [PassageGoogleGatekeeper].
 *
 * This class handles authentication using Google Sign-In on iOS devices. It integrates with Firebase
 * for user management and utilizes the Google Identity SDK for iOS to retrieve tokens for Google authentication.
 * The class provides functionality for signing in, signing out, and re-authenticating users.
 *
 * Responsibilities:
 * - Facilitating Google Sign-In on iOS and retrieving authentication tokens.
 * - Using Firebase credentials to authenticate or re-authenticate users.
 * - Managing error handling for the authentication process.
 *
 * @param firebaseAuth The Firebase authentication instance used for managing authenticated users.
 * @param serverClientId The server client ID associated with the Google Sign-In configuration.
 *
 * @author Vivien Mahe
 * @since 01/12/2024
 */
internal class PassageGoogleGatekeeperIos(
    private val firebaseAuth: FirebaseAuth,
    serverClientId: String,
) : PassageGoogleGatekeeper(serverClientId = serverClientId) {

    /**
     * Signs in a user using Google Sign-In on iOS.
     *
     * This method retrieves Google tokens using the Google Identity SDK, then authenticates the user
     * with Firebase. On success, it returns an authenticated [Entrant]. On failure, it logs the error
     * and provides an appropriate exception.
     *
     * @param params Unused, as no parameters are required for Google Sign-In.
     * @return A [Result] containing the authenticated [Entrant] if successful, or an error if the process fails.
     */
    override suspend fun signIn(params: Unit): Result<Entrant> = suspendCatching {
        retrieveGoogleTokens().fold(
            onSuccess = { googleTokens ->
                val firebaseCredential = GoogleAuthProvider.credential(idToken = googleTokens.idToken, accessToken = googleTokens.accessToken)
                firebaseAuth.signInWithCredential(authCredential = firebaseCredential).user?.toEntrant()
                    ?: throw PassageGatekeeperUnknownEntrantException()
            },
            onFailure = { throwable -> throw throwable },
        )
    }.onFailure { throwable ->
        println("Couldn't sign up the user: $throwable")
    }

    /**
     * Signs out the current user for Google Sign-In on iOS.
     *
     * As Google Sign-In on iOS does not require explicit session management, this method performs no actions.
     */
    override suspend fun signOut() {
        // Nothing to do here
    }

    /**
     * Re-authenticates the currently authenticated user using Google Sign-In on iOS.
     *
     * This method retrieves new Google tokens and uses them to re-authenticate the user with Firebase.
     * On success, it ensures the user's session is refreshed.
     *
     * @return A [Result] indicating the success or failure of the re-authentication process.
     */
    override suspend fun reauthenticate(): Result<Unit> = suspendCatching {
        retrieveGoogleTokens().fold(
            onSuccess = { googleTokens ->
                val firebaseCredential = GoogleAuthProvider.credential(idToken = googleTokens.idToken, accessToken = googleTokens.accessToken)
                firebaseAuth.currentUser?.reauthenticate(credential = firebaseCredential)
                    ?: throw PassageGatekeeperUnknownEntrantException()
            },
            onFailure = { throwable -> throw throwable },
        )
    }.onFailure { throwable ->
        println("Couldn't sign in the user: $throwable")
    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun retrieveGoogleTokens(): Result<GoogleTokens> = suspendCoroutine { continuation ->
        UIApplication.sharedApplication.keyWindow?.rootViewController
            ?.let { rootViewController ->
                GIDSignIn.sharedInstance.signInWithPresentingViewController(rootViewController) { authResult, error ->
                    error?.let { println("Couldn't sign in with Google on iOS! $error") }

                    when {
                        error != null -> continuation.resumeWithException(PassageGoogleGatekeeperException())

                        else -> {
                            safeLet(authResult?.user?.idToken?.tokenString, authResult?.user?.accessToken?.tokenString) { idToken, accessToken ->
                                continuation.resume(Result.success(GoogleTokens(idToken = idToken, accessToken = accessToken)))
                            } ?: continuation.resumeWithException(PassageGoogleGatekeeperException())
                        }
                    }
                }
            }
            ?: continuation.resumeWithException(PassageGoogleGatekeeperException())
    }
}
