package com.tweener.passage

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.tweener.passage.gatekeeper.apple.PassageAppleGatekeeper
import com.tweener.passage.gatekeeper.apple.PassageAppleGatekeeperAndroid
import com.tweener.passage.gatekeeper.google.PassageGoogleGatekeeper
import com.tweener.passage.gatekeeper.google.PassageGoogleGatekeeperAndroid
import com.tweener.passage.model.AppleGatekeeperConfiguration
import com.tweener.passage.model.GoogleGatekeeperConfiguration
import dev.gitlive.firebase.auth.FirebaseAuth

/**
 * @author Vivien Mahe
 * @since 02/12/2024
 */

@Composable
actual fun rememberPassageService(): PassageService {
    val context = LocalContext.current
    return remember { PassageServiceAndroid(context = context) }
}

class PassageServiceAndroid(
    private val context: Context,
) : PassageService() {

    override fun initializeFirebase() {
//        Firebase.initialize(context = context)
    }

    // region Google gatekeeper

    override fun createGoogleGatekeeper(configuration: GoogleGatekeeperConfiguration, firebaseAuth: FirebaseAuth): PassageGoogleGatekeeper =
        PassageGoogleGatekeeperAndroid(
            firebaseAuth = firebaseAuth,
            context = context,
            serverClientId = configuration.serverClientId,
            filterByAuthorizedAccounts = configuration.android.filterByAuthorizedAccounts,
            autoSelectEnabled = configuration.android.autoSelectEnabled,
            maxRetries = configuration.android.maxRetries,
        )

    // endregion Google gatekeeper

    // region Apple gatekeeper

    override fun createAppleGatekeeper(configuration: AppleGatekeeperConfiguration): PassageAppleGatekeeper =
        PassageAppleGatekeeperAndroid()

    // endregion Apple gatekeeper

}
