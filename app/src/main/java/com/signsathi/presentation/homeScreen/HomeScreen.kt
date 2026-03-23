package com.signsathi.presentation.homeScreen

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

//colors
private val NodeCompleted = Color(0xFF58CC02)
private val NodeActive = Color(0xFFFF9600)
private val NodeLocked      = Color(0xFFAFAFAF)   // Grey
private val NodeBorder      = Color(0x33000000)   // Subtle shadow border
private val UnitCardColor   = Color(0xFFFFD900)

// ─────────────────────────────────────────────
// Zigzag X positions — 5 columns, nodes step
// left-centre-right-centre-left across the screen
// ─────────────────────────────────────────────
private val zigzagOffsets: List<Dp> = listOf(60.dp, 120.dp, 180.dp, 120.dp, 60.dp)
@Composable
fun HomeScreen (
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
){
    val uiState by viewModel.uiState.collectAsState()
}