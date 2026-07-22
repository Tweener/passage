
# Changelog

## [1.7.0] - July 22nd, 2026
- ✨ **`[FEATURE]`**: Add `retrieveGoogleCredential()` and `retrieveAppleCredential()` methods in `Passage` to retrieve the raw Google/Apple provider credential (ID token + profile) **without** signing the user into Firebase, for apps authenticating against a first-party backend.
- 📦 **`[BUILD]`**: 🤖 Migrate Android modules to the AGP 9 Kotlin Multiplatform library plugin.
- 📦 **`[BUILD]`**: Bump Android compile SDK to 37.
- 🔄 Update Kotlin to 2.4.10.
- 🔄 Update Gradle to 9.1.1.
- 🔄 Update Compose Multiplatform to 1.11.1.
- 🔄 Update Maven Publish to 0.37.0.
- 🔄 Update Firebase GitLive to 2.5.0.
- 🔄 Update KMPKit to 1.0.14.
- 🔄 Update Android Activity to 1.13.0.
- 🔄 Update Play Services to 21.6.0.
- 🔄 Update AndroidX Credentials to 1.6.0.

## [1.6.0] - January 23rd, 2026
- ✨ **`[FEATURE]`**: Add `getIdToken()` method in `Passage` to retrieve the current user's Firebase ID token, with an option to force refresh.
- 🔄 Update Kotlin to 2.3.0.
- 🔄 Update Gradle to 8.13.2.
- 🔄 Update Compose Multiplatform to 1.10.0.
- 🔄 Update Maven Publish to 0.36.0.
- 🔄 Update Android Activity to 1.12.2.
- 🔄 Update Play Services to 21.5.0.
- 🔄 Update Google ID Identity to 1.2.0.

## [1.5.1] - November 3rd, 2025
- 📦 **`[BUILD]`**: Bump Android compile SDK to 36.
- 🔄 Update Kotlin to 2.2.21.
- 🔄 Update Gradle to 8.13.0.
- 🔄 Update Compose Multiplatform to 1.9.1.
- 🔄 Update Material3 to 1.9.0.
- 🔄 Update Firebase GitLive to 2.4.0.
- 🔄 Update KMPKit to 1.0.13.
- 🔄 Update Dokka to 2.1.0.
- 🔄 Update various Android dependencies (AndroidX Core, Activity, etc.).

