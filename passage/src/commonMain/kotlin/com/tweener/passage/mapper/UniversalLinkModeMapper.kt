package com.tweener.passage.mapper

import com.tweener.passage.model.PassageUniversalLinkMode

/**
 * @author Vivien Mahe
 * @since 15/12/2024
 */

internal fun String?.toPassageUniversalLinkMode(): PassageUniversalLinkMode? =
    when (this) {
        "verifyEmail" -> PassageUniversalLinkMode.VERIFY_EMAIL
        "resetPassword" -> PassageUniversalLinkMode.RESET_PASSWORD
        else -> null
    }
