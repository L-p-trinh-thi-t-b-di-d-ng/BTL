package com.dex.lingbook.learn.model

/** Tiến độ của một user đối với một skill cụ thể */
data class SkillProgress(
    val unlocked: Boolean = false,
    val progress: Int = 0,              // 0..100
    val currentLessonIndex: Int = 0     // 0-based: 0=bài1, 1=bài2,...
)
