package com.tweener.passage.auth.firebase

import com.tweener.passage.core.model.EntrantInterface
import dev.gitlive.firebase.auth.FirebaseUser

/**
 * Maps a Firebase [FirebaseUser] to the domain user model [T].
 *
 * Implement this interface to provide a custom mapping from Firebase's user representation
 * to your application's [EntrantInterface] implementation.
 *
 * @param T The domain user type, constrained to [EntrantInterface].
 *
 * @see DefaultFirebaseUserMapper
 *
 * @author Chirag Redij
 * @since 31/03/2026
 */
interface FirebaseUserMapper<T : EntrantInterface> {

    /**
     * Converts a [FirebaseUser] into the domain model [T].
     *
     * @param firebaseUser The Firebase user to map.
     * @return The mapped domain user.
     */
    fun map(firebaseUser: FirebaseUser): T
}