## [1.5.0] - August 20th, 2025
- 🚨 **`[BREAKING]`**: **Complete elimination of Firebase Dynamic Links**
> [!WARNING]  
> Starting **August 25, 2025**, email actions will no longer work due to the [shutdown of Firebase Dynamic Links](https://firebase.google.com/support/dynamic-links-faq).  
>   
> I decided to drop support of these features for two main reasons:  
> - I attempted to follow the [migration guide](https://firebase.google.com/docs/auth/android/email-link-migration) to move from Dynamic Links to Firebase Hosting, but had no success.  
> - Firebase recently [updated the free plan limit](https://firebase.google.com/docs/auth/limits#email_sending_limits) for email link sign-in emails to only **5 per day**, which makes development both harder and more expensive.  
>   
> If you discover a reliable solution, contributions via PR are very welcome!

- 🔄 Update GitLiveApp to 2.3.0.

## [1.4.2] - August 1st, 2025
- 🚨 **`[BREAKING]`**: The property `useSignInWithGoogle` from the class `GoogleGatekeeperAndroidConfiguration` has been renamed to `useGoogleButtonFlow` to use a more descriptive naming.

When using `useGoogleButtonFlow = true`, Passage uses the [Google button flow](https://developer.android.com/identity/sign-in/credential-manager-siwg#trigger-siwg). Otherwise, use the [Google sign-in request](https://developer.android.com/identity/sign-in/credential-manager-siwg#instantiate-google).
- 🔄 Update Gradle to 8.11.0.
- 🔄 Update KMPKit to 1.0.10.
 
## [1.4.1] - July 8th, 2025
- 🔄 Update Kotlin to 2.2.0.
- 🔄 Update Compose Multiplatform to 1.8.2.
- 🔄 Update Gradle to 8.11.0.
- 🔄 Update KMPKit to 1.0.9.
 
## [1.4.0] - May 14, 2025
- ✨ **`[FEATURE]`**: Add email link sign-in feature. (https://github.com/Tweener/passage/issues/1)
- 🛠 **`[IMPROVMENT]`**: Migrate publishing from Sonatype to Maven Central.

## [1.3.5] - May 13, 2025
- 🔄 Update Kotlin to 2.1.21.
- 🔄 Update Compose Multiplatform to 1.8.0.
- 🔄 Update KMPKit to 1.0.7.

## [1.3.4] - April 15, 2025
- 🔄 Update Kotlin to 2.1.20.

## [1.3.3] - February 28, 2025
- 📦 **`[BUILD]`**: Remove Napier library from Passage and use basic `println` to display log messages.

## [1.3.2] - February 27, 2025
- 🐛 **`[FIX]`**: 🤖 Fixed sign-out issue on Android with the Google Legacy Gatekeeper when the binding activity was null.

## [1.3.1] - February 24, 2025
- 🛠 **`[IMPROVMENT]`**: 🤖 Add a thrid way to authenticate with Google on Android: Sign-In with Google UI button flow.

## [1.3.0] - February 18, 2025
- 🛠 **`[IMPROVMENT]`**: 🤖 Enhanced Google Authentication on Android: When authentication with the Google Gatekeeper fails, the library now automatically falls back to Google Legacy Sign-In.

## [1.2.0] - February 12, 2025
- 🚨 **`[BREAKING]`**: 🤖 `PassageAndroid` parameter `context` has been renamed `applicationContext` to avoid confusion with the Activity-based context required for the Google Sign-In UI: `PassageAndroid(applicationContext: Context)`.
- 🤖 When using the Google gatekeeper on Android, you must now call [`bindToView()`](https://github.com/Tweener/passage/blob/main/passage/src/commonMain/kotlin/com/tweener/passage/Passage.kt#L107) in Passage before performing any authentication operations. This ensures that Passage can access the Activity-based context needed to display the Google Sign-In UI.
- 📦 **`[BUILD]`**: Remove some GitLive Firebase dependencies that were not used.

## [1.1.2] - February 11, 2025
- 🐛 **`[FIX]`**: 🤖 On Android, use Activity-based context instead of Application-based context to prevent a crash on some Android devices.
- 🐛 **`[FIX]`**: 🤖 On Android, the sign-in retry mechanism now works as expected.
- Update Kotlin to 2.1.10.

## [1.1.1] - January 24, 2025
- ✨ **`[FEATURE]`**: `Passage` now provides two separate methods to check if the user is logged in:
  - [`isUserLoggedIn`](https://github.com/Tweener/passage/blob/main/passage/src/commonMain/kotlin/com/tweener/passage/Passage.kt#L128): Returns instantly.
  - [`isUserLoggedInAsFlow`](https://github.com/Tweener/passage/blob/main/passage/src/commonMain/kotlin/com/tweener/passage/Passage.kt#L136): Allows observing changes over time.
- 🔄 Update Kotlin to 2.1.0.
- 🔄 Update Compose Multiplatform to 1.7.3.
- 🔄 Update Gradle to 8.11.1.

## [1.1.0] - December 16, 2024
- ✨ **`[FEATURE]`**: Added new method `Passage#confirmResetPassword(params: PassageResetPasswordParams)` to confirm the reset password and set the Entrant account's new password.
- 🚨 **`[BREAKING]`**: Method `Passage#handlePasswordResetCode(oobCode: String)` now returns `Result<EmailAddress>`, instead of `Result<Unit>`, where `EmailAddress` is a the Entrant account's email address.
- 🚨 **`[BREAKING]`**: `PassageUniversalLink.mode` property is now an enum class `PassageUniversalLinkMode` instead of a String.

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
