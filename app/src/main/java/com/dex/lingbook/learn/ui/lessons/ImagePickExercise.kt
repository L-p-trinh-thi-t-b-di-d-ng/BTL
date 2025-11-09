@file:OptIn(ExperimentalLayoutApi::class)

package com.dex.lingbook.learn.ui.lessons

import android.speech.tts.TextToSpeech
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dex.lingbook.learn.model.ImagePickQuestion

/* ------ Duolingo-like palette (cục bộ) ------ */
private val DuoBlue         = Color(0xFFAED2FA) // nút xanh pastel
private val DuoBlueDark     = Color(0xFF2E6FD3) // xanh đậm cho icon/viền chọn
private val DuoLine         = Color(0xFFE8EEF6) // đường kẻ rất nhạt
private val DuoChipBg       = Color(0xFFF2F6FF) // chip nền nhạt

@Composable
fun ImagePickExercise(
    q: ImagePickQuestion,
    onCorrect: () -> Unit,
    onWrong: () -> Unit
) {
    var picked by remember(q.id) { mutableStateOf<Int?>(null) }
    var submitted by remember(q.id) { mutableStateOf(false) }
    var correct by remember(q.id) { mutableStateOf<Boolean?>(null) }

    // TTS cho prompt (“Tea”)
    val context = LocalContext.current
    val tts = remember(q.prompt) { TextToSpeech(context) {} }
    DisposableEffect(Unit) { onDispose { tts.shutdown() } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            "Chọn ảnh đúng",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        // Chip phát âm + prompt
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = DuoChipBg,
            tonalElevation = 1.dp
        ) {
            Row(
                Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = { tts.speak(q.prompt, TextToSpeech.QUEUE_FLUSH, null, "tts-${q.id}") },
                    shape = RoundedCornerShape(10.dp)
                ) { Icon(Icons.Outlined.VolumeUp, contentDescription = "Play", tint = DuoBlueDark) }
                Spacer(Modifier.width(10.dp))
                Text(q.prompt, style = MaterialTheme.typography.bodyLarge)
            }
        }

        // Lưới 2 cột giống ảnh mẫu
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(q.options) { idx, opt ->
                val selected = picked == idx
                ElevatedCard(
                    onClick = { if (!submitted) picked = idx },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(
                            width = if (selected) 2.dp else 1.dp,
                            color = if (selected) DuoBlueDark else DuoLine,
                            shape = RoundedCornerShape(18.dp)
                        )
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        AsyncImage(
                            model = opt.imageUrl,
                            contentDescription = opt.label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            opt.label,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        // Nút KIỂM TRA to ở đáy
        Button(
            onClick = {
                submitted = true
                val ok = picked == q.answerIndex
                correct = ok
                if (ok) onCorrect() else onWrong()
            },
            enabled = picked != null && !submitted,
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
        ) { Text("Kiểm tra") }

        if (submitted && correct != null) {
            val msg = if (correct == true) "Chính xác!" else "Không đúng."
            val tint = if (correct == true) Color(0xFF2E7D32) else Color(0xFFC62828)
            Text(msg, color = tint)
        }

        Spacer(Modifier.height(4.dp))
    }
}
