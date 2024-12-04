package com.tweener.passage.sample

import android.content.Context
import androidx.startup.Initializer

/**
 * @author Vivien Mahe
 * @since 04/12/2024
 */

lateinit var applicationContext: Context

class ContextInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        applicationContext = context.applicationContext
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}
