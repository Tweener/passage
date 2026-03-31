package com.tweener.passage.core.model

/**
 * @author Chirag Redij
 * @since 29/03/2026
 */

sealed interface AuthResult<out T> {
    data class Success<T>(val data: T) : AuthResult<T>
    data class Error(val throwable: Throwable) : AuthResult<Nothing>
}

fun <T> AuthResult<T>.getOrThrow(): T {
    return when (this) {
        is AuthResult.Success -> data
        is AuthResult.Error -> throw throwable
    }
}