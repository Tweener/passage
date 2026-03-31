package com.tweener.passage.auth.firebase

import com.tweener.passage.core.authplugin.AuthPlugin
import com.tweener.passage.core.error.PassageGatekeeperUnknownEntrantException
import com.tweener.passage.core.gatekeeper.email.model.PassageEmailVerificationParams
import com.tweener.passage.core.gatekeeper.email.model.PassageForgotPasswordParams
import com.tweener.passage.core.gatekeeper.email.model.PassageSignInLinkToEmailParams
import com.tweener.passage.core.model.ActionCodeType
import com.tweener.passage.core.model.AuthCredential
import com.tweener.passage.core.model.AuthResult
import com.tweener.passage.core.model.EntrantInterface
import dev.gitlive.firebase.auth.ActionCodeResult
import dev.gitlive.firebase.auth.ActionCodeSettings
import dev.gitlive.firebase.auth.AndroidPackageName
import dev.gitlive.firebase.auth.EmailAuthProvider
import dev.gitlive.firebase.auth.FirebaseAuth
import dev.gitlive.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Project       : Passage
 * Author        : Chirag Redij
 * Created on    : Tuesday, 31/03/26 at 12:12
 * -------------------------------------------------------------------------------------
 * Last updated  : chiragredij on Tuesday, 31/03/26 at 12:12
 *
 * Description   : [Add a brief description of this file or component]
 *
 * Copyright (c) 2026 ChiragRedij. All rights reserved.
 */

