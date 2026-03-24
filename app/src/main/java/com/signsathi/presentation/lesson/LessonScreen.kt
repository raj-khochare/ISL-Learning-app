package com.signsathi.presentation.lesson

import android.net.Uri
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.NavController
import com.signsathi.ui.theme.Background
import com.signsathi.ui.theme.DarkGrey
import com.signsathi.ui.theme.Orange
import com.signsathi.ui.theme.nunito

// ─── Colours ──────────────────────────────────────────────────────────────────

private val CorrectGreen   = Color(0xFF58CC02)
private val WrongRed       = Color(0xFFFF4B4B)
private val SelectedBorder = Color(0xFF1CB0F6)
private val CardWhite      = Color.White

// ─── Root screen ──────────────────────────────────────────────────────────────

@Composable
fun LessonScreen(
    navController: NavController,
    viewModel: LessonViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(containerColor = Background) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            if (state.phase !is LessonPhase.Completed) {
                LessonTopBar(
                    progress = state.progress,
                    onClose  = { navController.popBackStack() }
                )
            }

            when (val phase = state.phase) {
                is LessonPhase.Loading -> LoadingContent()

                is LessonPhase.Error -> ErrorContent(
                    message = phase.message,
                    onRetry = viewModel::retryLoad
                )

                is LessonPhase.Learn -> LearnPhase(
                    state      = state,
                    onContinue = viewModel::onContinueToQuiz
                )

                is LessonPhase.Quiz,
                is LessonPhase.Result -> QuizPhase(
                    state          = state,
                    onChoiceSelect = viewModel::onChoiceSelected,
                    onCheckAnswer  = viewModel::onCheckAnswer,
                    onNext         = viewModel::onNext
                )

                is LessonPhase.Completed -> CompletedPhase(
                    lessonTitle = state.lessonTitle,
                    totalXp     = phase.totalXpEarned,
                    onFinish    = { navController.popBackStack() }
                )
            }
        }
    }
}

// ─── Top bar ─────────────────────────────────────────────────────────────────

@Composable
private fun LessonTopBar(progress: Float, onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
        }
        Spacer(Modifier.width(8.dp))
        LinearProgressIndicator(
            progress      = { progress },
            modifier      = Modifier
                .weight(1f)
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp)),
            color         = Orange,
            trackColor    = Color(0xFFE5E5E5)
        )
        Spacer(Modifier.width(16.dp))
    }
}

// ─── Loading ──────────────────────────────────────────────────────────────────

@Composable
private fun LoadingContent() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Orange)
    }
}

// ─── Error ────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text      = message,
                style     = TextStyle(fontFamily = nunito, fontSize = 16.sp, color = DarkGrey),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors  = ButtonDefaults.buttonColors(containerColor = Orange)
            ) {
                Icon(Icons.Filled.Replay, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Try Again", style = TextStyle(fontFamily = nunito, fontWeight = FontWeight.Bold))
            }
        }
    }
}

// ─── Learn phase ─────────────────────────────────────────────────────────────

@Composable
private fun LearnPhase(state: LessonUiState, onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text  = state.lessonTitle,
            style = TextStyle(
                fontFamily = nunito,
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                color      = DarkGrey
            )
        )
        Spacer(Modifier.height(20.dp))

        SignVideoCard(
            videoUrl = state.videoUrl,
            title    = state.lessonTitle,
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        )

        Spacer(Modifier.height(20.dp))

        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text  = "How to sign",
                    style = TextStyle(
                        fontFamily = nunito,
                        fontSize   = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Orange
                    )
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = state.description.ifBlank { "Watch the video to learn this sign." },
                    style = TextStyle(
                        fontFamily = nunito,
                        fontSize   = 15.sp,
                        color      = DarkGrey,
                        lineHeight = 22.sp
                    )
                )
            }
        }

        Spacer(Modifier.weight(1f))

        PrimaryButton(
            text    = "GOT IT — LET'S PRACTICE",
            onClick = onContinue,
            color   = Orange
        )
        Spacer(Modifier.height(24.dp))
    }
}

// ─── Quiz phase ───────────────────────────────────────────────────────────────

