[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.tweener/passage?color=orange)](https://central.sonatype.com/artifact/io.github.tweener/passage)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-1.7.0-blue.svg?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform)
![gradle-version](https://img.shields.io/badge/gradle-8.5.2-blue?logo=gradle)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

[![Website](https://img.shields.io/badge/Author-vivienmahe.com-purple)](https://vivienmahe.com/)
[![X/Twitter](https://img.shields.io/twitter/follow/VivienMahe)](https://twitter.com/VivienMahe)

<br>

![Passage logo](https://github.com/user-attachments/assets/c92f7d44-df02-4860-9ba4-fb4d4c3f3d68#gh-light-mode-only)
![Passage logo](https://github.com/user-attachments/assets/28914622-f8b9-403b-a1b6-3c736af5e98a#gh-dark-mode-only)

---

# Passage

**Passage** is a Kotlin Multiplatform library designed to simplify authentication flows across Android and iOS platforms. Built on **Firebase Authentication**, Passage abstracts common operations and provides composable APIs to manage authentication using popular providers like Google, Apple, and Email/Password.

<br/>
Be sure to show your support by starring ⭐️ this repository, and feel free to [contribute](#-contributing) if you're interested!

---

## 🌟 Features

- **Kotlin Multiplatform**: Shared logic for Android and iOS.
- **Firebase Authentication**: Powered by Firebase for robust and secure authentication.
- **Composables for Compose Multiplatform**: Seamless integration with Compose-based UIs.
- **Provider Support**:
  - Google
  - Apple
  - Email/Password
- **Extensible Configuration**: Customize authentication flows with platform-specific settings.
- **Email actions**: Send email actions for password resets or verifying a user's email. 

---

## 🎯 Concept

Passage uses [**Firebase Authentication**](https://firebase.google.com/) as the backbone for secure and reliable user identity management. It abstracts the complexity of integrating with Firebase's SDKs on multiple platforms, providing a unified API for developers.

Passage abstracts the authentication flow into three main components:

1. **Passage**: The entry point for managing authentication flows.
2. **Gatekeepers**: Providers like Google, Apple, and Email/Password, which handle specific authentication mechanisms.
3. **Entrants**: Users who have successfully authenticated and gained access.

---

## 🛠️ Installation

In your `build.gradle.kts` file, add Maven Central to your repositories:
```Groovy
repositories {
    mavenCentral()
}
```

Then add Passage dependency to your module:

- With version catalog, open `libs.versions.toml`:
```Groovy
[versions]
passage = "1.2.0" // Check latest version

[libraries]
passage = { group = "io.github.tweener", name = "passage", version.ref = "passage" }
```

Then in your module `build.gradle.kts` add:
```Groovy
dependencies {
    implementation(libs.passage)
}
```

- Without version catalog, in your module `build.gradle.kts` add:
```Groovy
dependencies {
    val passage_version = "1.2.0" // Check latest version

    implementation("io.github.tweener:passage:$passage_version")
}
```

The latest version is: [![Maven Central Version](https://img.shields.io/maven-central/v/io.github.tweener/passage?color=orange)](https://central.sonatype.com/artifact/io.github.tweener/passage)

---

## 🔧 Configuration

### 1. Create a Passage
Create an instance of `PassageConfiguration` using `rememberPassage()`:

```Kotlin
val passage: Passage = rememberPassage()
```

### 2. Configure Passage
Set up your `PassageConfiguration` with the desired gatekeepers (authentication providers):

```kotlin
val passageConfiguration = PassageConfiguration(
    google = GoogleGatekeeperConfiguration(
        serverClientId = "your-google-server-client-id",
        android = GoogleGatekeeperAndroidConfiguration(
            filterByAuthorizedAccounts = false,
            autoSelectEnabled = true,
            maxRetries = 3
        )
    ),
    apple = AppleGatekeeperConfiguration(),
    emailPassword = EmailPasswordGatekeeperConfiguration()
)
```

If you only want to use Google gatekeeper, create a `PassageConfiguration`  like this:

```kotlin
val passageConfiguration = PassageConfiguration(
    google = GoogleGatekeeperConfiguration(
        serverClientId = "your-google-server-client-id",
        android = GoogleGatekeeperAndroidConfiguration(
            filterByAuthorizedAccounts = false,
            autoSelectEnabled = true,
            maxRetries = 3
        )
    ),
)
```

_Note: Replace `your-google-server-client-id` with your actual [Google serverClientId](https://firebase.google.com/docs/auth/android/google-signin#authenticate_with_firebase)._


### 3. Initialize Passage
Initialize Passage in your common module entry point:

```kotlin
passage.initialize(passageConfiguration)
```

---

## 🧑‍💻 Usage

### 1. Authenticate a User
Use the provider-specific methods to authenticate users.

#### Google Authentication
`Passage#authenticateWithGoogle()` authenticates a user via Google Sign-In. If the user does not already exist, a new account will be created automatically.

```kotlin
val result = passage.authenticateWithGoogle()
result.fold(
    onSuccess = { entrant -> Log.d("Passage", "Welcome, ${entrant.displayName}") },
    onFailure = { error -> Log.e("Passage", "Authentication failed", error) }
)
```

#### Apple Authentication
`Passage#authenticateWithApple()` authenticates a user via Apple Sign-In. If the user does not already exist, a new account will be created automatically.

```kotlin
val result = passage.authenticateWithApple()
// Handle result similarly
```

#### Email/Password Authentication

##### a. Create a user:
Creating a user with email & password will automatically authenticate the user upon successful account creation.

```kotlin
val result = passage.authenticateWithEmailAndPassword(PassageEmailAuthParams(email, password))
// Handle result similarly
```

##### b. Authenticate a user:
```kotlin
val result = passage.authenticateWithEmailAndPassword(PassageEmailAuthParams(email, password))
// Handle result similarly
```

### 2. Sign Out or Reauthenticate
```kotlin
passage.signOut()

passage.reauthenticateWithGoogle()
passage.reauthenticateWithApple()
passage.reauthenticateWithEmailAndPassword(params)
```

### 3. Email actions
You may need to [send emails](https://firebase.google.com/docs/auth/android/passing-state-in-email-actions) to the user for a password reset if the user forgot its password, or for verifying the user's email address when creating an account.

Passage uses [Firebase Dynamic Links](https://firebase.google.com/docs/dynamic-links) to send emails containing universal links. Follow the documentation to configure your app with Firebase Dynamic Links (you don't need to add Firebase Dynamic Links SDK to your app).

To handle universal links, you need to create a unique instance of `PassageUniversalLinkHandler` and configure Passage with it:
```Kotlin
val passage: Passage = rememberPassage(universalLinkHandler = providePassageUniversalLinkHandler())
```

Additional configuration is required for each platform:

<details>
	<summary>🤖 Android</summary>

In your activity configured to be open when a universal link is clicked:
```Kotlin
class MainActivity : ComponentActivity() {

    private val universalLinkHandler = providePassageUniversalLinkHandler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handleUniversalLink(intent = intent)

        // ...
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleUniversalLink(intent = intent)

        // ...
    }

    private fun handleUniversalLink(intent: Intent) {
        intent.data?.let {
            universalLinkHandler.handle(url = it.toString())
        }
    }
}
```

</details>

<details>
	<summary>🍎 iOS</summary>

Create a class `PassageUniversalLinkHandlerHelper` in your `iosMain` module:
```Kotlin
class PassageUniversalLinkHandlerHelper {

    private val universalLinkHandler = providePassageUniversalLinkHandler()

    fun handle(url: String): Boolean =
        universalLinkHandler.handle(url = url)

}
```

Then, in your `AppDelegate`, add the following lines:
```Swift
class AppDelegate : NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    // ...

    func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        if userActivity.activityType == NSUserActivityTypeBrowsingWeb,
           let url = userActivity.webpageURL {
            handleIncomingURL(url)
            return true
        }
        
        print("No valid URL in user activity.")
        return false
    }
    
    func handleIncomingURL(_ url: URL) {
        print("handleIncomingURL", url)
        
        // Check if the URL is handled by Google Sign In
        if (GIDSignIn.sharedInstance.handle(url)) {
            print("Handled by GIDSignIn")
        }

        // Then check if the URL is handled by Passage (Firebase Dynamic Links)
        else if (PassageUniversalLinkHandlerHelper().handle(url: url.absoluteString)) {
            print("Handled by Passage")
        }
    }
}
```

</details>

Passage exposes a `universalLinkToHandle` StateFlow, which you can use to be notified when a new unviersal link has been clicked and validated by Passage:

```Kotlin
val link = passage.universalLinkToHandle.collectAsStateWithLifecycle()

LaunchedEffect(link.value) {
    link.value?.let {
        println("Universal link handled for mode: ${it.mode} with continueUrl: ${it.continueUrl}")
    }
}
```

##### a. Send an email for verifying a user's email
If you want to reinforce authentication, you can send the user an email to verify its email address:

```Kotlin
val result = passage.sendEmailVerification(
    params = PassageEmailVerificationParams(
        url = "https://passagesample.page.link/action/email_verified",
        iosParams = PassageEmailVerificationIosParams(bundleId = "com.tweener.passage.sample"),
        androidParams = PassageEmailVerificationAndroidParams(
            packageName = "com.tweener.passage.sample",
            installIfNotAvailable = true,
            minimumVersion = "1.0",
        ),
        canHandleCodeInApp = true,
    )
)
result.fold(
    onSuccess = { entrant -> Log.d("Passage", "An email has been sent to the user to verify its email address.") },
    onFailure = { error -> Log.e("Passage", "Couldn't send the email", error) }
)
```

##### b. Send an email for a password reset
If you want to reinforce authentication, you can send the user an email to verify its email address:

```Kotlin
val result = passage.sendPasswordResetEmail(
    params = PassageForgotPasswordParams(
        email = passage.getCurrentUser()!!.email,
        url = "https://passagesample.page.link/action/password_reset",
        iosParams = PassageEmailVerificationIosParams(bundleId = "com.tweener.passage.sample"),
        androidParams = PassageEmailVerificationAndroidParams(
            packageName = "com.tweener.passage.sample",
            installIfNotAvailable = true,
            minimumVersion = "1.0",
        ),
        canHandleCodeInApp = true,
    )
)
result.fold(
    onSuccess = { entrant -> Log.d("Passage", "An email has been sent to the user to reset its password.") },
    onFailure = { error -> Log.e("Passage", "Couldn't send the email", error) }
)
```

---

## 🤝 Contributing

We love your input and welcome any contributions! Please read our [contribution guidelines](https://github.com/Tweener/passage/blob/master/CONTRIBUTING.md) before submitting a pull request.

---

## 🙏 Credits

- Logo by [Freeicons](https://freeicons.io/pet-shop-32/mine-tunnel-passage-passageway-underground-icon-904280)

---

## 📜 Licence

Passage is licensed under the [Apache-2.0](https://github.com/Tweener/passage?tab=Apache-2.0-1-ov-file#readme).
