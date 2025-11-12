@file:OptIn(ExperimentalLayoutApi::class)

package com.dex.lingbook.learn.ui.lessons

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dex.lingbook.learn.model.WordOrderQuestion
import java.util.Locale

private val Check = Color(0xFF00C244)
private val LingBookChip = Color(0xFFF9FBFF)
private val LingBookChipSelected = Color(0xFFE8F2FF)

@Composable
fun WordOrderExercise(
    q: WordOrderQuestion,
    onCorrect: () -> Unit,
    onWrong: () -> Unit
) {
    val context = LocalContext.current
    var selected by remember(q.id) { mutableStateOf(listOf<String>()) }
    var pool by remember(q.id) { mutableStateOf(q.shuffled) }

    val tts = remember(q.id, q.ttsText) {
        q.ttsText?.let {
            lateinit var currentTts: TextToSpeech
            currentTts = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
                if (status == TextToSpeech.SUCCESS) {
                    currentTts.setLanguage(Locale.US)
                }
            })
            currentTts
        }
    }
    DisposableEffect(q.id) { onDispose { tts?.shutdown() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            "Sắp xếp lại các từ",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        if (q.ttsText != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                tonalElevation = 0.dp,
                color = Color.White,
                border = ButtonDefaults.outlinedButtonBorder
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(
                        onClick = {
                            tts?.speak(q.ttsText, TextToSpeech.QUEUE_FLUSH, null, "tts-${q.id}")
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            // ===================================
                            // MÀU NỀN CỦA NÚT LOA
                            containerColor = Color(0xFF3E9BEC), // Màu xanh lam mới
                            // ===================================
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Outlined.VolumeUp, contentDescription = "Phát âm")
                    }

                    Spacer(Modifier.width(12.dp))
                    Text(
                        q.ttsText,
                        style = MaterialTheme.typography.bodyLarge,
                        fontStyle = FontStyle.Italic,
                        color = Color.Black
                    )
                }
            }
        }

        FlowRow(
            Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
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
                        containerColor = LingBookChipSelected,
                        labelColor = Color.Black
                    )
                )
            }
        }

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
                        containerColor = LingBookChip,
                        labelColor = Color.Black
                    )
                )
            }
        }

        TextButton(
            onClick = { selected = emptyList(); pool = q.shuffled }
        ) {
            Text("Làm lại", color = Color.Black)
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                val ok = selected.joinToString(" ").trim() == q.sentence.trim()
                if (ok) onCorrect() else onWrong()
            },
            enabled = selected.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Check,
                contentColor = Color.White,
                disabledContainerColor = Check.copy(alpha = 0.45f),
                disabledContentColor = Color.White
            )
        ) {
            Text("Kiểm tra")
        }

        Spacer(Modifier.height(100.dp))
    }
}