@Composable
private fun QuizPhase(
    state          : LessonUiState,
    onChoiceSelect : (String) -> Unit,
    onCheckAnswer  : () -> Unit,
    onNext         : () -> Unit
) {
    val question  = state.currentQuestion ?: return
    val isChecked = state.isAnswerChecked

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))

        val promptText = when (question.type) {
            QuestionType.VIDEO_TO_NAME -> "What is this sign?"
            QuestionType.WORD_TO_VIDEO -> "Which video shows \"${question.signTitle}\"?"
        }
        Text(
            text      = promptText,
            style     = TextStyle(
                fontFamily = nunito,
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = DarkGrey
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        when (question.type) {
            QuestionType.VIDEO_TO_NAME -> {
                SignVideoCard(
                    videoUrl = question.videoUrl,
                    title    = question.signTitle,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )
            }
            QuestionType.WORD_TO_VIDEO -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFFFFF3E0)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text  = question.signTitle,
                        style = TextStyle(
                            fontFamily = nunito,
                            fontSize   = 36.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Orange
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        when (question.type) {
            QuestionType.VIDEO_TO_NAME -> {
                question.choices.forEach { choice ->
                    NameChoiceItem(
                        choice     = choice,
                        isSelected = state.selectedChoiceId == choice.id,
                        isChecked  = isChecked,
                        isCorrect  = choice.id == question.correctChoiceId,
                        onClick    = { onChoiceSelect(choice.id) }
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }
            QuestionType.WORD_TO_VIDEO -> {
                Row(
                    modifier              = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    question.choices.forEach { choice ->
                        VideoChoiceItem(
                            choice     = choice,
                            isSelected = state.selectedChoiceId == choice.id,
                            isChecked  = isChecked,
                            isCorrect  = choice.id == question.correctChoiceId,
                            onClick    = { onChoiceSelect(choice.id) },
                            modifier   = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        if (isChecked) {
            ResultBanner(isCorrect = state.isCorrect, xpEarned = if (state.isCorrect) 10 else 0)
            Spacer(Modifier.height(12.dp))
        }

        PrimaryButton(
            text    = when {
                !isChecked      -> "CHECK ANSWER"
                state.isCorrect -> "CONTINUE"
                else            -> "GOT IT"
            },
            onClick = if (isChecked) onNext else onCheckAnswer,
            enabled = state.selectedChoiceId != null,
            color   = when {
                !isChecked      -> Orange
                state.isCorrect -> CorrectGreen
                else            -> WrongRed
            }
        )
        Spacer(Modifier.height(24.dp))
    }
}

// ─── Completed screen ─────────────────────────────────────────────────────────

@Composable
private fun CompletedPhase(lessonTitle: String, totalXp: Int, onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🎉", fontSize = 72.sp)
        Spacer(Modifier.height(24.dp))
        Text(
            text  = "Lesson Complete!",
            style = TextStyle(
                fontFamily = nunito,
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                color      = DarkGrey
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = "You learned \"$lessonTitle\"",
            style     = TextStyle(fontFamily = nunito, fontSize = 16.sp, color = Color.Gray),
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFFFF3CD))
                .padding(horizontal = 32.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text  = "⭐ +$totalXp XP earned",
                style = TextStyle(
                    fontFamily = nunito,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = Color(0xFF856404)
                )
            )
        }

        Spacer(Modifier.height(48.dp))

        PrimaryButton(
            text    = "BACK TO HOME",
            onClick = onFinish,
            color   = Orange
        )
    }
}

// ─── Sign video card ──────────────────────────────────────────────────────────

@Composable
fun SignVideoCard(
    videoUrl : String,
    title    : String,
    modifier : Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFE8F4FF)),
        contentAlignment = Alignment.Center
    ) {
        if (videoUrl.isNotBlank()) {
            VideoPlayer(videoUrl = videoUrl, modifier = Modifier.fillMaxSize())
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("🤟", fontSize = 72.sp)
                Spacer(Modifier.height(8.dp))
                Text(
                    text  = title,
                    style = TextStyle(
                        fontFamily = nunito,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = DarkGrey
                    )
                )
            }
        }
    }
}

// ─── ExoPlayer ────────────────────────────────────────────────────────────────

@Composable
fun VideoPlayer(videoUrl: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val player  = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            prepare()
            playWhenReady = true
            repeatMode    = ExoPlayer.REPEAT_MODE_ONE
        }
    }
    DisposableEffect(Unit) { onDispose { player.release() } }
    AndroidView(
        factory  = { ctx -> PlayerView(ctx).apply { this.player = player } },
        modifier = modifier
    )
}

// ─── Name choice (VIDEO_TO_NAME) ─────────────────────────────────────────────

