package com.tweener.passage.core

import android.content.Context
import android.content.Intent
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.tweener.passage.core.authplugin.AuthPlugin
import com.tweener.passage.core.gatekeeper.apple.PassageAppleGatekeeper
import com.tweener.passage.core.gatekeeper.apple.PassageAppleGatekeeperAndroid
import com.tweener.passage.core.gatekeeper.google.PassageGoogleGatekeeper
import com.tweener.passage.core.gatekeeper.google.PassageGoogleGatekeeperAndroid
import com.tweener.passage.core.model.AppleGatekeeperConfiguration
import com.tweener.passage.core.model.EntrantInterface
import com.tweener.passage.core.model.GoogleGatekeeperConfiguration

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
 * @param applicationContext The Android [Context] required for accessing platform resources.
 *
 * @author Chirag Redij
 * @since 29/03/2026
 */
class PassageAndroid<T : EntrantInterface>(private val applicationContext: Context) : Passage<T>() {

    private var activityContext: Context? = null
    private var activityResultLauncher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null
    private var activityResult: ActivityResult? = null

    @Composable
    override fun bindToView() {
        activityContext = LocalContext.current

        activityResultLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            activityResult = result
        }
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
    override fun createGoogleGatekeeper(configuration: GoogleGatekeeperConfiguration, authPlugin: AuthPlugin<T>): PassageGoogleGatekeeper<T> =
        PassageGoogleGatekeeperAndroid(
            serverClientId = configuration.serverClientId,
            authPlugin = authPlugin,
            applicationContext = applicationContext,
            activityContext = { activityContext },
            activityResultLauncher = { activityResultLauncher },
            activityResult = { activityResult },
            useGoogleButtonFlow = configuration.android.useGoogleButtonFlow,
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
    override fun createAppleGatekeeper(configuration: AppleGatekeeperConfiguration, authPlugin: AuthPlugin<T>): PassageAppleGatekeeper<T> =
        PassageAppleGatekeeperAndroid()

    // endregion Apple gatekeeper

}