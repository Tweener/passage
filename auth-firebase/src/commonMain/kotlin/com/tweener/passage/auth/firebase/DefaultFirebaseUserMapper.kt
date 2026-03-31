package com.tweener.passage.auth.firebase

import com.tweener.passage.core.model.DefaultEntrant
import dev.gitlive.firebase.auth.FirebaseUser

/**
 * Project       : Passage
 * Author        : Chirag Redij
 * Created on    : Tuesday, 31/03/26 at 23:43
 * -------------------------------------------------------------------------------------
 * Last updated  : chiragredij on Tuesday, 31/03/26 at 23:43
 *
 * Description   : [Add a brief description of this file or component]
 *
 * Copyright (c) 2026 ChiragRedij. All rights reserved.
 */
class DefaultFirebaseUserMapper(): FirebaseUserMapper<DefaultEntrant> {
    override fun map(firebaseUser: FirebaseUser): DefaultEntrant {
        return DefaultEntrant(
            uid = firebaseUser.uid,
            email = firebaseUser.email,
            displayName = firebaseUser.displayName,
            phoneNumber = firebaseUser.phoneNumber,
            photoUrl = firebaseUser.photoURL ?: firebaseUser.providerData.map { it.photoURL }.firstOrNull { it != null },
            isAnonymous = firebaseUser.isAnonymous,
            isEmailVerified = firebaseUser.isEmailVerified
        )
    }
}