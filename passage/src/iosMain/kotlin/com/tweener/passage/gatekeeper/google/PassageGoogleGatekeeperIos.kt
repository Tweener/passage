package com.tweener.passage.gatekeeper.google

import cocoapods.GoogleSignIn.GIDSignIn
import com.tweener.common._internal.safeLet
import com.tweener.common._internal.thread.suspendCatching
import com.tweener.passage.error.PassageGatekeeperUnknownEntrantException
import com.tweener.passage.gatekeeper.google.error.PassageGoogleGatekeeperException
import com.tweener.passage.gatekeeper.google.model.GoogleTokens
import com.tweener.passage.mapper.toEntrant
import com.tweener.passage.model.Entrant
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.GoogleAuthProvider
import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Handles Google Sign-In on iOS.
 *
 * This class provides functionality for signing in users using their Google account on an iOS device.
 * It utilizes the GIDSignIn API to manage credentials and handle the sign-in process.
 *
 * @param passageService The service for handling authentication with Passage.
 * @param serverClientId The server client ID for authenticating with Google.
 *
 * @author Vivien Mahe
 * @since 01/12/2024
 */
internal class PassageGoogleGatekeeperIos(
    private val firebaseAuth: FirebaseAuth,
    serverClientId: String,
) : PassageGoogleGatekeeper(serverClientId = serverClientId) {

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
        Napier.e(throwable) { "Couldn't sign in the user." }
    }

    override suspend fun signOut() {
        // Nothing to do here
    }

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
        Napier.e(throwable) { "Couldn't sign in the user." }
    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun retrieveGoogleTokens(): Result<GoogleTokens> = suspendCoroutine { continuation ->
        UIApplication.sharedApplication.keyWindow?.rootViewController
            ?.let { rootViewController ->
                GIDSignIn.sharedInstance.signInWithPresentingViewController(rootViewController) { authResult, error ->
                    error?.let { Napier.e { "Couldn't sign in with Google on iOS! $error" } }

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
