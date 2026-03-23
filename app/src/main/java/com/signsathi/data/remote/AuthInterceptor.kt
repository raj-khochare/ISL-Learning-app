package com.signsathi.data.remote

import com.amplifyframework.kotlin.core.Amplify
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber
import javax.inject.Inject

/**
 * Attaches the current Cognito JWT (id token) to every API request
 * as an Authorization header.
 *
 * API Gateway is configured to require authenticated users only,
 * so every request must carry a valid token or it returns 401.
 *
 * We fetch the token fresh each time — Amplify caches it internally
 * and refreshes it automatically when it expires, so this is safe
 * and always returns a valid token as long as the user is signed in.
 */
class AuthInterceptor @Inject constructor() : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { fetchToken() }

        val request = chain.request().newBuilder()
            .addHeader("Authorization", token)
            .build()

        return chain.proceed(request)
    }

    private suspend fun fetchToken(): String {
        return try {
            val session = Amplify.Auth.fetchAuthSession()
            // Amplify returns the session as a string — extract the id token
            // The toString() of AWSCognitoAuthSession contains the tokens
            val sessionStr = session.toString()
            val idToken = sessionStr
                .substringAfter("idToken=AWSCognitoUserPoolTokens(idToken=")
                .substringBefore(",")
                .trim()
            idToken
        } catch (e: Exception) {
            Timber.e(e, "AuthInterceptor: failed to fetch token")
            ""
        }
    }
}