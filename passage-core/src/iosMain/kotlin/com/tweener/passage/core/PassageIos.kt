package com.tweener.passage.core

import androidx.compose.runtime.Composable
import com.tweener.passage.core.authplugin.AuthPlugin
import com.tweener.passage.core.gatekeeper.apple.PassageAppleGatekeeper
import com.tweener.passage.core.gatekeeper.apple.PassageAppleGatekeeperIos
import com.tweener.passage.core.gatekeeper.google.PassageGoogleGatekeeper
import com.tweener.passage.core.gatekeeper.google.PassageGoogleGatekeeperIos
import com.tweener.passage.core.model.AppleGatekeeperConfiguration
import com.tweener.passage.core.model.EntrantInterface
import com.tweener.passage.core.model.GoogleGatekeeperConfiguration

/**
 * An iOS-specific implementation of the [Passage].
 *
 * This class provides platform-specific configurations and implementations for authentication on iOS.
 * It initializes and creates iOS-specific Gatekeepers for Google and Apple authentication,
 * leveraging platform APIs and SDKs to manage user authentication.
 *
 * Responsibilities:
 * - Creating iOS-specific Gatekeepers for Google and Apple authentication.
 *
 * @see Passage
 *
 * @author Vivien Mahe
 * @since 02/12/2024
 */
class PassageIos<T : EntrantInterface> : Passage<T>() {

    @Composable
    override fun bindToView() {
        // Nothing to do here
    }

    // region Google gatekeeper

    /**
     * Creates a Google Gatekeeper specifically for the iOS platform.
     *
     * This method uses the provided configuration and Backend adapter instance to create
     * an instance of [PassageGoogleGatekeeperIos], which handles Google Sign-In
     * operations on iOS using the Google Identity SDK.
     *
     * @param configuration The configuration for the Google Gatekeeper.
     * @param authPlugin The Backend authentication adapter instance used for user management.
     * @return An instance of [PassageGoogleGatekeeperIos].
     */
    override fun createGoogleGatekeeper(configuration: GoogleGatekeeperConfiguration, authPlugin: AuthPlugin<T>): PassageGoogleGatekeeper<T> =
        PassageGoogleGatekeeperIos(
            authPlugin = authPlugin,
            serverClientId = configuration.serverClientId,
        )

    // endregion Google gatekeeper

    // region Apple gatekeeper

    /**
     * Creates an Apple Gatekeeper specifically for the iOS platform.
     *
     * This method creates an instance of [PassageAppleGatekeeperIos], which manages
     * Apple Sign-In operations on iOS using the `ASAuthorizationAppleIDProvider`.
     *
     * @param configuration The configuration for the Apple Gatekeeper.
     * @param authPlugin The Backend authentication adapter instance used for user management.
     * @return An instance of [PassageAppleGatekeeperIos].
     */
    override fun createAppleGatekeeper(configuration: AppleGatekeeperConfiguration, authPlugin: AuthPlugin<T>): PassageAppleGatekeeper<T> =
        PassageAppleGatekeeperIos(authPlugin = authPlugin)

    // endregion Apple gatekeeper

}