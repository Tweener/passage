package com.tweener.passage.gatekeeper

import com.tweener.passage.model.Entrant

/**
 * Handles Firebase Authentication.
 *
 * This abstract class defines the common interface and shared functionality for platform-specific authentication implementations.
 *
 * @param SignInParams The type of the parameters required for the sign-in process.
 * @param passageService The service for handling authentication with Passage.
 *
 * @author Vivien Mahe
 * @since 30/11/2024
 */
internal abstract class PassageGatekeeper<SignInParams> {
    /**
     * Initiate the sign-in process.
     *
     * @param params The parameters required for the sign-in process.
     */
    abstract suspend fun signIn(params: SignInParams): Result<Entrant>

    /**
     * Signs out the current user.
     */
    abstract suspend fun signOut()
}
