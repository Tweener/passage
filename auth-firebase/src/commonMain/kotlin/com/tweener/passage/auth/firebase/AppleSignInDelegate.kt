package com.tweener.passage.auth.firebase

import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.AuthResult
import com.tweener.passage.core.model.EntrantInterface
import dev.gitlive.firebase.auth.FirebaseAuth

/**
 * Project       : Passage
 * Author        : Chirag Redij
 * Created on    : Tuesday, 31/03/26 at 23:08
 * -------------------------------------------------------------------------------------
 * Last updated  : chiragredij on Tuesday, 31/03/26 at 23:08
 *
 * Description   : [Add a brief description of this file or component]
 *
 * Copyright (c) 2026 ChiragRedij. All rights reserved.
 */
interface AppleSignInDelegate<T : EntrantInterface> {
    suspend fun signInWithApple(
        credential: AuthCredential.AppleCredential
    ): AuthResult<T>
}

expect fun <T : EntrantInterface> provideAppleSignInDelegate(
    firebaseAuth: FirebaseAuth,
    mapper: FirebaseUserMapper<T>
): AppleSignInDelegate<T>