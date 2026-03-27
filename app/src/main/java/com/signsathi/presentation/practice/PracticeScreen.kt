package com.signsathi.presentation.practice

import android.Manifest
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.signsathi.data.recognition.CameraManager
import com.signsathi.ui.theme.Background
import com.signsathi.ui.theme.DarkGrey
import com.signsathi.ui.theme.Orange
import com.signsathi.ui.theme.nunito
import dagger.hilt.android.EntryPointAccessors

private val CorrectGreen = Color(0xFF58CC02)
private val WrongRed     = Color(0xFFFF4B4B)

// ─── Root screen ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PracticeScreen(
    viewModel: PracticeViewModel = hiltViewModel()
) {
    val uiState        by viewModel.uiState.collectAsState()
    val cameraPermission = rememberPermissionState(Manifest.permission.CAMERA)

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) {
            cameraPermission.launchPermissionRequest()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when {
            uiState.isInitializing -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Orange)
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text  = "Loading recognition model...",
                            style = TextStyle(fontFamily = nunito, color = DarkGrey)
                        )
                    }
                }
            }

            uiState.initError != null -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text      = uiState.initError ?: "Unknown error",
                        style     = TextStyle(fontFamily = nunito, color = WrongRed, fontSize = 15.sp),
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.padding(32.dp)
                    )
                }
            }

            !cameraPermission.status.isGranted -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier            = Modifier.padding(32.dp)
                    ) {
                        Text(
                            text      = "Camera permission is required for sign recognition.",
                            style     = TextStyle(fontFamily = nunito, fontSize = 16.sp, color = DarkGrey),
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { cameraPermission.launchPermissionRequest() },
                            colors  = ButtonDefaults.buttonColors(containerColor = Orange)
                        ) {
                            Text("Grant Permission", style = TextStyle(fontFamily = nunito, fontWeight = FontWeight.Bold))
                        }
                    }
                }
            }

            else -> {
                PracticeContent(
                    uiState   = uiState,
                    viewModel = viewModel
                )
            }
        }
    }
}

// ─── Main content after permission granted ────────────────────────────────────

@Composable
private fun PracticeContent(
    uiState   : PracticeUiState,
    viewModel : PracticeViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {

        // Mode tabs
        val tabs = listOf("Recognize", "Guided", "Flashcards")
        val selectedTab = when (uiState.mode) {
            PracticeMode.FREE_RECOGNITION -> 0
            PracticeMode.GUIDED_PRACTICE  -> 1
            PracticeMode.FLASHCARD        -> 2
        }

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor   = Background
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick  = {
                        when (index) {
                            0 -> viewModel.selectMode(PracticeMode.FREE_RECOGNITION)
                            1 -> viewModel.selectMode(PracticeMode.GUIDED_PRACTICE)
                            2 -> viewModel.selectMode(PracticeMode.FLASHCARD)
                        }
                    },
                    text = {
                        Text(
                            text  = title,
                            style = TextStyle(
                                fontFamily = nunito,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 14.sp,
                                color      = if (selectedTab == index) Orange else DarkGrey
                            )
                        )
                    }
                )
            }
        }

        when (uiState.mode) {
            PracticeMode.FREE_RECOGNITION -> FreeRecognitionMode(uiState, viewModel)
            PracticeMode.GUIDED_PRACTICE  -> GuidedPracticeMode(uiState, viewModel)
            PracticeMode.FLASHCARD        -> FlashcardMode(uiState, viewModel)
        }
    }
}

// ─── Camera preview composable ────────────────────────────────────────────────

@Composable
private fun CameraPreview(
    viewModel : PracticeViewModel,
    modifier  : Modifier = Modifier
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Get CameraManager from Hilt entry point
    val cameraManager = remember {
        EntryPointAccessors.fromApplication(
            context.applicationContext,
            CameraManagerEntryPoint::class.java
        ).cameraManager()
    }

    val previewView = remember { PreviewView(context) }

    DisposableEffect(lifecycleOwner) {
        cameraManager.startCamera(
            lifecycleOwner   = lifecycleOwner,
            previewView      = previewView,
            onFrameAvailable = { bitmap -> viewModel.onCameraFrame(bitmap) }
        )
        onDispose { cameraManager.stopCamera() }
    }

    AndroidView(
        factory  = { previewView },
        modifier = modifier
    )
}

// ─── Free recognition mode ────────────────────────────────────────────────────

