package com.signsathi.presentation.homeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.signsathi.data.model.LessonNode
import com.signsathi.data.model.LessonUnit
import com.signsathi.data.model.NodeState
import com.signsathi.data.model.UserProgress
import com.signsathi.presentation.components.BottomNavBar
import com.signsathi.ui.theme.Background
import com.signsathi.ui.theme.DarkGrey
import com.signsathi.ui.theme.Orange
import com.signsathi.ui.theme.nunito

// ─── Colours ──────────────────────────────────────────────────────────────────

private val NodeCompleted    = Color(0xFF58CC02)
private val NodeActive       = Color(0xFFFF9600)
private val NodeLocked       = Color(0xFFAFAFAF)
private val NodeRingComplete = Color(0xFF3EA000)
private val NodeRingActive   = Color(0xFFD47000)
private val NodeRingLocked   = Color(0xFF888888)
private val UnitCardColor    = Color(0xFFFFD900)

// ─── Zigzag offsets ───────────────────────────────────────────────────────────
// 5-step pattern that creates the winding path effect

private val zigzagOffsets: List<Dp> = listOf(60.dp, 130.dp, 190.dp, 130.dp, 60.dp)

// ─── Root screen ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    Scaffold(
        bottomBar = { BottomNavBar(navController = navController) },
        containerColor = Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Background)
                .statusBarsPadding()
        ) {
            when (val state = uiState) {
                is HomeUiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Orange)
                    }
                }

                is HomeUiState.Error -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = state.message,
                            style = TextStyle(fontFamily = nunito, color = DarkGrey)
                        )
                    }
                }

                is HomeUiState.Success -> {
                    TopStatsBar(progress = state.progress)

                    PullToRefreshBox(
                        isRefreshing = state.isRefreshing,
                        onRefresh = { viewModel.forceRefresh() },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LessonPath(
                            units = state.units,
                            onNodeClick = { viewModel.onLessonClick(it) }
                        )
                    }
                }
            }
        }
    }
}

// ─── Top stats bar ────────────────────────────────────────────────────────────

@Composable
private fun TopStatsBar(progress: UserProgress) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 50.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatChip(
            icon = {
                Icon(
                    Icons.Filled.Star,
                    contentDescription = "XP",
                    tint = Color(0xFFFFD900),
                    modifier = Modifier.size(22.dp)
                )
            },
            value = progress.xp.toString()
        )
        StatChip(
            icon = {
                Icon(
                    Icons.Filled.Whatshot,
                    contentDescription = "Streak",
                    tint = Color(0xFFFF6B00),
                    modifier = Modifier.size(22.dp)
                )
            },
            value = progress.streakDays.toString()
        )
        StatChip(
            icon = {
                Icon(
                    Icons.Filled.Favorite,
                    contentDescription = "Hearts",
                    tint = Color(0xFFFF4D4D),
                    modifier = Modifier.size(22.dp)
                )
            },
            value = progress.heartsLeft.toString()
        )
    }
}

@Composable
private fun StatChip(
    icon: @Composable () -> Unit,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        icon()
        Spacer(Modifier.width(4.dp))
        Text(
            text = value,
            style = TextStyle(
                fontFamily = nunito,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGrey
            )
        )
    }
}

// ─── Lesson path ──────────────────────────────────────────────────────────────

@Composable
private fun LessonPath(
    units: List<LessonUnit>,
    onNodeClick: (LessonNode) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.Start
    ) {
        units.forEach { unit ->
            item(key = "header_${unit.id}") {
                UnitHeader(unit = unit)
                Spacer(modifier = Modifier.height(28.dp))
            }

            items(
                items = unit.nodes,
                key = { it.id }
            ) { node ->
                val index = unit.nodes.indexOf(node)
                val xOffset = zigzagOffsets[index % zigzagOffsets.size]

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    LessonNodeItem(
                        node = node,
                        modifier = Modifier.offset(x = xOffset),
                        onClick = { onNodeClick(node) }
                    )
                }
            }

            item(key = "spacer_${unit.id}") {
                Spacer(modifier = Modifier.height(36.dp))
            }
        }
    }
}

// ─── Unit header card ─────────────────────────────────────────────────────────

@Composable
private fun UnitHeader(unit: LessonUnit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = UnitCardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = unit.title,
                    style = TextStyle(
                        fontFamily = nunito,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF6B4C00)
                    )
                )
                Text(
                    text = unit.description,
                    style = TextStyle(
                        fontFamily = nunito,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3D2B00)
                    )
                )
            }

            // Unit number badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFB800)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = unit.id.removePrefix("unit_"),
                    style = TextStyle(
                        fontFamily = nunito,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF3D2B00)
                    )
                )
            }
        }
    }
}

// ─── Lesson node ──────────────────────────────────────────────────────────────

@Composable
private fun LessonNodeItem(
    node: LessonNode,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isLocked = node.state is NodeState.Locked

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // "Continue" bubble — only on the ActiveContinue node
        if (node.state is NodeState.ActiveContinue) {
            ContinueBubble()
            Spacer(Modifier.height(6.dp))
        }

        // Outer ring (gives the 3D coin border effect)
        IconButton(
            onClick = { if (!isLocked) onClick() },
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(nodeRingColor(node.state))
        ) {
            // Inner disc
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(CircleShape)
                    .background(nodeColor(node.state)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (node.state) {
                        is NodeState.Completed      -> Icons.Filled.Check
                        is NodeState.Active         -> Icons.Filled.Star
                        is NodeState.ActiveContinue -> Icons.Filled.Star
                        is NodeState.Locked         -> Icons.Filled.Lock
                    },
                    contentDescription = node.title,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

// ─── "Continue" tooltip bubble ────────────────────────────────────────────────

@Composable
private fun ContinueBubble() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = "Continue",
            style = TextStyle(
                fontFamily = nunito,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Orange
            )
        )
    }
}

// ─── Colour helpers ───────────────────────────────────────────────────────────

private fun nodeRingColor(state: NodeState): Color = when (state) {
    is NodeState.Completed      -> NodeRingComplete
    is NodeState.Active         -> NodeRingActive
    is NodeState.ActiveContinue -> NodeRingActive
    is NodeState.Locked         -> NodeRingLocked
}

private fun nodeColor(state: NodeState): Color = when (state) {
    is NodeState.Completed      -> NodeCompleted
    is NodeState.Active         -> NodeActive
    is NodeState.ActiveContinue -> NodeActive
    is NodeState.Locked         -> NodeLocked
}