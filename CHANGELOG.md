
# Changelog

## [1.3.4] - April 15, 2025
- Update Kotlin to 2.1.20.

## [1.3.3] - February 28, 2025
- Remove Napier library from Passage and use basic `println` to display log messages.

## [1.3.2] - February 27, 2025
- **[FIX]** 🤖 Fixed sign-out issue on Android with the Google Legacy Gatekeeper when the binding activity was null.

## [1.3.1] - February 24, 2025
- 🤖 Add a thrid way to authenticate with Google on Android: Sign-In with Google UI button flow.

## [1.3.0] - February 18, 2025
- 🤖 Enhanced Google Authentication on Android: When authentication with the Google Gatekeeper fails, the library now automatically falls back to Google Legacy Sign-In.

## [1.2.0] - February 12, 2025
- **[BREAKING]** 🤖 `PassageAndroid` parameter `context` has been renamed `applicationContext` to avoid confusion with the Activity-based context required for the Google Sign-In UI: `PassageAndroid(applicationContext: Context)`.
- 🤖 When using the Google gatekeeper on Android, you must now call [`bindToView()`](https://github.com/Tweener/passage/blob/main/passage/src/commonMain/kotlin/com/tweener/passage/Passage.kt#L107) in Passage before performing any authentication operations. This ensures that Passage can access the Activity-based context needed to display the Google Sign-In UI.
- Remove some GitLive Firebase dependencies that were not used.

## [1.1.2] - February 11, 2025
- **[FIX]** 🤖 On Android, use Activity-based context instead of Application-based context to prevent a crash on some Android devices.
- **[FIX]** 🤖 On Android, the sign-in retry mechanism now works as expected.
- Update Kotlin to 2.1.10.

## [1.1.1] - January 24, 2025
- `Passage` now provides two separate methods to check if the user is logged in:
  - [`isUserLoggedIn`](https://github.com/Tweener/passage/blob/main/passage/src/commonMain/kotlin/com/tweener/passage/Passage.kt#L128): Returns instantly.
  - [`isUserLoggedInAsFlow`](https://github.com/Tweener/passage/blob/main/passage/src/commonMain/kotlin/com/tweener/passage/Passage.kt#L136): Allows observing changes over time.
- Update Kotlin to 2.1.0.
- Update Compose Multiplatform to 1.7.3.
- Update Gradle to 8.11.1.

## [1.1.0] - December 16, 2024
- Added new method `Passage#confirmResetPassword(params: PassageResetPasswordParams)` to confirm the reset password and set the Entrant account's new password.
- **[BREAKING]** Method `Passage#handlePasswordResetCode(oobCode: String)` now returns `Result<EmailAddress>`, instead of `Result<Unit>`, where `EmailAddress` is a the Entrant account's email address.
- **[BREAKING]** `PassageUniversalLink.mode` property is now an enum class `PassageUniversalLinkMode` instead of a String.

## [1.0.0] - December 4, 2024

### 🚀 Initial Release

The first official release of **Passage**, a Kotlin/Compose Multiplatform library for seamless authentication on Android and iOS.

#### Features
- **Core Functionality**:
  - Unified API for managing authentication flows across platforms.
  - Built-in support for **Google**, **Apple**, and **Email/Password** Gatekeepers.

- **Gatekeeper Configuration**:
  - Supported Gatekeepers:
    - **Google**: Includes platform-specific configurations for Android and iOS.
    - **Apple**: iOS support for Universal Links.
    - **Email/Password**: Basic email and password authentication.

- **Authentication Flow**:
  - `authenticateWithGoogle()`: Authenticate users using Google Sign-In.
  - `authenticateWithApple()`: Authenticate users via Apple Sign-In.
  - `authenticateWithEmailAndPassword()`: Authenticate users using email and password.

- **User Management**:
  - `signOut()`: Sign out the current user.
  - `reauthenticate()`: Reauthenticate the current user.

- **Link Handling**:
  - `handleLink(url: String)`: Process Universal Links or App Links for deep linking.
  - `onLinkHandled()`: Notify Passage when a link is handled.

#### Additional Features
- **Firebase Integration**:
  - Reuse an existing Firebase instance or initialize a new one.
  - Support for authentication-related email actions like password resets and email verification.

- **Compose Multiplatform Compatibility**:
  - Seamless integration with Compose for Multiplatform.
