package com.ron.fakevoice.data.repository

import com.ron.fakevoice.data.api.SiliconFlowApi
import com.ron.fakevoice.data.api.CreateSpeechRequest
import com.ron.fakevoice.data.api.DeleteVoiceRequest
import com.ron.fakevoice.data.api.VoiceInfo
import com.ron.fakevoice.data.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class VoiceRepository @Inject constructor(
    private val api: SiliconFlowApi
) {
    
    suspend fun createSpeech(text: String, model: String = Constants.DEFAULT_MODEL, voice: String): Flow<ByteArray> = flow {
        try {
            println("Debug - Sending request with model: $model")
            println("Debug - Request text: $text")
            println("Debug - Using voice: $voice")
            
            val request = CreateSpeechRequest(
                model = model,
                input = text,
                voice = voice,
                response_format = "mp3",
                stream = true,
                speed = 1.0f,
                gain = 0.0f,
                sample_rate = 44100
            )
            
            println("Debug - Request body: $request")
            
            val response = api.createSpeech(request)
            
            if (response.isSuccessful) {
                response.body()?.bytes()?.let { emit(it) }
            } else {
                val errorBody = response.errorBody()?.string()
                println("Debug - Error response: $errorBody")
                throw Exception(errorBody ?: "Failed to create speech: ${response.code()}")
            }
        } catch (e: Exception) {
            println("Debug - Network error: ${e.message}")
            throw Exception("Network error: ${e.message}")
        }
    }.flowOn(Dispatchers.IO)

    suspend fun uploadVoice(file: File, text: String): String {
        val fileRequestBody = file.asRequestBody("audio/*".toMediaTypeOrNull())
        val filePart = MultipartBody.Part.createFormData("file", file.name, fileRequestBody)
        
        val modelPart = "fishaudio/fish-speech-1.5".toRequestBody("text/plain".toMediaTypeOrNull())
        val customNamePart = "voice_${System.currentTimeMillis()}".toRequestBody("text/plain".toMediaTypeOrNull())
        val textPart = text.toRequestBody("text/plain".toMediaTypeOrNull())

        val response = api.uploadVoice(filePart, modelPart, customNamePart, textPart)
        
        if (response.isSuccessful) {
            return response.body()?.uri ?: throw Exception("Upload response is empty")
        } else {
            val errorBody = response.errorBody()?.string()
            throw Exception(errorBody ?: "Failed to upload voice")
        }
    }

    suspend fun getVoiceList(): List<VoiceInfo> {
        val response = api.getVoiceList()
        if (response.isSuccessful) {
            return response.body()?.voices ?: emptyList()
        } else {
            val errorBody = response.errorBody()?.string()
            throw Exception(errorBody ?: "Failed to get voice list")
        }
    }

    suspend fun deleteVoice(uri: String) {
        val response = api.deleteVoice(DeleteVoiceRequest(uri))
        if (!response.isSuccessful) {
            val errorBody = response.errorBody()?.string()
            throw Exception(errorBody ?: "Failed to delete voice")
        }
    }
} 