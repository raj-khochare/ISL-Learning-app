package com.signsathi.data.repository

import android.util.Log
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.kotlin.core.Amplify
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor() : AuthRepository {

    override suspend fun signUp(email: String, password: String): Result<Unit> =
        runCatching {
            Amplify.Auth.signUp(
                email,
                password,
                AuthSignUpOptions.builder()
                    .userAttribute(AuthUserAttributeKey.email(), email)
                    .build()
            )
        }.map { Unit }

    override suspend fun confirmSignUp(email: String, code: String): Result<Unit> =
        runCatching {
            Amplify.Auth.confirmSignUp(email, code)
        }.map { Unit }

    override suspend fun signIn(email: String, password: String): Result<Unit> =
        runCatching {
            // ✅ sign out any existing session first
            try {
                Amplify.Auth.signOut()
            } catch (e: Exception) {
                Log.i("AuthRepository", "No existing session to sign out")
            }

            // now sign in fresh
            val result = Amplify.Auth.signIn(email, password)
            if (!result.isSignedIn) throw Exception("Sign in incomplete")
        }

    override suspend fun forgotPassword(email: String): Result<Unit> =
        runCatching {
            Amplify.Auth.resetPassword(email)
        }.map { Unit }

    override suspend fun resendConfirmationCode(email: String): Result<Unit> =
        runCatching {
            Amplify.Auth.resendSignUpCode(email)
        }.map { Unit }

    override suspend fun signOut(): Result<Unit> =
        runCatching {
            Amplify.Auth.signOut()
        }

    override suspend fun isSignedIn(): Boolean =
        try {
            Amplify.Auth.getCurrentUser()
            true   // no exception = user exists
        } catch (e: Exception) {
            false  // exception = no user signed in
        }

}