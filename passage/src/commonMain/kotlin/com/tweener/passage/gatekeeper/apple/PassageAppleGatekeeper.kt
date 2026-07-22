package com.tweener.passage.gatekeeper.apple

import com.tweener.passage.gatekeeper.PassageGatekeeper
import com.tweener.passage.model.PassageAppleCredential

/**
 * A Gatekeeper for handling authentication with Apple Sign-In in the Passage library.
 *
 * This class extends the [PassageGatekeeper] and provides a specific implementation
 * for managing authentication using Apple's identity provider. As Apple Sign-In does not
 * require additional parameters for the sign-in process, the generic type [SignInParams]
 * is set to [Unit].
 *
 * Responsibilities include:
 * - Authenticating users via Apple Sign-In.
 * - Managing user sessions specific to Apple authentication.
 *
 * @author Vivien Mahe
 * @since 30/11/2024
 */
internal abstract class PassageAppleGatekeeper : PassageGatekeeper<Unit>() {

    /**
     * Runs the native Apple Sign-In UI and returns the raw [PassageAppleCredential], without signing into Firebase.
     *
     * @return A [Result] with the raw [PassageAppleCredential], or an error if it fails.
     */
    abstract suspend fun retrieveCredential(): Result<PassageAppleCredential>
}
