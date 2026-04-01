package com.tweener.passage.auth.supabase

import com.tweener.passage.core.authplugin.AuthPlugin
import com.tweener.passage.core.error.PassageEmailAddressAlreadyExistsException
import com.tweener.passage.core.error.PassageGatekeeperUnknownEntrantException
import com.tweener.passage.core.error.PassageInvalidCredentialsException
import com.tweener.passage.core.error.PassageSignInLinkToEmailException
import com.tweener.passage.core.gatekeeper.email.model.PassageEmailVerificationParams
import com.tweener.passage.core.gatekeeper.email.model.PassageForgotPasswordParams
import com.tweener.passage.core.gatekeeper.email.model.PassageSignInLinkToEmailParams
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.AuthResult
import com.tweener.passage.core.model.EntrantInterface
import com.tweener.passage.core.model.PassageUniversalLinkMode
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.OtpVerifyResult
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Supabase implementation of [AuthPlugin].
 *
 * This class delegates authentication operations to the Supabase Auth API,
 * mapping Supabase-specific user representations to the domain model via [SupabaseUserMapper].
 *
 * Some operations that are Firebase-specific (e.g., `handleOobCode`, `verifyPasswordResetCode`)
 * return [UnsupportedOperationException] because Supabase handles those flows differently.
 *
 * @param T The domain user type, constrained to [EntrantInterface].
 * @property supabaseAuth The Supabase client auth plugin instance.
 * @property supabaseUserMapper The mapper that converts Supabase [io.github.jan.supabase.auth.user.UserInfo] to [T].
 *
 * @author Chirag Redij
 * @since 01/04/2026
 */