@Composable
private fun FreeRecognitionMode(
    uiState   : PracticeUiState,
    viewModel : PracticeViewModel
) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text  = "Perform any sign",
            style = TextStyle(
                fontFamily = nunito,
                fontSize   = 16.sp,
                color      = DarkGrey
            )
        )
        Spacer(Modifier.height(12.dp))

        // Camera feed
        CameraPreview(
            viewModel = viewModel,
            modifier  = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(Modifier.height(16.dp))

        // Recognition result card
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(
                containerColor = if (uiState.isHandDetected) Color(0xFFE8F5E9) else Color(0xFFF5F5F5)
            ),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column(
                modifier            = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!uiState.isHandDetected) {
                    Text(
                        text  = "Show your hand to the camera",
                        style = TextStyle(
                            fontFamily = nunito,
                            fontSize   = 16.sp,
                            color      = Color.Gray
                        )
                    )
                } else {
                    Text(
                        text  = uiState.recognizedLabel?.uppercase() ?: "",
                        style = TextStyle(
                            fontFamily = nunito,
                            fontSize   = 52.sp,
                            fontWeight = FontWeight.Bold,
                            color      = DarkGrey
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = uiState.recognizedCategory?.replaceFirstChar { it.uppercase() } ?: "",
                        style = TextStyle(
                            fontFamily = nunito,
                            fontSize   = 14.sp,
                            color      = Orange
                        )
                    )
                    Spacer(Modifier.height(12.dp))

                    // Confidence bar
                    LinearProgressIndicator(
                        progress      = { uiState.confidence },
                        modifier      = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color         = CorrectGreen,
                        trackColor    = Color(0xFFE0E0E0)
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text  = "${(uiState.confidence * 100).toInt()}% confidence",
                        style = TextStyle(fontFamily = nunito, fontSize = 12.sp, color = Color.Gray)
                    )
                }
            }
        }
    }
}

// ─── Guided practice mode ─────────────────────────────────────────────────────

@Composable
private fun GuidedPracticeMode(
    uiState   : PracticeUiState,
    viewModel : PracticeViewModel
) {
    val bgColor by animateColorAsState(
        targetValue   = when (uiState.attemptResult) {
            AttemptResult.CORRECT -> Color(0xFFD7F5B1)
            AttemptResult.WRONG   -> Color(0xFFFFDDDD)
            AttemptResult.WAITING -> Background
        },
        animationSpec = tween(300), label = "guidedBg"
    )

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Score
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text  = "Score: ${uiState.guidedScore}/${uiState.guidedTotal}",
                style = TextStyle(
                    fontFamily = nunito,
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color      = DarkGrey
                )
            )
        }

        Spacer(Modifier.height(12.dp))

        // Target sign display
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text  = "Sign this:",
                        style = TextStyle(fontFamily = nunito, fontSize = 14.sp, color = Color.Gray)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text  = uiState.targetSign?.label?.uppercase() ?: "",
                        style = TextStyle(
                            fontFamily = nunito,
                            fontSize   = 56.sp,
                            fontWeight = FontWeight.Bold,
                            color      = Orange
                        )
                    )
                    Text(
                        text  = uiState.targetSign?.category
                            ?.replaceFirstChar { it.uppercase() } ?: "",
                        style = TextStyle(fontFamily = nunito, fontSize = 13.sp, color = Color.Gray)
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Camera feed
        CameraPreview(
            viewModel = viewModel,
            modifier  = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
        )

        Spacer(Modifier.height(12.dp))

        // Result feedback
        when (uiState.attemptResult) {
            AttemptResult.CORRECT -> {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = CorrectGreen, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = "Correct!",
                        style = TextStyle(
                            fontFamily = nunito,
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color      = CorrectGreen
                        )
                    )
                }
            }
            AttemptResult.WRONG -> {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier              = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, tint = WrongRed, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text  = "Skipped",
                        style = TextStyle(
                            fontFamily = nunito,
                            fontSize   = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color      = WrongRed
                        )
                    )
                }
            }
            AttemptResult.WAITING -> {
                Text(
                    text  = "Perform the sign above",
                    style = TextStyle(fontFamily = nunito, fontSize = 15.sp, color = Color.Gray)
                )
            }
        }

        Spacer(Modifier.weight(1f))

        OutlinedButton(
            onClick = viewModel::onGuidedSkip,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text  = "Skip",
                style = TextStyle(fontFamily = nunito, fontWeight = FontWeight.Bold, color = DarkGrey)
            )
        }
    }
}

