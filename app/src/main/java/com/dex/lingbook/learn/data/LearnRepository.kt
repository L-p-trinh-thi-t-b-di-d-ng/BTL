package com.dex.lingbook.learn.data

import android.util.Log
import com.dex.lingbook.learn.model.*

class LearnRepository(
    private val remote: FirestoreSource = FirestoreSource()
) {
    suspend fun loadSkills(): Result<List<Skill>> = runCatching {
        val list = remote.getSkills()
        Log.d("SkillRepo", "skills count=${list.size}")
        list
    }.onFailure { Log.e("SkillRepo","loadSkills failed", it) }

    suspend fun loadLessons(skillId: String): Result<List<Learn>> =
        runCatching { remote.getLessons(skillId) }

    suspend fun loadQuestions(skillId: String, learn: Learn): Result<List<Any>> = runCatching {
        when (learn.type) {
            LessonType.WORD_ORDER    -> remote.getWordOrderQuestions(skillId, learn.id)
            LessonType.LISTEN_CHOOSE -> remote.getListenChooseQuestions(skillId, learn.id)
            LessonType.IMAGE_PICK    -> remote.getImagePickQuestions(skillId, learn.id)
        }
    }
}