class SupabaseAuthPlugin<T : EntrantInterface>(
    private val supabaseAuth: Auth,
    private val supabaseUserMapper: SupabaseUserMapper<T>,
) : AuthPlugin<T> {

    override val currentUser: T?
        get() = supabaseAuth.currentUserOrNull()?.let { supabaseUserMapper.map(it) }

    override val authStateChanged: Flow<T?>
        get() = supabaseAuth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> status.session.user?.let { supabaseUserMapper.map(it) }
                else -> null
            }
        }

    override suspend fun getCurrentUser(): AuthResult<T?> {
        return try {
            val user = supabaseAuth.currentUserOrNull()
                ?.let { supabaseUserMapper.map(it) }

            if (user != null) {
                AuthResult.Success(user)
            } else {
                AuthResult.Error(IllegalStateException("No user is currently signed in."))
            }
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun signIn(credential: AuthCredential): AuthResult<T> {
        return try {
            when (credential) {
                is AuthCredential.EmailCredential -> {
                    supabaseAuth.signInWith(Email) {
                        email = credential.email
                        password = credential.password
                    }
                }

                is AuthCredential.GoogleCredential -> {
                    supabaseAuth.signInWith(IDToken) {
                        idToken = credential.idToken
                        provider = Google
                        accessToken = credential.accessToken
                    }
                }

                is AuthCredential.AppleCredential -> {
                    supabaseAuth.signInWith(IDToken) {
                        idToken = credential.idToken
                        nonce = credential.rawNonce
                        provider = Apple
                    }
                }
            }

            val user = supabaseAuth.currentUserOrNull()
                ?.let { supabaseUserMapper.map(it) }
                ?: return AuthResult.Error(PassageGatekeeperUnknownEntrantException())

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun signUp(credential: AuthCredential): AuthResult<T> {
        return try {
            when (credential) {
                is AuthCredential.EmailCredential -> {
                    supabaseAuth.signUpWith(Email) {
                        email = credential.email
                        password = credential.password
                    }
                }

                else -> return AuthResult.Error(
                    UnsupportedOperationException("SignUp not supported for this credential")
                )
            }

            val user = supabaseAuth.currentUserOrNull()
                ?.let { supabaseUserMapper.map(it) }
                ?: return AuthResult.Error(PassageGatekeeperUnknownEntrantException())

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun reauthenticate(credential: AuthCredential): AuthResult<Unit> {
        return try {
            // Supabase doesn't have a direct reauthenticate method.
            // The common pattern is to sign in again, which refreshes the session.
            when (credential) {
                is AuthCredential.EmailCredential -> {
                    supabaseAuth.signInWith(Email) {
                        email = credential.email
                        password = credential.password
                    }
                    AuthResult.Success(Unit)
                }

                is AuthCredential.GoogleCredential -> {
                    supabaseAuth.signInWith(IDToken) {
                        idToken = credential.idToken
                        provider = Google
                        accessToken = credential.accessToken
                    }
                    AuthResult.Success(Unit)
                }

                else -> AuthResult.Error(
                    UnsupportedOperationException("Unsupported credential for reauthentication")
                )
            }
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun sendPasswordResetEmail(
        email: String,
        params: PassageForgotPasswordParams
    ): AuthResult<Unit> {
        return try {
            supabaseAuth.resetPasswordForEmail(
                email = email,
                redirectUrl = params.url
            )
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun verifyPasswordResetCode(oobCode: String): AuthResult<String> {
        // Supabase handles password reset differently — no code verification step.
        // The reset link contains a token that's validated server-side.
        return AuthResult.Error(
            UnsupportedOperationException("Supabase doesn't use oobCode verification. Password reset is handled via email link.")
        )
    }

    override suspend fun confirmPasswordReset(
        oobCode: String,
        newPassword: String
    ): AuthResult<Unit> {
        return try {
            // In Supabase, password update happens after clicking the reset link.
            // The user should be authenticated via the link, then update password.
            supabaseAuth.updateUser {
                password = newPassword
            }
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun sendEmailVerification(
        params: PassageEmailVerificationParams
    ): AuthResult<Unit> {
        return try {
            val email = supabaseAuth.currentUserOrNull()?.email
                ?: return AuthResult.Error(PassageGatekeeperUnknownEntrantException())

            supabaseAuth.resendEmail(
                type = OtpType.Email.SIGNUP,
                email = email
            )
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun handleOobCode(
        oobCode: String,
        mode: PassageUniversalLinkMode
    ): AuthResult<Unit> {

        val email = currentUser?.email
            ?: return AuthResult.Error(
                Exception("Email is required to verify OTP")
            )

        return try {
            val result = when (mode) {
                PassageUniversalLinkMode.VERIFY_EMAIL -> {
                    supabaseAuth.verifyEmailOtp(
                        type = OtpType.Email.EMAIL,
                        email = email,
                        token = oobCode
                    )
                }

                PassageUniversalLinkMode.RESET_PASSWORD -> {
                    supabaseAuth.verifyEmailOtp(
                        type = OtpType.Email.RECOVERY,
                        email = email,
                        token = oobCode
                    )
                }

                PassageUniversalLinkMode.SIGN_IN_EMAIL -> {
                    supabaseAuth.verifyEmailOtp(
                        type = OtpType.Email.SIGNUP,
                        email = email,
                        token = oobCode
                    )
                }
            }

            when (result) {
                is OtpVerifyResult.Authenticated -> {
                    AuthResult.Success(Unit)
                }

                OtpVerifyResult.VerifiedNoSession -> {
                    AuthResult.Error(
                        Exception(
                            "OTP verified but no session was created"
                        )
                    )
                }
            }

        } catch (e: Exception) {
            AuthResult.Error(
                Exception(
                    message = e.message ?: "OTP verification failed",
                    cause = e
                )
            )
        }
    }

    override suspend fun sendSignInLinkToEmail(
        email: String,
        params: PassageSignInLinkToEmailParams
    ): AuthResult<Unit> {
        return try {
            supabaseAuth.signInWith(OTP) {
                this.email = email
                createUser = false
            }
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun isSignInWithEmailLink(link: String): AuthResult<Boolean> {
        return try {
            // Check if the link contains Supabase auth tokens
            val isValidLink = link.contains("access_token=") ||
                    link.contains("type=magiclink") ||
                    link.contains("type=recovery")
            AuthResult.Success(isValidLink)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun signInWithEmailLink(
        email: String,
        link: String
    ): AuthResult<T> {
        return try {
            supabaseAuth.verifyEmailOtp(
                type = OtpType.Email.EMAIL,
                email = email,
                token = extractTokenFromLink(link)
            )

            val user = supabaseAuth.currentUserOrNull()
                ?.let { supabaseUserMapper.map(it) }
                ?: return AuthResult.Error(PassageGatekeeperUnknownEntrantException())

            AuthResult.Success(user)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun signOut(): AuthResult<Unit> {
        return try {
            supabaseAuth.signOut()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun deleteCurrentUser(): AuthResult<Unit> {
        // Supabase Admin APIs require a service-role key and must not be called from client/mobile code.
        // User deletion should be implemented via a secure server-side endpoint that uses the Admin API.
        return AuthResult.Error(
            UnsupportedOperationException(
                "Deleting users must be performed via a server-side endpoint using Supabase Admin API."
            )
        )

//        return try {
//            val userId = supabaseAuth.currentUserOrNull()?.id
//                ?: return AuthResult.Error(PassageGatekeeperUnknownEntrantException())
//
//            // Supabase requires admin API to delete users
//            supabaseAuth.admin.deleteUser(userId)
//
//            AuthResult.Success(Unit)
//        } catch (e: Exception) {
//            AuthResult.Error(mapPluginAuthError(e))
//        }
    }

    override fun mapPluginAuthError(throwable: Throwable): Throwable {
        val message = throwable.message ?: return throwable
        return when {
            message.contains("Invalid login credentials") -> PassageInvalidCredentialsException()
            message.contains("User already registered") -> PassageEmailAddressAlreadyExistsException()
            message.contains("Email not confirmed") -> IllegalStateException("Email not verified")
            else -> throwable
        }
    }

    /**
     * Extracts the OTP token from a magic link URL by parsing the `token` query parameter.
     *
     * @throws IllegalArgumentException if the link does not contain a valid token.
     */
    private fun extractTokenFromLink(link: String): String {
        if (!link.contains("token=")) {
            throw IllegalArgumentException("Invalid magic link: no token found")
        }

        return link.substringAfter("token=")
            .substringBefore("&")
            .takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Invalid magic link: empty token value")
    }
}