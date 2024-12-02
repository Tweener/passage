package com.tweener.passage.gatekeeper.google

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.tweener.common._internal.thread.suspendCatching
import com.tweener.passage.error.PassageGatekeeperUnknownAdmitteeException
import com.tweener.passage.gatekeeper.google.error.PassageGoogleGatekeeperUnknownCredentialException
import com.tweener.passage.gatekeeper.google.model.GoogleTokens
import com.tweener.passage.mapper.toAdmittee
import com.tweener.passage.model.Admittee
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.GoogleAuthProvider
import io.github.aakira.napier.Napier

/**
 * Handles Google Sign-In on Android.
 *
 * This class provides functionality for signing in users using their Google account on an Android device.
 * It utilizes the CredentialManager API to manage credentials and handle the sign-in process.
 *
 * @param serverClientId The server client ID for authenticating with Google.
 * @param context The context of the calling activity or application.
 * @param filterByAuthorizedAccounts Flag indicating whether to filter by authorized accounts.
 *
 * @author Vivien Mahe
 * @since 01/12/2024
 */
internal class PassageGoogleGatekeeperAndroid(
    private val firebaseAuth: FirebaseAuth,
    serverClientId: String,
    private val context: Context,
    private val filterByAuthorizedAccounts: Boolean = false,
    private val autoSelectEnabled: Boolean = true,
    private val maxRetries: Int = 3,
) : PassageGoogleGatekeeper(serverClientId = serverClientId) {

    private val credentialManager = CredentialManager.create(context)
    private var retryAttempts = 0

    override suspend fun signIn(params: Unit): Result<Admittee> = suspendCatching {
        retrieveGoogleTokens(credential = createCredentials()).fold(
            onSuccess = { googleTokens ->
                retryAttempts = 0

                val firebaseCredential = GoogleAuthProvider.credential(idToken = googleTokens.idToken, accessToken = googleTokens.accessToken)
                firebaseAuth.signInWithCredential(authCredential = firebaseCredential).user?.toAdmittee()
                    ?: throw PassageGatekeeperUnknownAdmitteeException()
            },
            onFailure = { throwable -> throw throwable },
        )
    }.onFailure { throwable ->
        Napier.e(throwable) { "Couldn't sign in the user." }

//        when (throwable) {
//            is NoCredentialException -> {
//                onSignOut()
//
//                if (retryAttempts < maxRetries) {
//                    signIn()
//                    retryAttempts++
//                }
//            }
//        }

        // TODO Handle NoCredentialException: attempt another sign in, or clear credential (in case the user changed its password), sign out then sign in again, etc.
        // https://developer.android.com/identity/sign-in/credential-manager#handle-exceptions
    }

    override suspend fun signOut() {
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
    }

    override suspend fun reauthenticate(): Result<Unit> = suspendCatching {
        retrieveGoogleTokens(createCredentials()).fold(
            onSuccess = { googleTokens ->
                retryAttempts = 0

                val firebaseCredential = GoogleAuthProvider.credential(idToken = googleTokens.idToken, accessToken = googleTokens.accessToken)
                firebaseAuth.currentUser?.reauthenticate(credential = firebaseCredential)
                    ?: throw PassageGatekeeperUnknownAdmitteeException()
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
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(filterByAuthorizedAccounts)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(autoSelectEnabled)
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        return credentialManager.getCredential(request = request, context = context).credential
    }
}
