[![Maven Central Version](https://img.shields.io/maven-central/v/io.github.tweener/passage?color=orange)](https://central.sonatype.com/artifact/io.github.tweener/passage)
[![Kotlin](https://img.shields.io/badge/kotlin-2.2.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-1.8.2-blue.svg?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform)
![gradle-version](https://img.shields.io/badge/gradle-8.11.0-blue?logo=gradle)
[![License](https://img.shields.io/badge/License-Apache%202.0-green.svg)](https://opensource.org/licenses/Apache-2.0)

[![Website](https://img.shields.io/badge/Author-vivienmahe.com-purple)](https://vivienmahe.com/)
[![X/Twitter](https://img.shields.io/twitter/follow/VivienMahe)](https://twitter.com/VivienMahe)

<br>

![Passage logo](https://github.com/user-attachments/assets/c92f7d44-df02-4860-9ba4-fb4d4c3f3d68#gh-light-mode-only)
![Passage logo](https://github.com/user-attachments/assets/28914622-f8b9-403b-a1b6-3c736af5e98a#gh-dark-mode-only)

---

# Passage

**Passage** is a Kotlin Multiplatform library designed to simplify authentication flows across Android and iOS platforms. Built on **Firebase Authentication**, Passage abstracts common operations and provides composable APIs to manage authentication using popular providers like Google, Apple, and Email/Password.

<br>

Be sure to show your support by starring ⭐️ this repository, and feel free to [contribute](#-contributing) if you're interested!

---

## 🌟 Features

- **Firebase Authentication**: Powered by Firebase for robust and secure authentication.
- **Gatekeeper (Provider) Support**: Google, Apple, Email/Password.
- **Extensible Configuration**: Customize authentication flows with platform-specific settings.
- **Email actions**: Send email actions for magic link sign-in, password resets or verifying a user's email.

> [!WARNING]  
> Starting **August 25, 2025**, email actions will no longer work due to the [shutdown of Firebase Dynamic Links](https://firebase.google.com/support/dynamic-links-faq).  
>   
> I decided to drop support of these features for two main reasons:  
> - I attempted to follow the [migration guide](https://firebase.google.com/docs/auth/android/email-link-migration) to move from Dynamic Links to Firebase Hosting, but had no success.  
> - Firebase recently [updated the free plan limit](https://firebase.google.com/docs/auth/limits#email_sending_limits) for email link sign-in emails to only **5 per day**, which makes development both harder and more expensive.  
>   
> If you discover a reliable solution, contributions via PR are very welcome!

---

## 🚀 Used in production

Alarmee powers notifications in real-world apps:

- [**KMPShip**](https://www.kmpship.app/): a Kotlin Multiplatform boilerplate to build mobile apps faster.
- [**Bloomeo**](https://bloomeo.app/): a personal finance app.

---

## 🎯 Concept

Passage uses [**Firebase Authentication**](https://firebase.google.com/) as the backbone for secure and reliable user identity management. It abstracts the complexity of integrating with Firebase's SDKs on multiple platforms, providing a unified API for developers.

Passage abstracts the authentication flow into three main components:

1. **Passage**: The entry point for managing authentication flows.
2. **Gatekeepers**: Providers like Google, Apple, and Email/Password, which handle specific authentication mechanisms.
3. **Entrants**: Users who have successfully authenticated and gained access.

---

## 🛠️ Installation

In your `settings.gradle.kts` file, add Maven Central to your repositories:
```Groovy
repositories {
    mavenCentral()
}
```

Then add Passage dependency to your module:

- With version catalog, open `libs.versions.toml`:
```Groovy
[versions]
passage = "1.0.0" // Check latest version

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
    val passage_version = "1.0.0" // Check latest version

    implementation("io.github.tweener:passage:$passage_version")
}
```

The latest version is: [![Maven Central Version](https://img.shields.io/maven-central/v/io.github.tweener/passage?color=orange)](https://central.sonatype.com/artifact/io.github.tweener/passage)

---

## 🔧 Configuration

### 1. Create a Passage
First, you need to create an instance of `Passage` for each platform:

- 🤖 Android

  Create an instance of `PassageAndroid` passing an Application-based `Context`:
```Kotlin
val passage: Passage = PassageAndroid(applicationContext = context)
```

- 🍎 iOS

  Create an instance of `PassageIos`:
```Kotlin
val passage: Passage = PassageIos()
```

### 2. Configure Passage
Provide a list of the desired gatekeepers (authentication providers) to configure:

```kotlin
val gatekeeperConfigurations = listOf(
    GoogleGatekeeperConfiguration(
        serverClientId = "your-google-server-client-id",
        android = GoogleGatekeeperAndroidConfiguration(
            useGoogleButtonFlow = true,
            filterByAuthorizedAccounts = false,
            autoSelectEnabled = true,
            maxRetries = 3
        )
    ),
    AppleGatekeeperConfiguration(),
    EmailPasswordGatekeeperConfiguration
)
```

For example, if you only want to use the Google Gatekeeper, simply provide the `GoogleGatekeeperConfiguration` like this:

```kotlin
val gatekeeperConfigurations = listOf(
    GoogleGatekeeperConfiguration(
        serverClientId = "your-google-server-client-id",
        android = GoogleGatekeeperAndroidConfiguration(
            useGoogleButtonFlow = true,
            filterByAuthorizedAccounts = false,
            autoSelectEnabled = true,
            maxRetries = 3
        )
    ),
)
```

> [!IMPORTANT]  
> Replace `your-google-server-client-id` with your actual [Google serverClientId](https://firebase.google.com/docs/auth/android/google-signin#authenticate_with_firebase).


### 3. Initialize Passage
Initialize Passage in your common module entry point:

```kotlin
passage.initialize(gatekeepersConfigurations = gatekeeperConfigurations)
```

> [!NOTE]  
> If your app already uses Firebase, you can pass the existing Firebase instance to Passage to reuse it and prevent reinitializing Firebase unnecessarily:
```kotlin
passage.initialize(
    gatekeepersConfigurations = gatekeeperConfigurations,
    firebase = Firebase,
)
```

### 4. Bind Passage to the current view
When using Google gatekeeper on Android, you must now call `bindToView()` in Passage before performing any authentication operations. This ensures that Passage can access the Activity-based context needed to display the Google Sign-In UI.
```Kotlin
@Composable
fun MyApp() {
    val passage = { inject Passage }

    passage.initialize(...)

    passage.bindtoView() // <- Add this line when using Google gatekeeper on Android
}
```


---

## 🧑‍💻 Usage

### 1. Authenticate a user
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

> [!WARNING]  
> Apple Sign-In only works on iOS. Passage does not handle Apple Sign-In on Android, since this scenario is quite uncommon.


```kotlin
val result = passage.authenticateWithApple()
// Handle result similarly
```

#### Email/Password Authentication

##### a. Create a user:
Creating a user with email & password will automatically authenticate the user upon successful account creation.

```kotlin
val result = passage.createUserWithEmailAndPassword(PassageEmailAuthParams(email, password))
// Handle result similarly
```

##### b. Authenticate a user:
```kotlin
val result = passage.authenticateWithEmailAndPassword(PassageEmailAuthParams(email, password))
// Handle result similarly
```

#### Email Link Sign-in Authentication

##### a. Send the sign-in link email:
You can send an email with a sign-in link to the user's email adress:

```kotlin
val result = passage.sendSignInLinkToEmail(
    params = PassageSignInLinkToEmailParams(
        email = userEmail, // Ask the user for its email address
        url = "https://passagesample.web.app/action/sign_in_link_email",
        iosParams = PassageSignInLinkToEmailIosParams(bundleId = "com.tweener.passage.sample"),
        androidParams = PassageSignInLinkToEmailAndroidParams(
            packageName = "com.tweener.passage.sample",
            installIfNotAvailable = true,
            minimumVersion = "1.0",
        ),
        canHandleCodeInApp = true,
    )
)
result.fold(
    onSuccess = { entrant -> Log.d("Passage", "An email has been sent to the user with a sign-in link.") },
    onFailure = { error -> Log.e("Passage", "Couldn't send the email", error) }
)
```

##### b. Verify sign-in link and authenticate the user:
Once the user clicks on this link from the email, it will redirect to your app and automatically sign-in the user:

```kotlin
passage.handleSignInLinkToEmail(email = "{Your email address}", emailLink = it.link)
    .onSuccess { entrant = it } // User is sucessfully signed-in
    .onFailure { println("Couldn't sign-in the user, error: ${it.message}") }
```

### 2. Sign Out or Reauthenticate
```kotlin
passage.signOut()

passage.reauthenticateWithGoogle()
passage.reauthenticateWithApple()
passage.reauthenticateWithEmailAndPassword(params)
```

### 3. Email actions
You may need to [send emails](https://firebase.google.com/docs/auth/android/passing-state-in-email-actions) to the user for a password reset if the user forgot their password for instance, or for verifying the user's email address when creating an account.

> [!IMPORTANT]
> Passage uses Firebase Hosting domains to send emails containing universal links for authentication flows.
> You need to configure your app to handle Firebase Hosting links (e.g., `PROJECT_ID.web.app` or `PROJECT_ID.firebaseapp.com`).

To handle universal links, additional configuration is required for each platform:

<details>
	<summary>🤖 Android</summary>

In your activity configured to be open when a universal link is clicked:
```Kotlin
class MainActivity : ComponentActivity() {

    private val passage = providePassage()

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
            passage.handleUrl(url = it.toString())
        }
    }
}
```

</details>

<details>
	<summary>🍎 iOS</summary>

Create a class `PassageHelper` in your `iosMain` module:
```Kotlin
class PassageHelper {

    private val passage = providePassage()

    fun handle(url: String): Boolean =
        passage.handleUrl(url = url)

}
```

Then, in your `AppDelegate`, add the following lines:
```Swift
class AppDelegate : NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate {

    // ...

    func application(_ application: UIApplication, continue userActivity: NSUserActivity, restorationHandler: @escaping ([UIUserActivityRestoring]?) -> Void) -> Bool {
        if userActivity.activityType == NSUserActivityTypeBrowsingWeb,
           let url = userActivity.webpageURL {
            if (PassageHelper().handle(url: url.absoluteString)) {
                print("Handled by Passage")
            }

            return true
        }
        
        print("No valid URL in user activity.")
        return false
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
        passage.onLinkHandled() // Important: call 'onLinkHandled()' to let Passage know the link has been handled and can update the authentication state
    }
}
```

##### a. Send an email for verifying a user's email
If you want to reinforce authentication, you can send the user an email to verify its email address:

```Kotlin
val result = passage.sendEmailVerification(
    params = PassageEmailVerificationParams(
        url = "https://passagesample.web.app/action/email_verified",
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
        url = "https://passagesample.web.app/action/password_reset",
        iosParams = PassageForgotPasswordIosParams(bundleId = "com.tweener.passage.sample"),
        androidParams = PassageForgotPasswordAndroidParams(
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
