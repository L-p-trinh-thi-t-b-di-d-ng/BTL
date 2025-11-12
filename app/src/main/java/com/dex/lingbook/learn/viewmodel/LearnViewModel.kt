package com.dex.lingbook.learn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dex.lingbook.learn.data.LearnRepository
import com.dex.lingbook.learn.model.*
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SkillMapUiState(
    val loading: Boolean = false,
    val skills: List<Skill> = emptyList(),
    val error: String? = null
)

data class LessonUiState(
    val loading: Boolean = true,
    val lesson: Learn? = null,
    val questions: List<Any> = emptyList(),
    val index: Int = 0,
    val correctCount: Int = 0,
    val finished: Boolean = false,
    val error: String? = null
)

class LearnViewModel(
    private val repo: LearnRepository = LearnRepository(),
    private var currentSkillId: String? = null
) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val uid: String
        get() = auth.currentUser?.uid.orEmpty()

    private val _skillMap = MutableStateFlow(SkillMapUiState())
    val skillMap: StateFlow<SkillMapUiState> = _skillMap

    private val _lessonState = MutableStateFlow(LessonUiState())
    val lessonState: StateFlow<LessonUiState> = _lessonState

    private var cachedLessons: List<Learn> = emptyList()
    private var lessonIndex: Int = -1

    fun ensureUserProgressInitialized() {
        val me = uid
        if (me.isEmpty()) return
        viewModelScope.launch { repo.ensureUserProgressInitialized(me) }
    }

    fun loadSkillMap() {
        val me = uid
        viewModelScope.launch {
            _skillMap.value = _skillMap.value.copy(loading = true, error = null)
            if (me.isEmpty()) {
                _skillMap.value = SkillMapUiState(loading = false, skills = emptyList(), error = "Bạn chưa đăng nhập.")
                return@launch
            }
            repo.getSkillsForUser(me)
                .onSuccess { all -> _skillMap.value = SkillMapUiState(loading = false, skills = all) }
                .onFailure { e -> _skillMap.value = SkillMapUiState(loading = false, error = e.message) }
        }
    }

    fun loadLessonsAndStart(skill: Skill, onOpenLesson: (Learn) -> Unit) {
        currentSkillId = skill.id
        viewModelScope.launch {
            repo.loadLessons(skill.id)
                .onSuccess { lessons ->
                    cachedLessons = lessons
                    // --- BẮT ĐẦU LẠI TỪ BÀI HỌC ĐẦU TIÊN
                    lessonIndex = 0
                    val me = uid

//                    val savedIndex = if (me.isNotEmpty()) {
//                        repo.getSkillProgressForUser(me, skill.id).getOrNull()?.currentLessonIndex ?: 0
//                    } else 0
//                    lessonIndex = savedIndex.coerceIn(0, (lessons.size - 1).coerceAtLeast(0))

                    lessons.getOrNull(lessonIndex)?.let(onOpenLesson)
                        ?: run { _skillMap.value = _skillMap.value.copy(error = "Skill '${skill.title}' chưa có bài.") }
                }
                .onFailure { e -> _skillMap.value = _skillMap.value.copy(error = "Lỗi tải bài: ${e.message}") }
        }
    }

    fun startLesson(skillId: String, lesson: Learn) {
        currentSkillId = skillId
        viewModelScope.launch {
            cachedLessons.indexOfFirst { it.id == lesson.id }
                .takeIf { it >= 0 }?.let { lessonIndex = it }
            val me = uid
            if (me.isNotEmpty()) { repo.setCurrentLessonIndex(me, skillId, lessonIndex) }
            _lessonState.value = LessonUiState(loading = true, lesson = lesson)
            repo.loadQuestions(skillId, lesson)
                .onSuccess { qs ->
                    _lessonState.value = LessonUiState(
                        loading = false,
                        lesson = lesson,
                        questions = qs,
                        finished = qs.isEmpty()
                    )
                }
                .onFailure { e ->
                    _lessonState.value = LessonUiState(loading = false, lesson = lesson, error = e.message)
                }
        }
    }

    fun answerCorrect() {
        val s = _lessonState.value
        val next = s.index + 1
        _lessonState.value = s.copy(index = next, correctCount = s.correctCount + 1, finished = next >= s.questions.size)
    }

    fun answerWrong() {
        val s = _lessonState.value
        val next = s.index + 1
        _lessonState.value = s.copy(index = next, finished = next >= s.questions.size)
    }

    fun openNextLesson(onOpen: (Learn) -> Unit): Boolean {
        val next = lessonIndex + 1
        return if (next in cachedLessons.indices) {
            lessonIndex = next
            val me = uid
            val sid = currentSkillId
            if (me.isNotEmpty() && sid != null) {
                viewModelScope.launch { repo.setCurrentLessonIndex(me, sid, lessonIndex) }
            }
            onOpen(cachedLessons[next])
            true
        } else false
    }

    fun resetLesson() { _lessonState.value = LessonUiState() }

    fun unlockNextSkillAndRefresh() {
        val sid = currentSkillId ?: return
        val me = uid
        if (me.isEmpty()) return
        viewModelScope.launch {
            repo.unlockNextSkillForUser(me, sid)
            loadSkillMap()
        }
    }
}