package com.tweener.passage.gatekeeper.google

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.tweener.kmpkit.thread.suspendCatching
import com.tweener.passage.error.PassageGatekeeperUnknownEntrantException
import com.tweener.passage.gatekeeper.google.error.PassageActivityContextNotInitializedException
import com.tweener.passage.gatekeeper.google.error.PassageGoogleGatekeeperUnknownCredentialException
import com.tweener.passage.gatekeeper.google.model.GoogleTokens
import com.tweener.passage.mapper.toEntrant
import com.tweener.passage.model.Entrant
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.GoogleAuthProvider

/**
 * An Android-specific implementation of the [PassageGoogleGatekeeper].
 *
 * This class handles authentication using Google Sign-In on Android devices. It integrates with Firebase
 * for user management and leverages the Credential Manager API to retrieve and manage Google credentials.
 * The class provides functionality for signing in, signing out, and re-authenticating users.
 *
 * Responsibilities:
 * - Initiating Google Sign-In and retrieving tokens for Firebase authentication.
 * - Managing user sessions, including signing out and re-authentication.
 * - Handling credential retrieval and error scenarios during authentication flows.
 *
 * @param serverClientId The server client ID associated with the Google Sign-In configuration.
 * @param firebaseAuth The Firebase authentication instance used for managing authenticated users.
 * @param applicationContext The Android [Context] required for accessing system resources and APIs.
 * @param activityContext A lambda that provides the current Android [Context] for activity-related operations.
 * @param activityResultLauncher A lambda that provides the [ManagedActivityResultLauncher] for activity results.
 * @param activityResult A lambda that provides the current [ActivityResult] for activity results.
 * @param useSignInWithGoogle If true, uses the `signInWithGoogle` method for authentication.
 * @param filterByAuthorizedAccounts If true, filters credentials by authorized accounts for the app.
 * @param autoSelectEnabled If true, enables automatic credential selection when possible.
 * @param maxRetries The maximum number of retries for authentication attempts.
 *
 * @author Vivien Mahe
 * @since 01/12/2024
 */
