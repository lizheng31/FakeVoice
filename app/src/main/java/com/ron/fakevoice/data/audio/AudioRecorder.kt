package com.ron.fakevoice.data.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.FileOutputStream

class AudioRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null

    fun startRecording(): File {
        val file = File(context.cacheDir, "recording_${System.currentTimeMillis()}.mp3")
        currentFile = file

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(file).fd)
            
            try {
                prepare()
                start()
            } catch (e: Exception) {
                throw RecordingException("Failed to start recording", e)
            }
        }

        return file
    }

    fun stopRecording() {
        try {
            recorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            throw RecordingException("Failed to stop recording", e)
        } finally {
            recorder = null
        }
    }

    fun cancelRecording() {
        recorder?.apply {
            try {
                stop()
                release()
            } catch (e: Exception) {
                // Ignore exceptions during cancellation
            }
        }
        currentFile?.delete()
        recorder = null
        currentFile = null
    }
}

class RecordingException(message: String, cause: Throwable? = null) : Exception(message, cause) 