
# Changelog

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
