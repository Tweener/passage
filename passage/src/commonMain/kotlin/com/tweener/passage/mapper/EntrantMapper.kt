package com.tweener.passage.mapper

import com.tweener.passage.model.Entrant
import dev.gitlive.firebase.auth.FirebaseUser

/**
 * @author Vivien Mahe
 * @since 30/11/2024
 */

internal fun FirebaseUser.toEntrant(): Entrant =
    Entrant(
        uid = uid,
        email = email,
        displayName = displayName ?: providerData.map { it.displayName }.firstOrNull { it != null },
        phoneNumber = phoneNumber ?: providerData.map { it.phoneNumber }.firstOrNull { it != null },
        photoUrl = photoURL ?: providerData.map { it.photoURL }.firstOrNull { it != null },
        isAnonymous = isAnonymous,
        isEmailVerified = isEmailVerified,
    )
