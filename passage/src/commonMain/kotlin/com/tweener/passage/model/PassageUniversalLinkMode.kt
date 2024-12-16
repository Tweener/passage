package com.tweener.passage.model

/**
 * Represents the mode of a universal link, indicating the type of Firebase email action,
 * such as verifying an email address or resetting a password.
 *
 * @author Vivien Mahe
 * @since 15/12/2024
 */
enum class PassageUniversalLinkMode {
    VERIFY_EMAIL,
    RESET_PASSWORD
}
