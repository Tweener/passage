package com.tweener.passage.auth.firebase

import com.tweener.passage.core.model.DefaultEntrant
import dev.gitlive.firebase.auth.FirebaseUser

/**
 * Default [FirebaseUserMapper] implementation that maps [FirebaseUser] to [DefaultEntrant].
 *
 * If the user's primary `photoURL` is not set, this mapper falls back to the first
 * non-null photo URL from the user's linked provider data.
 *
 * @author Chirag Redij
 * @since 31/03/2026
 */
class DefaultFirebaseUserMapper : FirebaseUserMapper<DefaultEntrant> {
    override fun map(firebaseUser: FirebaseUser): DefaultEntrant {
        return DefaultEntrant(
            uid = firebaseUser.uid,
            email = firebaseUser.email,
            displayName = firebaseUser.displayName,
            phoneNumber = firebaseUser.phoneNumber,
            photoUrl = firebaseUser.photoURL ?: firebaseUser.providerData.firstNotNullOfOrNull { it.photoURL },
            isAnonymous = firebaseUser.isAnonymous,
            isEmailVerified = firebaseUser.isEmailVerified
        )
    }
}