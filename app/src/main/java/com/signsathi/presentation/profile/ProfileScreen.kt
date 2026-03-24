package com.signsathi.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.signsathi.navigationGraph.Screens
import com.signsathi.presentation.components.BottomNavBar
import com.signsathi.ui.theme.*

@Composable
fun ProfileScreen(
    navController : NavController,
    viewModel     : ProfileViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    // Navigate to login when sign-out completes
    LaunchedEffect(state.error) {
        if (state.error == "Not signed in") {
            navController.navigate(Screens.Login.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    Scaffold(
        bottomBar      = { BottomNavBar(navController) },
        containerColor = Background
    ) { padding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize(), Alignment.Center
            ) { CircularProgressIndicator(color = Orange) }

            else -> ProfileContent(
                state    = state,
                padding  = padding,
                onSignOut = { viewModel.signOut() }
            )
        }
    }
}

@Composable
private fun ProfileContent(
    state    : ProfileUiState,
    padding  : PaddingValues,
    onSignOut: () -> Unit
) {
    LazyColumn(
        modifier              = Modifier
            .fillMaxSize()
            .padding(padding)
            .statusBarsPadding(),
        horizontalAlignment   = Alignment.CenterHorizontally,
        contentPadding        = PaddingValues(bottom = 32.dp)
    ) {

        // ── Avatar + name ──────────────────────────────────────────────────
        item {
            Spacer(Modifier.height(36.dp))
            Box(
                modifier            = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(Orange),
                contentAlignment    = Alignment.Center
            ) {
                Text(
                    text  = state.username.take(1).uppercase(),
                    style = TextStyle(
                        fontFamily = nunito,
                        fontSize   = 40.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                )
            }
            Spacer(Modifier.height(14.dp))
            Text(
                text  = state.username,
                style = TextStyle(
                    fontFamily = nunito,
                    fontSize   = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color      = DarkGrey
                )
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text  = state.email,
                style = TextStyle(
                    fontFamily = nunito,
                    fontSize   = 14.sp,
                    color      = Color.Gray
                )
            )
            Spacer(Modifier.height(32.dp))
        }

        // ── Stats grid ─────────────────────────────────────────────────────
        item {
            Row(
                modifier            = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label    = "XP",
                    value    = state.xp.toString(),
                    icon     = Icons.Filled.Star,
                    tint     = Color(0xFFFFD900),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label    = "Streak",
                    value    = "${state.streakDays}d",
                    icon     = Icons.Filled.Whatshot,
                    tint     = Color(0xFFFF6B00),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label    = "Hearts",
                    value    = state.heartsLeft.toString(),
                    icon     = Icons.Filled.Favorite,
                    tint     = Color(0xFFFF4D4D),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label    = "Done",
                    value    = state.lessonsCompleted.toString(),
                    icon     = Icons.Filled.Check,
                    tint     = Color(0xFF58CC02),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(32.dp))
        }

        // ── Sign out button ────────────────────────────────────────────────
        item {
            Button(
                onClick = onSignOut,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(52.dp),
                shape  = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFEEEE),
                    contentColor   = Color(0xFFCC0000)
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Icon(
                    imageVector         = Icons.Filled.ExitToApp,
                    contentDescription  = "Sign out",
                    modifier            = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text  = "Sign Out",
                    style = TextStyle(
                        fontFamily = nunito,
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    label    : String,
    value    : String,
    icon     : ImageVector,
    tint     : Color,
    modifier : Modifier
) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = tint.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier            = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector        = icon,
                contentDescription = label,
                tint               = tint,
                modifier           = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text  = value,
                style = TextStyle(
                    fontFamily = nunito,
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = tint
                )
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text  = label,
                style = TextStyle(
                    fontFamily = nunito,
                    fontSize   = 11.sp,
                    color      = DarkGrey
                )
            )
        }
    }
}