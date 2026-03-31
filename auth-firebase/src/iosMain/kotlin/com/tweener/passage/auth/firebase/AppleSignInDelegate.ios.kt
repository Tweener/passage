package com.tweener.passage.auth.firebase

import cocoapods.FirebaseAuth.FIRAuth
import cocoapods.FirebaseAuth.FIROAuthProvider
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.AuthResult
import com.tweener.passage.core.model.EntrantInterface
import dev.gitlive.firebase.auth.FirebaseAuth
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSPersonNameComponents
import kotlin.coroutines.resume

actual fun <T : EntrantInterface> provideAppleSignInDelegate(
    firebaseAuth: FirebaseAuth,
    mapper: FirebaseUserMapper<T>
): AppleSignInDelegate<T> {
    return AppleSignInDelegateImpl(firebaseAuth, mapper)
}

@OptIn(ExperimentalForeignApi::class)
class AppleSignInDelegateImpl<T : EntrantInterface>(
    private val firebaseAuth: FirebaseAuth,
    private val mapper: FirebaseUserMapper<T>
) : AppleSignInDelegate<T> {

    override suspend fun signInWithApple(
        credential: AuthCredential.AppleCredential
    ): AuthResult<T> = suspendCancellableCoroutine { continuation ->

        val firebaseCredential = FIROAuthProvider.appleCredentialWithIDToken(
            idToken = credential.idToken,
            rawNonce = credential.rawNonce,
            fullName = credential.fullName as? NSPersonNameComponents
        )

        FIRAuth.auth().signInWithCredential(firebaseCredential) { _, error ->

            when {
                error != null -> {

                    val throwable = Exception(
                        buildString {
                            append(error.localizedDescription)
                            append(" (code: ${error.code})")
                            error.domain?.let { append(" domain: $it") }
                        }
                    )

                    continuation.resume(AuthResult.Error(throwable))
                }

                firebaseAuth.currentUser != null -> {
                    val mapped = mapper.map(firebaseAuth.currentUser!!)
                    continuation.resume(AuthResult.Success(mapped))
                }

                else -> {
                    continuation.resume(
                        AuthResult.Error(
                            Exception("User is null after Apple sign-in")
                        )
                    )
                }
            }
        }
    }
}