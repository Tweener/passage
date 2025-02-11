package com.tweener.passage

import androidx.compose.runtime.Composable
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
 * An iOS-specific implementation of the [Passage].
 *
 * This class provides platform-specific configurations and implementations for authentication on iOS.
 * It initializes Firebase and creates iOS-specific Gatekeepers for Google and Apple authentication,
 * leveraging platform APIs and SDKs to manage user authentication.
 *
 * Responsibilities:
 * - Initializing Firebase for iOS.
 * - Creating iOS-specific Gatekeepers for Google and Apple authentication.
 *
 * @see Passage
 *
 * @author Vivien Mahe
 * @since 02/12/2024
 */
class PassageIos : Passage() {

    @Composable
    override fun bindToView() {
        // Nothing to do here
    }

    /**
     * Initializes Firebase for the iOS platform.
     *
     * This method sets up Firebase for use on iOS. Ensure that the Firebase iOS SDK
     * is properly configured in your project.
     */
    override fun initializeFirebase() {
        Firebase.initialize()
    }

    // region Google gatekeeper

    /**
     * Creates a Google Gatekeeper specifically for the iOS platform.
     *
     * This method uses the provided configuration and Firebase instance to create
     * an instance of [PassageGoogleGatekeeperIos], which handles Google Sign-In
     * operations on iOS using the Google Identity SDK.
     *
     * @param configuration The configuration for the Google Gatekeeper.
     * @param firebaseAuth The Firebase authentication instance used for user management.
     * @return An instance of [PassageGoogleGatekeeperIos].
     */
    override fun createGoogleGatekeeper(configuration: GoogleGatekeeperConfiguration, firebaseAuth: FirebaseAuth): PassageGoogleGatekeeper =
        PassageGoogleGatekeeperIos(
            firebaseAuth = firebaseAuth,
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
     * @return An instance of [PassageAppleGatekeeperIos].
     */
    override fun createAppleGatekeeper(configuration: AppleGatekeeperConfiguration): PassageAppleGatekeeper =
        PassageAppleGatekeeperIos(firebaseAuth = firebaseAuth)

    // endregion Apple gatekeeper

}
