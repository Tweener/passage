package com.tweener.passage

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.tweener.passage.gatekeeper.apple.PassageAppleGatekeeper
import com.tweener.passage.gatekeeper.apple.PassageAppleGatekeeperIos
import com.tweener.passage.gatekeeper.google.PassageGoogleGatekeeper
import com.tweener.passage.gatekeeper.google.PassageGoogleGatekeeperIos
import com.tweener.passage.model.AppleGatekeeperConfiguration
import com.tweener.passage.model.GoogleGatekeeperConfiguration
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.initialize

/**
 * @author Vivien Mahe
 * @since 02/12/2024
 */

@Composable
actual fun rememberPassageService(): PassageService {
    return remember { PassageServiceIos() }
}

class PassageServiceIos : PassageService() {

    override fun initializeFirebase() {
        Firebase.initialize()
    }

    // region Google gatekeeper

    override fun createGoogleGatekeeper(configuration: GoogleGatekeeperConfiguration, firebaseAuth: FirebaseAuth): PassageGoogleGatekeeper =
        PassageGoogleGatekeeperIos(
            firebaseAuth = firebaseAuth,
            serverClientId = configuration.serverClientId,
        )

    // endregion Google gatekeeper

    // region Apple gatekeeper

    override fun createAppleGatekeeper(configuration: AppleGatekeeperConfiguration): PassageAppleGatekeeper =
        PassageAppleGatekeeperIos(firebaseAuth = firebaseAuth)

    // endregion Apple gatekeeper

}
