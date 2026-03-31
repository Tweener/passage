package com.tweener.passage.core.gatekeeper.google

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.tweener.kmpkit.thread.suspendCatching
import com.tweener.passage.core.authplugin.AuthPlugin
import com.tweener.passage.core.gatekeeper.google.model.GoogleTokens
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.AuthResult
import com.tweener.passage.core.model.EntrantInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield

/**
 * @author Chirag Redij
 * @since 29/03/2026
 */

internal class PassageGoogleLegacyGatekeeperAndroid<T : EntrantInterface>(
    serverClientId: String,
    private val authPlugin: AuthPlugin<T>,
    private val activityContext: () -> Context?,
    private val activityResultLauncher: () -> ManagedActivityResultLauncher<Intent, ActivityResult>?,
    private val activityResult: () -> ActivityResult?,
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
                        Result.failure(authPlugin.mapPluginAuthError(result.throwable))
                    }
                }
            },

            onFailure = { throwable ->
                Result.failure(throwable)
            }
        )
    }

    override suspend fun signOut() {
        try {
            getGoogleSignInClient().signOut()
        } catch (throwable: Throwable) {
            println("Google Legacy sign out failed with an unknown error: $throwable")
        }

        // Optional: also call backend
        when (val result = authPlugin.signOut()) {
            is AuthResult.Error -> {
                println("Backend signOut failed: ${result.throwable}")
            }
            else -> Unit
        }
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
                        Result.failure(authPlugin.mapPluginAuthError(result.throwable))
                    }
                }
            },

            onFailure = { throwable ->
                Result.failure(throwable)
            }
        )
    }

    private suspend fun retrieveGoogleTokens(): Result<GoogleTokens> = suspendCatching {
        try {
            activityResultLauncher.invoke()?.launch(getGoogleSignInClient().signInIntent)

            withContext(Dispatchers.Default) {
                while (activityResult.invoke() == null) yield()
            }

            val data: Intent? = activityResult.invoke()!!.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)

            account.idToken
                ?.let { GoogleTokens(idToken = it) }
                ?: throw Exception("Google idToken is null while signing in with Google Legacy provider.")

        } catch (throwable: Throwable) {
            when (throwable) {
                is ApiException -> println("Google Legacy sign in failed with ApiException: ${throwable.statusCode}")
                else -> println("Google Legacy sign in failed with an unknown error: $throwable")
            }
            throw throwable
        }
    }

    private fun getGoogleSignInClient(): GoogleSignInClient {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(serverClientId)
            .requestEmail()
            .build()

        return GoogleSignIn.getClient(activityContext.invoke()!!, googleSignInOptions)
    }
}