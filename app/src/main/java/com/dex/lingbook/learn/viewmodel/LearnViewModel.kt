package com.dex.lingbook.learn.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dex.lingbook.learn.data.LearnRepository
import com.dex.lingbook.learn.model.*
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

    private val _skillMap = MutableStateFlow(SkillMapUiState())
    val skillMap: StateFlow<SkillMapUiState> = _skillMap

    private val _lessonState = MutableStateFlow(LessonUiState())
    val lessonState: StateFlow<LessonUiState> = _lessonState

    // Cache danh sách lesson để chuyển tiếp theo order
    private var cachedLessons: List<Learn> = emptyList()
    private var lessonIndex: Int = -1

    fun loadSkillMap() {
        viewModelScope.launch {
            _skillMap.value = _skillMap.value.copy(loading = true, error = null)
            repo.loadSkills()
                .onSuccess { all ->
                    // muốn ẩn skill khoá: all.filter { it.unlocked }
                    _skillMap.value = SkillMapUiState(loading = false, skills = all)
                }
                .onFailure { e ->
                    _skillMap.value = SkillMapUiState(loading = false, error = e.message)
                }
        }
    }

    fun loadLessonsAndStart(skill: Skill, onOpenLesson: (Learn) -> Unit) {
        currentSkillId = skill.id
        viewModelScope.launch {
            repo.loadLessons(skill.id)
                .onSuccess { lessons ->
                    cachedLessons = lessons // Firestore đã orderBy("order")
                    lessonIndex = 0
                    lessons.firstOrNull()?.let(onOpenLesson)
                        ?: run { _skillMap.value = _skillMap.value.copy(error = "Skill '${skill.title}' chưa có bài.") }
                }
                .onFailure { e ->
                    _skillMap.value = _skillMap.value.copy(error = "Lỗi tải bài: ${e.message}")
                }
        }
    }

    fun startLesson(skillId: String, lesson: Learn) {
        currentSkillId = skillId
        viewModelScope.launch {
            // cập nhật chỉ số bài hiện tại trong cache
            cachedLessons.indexOfFirst { it.id == lesson.id }
                .takeIf { it >= 0 }?.let { lessonIndex = it }

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
        _lessonState.value = s.copy(
            index = next,
            correctCount = s.correctCount + 1,
            finished = next >= s.questions.size
        )
    }

    fun answerWrong() {
        val s = _lessonState.value
        val next = s.index + 1
        _lessonState.value = s.copy(
            index = next,
            finished = next >= s.questions.size
        )
    }

    /** Gọi khi Finish để mở bài theo order kế tiếp. Trả về true nếu có bài tiếp. */
    fun openNextLesson(onOpen: (Learn) -> Unit): Boolean {
        val next = lessonIndex + 1
        return if (next in cachedLessons.indices) {
            lessonIndex = next
            onOpen(cachedLessons[next])
            true
        } else false
    }

    fun resetLesson() { _lessonState.value = LessonUiState() }

    fun unlockNextSkillAndRefresh() {
        val sid = currentSkillId ?: return
        viewModelScope.launch {
            repo.unlockNextSkill(sid)
            loadSkillMap() // gọi lại hàm đang load danh sách skill để UI cập nhật
        }
    }
}
