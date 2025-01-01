package com.ron.fakevoice.data.api

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface SiliconFlowApi {
    @POST("audio/speech")
    suspend fun createSpeech(
        @Body request: CreateSpeechRequest
    ): Response<ResponseBody>

    @Multipart
    @POST("uploads/audio/voice")
    suspend fun uploadVoice(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("customName") customName: RequestBody,
        @Part("text") text: RequestBody
    ): Response<UploadVoiceResponse>

    @GET("audio/voice/list")
    suspend fun getVoiceList(): Response<VoiceListResponse>

    @POST("audio/voice/deletions")
    suspend fun deleteVoice(
        @Body request: DeleteVoiceRequest
    ): Response<Unit>
}

data class CreateSpeechRequest(
    val model: String = "fishaudio/fish-speech-1.5",
    val input: String,
    val voice: String,
    val response_format: String = "mp3",
    val stream: Boolean = true
)

data class UploadVoiceResponse(
    val uri: String
)

data class VoiceListResponse(
    val voices: List<VoiceInfo>
)

data class VoiceInfo(
    val uri: String,
    val customName: String
)

data class DeleteVoiceRequest(
    val uri: String
) 