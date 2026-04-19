package com.example.nursewearconnect.data.api.interceptors

import com.example.nursewearconnect.data.security.SecurityManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val securityManager: SecurityManager,
    private val supabaseKey: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = securityManager.getToken()

        val requestBuilder = originalRequest.newBuilder()
            .header("apikey", supabaseKey)

        if (token != null) {
            requestBuilder.header("Authorization", "Bearer $token")
        } else {
            requestBuilder.header("Authorization", "Bearer $supabaseKey")
        }

        return chain.proceed(requestBuilder.build())
    }
}
