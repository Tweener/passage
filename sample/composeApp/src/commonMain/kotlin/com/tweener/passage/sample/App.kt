package com.tweener.passage.sample

/**
 * @author Vivien Mahe
 * @since 25/11/2024
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.tweener.passage.gatekeeper.email.model.PassageEmailAuthParams
import com.tweener.passage.gatekeeper.email.model.PassageEmailVerificationAndroidParams
import com.tweener.passage.gatekeeper.email.model.PassageEmailVerificationIosParams
import com.tweener.passage.gatekeeper.email.model.PassageEmailVerificationParams
import com.tweener.passage.gatekeeper.email.model.PassageForgotPasswordAndroidParams
import com.tweener.passage.gatekeeper.email.model.PassageForgotPasswordIosParams
import com.tweener.passage.gatekeeper.email.model.PassageForgotPasswordParams
import com.tweener.passage.model.AppleGatekeeperConfiguration
import com.tweener.passage.model.EmailPasswordGatekeeperConfiguration
import com.tweener.passage.model.Entrant
import com.tweener.passage.model.GoogleGatekeeperAndroidConfiguration
import com.tweener.passage.model.GoogleGatekeeperConfiguration
import com.tweener.passage.model.PassageServiceConfiguration
import com.tweener.passage.rememberPassageService
import com.tweener.passage.sample.ui.theme.PassageTheme
import kotlinx.coroutines.launch

@Composable
fun App() {
    val buttonsScope = rememberCoroutineScope()
    val snackbarScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val passageService = rememberPassageService(universalLinkHandler = providePassageUniversalLinkHandler())
    var entrant by remember { mutableStateOf<Entrant?>(null) }
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(passageService) {
        lifecycleOwner.repeatOnLifecycle(state = Lifecycle.State.CREATED) {
            // Initialize Passage
            passageService.initialize(
                configuration = PassageServiceConfiguration(
                    google = GoogleGatekeeperConfiguration(
                        serverClientId = "669986017952-72t1qil6sanreihoeumpb88junr9r8jt.apps.googleusercontent.com",
                        android = GoogleGatekeeperAndroidConfiguration(),
                    ),
                    apple = AppleGatekeeperConfiguration(),
                    emailPassword = EmailPasswordGatekeeperConfiguration,
                )
            )

            // Check if an Entrant already exists
            entrant = passageService.getCurrentUser()
        }
    }

    // Listen to universal links to be handled
    val link = passageService.universalLinkToHandle.collectAsStateWithLifecycle()
    LaunchedEffect(link.value) {
        println("New link: ${link.value}")
        link.value?.let {
            snackbarScope.launch { snackbarHostState.showSnackbar(message = "Universal link handled for mode: ${it.mode} with continueUrl: ${it.continueUrl}") }
        }
    }

    PassageTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = 24.dp),
            ) {
                Spacer(modifier = Modifier.padding(vertical = 8.dp))

                Text(entrant?.email?.let { "Entrant email: $it" } ?: "User not logged in")

                HorizontalDivider(modifier = Modifier.width(250.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

                if (entrant == null) {
                    Button(onClick = {
                        buttonsScope.launch {
                            passageService.authenticateWithGoogle()
                                .onSuccess { entrant = it }
                                .onFailure {
                                    println(it)
                                    it.message?.let { message -> snackbarScope.launch { snackbarHostState.showSnackbar(message = message) } }
                                }
                        }
                    }) {
                        Text("Sign in with Google")
                    }

                    HorizontalDivider(modifier = Modifier.width(50.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

                    Button(onClick = {
                        buttonsScope.launch {
                            passageService.authenticateWithApple()
                                .onSuccess { entrant = it }
                                .onFailure {
                                    println(it)
                                    it.message?.let { message -> snackbarScope.launch { snackbarHostState.showSnackbar(message = message) } }
                                }
                        }
                    }) {
                        Text("Sign in with Apple")
                    }

                    HorizontalDivider(modifier = Modifier.width(50.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

                    Button(onClick = {
                        buttonsScope.launch {
                            passageService
                                .createUserWithEmailAndPassword(params = PassageEmailAuthParams(email = "vivien.mahe@gmail.com", password = "testest1!"))
                                .onSuccess { entrant = it }
                                .onFailure {
                                    println(it)
                                    it.message?.let { message -> snackbarScope.launch { snackbarHostState.showSnackbar(message = message) } }
                                }
                        }
                    }) {
                        Text("Sign up with Email/Password")
                    }

                    Button(onClick = {
                        buttonsScope.launch {
                            passageService
                                .authenticateWithEmailAndPassword(params = PassageEmailAuthParams(email = "vivien.mahe@gmail.com", password = "testest1!"))
                                .onSuccess { entrant = it }
                                .onFailure {
                                    println(it)
                                    it.message?.let { message -> snackbarScope.launch { snackbarHostState.showSnackbar(message = message) } }
                                }
                        }
                    }) {
                        Text("Sign in with Email/Password")
                    }
                } else {
                    Button(onClick = {
                        buttonsScope.launch {
                            passageService
                                .sendEmailVerification(
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
                                .onSuccess {
                                    entrant?.email?.let { snackbarScope.launch { snackbarHostState.showSnackbar(message = "An email has been sent to $it to verify this address.") } }
                                }
                                .onFailure {
                                    println(it)
                                    it.message?.let { message -> snackbarScope.launch { snackbarHostState.showSnackbar(message = message) } }
                                }
                        }
                    }) {
                        Text("Send email address verification email")
                    }

                    HorizontalDivider(modifier = Modifier.width(50.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

                    Button(onClick = {
                        buttonsScope.launch {
                            passageService
                                .sendPasswordResetEmail(
                                    params = PassageForgotPasswordParams(
                                        email = entrant?.email!!,
                                        url = "https://passagesample.page.link/action/password_reset",
                                        iosParams = PassageForgotPasswordIosParams(bundleId = "com.tweener.passage.sample"),
                                        androidParams = PassageForgotPasswordAndroidParams(
                                            packageName = "com.tweener.passage.sample",
                                            installIfNotAvailable = true,
                                            minimumVersion = "1.0",
                                        ),
                                        canHandleCodeInApp = true,
                                    )
                                )
                                .onSuccess {
                                    entrant?.email?.let { snackbarScope.launch { snackbarHostState.showSnackbar(message = "An email has been sent to $it to reset the password.") } }
                                }
                                .onFailure {
                                    println(it)
                                    it.message?.let { message -> snackbarScope.launch { snackbarHostState.showSnackbar(message = message) } }
                                }
                        }
                    }) {
                        Text("Send password reset email")
                    }

                    HorizontalDivider(modifier = Modifier.width(250.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

                    Button(onClick = {
                        buttonsScope.launch {
                            passageService.signOut()
                            entrant = passageService.getCurrentUser()
                        }
                    }) {
                        Text("Sign out")
                    }
                }
            }
        }
    }
}
