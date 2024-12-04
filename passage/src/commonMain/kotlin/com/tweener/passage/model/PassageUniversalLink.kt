package com.tweener.passage.model

/**
 * Represents a parsed universal link from an email sent via Firebase.
 *
 * This class encapsulates the details of a universal link, including the original link,
 * the operation mode, the one-time-use code (oobCode), and the continue URL.
 * These links are typically used in Firebase email actions, such as email verification
 * or password resets.
 *
 * @property link The original universal link as a string.
 * @property mode The operation mode, indicating the type of email action (e.g., "verifyEmail", "resetPassword").
 * @property oobCode The one-time-use code (oobCode) included in the link.
 * @property continueUrl The continue URL included in the link for redirection.
 *
 * @author Vivien Mahe
 * @since 03/12/2024
 */
data class PassageUniversalLink(
    val link: String,
    val mode: String,
    val oobCode: String,
    val continueUrl: String,
)
