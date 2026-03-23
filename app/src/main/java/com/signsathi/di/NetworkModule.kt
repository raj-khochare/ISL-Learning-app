package com.signsathi.di

import android.content.Context
import androidx.room.Room
import com.signsathi.data.local.AppDatabase
import com.signsathi.data.local.dao.LessonDao
import com.signsathi.data.local.dao.UserProgressDao
import com.signsathi.data.remote.AuthInterceptor
import com.signsathi.data.remote.LessonApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ─── API base URL ────────────────────────────────────────────────────────
    // This is your deployed API Gateway endpoint.
    // Trailing slash is required by Retrofit.
    private const val BASE_URL =
        "https://5lxpwrx38a.execute-api.ap-south-1.amazonaws.com/dev/"

    // ─── OkHttp ──────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideAuthInterceptor(): AuthInterceptor = AuthInterceptor()

    @Provides
    @Singleton
    fun provideOkHttpClient(authInterceptor: AuthInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            // Log full request/response bodies in debug builds only
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)   // auth first
            .addInterceptor(logging)            // then logging
            .build()
    }

    // ─── Retrofit ────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    @Provides
    @Singleton
    fun provideLessonApiService(retrofit: Retrofit): LessonApiService =
        retrofit.create(LessonApiService::class.java)

    // ─── Room ─────────────────────────────────────────────────────────────────

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "signsathi.db"
        )
            .fallbackToDestructiveMigration()   // safe during development
            // Switch to proper migrations before production release
            .build()

    @Provides
    @Singleton
    fun provideLessonDao(db: AppDatabase): LessonDao = db.lessonDao()

    @Provides
    @Singleton
    fun provideUserProgressDao(db: AppDatabase): UserProgressDao = db.userProgressDao()
}