package com.signsathi.data.repository

interface AuthRepository {
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun confirmSignUp(email: String, code: String): Result<Unit>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun forgotPassword(email: String): Result<Unit>
    suspend fun resendConfirmationCode(email: String): Result<Unit>
    suspend fun signOut(): Result<Unit>      //  add this
    suspend fun isSignedIn(): Boolean
}