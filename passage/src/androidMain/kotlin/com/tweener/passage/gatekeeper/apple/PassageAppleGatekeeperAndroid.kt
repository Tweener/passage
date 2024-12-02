package com.tweener.passage.gatekeeper.apple

import com.tweener.common._internal.Platform
import com.tweener.common._internal.thread.suspendCatching
import com.tweener.passage.error.PassageGatekeeperNotImplementedException
import com.tweener.passage.model.Admittee

/**
 * @author Vivien Mahe
 * @since 30/11/2024
 */
internal class PassageAppleGatekeeperAndroid : PassageAppleGatekeeper() {

    override suspend fun signIn(params: Unit): Result<Admittee> = suspendCatching {
        throw PassageGatekeeperNotImplementedException(gatekeeper = "Apple", platform = Platform.ANDROID)
    }

    override suspend fun signOut() {
        // Nothing to do here
    }
}
