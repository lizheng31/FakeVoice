# FakeVoice - 文本转语音 Android 应用

一个基于 SiliconFlow API 的文本转语音 Android 应用。

## 功能特性

- 支持文本转语音
- 支持多种预置音色选择（8种，包含男声和女声）
- 支持语言选择（中文、英文、日语、韩语）
- 支持方言选择（普通话、粤语、四川话等）
- 支持情感控制（正常、快乐、兴奋、悲伤、愤怒）
- 支持语气控制（语速、音调、语气强度）
- 支持录音并保存
- 支持音频播放控制

## 已知问题

1. 粤语转换功能目前无法正常工作，需要进一步调研 API 的方言支持
2. 部分语音控制参数（情感、语气）的效果需要优化

## 后续计划

1. 修复粤语转换问题
2. 优化语音控制参数
3. 添加音频文件管理功能
4. 添加用户自定义音色支持
5. 优化 UI/UX 设计

## 技术栈

- Kotlin
- Jetpack Compose
- Retrofit
- Hilt (依赖注入)
- Coroutines & Flow
- Material3 Design

## 开发环境要求

- Android Studio Hedgehog | 2023.1.1
- Kotlin 1.9.0
- Minimum SDK: 24
- Target SDK: 34

## 如何使用

1. 克隆项目
2. 在 ApiConfig.kt 中配置你的 API Key
3. 运行项目

## License

[MIT License](LICENSE)