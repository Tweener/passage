package com.tweener.passage.core.model

/**
 * @author Chirag Redij
 * @since 29/03/2026
 */

sealed interface ActionCodeType {
    data object VerifyEmail : ActionCodeType
    data object PasswordReset : ActionCodeType
    data object SignInWithEmailLink : ActionCodeType
}