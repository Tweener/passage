package com.tweener.passage.auth.firebase

import com.tweener.kmpkit.Platform
import com.tweener.passage.core.error.PassageGatekeeperNotImplementedException
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.AuthResult
import com.tweener.passage.core.model.EntrantInterface
import com.tweener.passage.core.model.GatekeeperType
import dev.gitlive.firebase.auth.FirebaseAuth

actual fun <T : EntrantInterface> provideAppleSignInDelegate(
    firebaseAuth: FirebaseAuth,
    mapper: FirebaseUserMapper<T>
): AppleSignInDelegate<T> {

    return object : AppleSignInDelegate<T> {
        override suspend fun signInWithApple(
            credential: AuthCredential.AppleCredential
        ): AuthResult<T> {
            return AuthResult.Error(
                PassageGatekeeperNotImplementedException(
                    gatekeeper = GatekeeperType.APPLE,
                    platform = Platform.ANDROID
                )
            )
        }
    }

}