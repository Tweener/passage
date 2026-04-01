package com.tweener.passage.core.gatekeeper.google

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
import com.tweener.passage.core.authplugin.AuthPlugin
import com.tweener.passage.core.error.PassageGatekeeperUnknownEntrantException
import com.tweener.passage.core.gatekeeper.google.error.PassageActivityContextNotInitializedException
import com.tweener.passage.core.gatekeeper.google.error.PassageGoogleGatekeeperUnknownCredentialException
import com.tweener.passage.core.gatekeeper.google.model.GoogleTokens
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.AuthResult
import com.tweener.passage.core.model.EntrantInterface

/**
 * An Android-specific implementation of the [PassageGoogleGatekeeper].
 *
 * This class handles authentication using Google Sign-In on Android devices. It integrates with Backend adapter
 * for user management and leverages the Credential Manager API to retrieve and manage Google credentials.
 * The class provides functionality for signing in, signing out, and re-authenticating users.
 *
 * Responsibilities:
 * - Initiating Google Sign-In and retrieving tokens for Backend authentication.
 * - Managing user sessions, including signing out and re-authentication.
 * - Handling credential retrieval and error scenarios during authentication flows.
 *
 * @param serverClientId The server client ID associated with the Google Sign-In configuration.
 * @param authPlugin The Backend authentication adapter instance used for managing authenticated users.
 * @param applicationContext The Android [Context] required for accessing system resources and APIs.
 * @param activityContext A lambda that provides the current Android [Context] for activity-related operations.
 * @param activityResultLauncher A lambda that provides the [ManagedActivityResultLauncher] for activity results.
 * @param activityResult A lambda that provides the current [ActivityResult] for activity results.
 * @param useGoogleButtonFlow If true, uses the [Google button flow](https://developer.android.com/identity/sign-in/credential-manager-siwg#trigger-siwg). Otherwise, use the [Google sign-in request](https://developer.android.com/identity/sign-in/credential-manager-siwg#instantiate-google).
 * @param filterByAuthorizedAccounts If true, filters credentials by authorized accounts for the app.
 * @param autoSelectEnabled If true, enables automatic credential selection when possible.
 * @param maxRetries The maximum number of retries for authentication attempts.
 *
 * @author Vivien Mahe
 * @since 01/12/2024
 */
internal class PassageGoogleGatekeeperAndroid<T : EntrantInterface>(
    serverClientId: String,
    private val authPlugin: AuthPlugin<T>,
    private val applicationContext: Context,
    private val activityContext: () -> Context?,
    activityResultLauncher: () -> ManagedActivityResultLauncher<Intent, ActivityResult>?,
    activityResult: () -> ActivityResult?,
    private val useGoogleButtonFlow: Boolean,
    private val filterByAuthorizedAccounts: Boolean,
    private val autoSelectEnabled: Boolean,
    private val maxRetries: Int,
) : PassageGoogleGatekeeper<T>(serverClientId = serverClientId) {

    private val credentialManager = CredentialManager.create(applicationContext)
    private val legacyGatekeeper = PassageGoogleLegacyGatekeeperAndroid(
        serverClientId = serverClientId,
        authPlugin = authPlugin,
        activityContext = activityContext,
        activityResultLauncher = activityResultLauncher,
        activityResult = activityResult,
    )

    /**
     * Signs in a user using Google Sign-In.
     *
     * This method retrieves Google tokens using the Credential Manager API and uses them to
     * authenticate the user with Backend adapter. On success, it returns an authenticated [Entrant].
     * On failure, it logs the error and provides an appropriate exception.
     *
     * @param params Unused, as no parameters are required for Google Sign-In.
     * @return A [Result] containing the authenticated [Entrant] if successful, or an error if the process fails.
     */
    override suspend fun signIn(params: Unit): Result<T> {
        var attempts = 0
        var lastThrowable: Throwable? = null
        var useGoogleButtonFlow = useGoogleButtonFlow

        while (attempts <= maxRetries) {

            val result = retrieveGoogleTokens(useGoogleButtonFlow).fold(

                onSuccess = { tokens ->

                    when (
                        val authResult = authPlugin.signIn(AuthCredential.GoogleCredential(
                            idToken = tokens.idToken,
                            accessToken = tokens.accessToken
                        ))
                    ) {
                        is AuthResult.Success -> Result.success(authResult.data)

                        is AuthResult.Error -> {
                            lastThrowable = authResult.throwable
                            Result.failure(authPlugin.mapPluginAuthError(authResult.throwable))
                        }
                    }
                },

                onFailure = { throwable ->

                    lastThrowable = throwable

                    println("Couldn't sign in user. Attempt ${attempts + 1} of $maxRetries. Error:\n$throwable")

                    if (throwable is NoCredentialException) {
                        signOut()
                    }

                    if (throwable !is GetCredentialCancellationException) {
                        println("Attempt to sign in with Google Legacy provider.")

                        val legacyResult = legacyGatekeeper.signIn(Unit)
                        if (legacyResult.isSuccess) return legacyResult
                        else println("Legacy failed: ${legacyResult.exceptionOrNull()}")
                    }

                    useGoogleButtonFlow = useGoogleButtonFlow.not()
                    attempts++

                    if (attempts >= maxRetries) {
                        Result.failure(lastThrowable!!)
                    } else {
                        null // signal retry
                    }
                }
            )

            if (result != null) return result
        }

        return Result.failure(PassageGatekeeperUnknownEntrantException())
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
     * This method retrieves new Google tokens and uses them to re-authenticate the user with Backend adapter.
     * On success, it ensures the user's session is refreshed.
     *
     * @return A [Result] indicating the success or failure of the re-authentication process.
     */
    override suspend fun reauthenticate(): Result<Unit> {

        return retrieveGoogleTokens(useGoogleButtonFlow).fold(

            onSuccess = { tokens ->

                when (
                    val result = authPlugin.reauthenticate(AuthCredential.GoogleCredential(
                        idToken = tokens.idToken,
                        accessToken = tokens.accessToken
                    ))
                ) {
                    is AuthResult.Success -> Result.success(Unit)

                    is AuthResult.Error -> {
                        println("Reauth failed: ${result.throwable}")
                        Result.failure(authPlugin.mapPluginAuthError(result.throwable))
                    }
                }
            },

            onFailure = { throwable ->

                println("Couldn't re-authenticate user. Error:\n$throwable")

                if (throwable is NoCredentialException) {
                    signOut()
                }

                if (throwable !is GetCredentialCancellationException) {
                    println("Attempt legacy re-auth.")

                    val legacyResult = legacyGatekeeper.reauthenticate()
                    if (legacyResult.isSuccess) return legacyResult
                    else println("Legacy failed: ${legacyResult.exceptionOrNull()}")
                }

                Result.failure(throwable)
            }
        )
    }

    private suspend fun retrieveGoogleTokens(useGoogleButtonFlow: Boolean): Result<GoogleTokens> = suspendCatching {
        when (val credential = createCredentials(useGoogleButtonFlow = useGoogleButtonFlow)) {
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

    private suspend fun createCredentials(useGoogleButtonFlow: Boolean): Credential {
        activityContext.invoke() ?: throw PassageActivityContextNotInitializedException()

        val credentialOption = when (useGoogleButtonFlow) {
            // https://developer.android.com/identity/sign-in/credential-manager-siwg#create-sign
            true -> GetSignInWithGoogleOption.Builder(serverClientId).build()

            // https://developer.android.com/identity/sign-in/credential-manager-siwg#instantiate-google
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