import com.tweener.passage.auth.supabase.SupabaseUserMapper
import com.tweener.passage.core.authplugin.AuthPlugin
import com.tweener.passage.core.error.PassageGatekeeperUnknownEntrantException
import com.tweener.passage.core.gatekeeper.email.model.PassageEmailVerificationParams
import com.tweener.passage.core.gatekeeper.email.model.PassageForgotPasswordParams
import com.tweener.passage.core.gatekeeper.email.model.PassageSignInLinkToEmailParams
import com.tweener.passage.core.model.ActionCodeType
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.AuthResult
import com.tweener.passage.core.model.EntrantInterface
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SupabaseAuthPlugin<T : EntrantInterface>(
    private val supabaseClient: SupabaseClient,
    private val supabaseUserMapper: SupabaseUserMapper<T>,
) : AuthPlugin<T> {

    override val currentUser: T?
        get() = supabaseClient.auth.currentUserOrNull()?.let { supabaseUserMapper.map(it) }

    override val authStateChanged: Flow<T?>
        get() = supabaseClient.auth.sessionStatus.map { status ->
            when (status) {
                is SessionStatus.Authenticated -> supabaseUserMapper.map(status.session.user!!)
                else -> null
            }
        }

    override suspend fun getCurrentUser(): AuthResult<T?> {
        return try {
            val user = supabaseClient.auth.currentUserOrNull()
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
                    supabaseClient.auth.signInWith(Email) {
                        email = credential.email
                        password = credential.password
                    }

                    val user = supabaseClient.auth.currentUserOrNull()
                        ?.let { supabaseUserMapper.map(it) }
                        ?: return AuthResult.Error(Exception("User is null"))

                    AuthResult.Success(user)
                }

                is AuthCredential.GoogleCredential -> {
                    supabaseClient.auth.signInWith(IDToken) {
                        idToken = credential.idToken
                        provider = Google
                        accessToken = credential.accessToken
                    }

                    val user = supabaseClient.auth.currentUserOrNull()
                        ?.let { supabaseUserMapper.map(it) }
                        ?: return AuthResult.Error(Exception("User is null"))

                    AuthResult.Success(user)
                }

                is AuthCredential.AppleCredential -> {
                    supabaseClient.auth.signInWith(IDToken) {
                        idToken = credential.idToken
                        nonce = credential.rawNonce
                        accessToken = credential.fullName as String
                        provider = Apple
                    }

                    val user = supabaseClient.auth.currentUserOrNull()
                        ?.let { supabaseUserMapper.map(it) }
                        ?: return AuthResult.Error(Exception("User is null"))

                    AuthResult.Success(user)
                }
            }
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun signUp(credential: AuthCredential): AuthResult<T> {
        return try {
            when (credential) {
                is AuthCredential.EmailCredential -> {
                    supabaseClient.auth.signUpWith(Email) {
                        email = credential.email
                        password = credential.password
                    }

                    val user = supabaseClient.auth.currentUserOrNull()
                        ?.let { supabaseUserMapper.map(it) }
                        ?: return AuthResult.Error(Exception("User is null"))

                    AuthResult.Success(user)
                }

                else -> AuthResult.Error(
                    UnsupportedOperationException("SignUp not supported for this credential")
                )
            }
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun reauthenticate(credential: AuthCredential): AuthResult<Unit> {
        return try {
            // Supabase doesn't have a direct reauthenticate method
            // The common pattern is to sign in again, which refreshes the session
            when (credential) {
                is AuthCredential.EmailCredential -> {
                    supabaseClient.auth.signInWith(Email) {
                        email = credential.email
                        password = credential.password
                    }
                    AuthResult.Success(Unit)
                }

                is AuthCredential.GoogleCredential -> {
                    supabaseClient.auth.signInWith(IDToken) {
                        idToken = credential.idToken
                        provider = Google
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
            supabaseClient.auth.resetPasswordForEmail(
                email = email,
                redirectUrl = params.url
            )
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun verifyPasswordResetCode(oobCode: String): AuthResult<String> {
        // Supabase handles password reset differently - no code verification step
        // The reset link contains a token that's validated server-side
        return AuthResult.Error(
            UnsupportedOperationException("Supabase doesn't use oobCode verification. Password reset is handled via email link.")
        )
    }

    override suspend fun confirmPasswordReset(
        oobCode: String,
        newPassword: String
    ): AuthResult<Unit> {
        return try {
            // In Supabase, password update happens after clicking the reset link
            // The user should be authenticated via the link, then update password
            supabaseClient.auth.updateUser {
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
            val email = supabaseClient.auth.currentUserOrNull()?.email
                ?: return AuthResult.Error(PassageGatekeeperUnknownEntrantException())

            supabaseClient.auth.resendEmail(
                type = OtpType.Email.SIGNUP,
                email = email
            )
            AuthResult.Success(Unit)
        } catch (e: Throwable) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun handleOobCode(
        oobCode: String,
        type: ActionCodeType
    ): AuthResult<Unit> {
        // Supabase doesn't use Firebase's oobCode pattern
        // Email verification and password reset are handled through direct links
        return AuthResult.Error(
            UnsupportedOperationException("Supabase doesn't use oobCode. Use direct email links instead.")
        )
    }

    override suspend fun sendSignInLinkToEmail(
        email: String,
        params: PassageSignInLinkToEmailParams
    ): AuthResult<Unit> {
        return try {
            supabaseClient.auth.signInWith(OTP) {
                this.email = email
                createUser = false
            }
            AuthResult.Success(Unit)
        } catch (e: Throwable) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun isSignInWithEmailLink(link: String): AuthResult<Boolean> {
        return try {
            // Check if the link contains Supabase auth tokens
            // Typically checks for 'access_token' or 'refresh_token' fragments
            val isValidLink = link.contains("access_token=") ||
                    link.contains("type=magiclink") ||
                    link.contains("type=recovery")
            AuthResult.Success(isValidLink)
        } catch (e: Throwable) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun signInWithEmailLink(
        email: String,
        link: String
    ): AuthResult<T> {
        return try {
            // Supabase OTP verification with token from email
            supabaseClient.auth.verifyEmailOtp(
                type = OtpType.Email.EMAIL,
                email = email,
                token = extractTokenFromLink(link)
            )

            val user = supabaseClient.auth.currentUserOrNull()
                ?.let { supabaseUserMapper.map(it) }
                ?: return AuthResult.Error(PassageGatekeeperUnknownEntrantException())

            AuthResult.Success(user)
        } catch (e: Throwable) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun signOut(): AuthResult<Unit> {
        return try {
            supabaseClient.auth.signOut()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun deleteCurrentUser(): AuthResult<Unit> {
        return try {
            val userId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return AuthResult.Error(Exception("No user"))

            // Supabase requires admin API to delete users
            // Alternative: call a custom edge function or admin endpoint
            supabaseClient.auth.admin.deleteUser(userId)

            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override fun mapPluginAuthError(throwable: Throwable): Throwable {
        return when {
            throwable.message?.contains("Invalid login credentials") == true ->
                IllegalArgumentException("Invalid email or password")
            throwable.message?.contains("User already registered") == true ->
                IllegalStateException("User already exists")
            throwable.message?.contains("Email not confirmed") == true ->
                IllegalStateException("Email not verified")
            else -> throwable
        }
    }

    /**
     * Extracts OTP token from magic link URL
     */
    private fun extractTokenFromLink(link: String): String {
        // Extract token from URL fragments or query parameters
        return link.substringAfter("token=")
            .substringBefore("&")
            .takeIf { it.isNotEmpty() }
            ?: throw IllegalArgumentException("Invalid magic link: no token found")
    }
}