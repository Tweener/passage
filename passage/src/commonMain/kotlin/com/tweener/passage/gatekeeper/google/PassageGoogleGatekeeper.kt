package com.tweener.passage.gatekeeper.google

import com.tweener.passage.gatekeeper.PassageGatekeeper

/**
 * FirebaseGoogleAuthProvider class for handling Google Sign-In.
 *
 * This class extends the `FirebaseAuthProvider` to provide functionality for signing in users using their Google account.
 *
 * @param passageService The data source for Firebase authentication.
 * @param serverClientId The server client ID for authenticating with Google.
 *
 * @author Vivien Mahe
 * @since 01/12/2024
 */
internal abstract class PassageGoogleGatekeeper(
    protected val serverClientId: String,
) : PassageGatekeeper<Unit>() {

    abstract suspend fun reauthenticate(): Result<Unit>
}
