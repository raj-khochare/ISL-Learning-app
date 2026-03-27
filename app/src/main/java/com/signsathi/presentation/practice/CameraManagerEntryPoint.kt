package com.signsathi.presentation.practice

import com.signsathi.data.recognition.CameraManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CameraManagerEntryPoint {
    fun cameraManager(): CameraManager
}