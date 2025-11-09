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
                ui.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                ui.error != null -> ErrorState(
                    message = ui.error ?: "Lỗi tải danh sách kỹ năng",
                    onRetry = vm::loadSkillMap
                )
                ui.skills.isEmpty() -> EmptyState()
                else -> CandyTrailMap(
                    skills = ui.skills,
                    onClick = onOpenLesson
                )
            }
        }
    }
}

@Composable
private fun CandyTrailMap(
    skills: List<Skill>,
    onClick: (Skill) -> Unit
) {
    // nền nhẹ kiểu “map”
    val bg = Brush.verticalGradient(
        listOf(
            MaterialTheme.colorScheme.surface,
            MaterialTheme.colorScheme.surface.copy(alpha = .96f),
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = .88f)
        )
    )

    Column(
        Modifier
            .fillMaxSize()
            .background(bg)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(Modifier.height(8.dp))

        skills.forEachIndexed { index, s ->
            MapRow(index = index, total = skills.size) {
                val left = index % 2 == 0
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = if (left) Arrangement.Start else Arrangement.End
                ) {
                    CandyNode(skill = s, index = index, onClick = onClick)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

/** Một hàng của đường “kẹo”: vẽ đường đứt giữa + đặt node trái/phải */
@Composable
private fun MapRow(
    index: Int,
    total: Int,
    content: @Composable RowScope.() -> Unit
) {
    val trailColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
    val nodeColor  = MaterialTheme.colorScheme.primary.copy(alpha = 0.40f)
    // Tạo PathEffect một lần
    val dash = remember { PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f) }
    Box(
        Modifier
            .fillMaxWidth()
            .height(140.dp)
            .padding(horizontal = 8.dp)
            .drawBehind {
                val x = size.width / 2
                val pe = PathEffect.dashPathEffect(floatArrayOf(18f, 14f), 0f)
                drawLine(
                    color = trailColor,                          // ✅ dùng màu đã lấy sẵn
                    start = androidx.compose.ui.geometry.Offset(x, 0f),
                    end   = androidx.compose.ui.geometry.Offset(x, size.height),
                    strokeWidth = 12f,
                    pathEffect = dash
                )

                drawCircle(
                    color = nodeColor,                           // ✅ dùng màu đã lấy sẵn
                    radius = 8.dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(x, size.height / 2f)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(Modifier.fillMaxWidth()) { content() }
    }
}

/** Nút level kiểu “kẹo” (skill card) */
@Composable
private fun CandyNode(
    skill: Skill,
    index: Int,
    onClick: (Skill) -> Unit
) {
    val locked = !skill.unlocked
    val gradient = Brush.linearGradient(
        listOf(
            MaterialTheme.colorScheme.primary.copy(alpha = .15f),
            MaterialTheme.colorScheme.primary.copy(alpha = .05f)
        )
    )

    val offsetTop = if (index % 2 == 0) 0.dp else 10.dp

    Box(Modifier.padding(top = offsetTop)) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            tonalElevation = 3.dp,
            shadowElevation = 6.dp,
            modifier = Modifier
                .width(170.dp)
                .alpha(if (locked) 0.55f else 1f)
                .then(if (!locked) Modifier.clickable { onClick(skill) } else Modifier)
        ) {
            Column(
                Modifier
                    .background(gradient)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // icon emoji từ Firestore
                    Text(text = skill.icon, style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = skill.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    if (locked) {
                        Spacer(Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Rounded.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp).clip(CircleShape)
                        )
                    }
                }
                // progress
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    val p = (skill.progress / 100f).coerceIn(0f, 1f)
                    Box(
                        Modifier
                            .fillMaxWidth(p)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = .55f))
                    )
                }
            }
        }
    }
}

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
