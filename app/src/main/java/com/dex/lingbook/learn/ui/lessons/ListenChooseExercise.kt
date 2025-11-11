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
import androidx.compose.material3.Divider
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

/* ===== Duolingo-like palette (cục bộ, không ảnh hưởng toàn app) ===== */
private val DuoBlue         = Color(0xFFAED2FA) // nút xanh pastel
private val DuoBlueDark     = Color(0xFF2E6FD3) // xanh đậm cho icon
private val DuoLine         = Color(0xFFE8EEF6) // đường kẻ rất nhạt
private val DuoChip         = Color(0xFFF9FBFF) // chip thường
private val DuoChipSelected = Color(0xFFE8F2FF) // chip đã chọn nhạt

/**
 * Bài "Nghe và chọn" — body nội dung. Điều khiển đúng/sai qua onCorrect/onWrong.
 */
@Composable
fun ListenChooseExercise(
    q: ListenChooseQuestion,
    onCorrect: () -> Unit,
    onWrong: () -> Unit
) {
    val context = LocalContext.current
    val isUrl = q.audioMode == AudioMode.URL

    // Chuẩn bị audio (URL -> ExoPlayer, TTS -> TextToSpeech)
    val player = remember(if (isUrl) q.id else "no-player") {
        if (isUrl) ExoPlayer.Builder(context).build() else null
    }
    val tts = remember(if (!isUrl) q.id else "no-tts") {
        if (!isUrl) {
            var ref: TextToSpeech? = null
            ref = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    val r = ref?.setLanguage(Locale.US)
                }
            }
            ref
        } else null
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

    // Trạng thái lựa chọn và nộp bài
    var pickedIndex by remember(q.id) { mutableStateOf<Int?>(null) }
    var submitted by remember(q.id) { mutableStateOf(false) }
    var correct by remember(q.id) { mutableStateOf<Boolean?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        /* Tiêu đề giống ảnh mẫu */
        Text(
            "Chọn những gì bạn nghe thấy",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        /* Nút loa tròn lớn màu xanh đậm */
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Surface(shape = CircleShape, color = DuoChipSelected, tonalElevation = 2.dp) {
                IconButton(
                    onClick = {
                        if (isUrl) {
                            player?.seekTo(0)
                            player?.playWhenReady = true
                        } else {
                            q.ttsText?.let { tts?.speak(it, TextToSpeech.QUEUE_FLUSH, null, "tts-${q.id}") }
                        }
                    },
                    modifier = Modifier.padding(8.dp) // tạo khoảng cho vòng tròn
                ) {
                    Icon(
                        Icons.Outlined.VolumeUp,
                        contentDescription = "Play",
                        tint = DuoBlueDark,
                        modifier = Modifier
                            .padding(8.dp)
                            .then(Modifier.height(56.dp)) // kích thước icon
                    )
                }
            }
        }

        /* Ba đường kẻ placeholder rất nhạt */
        repeat(3) { Divider(thickness = 1.dp, color = DuoLine) }

        /* Các PALETTE lựa chọn dạng chip */
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
                        containerColor = DuoChip,
                        selectedContainerColor = DuoChipSelected
                    )
                )
            }
        }

        Spacer(Modifier.weight(1f))

        /* Nút KIỂM TRA ở đáy */
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
                containerColor = DuoBlue,
                contentColor = Color.White,
                disabledContainerColor = DuoBlue.copy(alpha = 0.45f),
                disabledContentColor = Color.White
            )
        ) {
            Text("Kiểm tra")
        }

        // Phản hồi ngắn (tùy chọn)
        if (submitted && correct != null) {
            val msg = if (correct == true) "Chính xác!" else "Sai rồi."
            val tint = if (correct == true) Color(0xFF2E7D32) else Color(0xFFC62828)
            Text(msg, color = tint)
        }

        Spacer(Modifier.height(4.dp))
    }
}
