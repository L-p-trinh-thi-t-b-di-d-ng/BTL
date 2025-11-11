package com.dex.lingbook.learn.model

enum class AudioMode { TTS, URL }
enum class LessonType { WORD_ORDER, LISTEN_CHOOSE, IMAGE_PICK }

data class Skill(
    val id: String = "",
    val title: String = "",
    val icon: String = "🐣",
    val unlocked: Boolean = false,
    val progress: Int = 0 // 0..100
)

data class Learn(
    val id: String = "",
    val skillId: String = "",
    val title: String = "",
    val type: LessonType = LessonType.WORD_ORDER,
    val questionCount: Int = 0
)

/** Các dạng câu hỏi */
data class WordOrderQuestion(
    val id: String = "",
    val sentence: String = "",
    val shuffled: List<String> = emptyList(),
    val ttsText: String? = null
)

data class ListenChooseQuestion(
    val id: String = "",
    val audioMode: AudioMode = AudioMode.TTS,
    val audioUrl: String? = null,
    val ttsText: String? = null,
    val options: List<String> = emptyList(),
    val answerIndex: Int = 0
)

data class ImageOption(
    val imageUrl: String = "",
    val label: String = ""
)
data class ImagePickQuestion(
    val id: String = "",
    val prompt: String = "",
    val options: List<ImageOption> = emptyList(),
    val answerIndex: Int = 0
)
