@file:OptIn(ExperimentalMaterial3Api::class)

package com.dex.lingbook.learn.ui.lessons

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dex.lingbook.learn.model.*
import com.dex.lingbook.learn.viewmodel.LearnViewModel

@Composable
fun LessonScreen(
    skillId: String,
    lesson: Learn,
    vm: LearnViewModel,
    onExit: () -> Unit,
    onOpenNext: (Learn) -> Unit
) {
    val ui by vm.lessonState.collectAsState()

    LaunchedEffect(lesson.id) { vm.startLesson(skillId, lesson) }

    // progress 0..1 theo số câu trong bài
    val total = ui.questions.size.coerceAtLeast(1)
    val raw = (ui.index.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val progress by animateFloatAsState(raw, label = "progress")

    Scaffold(
        topBar = {
            // TopBar gọn: nút thoát + progress (không thêm Scaffold ở bài con)
            Row(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onExit(); vm.resetLesson() }) {
                    Icon(Icons.Outlined.Close, contentDescription = "Exit")
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(lesson.title, style = MaterialTheme.typography.titleMedium)
                }
            }
        },
        bottomBar = {}
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when {
                ui.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                ui.error != null -> Text(ui.error!!, Modifier.align(Alignment.Center))

                // an toàn khi index vượt size (đã hoàn thành)
                ui.finished || ui.index !in ui.questions.indices || ui.questions.isEmpty() -> {
                    FinishCard(ui.correctCount, ui.questions.size) {
                        val moved = vm.openNextLesson { next -> onOpenNext(next) }
                        if (!moved) {
                            vm.unlockNextSkillAndRefresh()
                            onExit()
                            vm.resetLesson()
                        }
                    }
                }

                else -> {
                    when (val any = ui.questions[ui.index]) {
                        is WordOrderQuestion ->
                            WordOrderExercise(
                                q = any,
                                onCorrect = vm::answerCorrect,
                                onWrong = vm::answerWrong
                            )
                        is ListenChooseQuestion ->
                            ListenChooseExercise(
                                q = any,
                                onCorrect = vm::answerCorrect,
                                onWrong = vm::answerWrong
                            )
                        is ImagePickQuestion ->
                            ImagePickExercise(
                                q = any,
                                onCorrect = vm::answerCorrect,
                                onWrong = vm::answerWrong
                            )
                        else -> Text("Loại câu hỏi chưa hỗ trợ.", Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}

@Composable
private fun FinishCard(correct: Int, total: Int, onDone: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Hoàn thành!", style = MaterialTheme.typography.headlineSmall)
        Text("$correct / $total đúng")
        Spacer(Modifier.height(12.dp))
        Button(onClick = onDone, shape = MaterialTheme.shapes.large) { Text("Tiếp tục") }
    }
}
