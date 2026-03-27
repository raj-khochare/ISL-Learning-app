package com.signsathi.di

import android.content.Context
import com.signsathi.data.recognition.CameraManager
import com.signsathi.data.recognition.ModelManager
import com.signsathi.data.recognition.SignRecognizer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RecognitionModule {

    @Provides
    @Singleton
    fun provideModelManager(
        @ApplicationContext context: Context
    ): ModelManager = ModelManager(context)

    @Provides
    @Singleton
    fun provideSignRecognizer(
        @ApplicationContext context: Context,
        modelManager: ModelManager
    ): SignRecognizer = SignRecognizer(context, modelManager)

    @Provides
    @Singleton
    fun provideCameraManager(
        @ApplicationContext context: Context
    ): CameraManager = CameraManager(context)
}