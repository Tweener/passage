package com.tweener.passage.auth.firebase

import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.AuthResult
import com.tweener.passage.core.model.EntrantInterface
import dev.gitlive.firebase.auth.FirebaseAuth

/**
 * Platform-specific delegate for Apple Sign-In with Firebase.
 *
 * On iOS this performs the native ASAuthorization flow and signs into Firebase;
 * on Android a [com.tweener.passage.core.error.PassageGatekeeperNotImplementedException] is returned.
 *
 * @param T The domain user type, constrained to [EntrantInterface].
 *
 * @author Chirag Redij
 * @since 31/03/2026
 */
interface AppleSignInDelegate<T : EntrantInterface> {

    /**
     * Signs in a user with an Apple credential via Firebase.
     *
     * @param credential The Apple credential containing an ID token and raw nonce.
     * @return [AuthResult.Success] containing the authenticated user, or [AuthResult.Error] on failure.
     */
    suspend fun signInWithApple(
        credential: AuthCredential.AppleCredential
    ): AuthResult<T>
}

/**
 * Creates a platform-specific [AppleSignInDelegate] instance.
 *
 * @param firebaseAuth The Firebase Auth instance.
 * @param mapper The mapper used to convert Firebase users to the domain model.
 * @return A platform-specific [AppleSignInDelegate].
 */
expect fun <T : EntrantInterface> provideAppleSignInDelegate(
    firebaseAuth: FirebaseAuth,
    mapper: FirebaseUserMapper<T>
): AppleSignInDelegate<T>