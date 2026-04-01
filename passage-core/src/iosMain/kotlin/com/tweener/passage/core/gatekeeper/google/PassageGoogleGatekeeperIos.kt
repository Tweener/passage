package com.tweener.passage.core.gatekeeper.google

import cocoapods.GoogleSignIn.GIDSignIn
import com.tweener.kmpkit.utils.safeLet
import com.tweener.passage.core.authplugin.AuthPlugin
import com.tweener.passage.core.gatekeeper.google.error.PassageGoogleGatekeeperException
import com.tweener.passage.core.gatekeeper.google.model.GoogleTokens
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.AuthResult
import com.tweener.passage.core.model.EntrantInterface
import kotlinx.cinterop.ExperimentalForeignApi
import platform.UIKit.UIApplication
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * An iOS-specific implementation of the [PassageGoogleGatekeeper].
 *
 * This class handles authentication using Google Sign-In on iOS devices. It integrates with Backend adapter
 * for user management and utilizes the Google Identity SDK for iOS to retrieve tokens for Google authentication.
 * The class provides functionality for signing in, signing out, and re-authenticating users.
 *
 * Responsibilities:
 * - Facilitating Google Sign-In on iOS and retrieving authentication tokens.
 * - Using Auth credentials to authenticate or re-authenticate users.
 * - Managing error handling for the authentication process.
 *
 * @param authPlugin The Backend authentication adapter instance used for managing authenticated users.
 * @param serverClientId The server client ID associated with the Google Sign-In configuration.
 *
 * @author Vivien Mahe
 * @since 01/12/2024
 */
internal class PassageGoogleGatekeeperIos<T : EntrantInterface>(
    private val authPlugin: AuthPlugin<T>,
    serverClientId: String,
) : PassageGoogleGatekeeper<T>(serverClientId = serverClientId) {

    override suspend fun signIn(params: Unit): Result<T> {
        return retrieveGoogleTokens().fold(

            onSuccess = { googleTokens ->

                when (
                    val result = authPlugin.signIn(AuthCredential.GoogleCredential(
                        idToken = googleTokens.idToken,
                        accessToken = googleTokens.accessToken
                    ))
                ) {
                    is AuthResult.Success -> Result.success(result.data)

                    is AuthResult.Error -> {
                        println("Couldn't sign in user: ${result.throwable}")
                        Result.failure(authPlugin.mapPluginAuthError(result.throwable))
                    }
                }
            },

            onFailure = { throwable ->
                println("Couldn't sign in user: $throwable")
                Result.failure(throwable)
            }
        )
    }

    override suspend fun signOut() {
        // Nothing to do here (same behavior)
    }

    override suspend fun reauthenticate(): Result<Unit> {
        return retrieveGoogleTokens().fold(

            onSuccess = { googleTokens ->

                when (
                    val result = authPlugin.reauthenticate(AuthCredential.GoogleCredential(
                        idToken = googleTokens.idToken,
                        accessToken = googleTokens.accessToken
                    ))
                ) {
                    is AuthResult.Success -> Result.success(Unit)

                    is AuthResult.Error -> {
                        println("Couldn't re-authenticate user: ${result.throwable}")
                        Result.failure(authPlugin.mapPluginAuthError(result.throwable))
                    }
                }
            },

            onFailure = { throwable ->
                println("Couldn't re-authenticate user: $throwable")
                Result.failure(throwable)
            }
        )
    }

    @OptIn(ExperimentalForeignApi::class)
    private suspend fun retrieveGoogleTokens(): Result<GoogleTokens> =
        suspendCoroutine { continuation ->

            UIApplication.sharedApplication.keyWindow?.rootViewController
                ?.let { rootViewController ->

                    GIDSignIn.sharedInstance.signInWithPresentingViewController(rootViewController) { authResult, error ->

                        error?.let {
                            println("Couldn't sign in with Google on iOS! $error")
                        }

                        when {
                            error != null -> {
                                continuation.resumeWithException(
                                    PassageGoogleGatekeeperException()
                                )
                            }

                            else -> {
                                safeLet(
                                    authResult?.user?.idToken?.tokenString,
                                    authResult?.user?.accessToken?.tokenString
                                ) { idToken, accessToken ->
                                    continuation.resume(
                                        Result.success(
                                            GoogleTokens(
                                                idToken = idToken,
                                                accessToken = accessToken
                                            )
                                        )
                                    )
                                } ?: continuation.resumeWithException(
                                    PassageGoogleGatekeeperException()
                                )
                            }
                        }
                    }
                }
                ?: continuation.resumeWithException(
                    PassageGoogleGatekeeperException()
                )
        }
}