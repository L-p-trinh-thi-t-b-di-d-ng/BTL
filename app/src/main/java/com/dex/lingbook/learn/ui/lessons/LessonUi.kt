// learn/ui/lessons/LessonUi.kt
package com.dex.lingbook.learn.ui.lessons

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val BlueLight = Color(0xFF9ED0FF)
private val BlueDeep  = Color(0xFF6AB5FF)
private val BgLight   = Color(0xFFF8FAFD)
private val Surface1  = Color(0xFFFFFFFF)
private val Outline   = Color(0xFFE5E8EE)

@Composable
fun LessonTheme(content: @Composable () -> Unit) {
    val base = MaterialTheme.colorScheme
    val scheme = base.copy(
        primary = BlueLight,
        onPrimary = Color.White,
        secondary = BlueDeep,
        background = BgLight,
        surface = Surface1,
        surfaceVariant = Surface1,
        outline = Outline,
        onSurface = Color(0xFF111318)
    )
    MaterialTheme(colorScheme = scheme, typography = MaterialTheme.typography) {
        Surface(Modifier.fillMaxSize(), color = scheme.background) { content() }
    }
}

@Composable
fun LessonHeader(
    progress: Float,
    onClose: () -> Unit
) {
    Surface(color = MaterialTheme.colorScheme.background) {
        Row(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Outlined.Close, contentDescription = "Close")
            }

            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(50)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.size(40.dp))
        }
    }
}

@Composable
fun PrimaryLargeButton(
    enabled: Boolean,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        enabled = enabled,
        onClick = onClick,
        modifier = modifier
            .height(56.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor   = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
            disabledContentColor   = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.9f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
    ) { Text(text, style = MaterialTheme.typography.titleMedium) }
}
