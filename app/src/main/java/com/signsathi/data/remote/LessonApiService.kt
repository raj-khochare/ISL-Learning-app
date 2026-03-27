package com.signsathi.data.remote

import com.signsathi.data.remote.dto.CompleteLessonRequest
import com.signsathi.data.remote.dto.CompleteLessonResponse
import com.signsathi.data.remote.dto.LessonsResponse
import com.signsathi.data.remote.dto.UserProgressResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface LessonApiService {

    @GET("lessons")
    suspend fun getLessons(): LessonsResponse

    @GET("progress")
    suspend fun getUserProgress(): UserProgressResponse

    @POST("progress/complete")       // ← add this
    suspend fun completeLesson(@Body request: CompleteLessonRequest): CompleteLessonResponse

}