@file:OptIn(ExperimentalLayoutApi::class)

package com.dex.lingbook.learn.ui.lessons

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dex.lingbook.learn.model.WordOrderQuestion

// learn/ui/lessons/WordOrderExercise.kt  (sửa file hiện tại của bạn)
@Composable
fun WordOrderExercise(
    q: WordOrderQuestion,
    onCorrect: () -> Unit,
    onWrong: () -> Unit,
    progress: Float = 0.35f,          // truyền % bài nếu có nhiều câu
    onClose: () -> Unit = {}
) {
    LessonTheme {
        // === state ===
        var selected by remember(q.id) { mutableStateOf(listOf<String>()) }
        var pool     by remember(q.id) { mutableStateOf(q.shuffled) }
        val context = LocalContext.current
        val tts = remember(q.id, q.ttsText) { q.ttsText?.let { TextToSpeech(context) {} } }
        DisposableEffect(Unit) { onDispose { tts?.shutdown() } }
        val canSubmit = selected.isNotEmpty()

        Box(Modifier.fillMaxSize()) {

            // Header đẹp + không có trái tim
            // LessonHeader(progress = progress, onClose = onClose)

            // --------- NỘI DUNG ----------
            Column(
                Modifier
                    .fillMaxSize()
                    .padding(top = 64.dp)             // chừa chỗ header
                    .padding(horizontal = 18.dp)
                    .padding(bottom = 92.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Text(
                    "Dịch và sắp xếp các từ sau",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )

                if (q.ttsText != null) {
                    Surface(shape = RoundedCornerShape(14.dp), tonalElevation = 0.dp,
                        border = ButtonDefaults.outlinedButtonBorder) {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledIconButton(
                                onClick = { tts?.speak(q.ttsText, TextToSpeech.QUEUE_FLUSH, null, "tts-${q.id}") },
                                shape = RoundedCornerShape(12.dp),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) { Icon(Icons.Outlined.VolumeUp, contentDescription = "Play") }

                            Spacer(Modifier.width(12.dp))
                            Text(q.ttsText, style = MaterialTheme.typography.bodyLarge, fontStyle = FontStyle.Italic)
                        }
                    }
                }

                Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

                // VÙNG GHÉP (trên)
                FlowRow(
                    Modifier.fillMaxWidth().heightIn(min = 56.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    selected.forEachIndexed { i, w ->
                        AssistChip(
                            onClick = {
                                selected = selected.toMutableList().also { it.removeAt(i) }
                                pool = pool + w
                            },
                            label = { Text(w) },
                            shape = RoundedCornerShape(18.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        )
                    }
                }

                Divider(thickness = 1.dp, color = MaterialTheme.colorScheme.outline)

                // KHO TỪ (dưới)
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pool.forEach { w ->
                        AssistChip(
                            onClick = {
                                selected = selected + w
                                val idx = pool.indexOf(w)
                                if (idx >= 0) pool = pool.toMutableList().also { it.removeAt(idx) }
                            },
                            label = { Text(w) },
                            shape = RoundedCornerShape(18.dp),
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        )
                    }
                }

                TextButton(onClick = { selected = emptyList(); pool = q.shuffled }) { Text("Làm lại") }
                Spacer(Modifier.height(8.dp))
            }

            // --------- BOTTOM BUTTON ----------
            Surface(
                tonalElevation = 0.dp,
                shadowElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrimaryLargeButton(
                        enabled = canSubmit,
                        text = "Kiểm tra",
                        onClick = {
                            val ok = selected.joinToString(" ").trim() == q.sentence.trim()
                            if (ok) onCorrect() else onWrong()
                        }
                    )
                }
            }
        }
    }
}
