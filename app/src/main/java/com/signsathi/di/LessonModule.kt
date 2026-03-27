package com.signsathi.di

import com.signsathi.data.repository.LessonRepository
import com.signsathi.data.repository.LessonRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LessonModule {

    @Binds
    @Singleton
    abstract fun bindLessonRepository(
        impl: LessonRepositoryImpl
    ): LessonRepository
}