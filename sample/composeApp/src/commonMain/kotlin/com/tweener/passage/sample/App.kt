package com.tweener.passage.sample

/**
 * @author Vivien Mahe
 * @since 25/11/2024
 */

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import com.tweener.passage.gatekeeper.email.model.PassageEmailAuthParams
import com.tweener.passage.model.Entrant
import com.tweener.passage.model.AppleGatekeeperConfiguration
import com.tweener.passage.model.EmailPasswordGatekeeperConfiguration
import com.tweener.passage.model.GoogleGatekeeperAndroidConfiguration
import com.tweener.passage.model.GoogleGatekeeperConfiguration
import com.tweener.passage.model.PassageServiceConfiguration
import com.tweener.passage.rememberPassageService
import com.tweener.passage.sample.ui.theme.PassageTheme
import kotlinx.coroutines.launch

@Composable
fun App() {
    val scope = rememberCoroutineScope()
    val passageService = rememberPassageService()
    var entrant by remember { mutableStateOf<Entrant?>(null) }

    LaunchedEffect(passageService) {
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
    }

    PassageTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(space = 24.dp, alignment = Alignment.CenterVertically),
            ) {
                Text("Entrant email: ${entrant?.email}")

                HorizontalDivider(modifier = Modifier.width(150.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

                Button(onClick = { scope.launch { passageService.authenticateWithGoogle().onSuccess { entrant = it }.onFailure { println(it) } } }) {
                    Text("Sign in with Google")
                }

                HorizontalDivider(modifier = Modifier.width(50.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

                Button(onClick = { scope.launch { passageService.authenticateWithApple().onSuccess { entrant = it }.onFailure { println(it) } } }) {
                    Text("Sign in with Apple")
                }

                HorizontalDivider(modifier = Modifier.width(50.dp), thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

                Button(onClick = {
                    scope.launch {
                        passageService
                            .createUserWithEmailAndPassword(params = PassageEmailAuthParams(email = "vivien.mahe@gmail.com", password = "testest1!"))
                            .onSuccess { entrant = it }
                            .onFailure { println(it) }
                    }
                }) {
                    Text("Sign up with Email/Password")
                }

                Button(onClick = {
                    scope.launch {
                        passageService
                            .authenticateWithEmailAndPassword(params = PassageEmailAuthParams(email = "vivien.mahe@gmail.com", password = "testest1!"))
                            .onSuccess { entrant = it }
                            .onFailure { println(it) }
                    }
                }) {
                    Text("Sign in with Email/Password")
                }
            }
        }
    }
}