// ─── Flashcard mode ───────────────────────────────────────────────────────────

@Composable
private fun FlashcardMode(
    uiState   : PracticeUiState,
    viewModel : PracticeViewModel
) {
    if (uiState.flashcardSessionDone) {
        FlashcardResults(uiState = uiState, onRestart = viewModel::restartFlashcards)
        return
    }

    val card = uiState.currentFlashcard ?: return

    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Progress
        LinearProgressIndicator(
            progress      = { uiState.flashcardProgress },
            modifier      = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color         = Orange,
            trackColor    = Color(0xFFE0E0E0)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text  = "${uiState.currentFlashcardIndex + 1} / ${uiState.flashcards.size}",
            style = TextStyle(fontFamily = nunito, fontSize = 13.sp, color = Color.Gray)
        )

        Spacer(Modifier.height(16.dp))

        // Flashcard
        Card(
            modifier  = Modifier
                .fillMaxWidth()
                .height(200.dp),
            shape     = RoundedCornerShape(20.dp),
            colors    = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text  = card.sign.label.uppercase(),
                        style = TextStyle(
                            fontFamily = nunito,
                            fontSize   = 64.sp,
                            fontWeight = FontWeight.Bold,
                            color      = DarkGrey
                        )
                    )
                    Text(
                        text  = card.sign.category.replaceFirstChar { it.uppercase() },
                        style = TextStyle(fontFamily = nunito, fontSize = 14.sp, color = Orange)
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (!card.isRevealed) {
            Text(
                text      = "Perform this sign, then rate yourself",
                style     = TextStyle(fontFamily = nunito, fontSize = 15.sp, color = Color.Gray),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick  = viewModel::onFlashcardReveal,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Orange)
            ) {
                Text(
                    "I ATTEMPTED IT",
                    style = TextStyle(fontFamily = nunito, fontWeight = FontWeight.Bold, color = Color.White)
                )
            }
        } else {
            Text(
                text      = "How did you do?",
                style     = TextStyle(fontFamily = nunito, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = DarkGrey),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick  = { viewModel.onFlashcardRate(FlashcardRating.NOT_YET) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = WrongRed)
                ) {
                    Text(
                        "Not yet",
                        style = TextStyle(fontFamily = nunito, fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }
                Button(
                    onClick  = { viewModel.onFlashcardRate(FlashcardRating.GOT_IT) },
                    modifier = Modifier.weight(1f).height(52.dp),
                    shape    = RoundedCornerShape(14.dp),
                    colors   = ButtonDefaults.buttonColors(containerColor = CorrectGreen)
                ) {
                    Text(
                        "Got it",
                        style = TextStyle(fontFamily = nunito, fontWeight = FontWeight.Bold, color = Color.White)
                    )
                }
            }
        }
    }
}

// ─── Flashcard results ────────────────────────────────────────────────────────

@Composable
private fun FlashcardResults(
    uiState   : PracticeUiState,
    onRestart : () -> Unit
) {
    Column(
        modifier            = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector        = Icons.Filled.Check,
            contentDescription = "Session complete",
            tint               = CorrectGreen,
            modifier           = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text  = "Session Complete!",
            style = TextStyle(
                fontFamily = nunito,
                fontSize   = 26.sp,
                fontWeight = FontWeight.Bold,
                color      = DarkGrey
            )
        )
        Spacer(Modifier.height(24.dp))

        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ResultStat(
                label = "Got it",
                value = uiState.gotItCount.toString(),
                color = CorrectGreen,
                modifier = Modifier.weight(1f)
            )
            ResultStat(
                label = "Not yet",
                value = uiState.notYetCount.toString(),
                color = WrongRed,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(32.dp))

        Button(
            onClick  = onRestart,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(14.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = Orange)
        ) {
            Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color.White)
            Spacer(Modifier.width(8.dp))
            Text(
                "Practice Again",
                style = TextStyle(fontFamily = nunito, fontWeight = FontWeight.Bold, color = Color.White)
            )
        }
    }
}

@Composable
private fun ResultStat(
    label    : String,
    value    : String,
    color    : Color,
    modifier : Modifier = Modifier
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier            = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text  = value,
                style = TextStyle(
                    fontFamily = nunito,
                    fontSize   = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color      = color
                )
            )
            Text(
                text  = label,
                style = TextStyle(fontFamily = nunito, fontSize = 14.sp, color = Color.Gray)
            )
        }
    }
}