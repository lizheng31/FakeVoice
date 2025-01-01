package com.ron.fakevoice.data.audio

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject

class AudioPlayer @Inject constructor(
    private val context: Context
) {
    private var mediaPlayer: MediaPlayer? = null
    private var currentFile: File? = null
    
    fun playAudio(file: File) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(file.absolutePath)
                prepare()
                start()
            }
        } catch (e: Exception) {
            throw PlaybackException("Failed to play audio", e)
        }
    }

    fun playAudioFromUri(uri: String) {
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, Uri.parse(uri))
                prepare()
                start()
            }
        } catch (e: Exception) {
            throw PlaybackException("Failed to play audio from URI", e)
        }
    }

    fun pause() {
        mediaPlayer?.pause()
    }

    fun resume() {
        mediaPlayer?.start()
    }

    fun stop() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getDuration(): Int = mediaPlayer?.duration ?: 0

    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying ?: false

    fun getProgress(): Flow<Float> = flow {
        while (true) {
            val duration = getDuration()
            if (duration > 0) {
                emit(getCurrentPosition().toFloat() / duration)
            } else {
                emit(0f)
            }
            kotlinx.coroutines.delay(100)
        }
    }
}

class PlaybackException(message: String, cause: Throwable? = null) : Exception(message, cause) 