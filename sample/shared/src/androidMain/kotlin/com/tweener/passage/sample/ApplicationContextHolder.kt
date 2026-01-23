package com.tweener.passage.sample

import android.content.Context

/**
 * Holds a reference to the application context for use in shared code.
 * This must be initialized by the Android app module.
 *
 * @author Vivien Mahe
 * @since 04/12/2024
 */

lateinit var applicationContext: Context
    internal set

fun initializeApplicationContext(context: Context) {
    applicationContext = context.applicationContext
}
