package com.ron.fakevoice.data

object Constants {
    // 可用的模型列表
    val AVAILABLE_MODELS = listOf(
        "CosyVoice2-0.5B (支持方言)" to "FunAudioLLM/CosyVoice2-0.5B",
        "Fish Speech 1.5 (多语言)" to "fishaudio/fish-speech-1.5",
        "Fish Speech 1.4" to "fishaudio/fish-speech-1.4",
        "GPT-SoVITS" to "RVC-Boss/GPT-SoVITS"
    )

    // 默认模型
    const val DEFAULT_MODEL = "FunAudioLLM/CosyVoice2-0.5B"
    
    // 预置音色选项
    val PRESET_VOICES = listOf(
        "Alex (男)" to "fishaudio/fish-speech-1.5:alex",
        "Benjamin (男)" to "fishaudio/fish-speech-1.5:benjamin",
        "Charles (男)" to "fishaudio/fish-speech-1.5:charles",
        "David (男)" to "fishaudio/fish-speech-1.5:david",
        "Anna (女)" to "fishaudio/fish-speech-1.5:anna",
        "Bella (女)" to "fishaudio/fish-speech-1.5:bella",
        "Claire (女)" to "fishaudio/fish-speech-1.5:claire",
        "Diana (女)" to "fishaudio/fish-speech-1.5:diana"
    )

    // 语言选项
    val LANGUAGES = listOf(
        "中文" to "zh",
        "英文" to "en",
        "日语" to "jp",
        "韩语" to "kr"
    )

    // 方言选项
    val DIALECTS = listOf(
        "普通话" to "mandarin",
        "粤语" to "cantonese",
        "四川话" to "sichuan",
        "上海话" to "shanghai",
        "郑州话" to "zhengzhou",
        "长沙话" to "changsha",
        "天津话" to "tianjin"
    )

    // 情感选项
    val EMOTIONS = listOf(
        "正常" to "normal",
        "快乐" to "happy",
        "兴奋" to "excited",
        "悲伤" to "sad",
        "愤怒" to "angry"
    )

    // 语气控制
    val PROSODY_CONTROLS = listOf(
        "正常语速" to "speed:1.0",
        "快速语速" to "speed:1.5",
        "慢速语速" to "speed:0.8",
        "高音调" to "pitch:1.2",
        "低音调" to "pitch:0.8",
        "强调语气" to "emphasis:1.2",
        "轻柔语气" to "emphasis:0.8"
    )
} 