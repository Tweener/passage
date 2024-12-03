package com.tweener.passage.gatekeeper.google

import com.tweener.passage.gatekeeper.PassageGatekeeper

/**
 * An abstract Gatekeeper for handling authentication with Google in the Passage library.
 *
 * This class provides the foundation for managing Google Sign-In and re-authentication operations.
 * It extends the [PassageGatekeeper] with [Unit] as the generic parameter type, as no additional
 * parameters are required for Google Sign-In by default.
 *
 * Responsibilities:
 * - Facilitating Google Sign-In to authenticate users.
 * - Supporting re-authentication for previously signed-in users.
 *
 * @param serverClientId The server client ID associated with the Google Sign-In configuration.
 *
 * @author Vivien Mahe
 * @since 01/12/2024
 */
internal abstract class PassageGoogleGatekeeper(
    protected val serverClientId: String,
) : PassageGatekeeper<Unit>() {

    /**
     * Re-authenticates a previously authenticated user using Google Sign-In.
     *
     * This method is intended to refresh the authentication session or validate the user's credentials
     * again.
     *
     * @return A [Result] indicating the success or failure of the re-authentication process.
     */
    abstract suspend fun reauthenticate(): Result<Unit>
}
