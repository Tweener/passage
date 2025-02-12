package com.tweener.passage.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

class MainActivity : ComponentActivity() {

    private val passage = providePassage()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleUniversalLink(intent = intent)

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleUniversalLink(intent = intent)
    }

    private fun handleUniversalLink(intent: Intent) {
        intent.data?.let {
            passage.handleLink(url = it.toString())
        }
    }
}
