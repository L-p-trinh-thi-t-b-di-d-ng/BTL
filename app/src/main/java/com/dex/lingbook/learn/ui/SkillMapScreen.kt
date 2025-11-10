package com.dex.lingbook.learn.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dex.lingbook.learn.model.Skill
import com.dex.lingbook.learn.viewmodel.LearnViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillMapScreen(
    onOpenLesson: (skill: Skill) -> Unit,
    vm: LearnViewModel = viewModel()
) {
    val ui by vm.skillMap.collectAsState()
    LaunchedEffect(Unit) { vm.loadSkillMap() }

    Scaffold(
        topBar = { CenterAlignedTopAppBar(title = { Text("Learn") }) }
    ) { pad ->
        Box(Modifier.fillMaxSize().padding(pad)) {
            when {
                ui.loading      -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                ui.error != null -> ErrorState(ui.error ?: "Load error") { vm.loadSkillMap() }
                ui.skills.isEmpty() -> EmptyState()
                else -> CandyTrailMap(skills = ui.skills, onClick = onOpenLesson)
            }
        }
    }
}

@Composable
private fun CandyTrailMap(
    skills: List<Skill>,
    onClick: (Skill) -> Unit
) {
    val bg = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface.copy(alpha = .96f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .9f)
        )
    )
    Column(
        Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        skills.forEachIndexed { i, s ->
            MapRow(index = i) {
                val left = i % 2 == 0
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = if (left) Arrangement.Start else Arrangement.End
                ) {
                    CircleLevelNode(skill = s, index = i, onClick = onClick)
                }
            }
        }
        Spacer(Modifier.height(28.dp))
    }
}

/** Hàng có “đường kẹo” ở giữa */
@Composable
private fun MapRow(
    index: Int,
    content: @Composable RowScope.() -> Unit
) {
    val trailColor = MaterialTheme.colorScheme.primary.copy(alpha = .25f)
    val nodeColor  = MaterialTheme.colorScheme.primary.copy(alpha = .40f)
    val dash = remember { PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f) }
    Box(
        Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(horizontal = 8.dp)
            .drawBehind {
                val x = size.width / 2f
                val dash = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)
                drawLine(
                    color = trailColor,
                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                    end = androidx.compose.ui.geometry.Offset(x, size.height),
                    strokeWidth = 10f,
                    pathEffect = dash
                )

                drawCircle(
                    color = nodeColor,
                    radius = 6.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(x, size.height / 2f)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(Modifier.fillMaxWidth()) { content() }
    }
}

/** Nút level HÌNH TRÒN + vòng tiến độ */
@Composable
private fun CircleLevelNode(
    skill: Skill,
    index: Int,
    onClick: (Skill) -> Unit
) {
    val locked = !skill.unlocked
    Modifier.then(if (!locked) Modifier.clickable { onClick(skill) } else Modifier)
    val progress = (skill.progress / 100f).coerceIn(0f, 1f)
    val ringBg = MaterialTheme.colorScheme.surfaceVariant
    val ringFg = MaterialTheme.colorScheme.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        // khối tròn
        Box(
            modifier = Modifier
                .size(96.dp)
                .alpha(if (locked) 0.55f else 1f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .35f))
                .then(if (!locked) Modifier.clickable { onClick(skill) } else Modifier)
                .drawBehind {
                    val stroke = 8.dp.toPx()

                    // vòng nền
                    drawCircle(
                        color = ringBg,
                        radius = (size.minDimension / 2f) - stroke,
                        style = Stroke(width = stroke)
                    )
                    // vòng progress
                    drawArc(
                        color = ringFg,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(width = stroke),
                        topLeft = androidx.compose.ui.geometry.Offset(stroke, stroke),
                        size = androidx.compose.ui.geometry.Size(
                            size.width - stroke * 2,
                            size.height - stroke * 2
                        )
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            // icon emoji/nhãn trong tâm
            Text(text = skill.icon, style = MaterialTheme.typography.headlineSmall)
            if (locked) {
                Box(
                    Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = .35f))
                )
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        Spacer(Modifier.height(8.dp))
        Text(
            skill.title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
        )
    }
}

/** Trạng thái trống & lỗi */
@Composable
private fun EmptyState() {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Chưa có kỹ năng", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(6.dp))
        Text(
            "Hãy thêm collection /skills trong Firestore",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Lỗi tải danh sách kỹ năng", color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(4.dp))
        Text(message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onRetry) { Text("Thử lại") }
    }
}
