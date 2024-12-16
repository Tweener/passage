package com.tweener.passage.gatekeeper.email.model

/**
 * Parameters required to confirm a password reset operation.
 *
 * @property oobCode The out-of-band code sent to the user's email for password reset.
 * @property newPassword The new password to set for the user.
 *
 * @author Vivien Mahe
 * @since 15/12/2024
 */
data class PassageResetPasswordParams(
    val oobCode: String,
    val newPassword: String,
)
