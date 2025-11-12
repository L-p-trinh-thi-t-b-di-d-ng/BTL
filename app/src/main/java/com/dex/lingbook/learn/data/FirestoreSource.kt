package com.dex.lingbook.learn.data

import com.dex.lingbook.learn.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

private val DB_ID: String? = null

class FirestoreSource(
    app: FirebaseApp = FirebaseApp.getInstance()
) {
    val db: FirebaseFirestore =
        if (DB_ID == null) FirebaseFirestore.getInstance(app)
        else FirebaseFirestore.getInstance(app, DB_ID)


    // === Lessons ===//
    suspend fun getLessons(skillId: String): List<Learn> {
        val snap = db.collection("skills").document(skillId)
            .collection("lessons").orderBy("order").get().await()
        return snap.documents.map { it.toLesson(skillId) }
    }

    // === Questions theo kiểu bài ===//
    suspend fun getWordOrderQuestions(skillId: String, lessonId: String): List<WordOrderQuestion> {
        val snap = db.collection("skills").document(skillId)
            .collection("lessons").document(lessonId)
            .collection("questions").orderBy("order").get().await()
        return snap.documents.map {
            WordOrderQuestion(
                id = it.id,
                sentence = it.getString("sentence") ?: "",
                shuffled = it.get("shuffled") as? List<String> ?: emptyList(),
                ttsText = it.getString("ttsText")
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
                else -> AudioMode.TTS
            }
            ListenChooseQuestion(
                id = d.id,
                audioMode = mode,
                audioUrl = d.getString("audioUrl"),
                ttsText = d.getString("ttsText"),
                options = d.get("options") as? List<String> ?: emptyList(),
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

    suspend fun getSkillsForUser(uid: String): List<Skill> {
        val catSnap = db.collection("skills").orderBy("order").get().await()
        val catalog = catSnap.documents.map { d ->
            CatalogRow(
                id = d.id,
                title = d.getString("title") ?: "",
                icon = d.getString("icon") ?: "🐣",
                order = (d.getLong("order") ?: Long.MAX_VALUE).toInt()
            )
        }.sortedBy { it.order }

        val progSnap = db.collection("users").document(uid)
            .collection("skillProgress").get().await()
        val progById = progSnap.documents.associateBy({ it.id }) { p ->
            ProgressRow(
                unlocked = p.getBoolean("unlocked") ?: false,
                progress = (p.getLong("progress") ?: 0L).toInt(),
                currentLessonIndex = (p.getLong("currentLessonIndex") ?: 0L).toInt()
            )
        }

        val merged = catalog.map { c ->
            val p = progById[c.id]
            Skill(
                id = c.id,
                title = c.title,
                icon = c.icon,
                unlocked = p?.unlocked ?: false,
                progress = p?.progress ?: 0
            )
        }.toMutableList()

        if (merged.isNotEmpty() && merged.none { it.unlocked }) {
            merged[0] = merged[0].copy(unlocked = true)
            db.collection("users").document(uid)
                .collection("skillProgress").document(merged[0].id)
                .set(mapOf("unlocked" to true, "progress" to 0, "currentLessonIndex" to 0), SetOptions.merge())
                .await()
        }
        return merged
    }


    suspend fun unlockNextSkillForUser(uid: String, currentSkillId: String) {
        val cat = db.collection("skills").orderBy("order").get().await().documents
        val i = cat.indexOfFirst { it.id == currentSkillId }
        if (i < 0 || i + 1 >= cat.size) return
        val nextId = cat[i + 1].id
        val col = db.collection("users").document(uid).collection("skillProgress")
        db.runBatch { b ->
            b.set(col.document(currentSkillId),
                mapOf("progress" to 100, "unlocked" to true),
                SetOptions.merge()
            )
            b.set(col.document(nextId),
                mapOf("unlocked" to true),
                SetOptions.merge()
            )
        }.await()
    }

    suspend fun ensureUserProgressInitialized(uid: String) {
        val first = db.collection("skills").orderBy("order").limit(1).get().await()
        val firstId = first.documents.firstOrNull()?.id ?: return
        db.collection("users").document(uid)
            .collection("skillProgress").document(firstId)
            .set(mapOf("unlocked" to true, "progress" to 0, "currentLessonIndex" to 0), SetOptions.merge())
            .await()
    }

    suspend fun getSkillProgressForUser(uid: String, skillId: String): SkillProgress {
        val doc = db.collection("users").document(uid)
            .collection("skillProgress").document(skillId).get().await()
        return SkillProgress(
            unlocked = doc.getBoolean("unlocked") ?: false,
            progress = (doc.getLong("progress") ?: 0L).toInt(),
            currentLessonIndex = (doc.getLong("currentLessonIndex") ?: 0L).toInt()
        )
    }

    suspend fun setCurrentLessonIndex(uid: String, skillId: String, index: Int) {
        db.collection("users").document(uid)
            .collection("skillProgress").document(skillId)
            .set(mapOf("currentLessonIndex" to index), SetOptions.merge())
            .await()
    }
}


private fun DocumentSnapshot.toLesson(skillId: String) = Learn(
    id = id,
    skillId = skillId,
    title = getString("title") ?: "",
    type = when ((getString("type") ?: "WORD_ORDER").uppercase()) {
        "WORD_ORDER" -> LessonType.WORD_ORDER
        "LISTEN_CHOOSE" -> LessonType.LISTEN_CHOOSE
        "IMAGE_PICK" -> LessonType.IMAGE_PICK
         else -> LessonType.WORD_ORDER
    },
    questionCount = (getLong("questionCount") ?: 0L).toInt()
)
private data class CatalogRow(
    val id: String,
    val title: String,
    val icon: String,
    val order: Int
)

private data class ProgressRow(
    val unlocked: Boolean,
    val progress: Int,
    val currentLessonIndex: Int
)
