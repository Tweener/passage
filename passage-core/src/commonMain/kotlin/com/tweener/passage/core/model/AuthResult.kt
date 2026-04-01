package com.tweener.passage.core.model

/**
 * Represents the result of an authentication operation, encapsulating either a successful
 * outcome with data or a failure with an associated [Throwable].
 *
 * Use [Success] to wrap a successful result, and [Error] to wrap a failure.
 *
 * @param T The type of the data returned on success.
 *
 * @author Chirag Redij
 * @since 29/03/2026
 */
sealed interface AuthResult<out T> {

    /**
     * Represents a successful authentication result.
     *
     * @property data The data returned by the operation.
     */
    data class Success<T>(val data: T) : AuthResult<T>

    /**
     * Represents a failed authentication result.
     *
     * @property throwable The error that caused the failure.
     */
    data class Error(val throwable: Throwable) : AuthResult<Nothing>
}

/**
 * Returns the encapsulated data if this result is [AuthResult.Success],
 * or throws the encapsulated [Throwable] if it is [AuthResult.Error].
 *
 * @return The successful data.
 * @throws Throwable if the result is an error.
 */
fun <T> AuthResult<T>.getOrThrow(): T {
    return when (this) {
        is AuthResult.Success -> data
        is AuthResult.Error -> throw throwable
    }
}