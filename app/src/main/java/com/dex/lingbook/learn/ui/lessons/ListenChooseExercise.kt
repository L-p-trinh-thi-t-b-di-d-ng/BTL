@file:OptIn(ExperimentalLayoutApi::class)

package com.dex.lingbook.learn.ui.lessons

import android.net.Uri
import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.dex.lingbook.learn.model.AudioMode
import com.dex.lingbook.learn.model.ListenChooseQuestion
import java.util.Locale


private val Check     = Color(0xFF00C244)
private val LingBookChip         = Color(0xFFF9FBFF)
private val LingBookChipSelected = Color(0xFFE8F2FF)

@Composable
fun ListenChooseExercise(
    q: ListenChooseQuestion,
    onCorrect: () -> Unit,
    onWrong: () -> Unit
) {
    val context = LocalContext.current
    val isUrl = q.audioMode == AudioMode.URL

    val player = remember(if (isUrl) q.id else "no-player") {
        if (isUrl) ExoPlayer.Builder(context).build() else null
    }

    val tts = remember(if (!isUrl) q.id else "no-tts") {
        if (!isUrl) {
            lateinit var currentTts: TextToSpeech

            currentTts = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val result = currentTts.setLanguage(Locale.US)
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    }
                }
            })
            currentTts
        } else {
            null
        }
    }

    DisposableEffect(q.id) {
        if (isUrl && !q.audioUrl.isNullOrBlank()) {
            player?.apply {
                setMediaItem(MediaItem.fromUri(Uri.parse(q.audioUrl)))
                prepare()
            }
        }
        onDispose {
            player?.release()
            tts?.shutdown()
        }
    }

    var pickedIndex by remember(q.id) { mutableStateOf<Int?>(null) }
    var submitted by remember(q.id) { mutableStateOf(false) }
    var correct by remember(q.id) { mutableStateOf<Boolean?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            "Chọn những gì bạn nghe thấy",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(
                shape = CircleShape,
                // ===================================
                // MÀU NỀN CỦA NÚT LOA
                color = Color(0xFF3E9BEC),
                // ===================================
                tonalElevation = 2.dp
            ) {
                IconButton(
                    onClick = {
                        if (isUrl) {
                            player?.seekTo(0)
                            player?.playWhenReady = true
                        } else {
                            q.ttsText?.let { tts?.speak(it, TextToSpeech.QUEUE_FLUSH, null, "tts-${q.id}") }
                        }
                    },
                    modifier = Modifier.padding(8.dp)
                ) {
                    Icon(
                        Icons.Outlined.VolumeUp,
                        contentDescription = "Play",
                        tint = Color.White,
                        modifier = Modifier
                            .padding(8.dp)
                            .then(Modifier.height(56.dp))
                    )
                }
            }
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            q.options.forEachIndexed { idx, opt ->
                val selected = pickedIndex == idx
                FilterChip(
                    selected = selected,
                    onClick = {
                        if (!submitted) pickedIndex = if (selected) null else idx
                    },
                    label = { Text(opt) },
                    shape = RoundedCornerShape(14.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = LingBookChip,
                        selectedContainerColor = LingBookChipSelected
                    )
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = {
                submitted = true
                val ok = pickedIndex == q.answerIndex
                correct = ok
                if (ok) onCorrect() else onWrong()
            },
            enabled = pickedIndex != null && !submitted,
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