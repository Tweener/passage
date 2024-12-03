package com.tweener.passage.gatekeeper.apple

import com.tweener.common._internal.Platform
import com.tweener.common._internal.thread.suspendCatching
import com.tweener.passage.error.PassageGatekeeperNotImplementedException
import com.tweener.passage.model.Entrant
import com.tweener.passage.model.GatekeeperType

/**
 * An Android-specific implementation of the [PassageAppleGatekeeper].
 *
 * This class provides a placeholder implementation for Apple Sign-In on the Android platform.
 * As Apple Sign-In is not yet implemented on Android, the `signIn` method throws a
 * [PassageGatekeeperNotImplementedException] to indicate the lack of support.
 *
 * Responsibilities:
 * - Indicating that Apple Sign-In is unsupported on Android.
 * - Providing a no-op implementation for the `signOut` method.
 *
 * @author Vivien Mahe
 * @since 30/11/2024
 */
internal class PassageAppleGatekeeperAndroid : PassageAppleGatekeeper() {

    /**
     * Attempts to authenticate a user using Apple Sign-In on Android.
     *
     * As Apple Sign-In is not yet implemented on Android, this method always throws
     * a [PassageGatekeeperNotImplementedException].
     *
     * @param params The parameters for signing in (unused, as Apple Sign-In is not implemented).
     * @return A [Result] containing the exception indicating not implemented functionality.
     * @throws PassageGatekeeperNotImplementedException Always thrown to indicate lack of support.
     */
    override suspend fun signIn(params: Unit): Result<Entrant> = suspendCatching {
        throw PassageGatekeeperNotImplementedException(gatekeeper = GatekeeperType.APPLE, platform = Platform.ANDROID)
    }

    /**
     * Signs out the current user for Apple Sign-In on Android.
     *
     * As no session is managed for Apple Sign-In on Android, this method performs no actions.
     */
    override suspend fun signOut() {
        // Nothing to do here
    }
}
