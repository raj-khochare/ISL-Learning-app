package com.signsathi.data.remote

import com.signsathi.data.remote.dto.LessonsResponse
import com.signsathi.data.remote.dto.UserProgressResponse
import retrofit2.http.GET

interface LessonApiService {

    @GET("lessons")
    suspend fun getLessons(): LessonsResponse

    @GET("progress")
    suspend fun getUserProgress(): UserProgressResponse
}