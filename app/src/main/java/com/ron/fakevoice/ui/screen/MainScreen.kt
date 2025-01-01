package com.ron.fakevoice.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.unit.dp
import com.ron.fakevoice.ui.viewmodel.UiState
import com.ron.fakevoice.ui.viewmodel.VoiceViewModel
import com.ron.fakevoice.data.api.VoiceInfo

@Composable
fun MainScreen(
    viewModel: VoiceViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val voiceList by viewModel.voiceList.collectAsState()
    val isRecording by viewModel.isRecording
    val isPlaying by viewModel.isPlaying.collectAsState()
    val playbackProgress by viewModel.playbackProgress.collectAsState()

    var showErrorDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 文字输入区域
        OutlinedTextField(
            value = viewModel.inputText,
            onValueChange = viewModel::onTextInput,
            label = { Text("输入要转换的文字") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        // 操作按钮区域
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 文字转语音按钮
            Button(
                onClick = { viewModel.createSpeech("default") },
                enabled = !isRecording && uiState !is UiState.Loading && viewModel.inputText.isNotBlank()
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("转换语音")
            }

            // 录音按钮
            RecordButton(
                isRecording = isRecording,
                onStartRecording = viewModel::startRecording,
                onStopRecording = viewModel::stopRecording,
                enabled = uiState !is UiState.Loading
            )
        }

        // 音频播放控制器
        if (isPlaying || playbackProgress > 0f) {
            AudioPlayer(
                isPlaying = isPlaying,
                progress = playbackProgress,
                onPlayPause = {
                    if (isPlaying) viewModel.pausePlayback() else viewModel.resumePlayback()
                },
                onSeek = viewModel::seekTo,
                onSeekFinished = { /* 可以添加seek完成后的操作 */ },
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 已保存的语音列表
        Text(
            "已保存的语音",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.align(Alignment.Start)
        )
        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(
                items = voiceList,
                key = { it.uri }
            ) { voice ->
                VoiceItem(
                    voice = voice,
                    onPlay = { viewModel.playVoice(voice.uri) },
                    onDelete = { viewModel.deleteVoice(voice.uri) }
                )
            }
        }

        // 加载状态显示
        when (uiState) {
            is UiState.Loading -> LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            is UiState.Error -> {
                errorMessage = (uiState as UiState.Error).message
                showErrorDialog = true
            }
            else -> {}
        }
    }

    // 错误提示对话框
    if (showErrorDialog) {
        AlertDialog(
            onDismissRequest = { showErrorDialog = false },
            title = { Text("错误") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showErrorDialog = false }) {
                    Text("确定")
                }
            }
        )
    }

    // 添加错误提示对话框
    if (uiState is UiState.Error) {
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("错误") },
            text = { Text((uiState as UiState.Error).message) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("确定")
                }
            }
        )
    }
}

@Composable
fun RecordButton(
    isRecording: Boolean,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isRecording) 1.2f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recording_animation"
    )

    Button(
        onClick = if (isRecording) onStopRecording else onStartRecording,
        enabled = enabled,
        modifier = modifier.scale(if (isRecording) scale else 1f),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isRecording) MaterialTheme.colorScheme.error 
                           else MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(
            if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
            contentDescription = if (isRecording) "停止录音" else "开始录音"
        )
        Spacer(Modifier.width(8.dp))
        Text(if (isRecording) "停止录音" else "录制语音")
    }
}

@Composable
fun AudioPlayer(
    isPlaying: Boolean,
    progress: Float,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onSeekFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Slider(
            value = progress,
            onValueChange = onSeek,
            modifier = Modifier.fillMaxWidth()
        )
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "后退")
            }
            
            IconButton(
                onClick = onPlayPause
            ) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "暂停" else "播放"
                )
            }
            
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.SkipNext, contentDescription = "前进")
            }
        }
    }
}

@Composable
fun VoiceItem(
    voice: VoiceInfo,
    onPlay: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = voice.customName)
            Row {
                IconButton(onClick = onPlay) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "播放")
                }
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(Icons.Default.Delete, contentDescription = "删除")
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这个语音吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
} 