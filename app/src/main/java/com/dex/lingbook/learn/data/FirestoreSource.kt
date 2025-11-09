package com.dex.lingbook.learn.data

import com.dex.lingbook.learn.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/** Đổi DB_ID nếu Firestore của bạn KHÔNG phải (default) */
private val DB_ID: String? = null

class FirestoreSource(
    app: FirebaseApp = FirebaseApp.getInstance()
) {
    /** Firestore dùng chung cho module Learn */
    val db: FirebaseFirestore =
        if (DB_ID == null) FirebaseFirestore.getInstance(app)
        else FirebaseFirestore.getInstance(app, DB_ID)

    // === Skills ===
    suspend fun getSkills(): List<Skill> {
        val snap = db.collection("skills").orderBy("order").get().await()
        return snap.documents.map { it.toSkill() }
    }

    // === Lessons ===
    suspend fun getLessons(skillId: String): List<Learn> {
        val snap = db.collection("skills").document(skillId)
            .collection("lessons").orderBy("order").get().await()
        return snap.documents.map { it.toLesson(skillId) }
    }

    // === Questions theo kiểu bài ===
    suspend fun getWordOrderQuestions(skillId: String, lessonId: String): List<WordOrderQuestion> {
        val snap = db.collection("skills").document(skillId)
            .collection("lessons").document(lessonId)
            .collection("questions").orderBy("order").get().await()
        return snap.documents.map {
            WordOrderQuestion(
                id = it.id,
                sentence = it.getString("sentence") ?: "",
                shuffled = it.get("shuffled") as? List<String> ?: emptyList(),
                ttsText  = it.getString("ttsText")
            )
        }
    }

    suspend fun getListenChooseQuestions(skillId: String, lessonId: String): List<ListenChooseQuestion> {
        val snap = db.collection("skills").document(skillId)
            .collection("lessons").document(lessonId)
            .collection("questions").orderBy("order").get().await()
        return snap.documents.map { d ->
            val mode = when (d.getString("audioMode") ?: "TTS") {
                "URL" -> AudioMode.URL
                else  -> AudioMode.TTS
            }
            ListenChooseQuestion(
                id = d.id,
                audioMode = mode,
                audioUrl  = d.getString("audioUrl"),
                ttsText   = d.getString("ttsText"),
                options   = d.get("options") as? List<String> ?: emptyList(),
                answerIndex = (d.getLong("answerIndex") ?: 0L).toInt()
            )
        }
    }

    suspend fun getImagePickQuestions(skillId: String, lessonId: String): List<ImagePickQuestion> {
        val snap = db.collection("skills").document(skillId)
            .collection("lessons").document(lessonId)
            .collection("questions").orderBy("order").get().await()
        return snap.documents.map { d ->
            val opts = (d.get("options") as? List<Map<String, Any?>>)?.map { m ->
                ImageOption(
                    label = m["label"] as? String ?: "",
                    imageUrl = m["imageUrl"] as? String ?: ""
                )
            } ?: emptyList()
            ImagePickQuestion(
                id = d.id,
                prompt = d.getString("prompt") ?: "",
                options = opts,
                answerIndex = (d.getLong("answerIndex") ?: 0L).toInt()
            )
        }
    }

    // Debug nhanh để xác nhận app đang trỏ DB nào
    fun debugLogWhere(): String {
        val opt = db.app.options
        val s = "projectId=${opt.projectId}, appId=${opt.applicationId}, dbId=${DB_ID ?: "(default)"}"
        android.util.Log.d("FS", s)
        return s
    }
}

/** Mapping helpers */
private fun DocumentSnapshot.toSkill() = Skill(
    id = id,
    title = getString("title") ?: "",
    icon = getString("icon") ?: "🐣",
    unlocked = getBoolean("unlocked") ?: false,
    progress = (getLong("progress") ?: 0L).toInt()
)

private fun DocumentSnapshot.toLesson(skillId: String) = Learn(
    id = id,
    skillId = skillId,
    title = getString("title") ?: "",
    type = when ((getString("type") ?: "WORD_ORDER").uppercase()) {
        "WORD_ORDER"    -> LessonType.WORD_ORDER
        "LISTEN_CHOOSE" -> LessonType.LISTEN_CHOOSE
        "IMAGE_PICK"    -> LessonType.IMAGE_PICK
        else            -> LessonType.WORD_ORDER
    },
    questionCount = (getLong("questionCount") ?: 0L).toInt()
)
