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
    
    suspend fun createSpeech(text: String, model: String = Constants.DEFAULT_MODEL, voice: String, language: String, dialect: String, emotion: String): Flow<ByteArray> = flow {
        try {
            // 构建富文本控制标记
            val controlledText = buildString {
                if (model == "FunAudioLLM/CosyVoice2-0.5B") {
                    // 直接使用文本，不添加任何标记
                    append(text)
                } else {
                    append(text)
                }
            }

            println("""
                ┌──────────── Speech Request ────────────
                │ Model: $model
                │ Voice: $voice
                │ Text: $controlledText
                └─────────────────────────────────────────
            """.trimIndent())
            
            val request = CreateSpeechRequest(
                model = model,
                input = controlledText,
                voice = voice,
                response_format = "mp3",
                stream = true,
                speed = 1.0f,
                gain = 0.0f,
                sample_rate = 44100
            )
            
            println("""
                ┌──────────── Request Body ────────────
                │ ${request.toString()}
                └─────────────────────────────────────
            """.trimIndent())
            
            val response = api.createSpeech(request)
            
            if (response.isSuccessful) {
                println("""
                    ┌──────────── Response Success ────────────
                    │ Status Code: ${response.code()}
                    │ Content Length: ${response.body()?.contentLength() ?: 0} bytes
                    └──────────────────────────────────────────
                """.trimIndent())
                
                response.body()?.bytes()?.let { emit(it) }
            } else {
                val errorBody = response.errorBody()?.string()
                println("""
                    ┌──────────── Response Error ────────────
                    │ Status Code: ${response.code()}
                    │ Error Body: $errorBody
                    └─────────────────────────────────────────
                """.trimIndent())
                throw Exception(errorBody ?: "Failed to create speech: ${response.code()}")
            }
        } catch (e: Exception) {
            println("""
                ┌──────────── Network Error ────────────
                │ Error: ${e.message}
                │ Stack Trace: ${e.stackTraceToString()}
                └─────────────────────────────────────────
            """.trimIndent())
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

    suspend fun uploadReferenceVoice(audioFile: File, text: String): String {
        try {
            println("""
                ┌──────────── Upload Reference Voice Request ────────────
                │ File: ${audioFile.name} (${audioFile.length()} bytes)
                │ Text: $text
                │ Model: ${Constants.DEFAULT_MODEL}
                └───────────────────────────────────────────────────────
            """.trimIndent())
            
            val fileRequestBody = audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", audioFile.name, fileRequestBody)
            val modelPart = Constants.DEFAULT_MODEL.toRequestBody("text/plain".toMediaTypeOrNull())
            val customNamePart = "reference_voice_${System.currentTimeMillis()}".toRequestBody("text/plain".toMediaTypeOrNull())
            val textPart = text.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.uploadReferenceVoice(
                file = filePart,
                model = modelPart,
                customName = customNamePart,
                text = textPart
            )

            if (response.isSuccessful) {
                val uri = response.body()?.uri
                println("""
                    ┌──────────── Upload Success ────────────
                    │ Status Code: ${response.code()}
                    │ URI: $uri
                    └──────────────────────────────────────
                """.trimIndent())
                return uri ?: throw Exception("Upload response is empty")
            } else {
                val errorBody = response.errorBody()?.string()
                println("""
                    ┌──────────── Upload Error ────────────
                    │ Status Code: ${response.code()}
                    │ Error Body: $errorBody
                    └────────────────────────────────────
                """.trimIndent())
                throw Exception(errorBody ?: "Failed to upload reference voice")
            }
        } catch (e: Exception) {
            println("""
                ┌──────────── Upload Error ────────────
                │ Error: ${e.message}
                │ Stack Trace: ${e.stackTraceToString()}
                └────────────────────────────────────
            """.trimIndent())
            throw Exception("Failed to upload reference voice: ${e.message}")
        }
    }
} 