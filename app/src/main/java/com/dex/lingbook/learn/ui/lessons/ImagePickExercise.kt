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
import java.util.Locale


private val Check         = Color(0xFF1AE35F)
private val Check1     = Color(0xFF00C244)
private val LingBookLine         = Color(0xFFF9FBFF)
private val LingBookBg       = Color(0xFFE8F2FF)

@Composable
fun ImagePickExercise(
    q: ImagePickQuestion,
    onCorrect: () -> Unit,
    onWrong: () -> Unit
) {
    var picked by remember(q.id) { mutableStateOf<Int?>(null) }
    var submitted by remember(q.id) { mutableStateOf(false) }
    var correct by remember(q.id) { mutableStateOf<Boolean?>(null) }

    val context = LocalContext.current

    val tts = remember(q.prompt) {
        lateinit var currentTts: TextToSpeech

        currentTts = TextToSpeech(context, TextToSpeech.OnInitListener { status ->
            if (status == TextToSpeech.SUCCESS) {

                val result = currentTts.setLanguage(Locale.US)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                }
            } else {
            }
        })
        currentTts
    }

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

        Surface(
            shape = RoundedCornerShape(14.dp),
            color = LingBookBg,
            tonalElevation = 1.dp
        ) {
            Row(
                Modifier
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = { tts.speak(q.prompt, TextToSpeech.QUEUE_FLUSH, null, "tts-${q.id}") },
                    shape = RoundedCornerShape(10.dp),
                    // ===================================
                    // MÀU NỀN CỦA NÚT LOA
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color(0xFF3E9BEC),
                        contentColor = Color.White
                    )
                    // ===================================
                ) { Icon(Icons.Outlined.VolumeUp, contentDescription = "Play") }
                Spacer(Modifier.width(10.dp))
                Text(q.prompt, style = MaterialTheme.typography.bodyLarge)
            }
        }

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
                            color = if (selected) Check1 else LingBookLine,
                            shape = RoundedCornerShape(18.dp)
                        )
                ) {
                    Column(
                        Modifier
                            .fillMaxSize(),
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
                containerColor = Check,
                contentColor = Color.White,
                disabledContainerColor = Check.copy(alpha = 0.45f),
                disabledContentColor = Color.White
            )
        ) { Text("Kiểm tra") }

        Spacer(Modifier.height(100.dp))
    }
}