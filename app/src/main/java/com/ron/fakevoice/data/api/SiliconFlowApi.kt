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

    @Multipart
    @POST("uploads/audio/voice")
    suspend fun uploadReferenceVoice(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("customName") customName: RequestBody,
        @Part("text") text: RequestBody
    ): Response<UploadReferenceVoiceResponse>
}

data class CreateSpeechRequest(
    val model: String,
    val input: String,
    val voice: String? = null,
    val response_format: String = "mp3",
    val stream: Boolean = true,
    val speed: Float = 1.0f,
    val gain: Float = 0.0f,
    val sample_rate: Int = 44100
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

data class UploadReferenceVoiceResponse(
    val uri: String  // 返回的参考音色 ID
) 