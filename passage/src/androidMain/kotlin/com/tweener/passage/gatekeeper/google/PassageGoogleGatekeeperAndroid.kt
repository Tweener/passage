package com.tweener.passage.gatekeeper.google

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
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
import io.github.aakira.napier.Napier

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
    private val filterByAuthorizedAccounts: Boolean,
    private val autoSelectEnabled: Boolean,
    private val maxRetries: Int,
) : PassageGoogleGatekeeper(serverClientId = serverClientId) {

    private val credentialManager = CredentialManager.create(applicationContext)
    private var retryAttempts = 0

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
        retrieveGoogleTokens(credential = createCredentials()).fold(
            onSuccess = { googleTokens ->
                retryAttempts = 0

                val firebaseCredential = GoogleAuthProvider.credential(idToken = googleTokens.idToken, accessToken = googleTokens.accessToken)
                firebaseAuth.signInWithCredential(authCredential = firebaseCredential).user?.toEntrant()
                    ?: throw PassageGatekeeperUnknownEntrantException()
            },
            onFailure = { throwable -> throw throwable },
        )
    }.onFailure { throwable ->
        Napier.e(throwable) { "Couldn't sign in the user." }

        when (throwable) {
            is NoCredentialException -> {
                signOut()

                if (++retryAttempts <= maxRetries) {
                    println("Retrying sign in, attempt $retryAttempts")

                    signIn(params)
                }
            }
        }
    }

    /**
     * Signs out the current user by clearing the credential state.
     */
    override suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
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
        retrieveGoogleTokens(createCredentials()).fold(
            onSuccess = { googleTokens ->
                retryAttempts = 0

                val firebaseCredential = GoogleAuthProvider.credential(idToken = googleTokens.idToken, accessToken = googleTokens.accessToken)
                firebaseAuth.currentUser?.reauthenticate(credential = firebaseCredential)
                    ?: throw PassageGatekeeperUnknownEntrantException()
            },
            onFailure = { throwable -> throw throwable },
        )
    }.onFailure { throwable ->
        Napier.e(throwable) { "Couldn't re-authenticate the user." }
    }

    private suspend fun retrieveGoogleTokens(credential: Credential): Result<GoogleTokens> = suspendCatching {
        when (credential) {
            is CustomCredential -> {
                when (credential.type) {
                    GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                        val idToken = googleIdTokenCredential.idToken
                        Napier.d { "Successful Google Sin In flow with idToken: $idToken" }

                        GoogleTokens(idToken = idToken)
                    }

                    else -> {
                        Napier.d { "Unexpected type of credential" }
                        throw PassageGoogleGatekeeperUnknownCredentialException()
                    }
                }
            }

            else -> {
                Napier.d { "Unexpected type of credential" }
                throw PassageGoogleGatekeeperUnknownCredentialException()
            }
        }
    }.onFailure { throwable ->
        when (throwable) {
            is GoogleIdTokenParsingException -> Napier.e(throwable) { "Received an invalid google id token response." }
            else -> Napier.e(throwable) { "Couldn't handle sign in response with Google gatekeeper" }
        }
    }

    private suspend fun createCredentials(): Credential {
        activityContext.invoke() ?: throw PassageActivityContextNotInitializedException()

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(autoSelectEnabled)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return credentialManager.getCredential(request = request, context = activityContext.invoke()!!).credential
    }
}
