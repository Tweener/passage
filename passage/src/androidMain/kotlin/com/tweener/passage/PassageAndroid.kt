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

@Composable
actual fun rememberPassage(): Passage {
    val context = LocalContext.current
    return remember { PassageAndroid(context = context) }
}

/**
 * An Android-specific implementation of the [Passage].
 *
 * This class provides platform-specific configurations and implementations for authentication on Android.
 * It creates Android-specific Gatekeepers for Google and Apple authentication,
 * and integrates with the necessary platform APIs.
 *
 * Responsibilities:
 * - Creating Android-specific Gatekeepers for Google and Apple authentication.
 *
 * @param context The Android [Context] required for accessing platform resources.
 *
 * @author Vivien Mahe
 * @since 02/12/2024
 */
class PassageAndroid(private val context: Context) : Passage() {

    override fun initializeFirebase() {
        // Nothing to do here
    }

    // region Google gatekeeper

    /**
     * Creates a Google Gatekeeper specifically for the Android platform.
     *
     * This method uses the provided configuration and Firebase instance to create
     * an instance of [PassageGoogleGatekeeperAndroid], which handles Google Sign-In
     * operations on Android.
     *
     * @param configuration The configuration for the Google Gatekeeper.
     * @param firebaseAuth The Firebase authentication instance used for user management.
     * @return An instance of [PassageGoogleGatekeeperAndroid].
     */
    override fun createGoogleGatekeeper(configuration: GoogleGatekeeperConfiguration, firebaseAuth: FirebaseAuth): PassageGoogleGatekeeper =
        PassageGoogleGatekeeperAndroid(
            serverClientId = configuration.serverClientId,
            firebaseAuth = firebaseAuth,
            context = context,
            filterByAuthorizedAccounts = configuration.android.filterByAuthorizedAccounts,
            autoSelectEnabled = configuration.android.autoSelectEnabled,
            maxRetries = configuration.android.maxRetries,
        )

    // endregion Google gatekeeper

    // region Apple gatekeeper

    /**
     * Creates an Apple Gatekeeper specifically for the Android platform.
     *
     * As Apple Sign-In is not natively supported on Android, this method returns an
     * instance of [PassageAppleGatekeeperAndroid], which provides a placeholder
     * implementation for Apple authentication on Android.
     *
     * @param configuration The configuration for the Apple Gatekeeper.
     * @return An instance of [PassageAppleGatekeeperAndroid].
     */
    override fun createAppleGatekeeper(configuration: AppleGatekeeperConfiguration): PassageAppleGatekeeper =
        PassageAppleGatekeeperAndroid()

    // endregion Apple gatekeeper

}
