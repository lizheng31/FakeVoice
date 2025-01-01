package com.ron.fakevoice.ui.viewmodel

import android.media.MediaPlayer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ron.fakevoice.data.repository.VoiceRepository
import com.ron.fakevoice.data.audio.AudioRecorder
import com.ron.fakevoice.data.audio.AudioPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import androidx.compose.runtime.State
import com.ron.fakevoice.data.api.VoiceInfo

@HiltViewModel
class VoiceViewModel @Inject constructor(
    private val repository: VoiceRepository,
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val mediaPlayer = MediaPlayer()
    
    var inputText by mutableStateOf("")
        private set
        
    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState
    
    private val _voiceList = MutableStateFlow<List<VoiceInfo>>(emptyList())
    val voiceList: StateFlow<List<VoiceInfo>> = _voiceList

    private var recordingFile: File? = null
    private val _isRecording = mutableStateOf(false)
    val isRecording: State<Boolean> = _isRecording

    private var _playbackProgress = MutableStateFlow(0f)
    val playbackProgress: StateFlow<Float> = _playbackProgress

    private var _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    private var playbackJob: Job? = null

    init {
        loadVoiceList()
    }

    fun onTextInput(text: String) {
        inputText = text
    }

    fun createSpeech(voice: String) {
        if (inputText.isBlank()) {
            _uiState.value = UiState.Error("Please enter text")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repository.createSpeech(inputText, voice).collect { bytes ->
                    // Save to temp file and play
                    val tempFile = File.createTempFile("speech", ".mp3")
                    FileOutputStream(tempFile).use { it.write(bytes) }
                    playAudio(tempFile.absolutePath)
                    _uiState.value = UiState.Success
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to create speech")
            }
        }
    }

    fun uploadVoice(audioFile: File) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repository.uploadVoice(audioFile, inputText)
                loadVoiceList()
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to upload voice")
            }
        }
    }

    fun deleteVoice(uri: String) {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                repository.deleteVoice(uri)
                loadVoiceList()
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to delete voice")
            }
        }
    }

    fun startRecording() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                recordingFile = audioRecorder.startRecording()
                _isRecording.value = true
                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to start recording")
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading
                audioRecorder.stopRecording()
                _isRecording.value = false
                recordingFile?.let { file ->
                    uploadVoice(file)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to stop recording")
            }
        }
    }

    fun cancelRecording() {
        viewModelScope.launch {
            try {
                audioRecorder.cancelRecording()
                _isRecording.value = false
                recordingFile = null
                _uiState.value = UiState.Idle
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to cancel recording")
            }
        }
    }

    fun playAudio(filePath: String) {
        viewModelScope.launch {
            try {
                audioPlayer.playAudio(File(filePath))
                _isPlaying.value = true
                startProgressTracking()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to play audio")
            }
        }
    }

    fun playVoice(uri: String) {
        viewModelScope.launch {
            try {
                audioPlayer.playAudioFromUri(uri)
                _isPlaying.value = true
                startProgressTracking()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to play voice")
            }
        }
    }

    fun pausePlayback() {
        audioPlayer.pause()
        _isPlaying.value = false
        playbackJob?.cancel()
    }

    fun resumePlayback() {
        audioPlayer.resume()
        _isPlaying.value = true
        startProgressTracking()
    }

    fun seekTo(progress: Float) {
        val duration = audioPlayer.getDuration()
        audioPlayer.seekTo((progress * duration).toInt())
    }

    private fun startProgressTracking() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            audioPlayer.getProgress().collect { progress ->
                _playbackProgress.value = progress
                if (progress >= 1f) {
                    _isPlaying.value = false
                }
            }
        }
    }

    private fun loadVoiceList() {
        viewModelScope.launch {
            try {
                _voiceList.value = repository.getVoiceList()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to load voice list")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
        playbackJob?.cancel()
    }

    fun clearError() {
        _uiState.value = UiState.Idle
    }
}

sealed class UiState {
    object Idle : UiState()
    object Loading : UiState()
    object Success : UiState()
    data class Error(val message: String) : UiState()
} 