@Composable
private fun NameChoiceItem(
    choice     : QuizChoice,
    isSelected : Boolean,
    isChecked  : Boolean,
    isCorrect  : Boolean,
    onClick    : () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue   = when {
            isChecked && isCorrect  -> CorrectGreen
            isChecked && isSelected -> WrongRed
            isSelected              -> SelectedBorder
            else                    -> Color(0xFFE5E5E5)
        },
        animationSpec = tween(200), label = "border"
    )
    val bgColor by animateColorAsState(
        targetValue   = when {
            isChecked && isCorrect  -> Color(0xFFD7F5B1)
            isChecked && isSelected -> Color(0xFFFFDDDD)
            isSelected              -> Color(0xFFEAF6FF)
            else                    -> CardWhite
        },
        animationSpec = tween(200), label = "bg"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                enabled           = !isChecked
            ) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text  = choice.label,
            style = TextStyle(
                fontFamily = nunito,
                fontSize   = 17.sp,
                fontWeight = FontWeight.Bold,
                color      = when {
                    isChecked && isCorrect  -> Color(0xFF2D7A00)
                    isChecked && isSelected -> WrongRed
                    else                    -> DarkGrey
                }
            )
        )
    }
}

// ─── Video choice (WORD_TO_VIDEO) ─────────────────────────────────────────────

@Composable
private fun VideoChoiceItem(
    choice     : QuizChoice,
    isSelected : Boolean,
    isChecked  : Boolean,
    isCorrect  : Boolean,
    onClick    : () -> Unit,
    modifier   : Modifier = Modifier
) {
    val borderColor by animateColorAsState(
        targetValue   = when {
            isChecked && isCorrect  -> CorrectGreen
            isChecked && isSelected -> WrongRed
            isSelected              -> SelectedBorder
            else                    -> Color(0xFFE5E5E5)
        },
        animationSpec = tween(200), label = "videoBorder"
    )
    val bgColor by animateColorAsState(
        targetValue   = when {
            isChecked && isCorrect  -> Color(0xFFD7F5B1)
            isChecked && isSelected -> Color(0xFFFFDDDD)
            isSelected              -> Color(0xFFEAF6FF)
            else                    -> Color(0xFFF5F5F5)
        },
        animationSpec = tween(200), label = "videoBg"
    )
    Column(
        modifier = modifier
            .aspectRatio(0.75f)
            .clip(RoundedCornerShape(14.dp))
            .background(bgColor)
            .border(2.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication        = null,
                enabled           = !isChecked
            ) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (choice.videoUrl.isNotBlank()) {
            VideoPlayer(
                videoUrl = choice.videoUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
            )
        } else {
            Box(
                modifier         = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) { Text("🤟", fontSize = 36.sp) }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text      = choice.label,
            style     = TextStyle(
                fontFamily = nunito,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Bold,
                color      = when {
                    isChecked && isCorrect  -> Color(0xFF2D7A00)
                    isChecked && isSelected -> WrongRed
                    else                    -> DarkGrey
                }
            ),
            modifier  = Modifier.padding(bottom = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

// ─── Result banner ────────────────────────────────────────────────────────────

@Composable
private fun ResultBanner(isCorrect: Boolean, xpEarned: Int) {
    val bg      = if (isCorrect) Color(0xFFD7F5B1) else Color(0xFFFFDDDD)
    val color   = if (isCorrect) Color(0xFF2D7A00) else WrongRed
    val message = if (isCorrect) "Correct! +$xpEarned XP" else "Not quite — keep going!"
    val emoji   = if (isCorrect) "🎉" else "😅"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = "$emoji  $message",
            style     = TextStyle(
                fontFamily = nunito,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = color
            ),
            textAlign = TextAlign.Center
        )
    }
}

// ─── Primary button ───────────────────────────────────────────────────────────

@Composable
private fun PrimaryButton(
    text    : String,
    onClick : () -> Unit,
    color   : Color   = Orange,
    enabled : Boolean = true
) {
    Button(
        onClick  = onClick,
        enabled  = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape  = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor         = color,
            disabledContainerColor = Color(0xFFE5E5E5)
        )
    ) {
        Text(
            text  = text,
            style = TextStyle(
                fontFamily = nunito,
                fontSize   = 16.sp,
                fontWeight = FontWeight.Bold,
                color      = if (enabled) Color.White else Color.Gray
            )
        )
    }
}
