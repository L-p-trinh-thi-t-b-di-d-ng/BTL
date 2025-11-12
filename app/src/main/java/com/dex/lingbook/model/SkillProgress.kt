package com.dex.lingbook.learn.model

data class SkillProgress(
    val unlocked: Boolean = false,
    val progress: Int = 0,
    val currentLessonIndex: Int = 0
)