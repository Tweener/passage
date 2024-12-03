package com.tweener.passage.gatekeeper.apple

import com.tweener.common._internal.Platform
import com.tweener.common._internal.thread.suspendCatching
import com.tweener.passage.error.PassageGatekeeperNotImplementedException
import com.tweener.passage.model.Entrant

/**
 * An Android-specific implementation of the [PassageAppleGatekeeper].
 *
 * This class provides a placeholder implementation for Apple Sign-In on the Android platform.
 * As Apple Sign-In is not yet implemented on Android, both the `signIn` and `signOut` methods
 * throw a [PassageGatekeeperNotImplementedException] to indicate that this functionality is unavailable.
 *
 * Responsibilities:
 * - Throws exceptions for both `signIn` and `signOut` to indicate lack of support on Android.
 *
 * @see PassageAppleGatekeeper
 * @see PassageGatekeeperNotImplementedException
 *
 * @author Vivien Mahe
 * @since 30/11/2024
 */
internal class PassageAppleGatekeeperAndroid : PassageAppleGatekeeper() {

    /**
     * Attempts to authenticate a user using Apple Sign-In on Android.
     *
     * As Apple Sign-In is not eyt implemented on Android, this method always throws
     * a [PassageGatekeeperNotImplementedException].
     *
     * @param params The parameters for signing in (unused, as Apple Sign-In is not implemented).
     * @return A [Result] containing the exception indicating not implemented functionality.
     * @throws PassageGatekeeperNotImplementedException Always thrown to indicate lack of support.
     */
    override suspend fun signIn(params: Unit): Result<Entrant> = suspendCatching {
        throw PassageGatekeeperNotImplementedException(gatekeeper = "Apple", platform = Platform.ANDROID)
    }


    /**
     * Attempts to sign out a user authenticated with Apple Sign-In on Android.
     *
     * As Apple Sign-In is not yet implemented on Android, this method always throws
     * a [PassageGatekeeperNotImplementedException].
     *
     * @throws PassageGatekeeperNotImplementedException Always thrown to indicate lack of support.
     */
    override suspend fun signOut() {
        throw PassageGatekeeperNotImplementedException(gatekeeper = "Apple", platform = Platform.ANDROID)
    }
}