internal class PassageGoogleGatekeeperAndroid(
    serverClientId: String,
    private val firebaseAuth: FirebaseAuth,
    private val applicationContext: Context,
    private val activityContext: () -> Context?,
    activityResultLauncher: () -> ManagedActivityResultLauncher<Intent, ActivityResult>?,
    activityResult: () -> ActivityResult?,
    private val useSignInWithGoogle: Boolean,
    private val filterByAuthorizedAccounts: Boolean,
    private val autoSelectEnabled: Boolean,
    private val maxRetries: Int,
) : PassageGoogleGatekeeper(serverClientId = serverClientId) {

    private val credentialManager = CredentialManager.create(applicationContext)
    private val legacyGatekeeper = PassageGoogleLegacyGatekeeperAndroid(
        serverClientId = serverClientId,
        firebaseAuth = firebaseAuth,
        activityContext = activityContext,
        activityResultLauncher = activityResultLauncher,
        activityResult = activityResult,
    )

    /**
     * Signs in a user using Google Sign-In.
     *
     * This method retrieves Google tokens using the Credential Manager API and uses them to
     * authenticate the user with Firebase. On success, it returns an authenticated [Entrant].
     * On failure, it logs the error and provides an appropriate exception.
     *
     * @param params Unused, as no parameters are required for Google Sign-In.
     * @return A [Result] containing the authenticated [Entrant] if successful, or an error if the process fails.
     */
    override suspend fun signIn(params: Unit): Result<Entrant> = suspendCatching {
        var attempts = 0
        var lastThrowable: Throwable?

        var useGoogle = useSignInWithGoogle

        while (attempts <= maxRetries) {
            retrieveGoogleTokens(useSignInWithGoogle = useGoogle).fold(
                onSuccess = { googleTokens ->
                    val firebaseCredential = GoogleAuthProvider.credential(idToken = googleTokens.idToken, accessToken = googleTokens.accessToken)

                    return@suspendCatching firebaseAuth.signInWithCredential(authCredential = firebaseCredential).user?.toEntrant()
                        ?: throw PassageGatekeeperUnknownEntrantException()
                },
                onFailure = { throwable ->
                    lastThrowable = throwable

                    println("Couldn't sign in the user. Attempt ${++attempts} of $maxRetries. Error:\n$throwable")

                    if (throwable is NoCredentialException) {
                        signOut()
                    }

                    if (throwable !is GetCredentialCancellationException) {
                        // Try with the legacy gatekeeper, only if the user didn't intentionally cancel the sign in
                        println("Attempt to sign in with Google Legacy provider.")

                        legacyGatekeeper.signIn(Unit).fold(
                            onSuccess = { return@suspendCatching it },
                            onFailure = { legacyThrowable -> println("Couldn't sign in the user with Google Legacy provider. Error:\n$legacyThrowable") }
                        )
                    }

                    // Toggle the use of Google Sign-In method for the next attempt
                    useGoogle = useGoogle.not()

                    if (attempts >= maxRetries) {
                        throw lastThrowable!! // Only throw on the final attempt
                    }
                },
            )
        }

        // Fallback error (should not be reached but just in case)
        throw PassageGatekeeperUnknownEntrantException()
    }

    /**
     * Signs out the current user by clearing the credential state.
     */
    override suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        legacyGatekeeper.signOut()
    }

    /**
     * Re-authenticates the currently authenticated user using Google Sign-In.
     *
     * This method retrieves new Google tokens and uses them to re-authenticate the user with Firebase.
     * On success, it ensures the user's session is refreshed.
     *
     * @return A [Result] indicating the success or failure of the re-authentication process.
     */
    override suspend fun reauthenticate(): Result<Unit> = suspendCatching {
        retrieveGoogleTokens(useSignInWithGoogle = useSignInWithGoogle).fold(
            onSuccess = { googleTokens ->
                val firebaseCredential = GoogleAuthProvider.credential(idToken = googleTokens.idToken, accessToken = googleTokens.accessToken)
                firebaseAuth.currentUser?.reauthenticate(credential = firebaseCredential)
                    ?: throw PassageGatekeeperUnknownEntrantException()
            },
            onFailure = { throwable ->
                println("Couldn't re-authenticate the user. Error:\n$throwable")

                if (throwable is NoCredentialException) {
                    signOut()
                }

                if (throwable !is GetCredentialCancellationException) {
                    // Try with the legacy gatekeeper, only if the user didn't intentionally cancel the sign in
                    println("Attempt to re-authenticate with Google Legacy provider.")

                    legacyGatekeeper.reauthenticate().fold(
                        onSuccess = { return@suspendCatching it },
                        onFailure = { legacyThrowable -> println("Couldn't re-authenticate the user with Google Legacy provider. Error:\n$legacyThrowable") }
                    )
                }

                throw throwable
            },
        )
    }

    private suspend fun retrieveGoogleTokens(useSignInWithGoogle: Boolean): Result<GoogleTokens> = suspendCatching {
        when (val credential = createCredentials(useSignInWithGoogle = useSignInWithGoogle)) {
            is CustomCredential -> {
                when (credential.type) {
                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                        val idToken = googleIdTokenCredential.idToken
                        println("Successful Google Sin In flow with idToken: $idToken")

                        GoogleTokens(idToken = idToken)
                    }

                    else -> {
                        println("Unexpected type of credential")
                        throw PassageGoogleGatekeeperUnknownCredentialException()
                    }
                }
            }

            else -> {
                println("Unexpected type of credential")
                throw PassageGoogleGatekeeperUnknownCredentialException()
            }
        }
    }.onFailure { throwable ->
        when (throwable) {
            is GoogleIdTokenParsingException -> println("Received an invalid google id token response. Error:\n$throwable")
            else -> println("Couldn't handle sign in response with Google gatekeeper. Error:\n$throwable")
        }
    }

    private suspend fun createCredentials(useSignInWithGoogle: Boolean): Credential {
        activityContext.invoke() ?: throw PassageActivityContextNotInitializedException()

        val credentialOption = when (useSignInWithGoogle) {
            true -> GetSignInWithGoogleOption.Builder(serverClientId).build()

            false -> GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
                .setServerClientId(serverClientId)
                .setAutoSelectEnabled(autoSelectEnabled)
                .build()
        }

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(credentialOption)
            .build()

        return credentialManager.getCredential(request = request, context = activityContext.invoke()!!).credential
    }
}
