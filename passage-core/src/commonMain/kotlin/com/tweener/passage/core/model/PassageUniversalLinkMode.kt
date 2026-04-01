package com.tweener.passage.core.model

/**
 * Represents the type of email-based authentication action, used both for
 * universal link handling and out-of-band (OOB) action code processing.
 *
 * Each value corresponds to a specific email action handled by the
 * [com.tweener.passage.core.authplugin.AuthPlugin] and the universal link handler.
 *
 * @author Vivien Mahe
 * @since 15/12/2024
 */
enum class PassageUniversalLinkMode {

    /** Email address verification action. */
    VERIFY_EMAIL,

    /** Password reset action. */
    RESET_PASSWORD,

    /** Sign-in via email link (magic link) action. */
    SIGN_IN_EMAIL,
}