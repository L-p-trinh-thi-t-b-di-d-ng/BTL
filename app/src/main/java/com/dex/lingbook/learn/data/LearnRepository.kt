package com.dex.lingbook.learn.data

import android.util.Log
import com.dex.lingbook.learn.model.*

class LearnRepository(
    private val remote: FirestoreSource = FirestoreSource()
) {
    suspend fun getSkillsForUser(uid: String) =
        runCatching { remote.getSkillsForUser(uid) }

    suspend fun unlockNextSkillForUser(uid: String, skillId: String) =
        runCatching { remote.unlockNextSkillForUser(uid, skillId) }

    suspend fun ensureUserProgressInitialized(uid: String) =
        runCatching { remote.ensureUserProgressInitialized(uid) }

    suspend fun getSkillProgressForUser(uid: String, skillId: String) =
        runCatching { remote.getSkillProgressForUser(uid, skillId) }

    suspend fun setCurrentLessonIndex(uid: String, skillId: String, index: Int) =
        runCatching { remote.setCurrentLessonIndex(uid, skillId, index) }

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
