package com.tweener.passage.gatekeeper

import com.tweener.passage.model.Entrant

/**
 * An abstract representation of a Gatekeeper in the Passage library.
 *
 * A **Gatekeeper** is responsible for authenticating and managing users (Entrants) for a specific authentication provider,
 * such as Google, Apple, or Email/Password. It acts as the bridge between the user (Entrant) and the Passage, verifying
 * the user's identity and granting or revoking access.
 *
 * @param SignInParams The type of parameters required for the Gatekeeper's sign-in process.
 *
 * @author Vivien Mahe
 * @since 30/11/2024
 */
internal abstract class PassageGatekeeper<SignInParams> {

    /**
     * Authenticates an Entrant using the specified parameters.
     *
     * @param params The parameters required for signing in with this Gatekeeper (e.g., credentials or tokens).
     * @return A [Result] containing the authenticated [Entrant] if successful, or an error if the sign-in fails.
     */
    abstract suspend fun signIn(params: SignInParams): Result<Entrant>

    /**
     * Signs out the currently authenticated Entrant for this Gatekeeper.
     *
     * This revokes the user's session or token for the specific authentication provider managed by the Gatekeeper.
     */
    abstract suspend fun signOut()
}