class FirebaseAuthPlugin<T : EntrantInterface>(
    private val firebaseAuth: FirebaseAuth,
    private val firebaseUserMapper: FirebaseUserMapper<T>,
) : AuthPlugin<T> {

    val appleSignInDelegate: AppleSignInDelegate<T> = provideAppleSignInDelegate(firebaseAuth, firebaseUserMapper)

    override val currentUser: T?
        get() = firebaseAuth.currentUser?.let { firebaseUserMapper.map(it) }

    override val authStateChanged: Flow<T?>
        get() = firebaseAuth.authStateChanged.map { user ->
            user?.let { firebaseUserMapper.map(it) }
        }

    override suspend fun getCurrentUser(): AuthResult<T?> {
        return firebaseAuth.currentUser
            ?.let { firebaseUserMapper.map(it) }
            ?.let { AuthResult.Success(it) }
            ?: AuthResult.Error(IllegalStateException("No user is currently signed in."))
    }

    override suspend fun signIn(credential: AuthCredential): AuthResult<T> {
        return try {
            when (credential) {

                is AuthCredential.EmailCredential -> {
                    val result = firebaseAuth
                        .signInWithEmailAndPassword(
                            credential.email,
                            credential.password
                        )

                    val user = result.user
                        ?.let { firebaseUserMapper.map(it) }
                        ?: return AuthResult.Error(Exception("User is null"))

                    AuthResult.Success(user)
                }

                is AuthCredential.GoogleCredential -> {
                    val firebaseCredential = GoogleAuthProvider.credential(
                        credential.idToken,
                        credential.accessToken
                    )

                    val result = firebaseAuth
                        .signInWithCredential(firebaseCredential)


                    val user = result.user
                        ?.let { firebaseUserMapper.map(it) }
                        ?: return AuthResult.Error(Exception("User is null"))

                    AuthResult.Success(user)
                }

                is AuthCredential.AppleCredential -> {
                    appleSignInDelegate.signInWithApple(credential)
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
                    val result = firebaseAuth.createUserWithEmailAndPassword(
                        credential.email,
                        credential.password
                    )

                    val user = result.user
                        ?.let { firebaseUserMapper.map(it) }
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
            val user = firebaseAuth.currentUser
                ?: return AuthResult.Error(Exception("No user"))

            val firebaseCredential = when (credential) {
                is AuthCredential.EmailCredential -> EmailAuthProvider.credential(
                    credential.email,
                    credential.password
                )

                is AuthCredential.GoogleCredential -> GoogleAuthProvider.credential(
                    credential.idToken,
                    credential.accessToken
                )

                else -> return AuthResult.Error(
                    UnsupportedOperationException("Unsupported credential")
                )
            }

            user.reauthenticate(firebaseCredential)
            AuthResult.Success(Unit)

        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun sendPasswordResetEmail(
        email: String,
        params: PassageForgotPasswordParams
    ): AuthResult<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email)
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun verifyPasswordResetCode(oobCode: String): AuthResult<String> {
        return try {
            val email = firebaseAuth.verifyPasswordResetCode(oobCode)
            AuthResult.Success(email)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun confirmPasswordReset(
        oobCode: String,
        newPassword: String
    ): AuthResult<Unit> {
        return try {
            firebaseAuth.confirmPasswordReset(oobCode, newPassword)
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun sendEmailVerification(
        params: PassageEmailVerificationParams
    ): AuthResult<Unit> {
        return try {
            val actionCodeSettings = buildActionCodeSettings(
                url = params.url,
                linkDomain = params.hostingDomain,
                iOSBundleId = params.iosParams?.bundleId,
                androidPackageName = params.androidParams?.packageName,
                installIfNotAvailable = params.androidParams?.installIfNotAvailable ?: true,
                minimumVersion = params.androidParams?.minimumVersion,
                canHandleCodeInApp = params.canHandleCodeInApp,
            )

            firebaseAuth.currentUser
                ?.sendEmailVerification(actionCodeSettings)
                ?: return AuthResult.Error(PassageGatekeeperUnknownEntrantException())

            AuthResult.Success(Unit)
        } catch (e: Throwable) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun handleOobCode(
        oobCode: String,
        type: ActionCodeType
    ): AuthResult<Unit> {
        return try {

            when (type) {

                ActionCodeType.VerifyEmail -> {
                    firebaseAuth.checkActionCode<ActionCodeResult.VerifyEmail>(oobCode)
                    firebaseAuth.applyActionCode(oobCode)
                    firebaseAuth.currentUser?.reload()
                }

                ActionCodeType.PasswordReset -> {
                    firebaseAuth.checkActionCode<ActionCodeResult.PasswordReset>(oobCode)
                    // DO NOT apply here → handled separately via confirmPasswordReset
                }

                ActionCodeType.SignInWithEmailLink -> {
                    firebaseAuth.checkActionCode<ActionCodeResult.SignInWithEmailLink>(oobCode)
                    // No applyActionCode needed
                }
            }

            AuthResult.Success(Unit)

        } catch (e: Throwable) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun sendSignInLinkToEmail(
        email: String,
        params: PassageSignInLinkToEmailParams
    ): AuthResult<Unit> {
        return try {
            val actionCodeSettings = buildActionCodeSettings(
                url = params.url,
                linkDomain = params.hostingDomain,
                iOSBundleId = params.iosParams?.bundleId,
                androidPackageName = params.androidParams?.packageName,
                installIfNotAvailable = params.androidParams?.installIfNotAvailable ?: true,
                minimumVersion = params.androidParams?.minimumVersion,
                canHandleCodeInApp = params.canHandleCodeInApp,
            )

            firebaseAuth.sendSignInLinkToEmail(email, actionCodeSettings)
            AuthResult.Success(Unit)

        } catch (e: Throwable) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun isSignInWithEmailLink(link: String): AuthResult<Boolean> {
        return try {
            val result = firebaseAuth.isSignInWithEmailLink(link)
            AuthResult.Success(result)
        } catch (e: Throwable) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun signInWithEmailLink(
        email: String,
        link: String
    ): AuthResult<T> {
        return try {
            if (!firebaseAuth.isSignInWithEmailLink(link)) {
                return AuthResult.Error(Exception("Invalid email link"))
            }

            val result = firebaseAuth.signInWithEmailLink(email, link)

            val user = result.user
                ?.let { firebaseUserMapper.map(it) }
                ?: return AuthResult.Error(PassageGatekeeperUnknownEntrantException())

            AuthResult.Success(user)

        } catch (e: Throwable) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun signOut(): AuthResult<Unit> {
        return try {
            firebaseAuth.signOut()
            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    override suspend fun deleteCurrentUser(): AuthResult<Unit> {
        return try {
            firebaseAuth.currentUser?.delete()
                ?: return AuthResult.Error(Exception("No user"))

            AuthResult.Success(Unit)
        } catch (e: Exception) {
            AuthResult.Error(mapPluginAuthError(e))
        }
    }

    private fun buildActionCodeSettings(
        url: String,
        linkDomain: String,
        iOSBundleId: String?,
        androidPackageName: String?,
        installIfNotAvailable: Boolean,
        minimumVersion: String?,
        canHandleCodeInApp: Boolean,
    ): ActionCodeSettings {
        return ActionCodeSettings(
            url = url,
            linkDomain = linkDomain,
            androidPackageName = androidPackageName?.let {
                AndroidPackageName(
                    packageName = it,
                    installIfNotAvailable = installIfNotAvailable,
                    minimumVersion = minimumVersion
                )
            },
            iOSBundleId = iOSBundleId,
            canHandleCodeInApp = canHandleCodeInApp,
        )
    }

    override fun mapPluginAuthError(throwable: Throwable): Throwable {
        return throwable // extend later with Firebase error mapping
    }
}