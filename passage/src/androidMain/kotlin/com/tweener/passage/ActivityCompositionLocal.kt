package com.tweener.passage

import android.app.Activity
import androidx.compose.runtime.compositionLocalOf

/**
 * @author Vivien Mahe
 * @since 11/02/2025
 */

val LocalActivity = compositionLocalOf<Activity> {
    error("No Activity provided. Make sure to provide the Activity in CompositionLocalProvider.")
}
