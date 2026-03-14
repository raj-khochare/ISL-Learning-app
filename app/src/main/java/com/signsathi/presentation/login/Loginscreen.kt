package com.signsathi.presentation.login

import android.graphics.drawable.Icon
import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CorporateFare
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.signsathi.components.CustomButton
import com.signsathi.components.CustomTextField
import com.signsathi.navigationGraph.Screens
import com.signsathi.ui.theme.Background
import com.signsathi.ui.theme.DarkGrey
import com.signsathi.ui.theme.Orange
import com.signsathi.ui.theme.TextBlack
import com.signsathi.ui.theme.nunito
import com.signsathi.utils.UiState

@Composable
//@Preview
fun LoginScreen(
    navController: NavController,
    viewModel: LoginViewModel = hiltViewModel()
) {

    val authState = viewModel.authState.value
    val snackbarHostState = remember { SnackbarHostState() }

    // navigation & error handling
    LaunchedEffect(authState) {
        when (authState) {
            is UiState.Error -> {
                snackbarHostState.showSnackbar(authState.message)
                viewModel.resetState()
            }
            is UiState.Success -> {
                navController.navigate(Screens.Home.route) {
                    popUpTo(Screens.GetStarted.route) {
                        inclusive = true   // ← removes GetStarted too
                    }
                }
            }
            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .navigationBarsPadding()
            .padding(bottom = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TOP Text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            IconButton(
                onClick = {},
                modifier = Modifier.align(Alignment.CenterStart)

            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "close"
                )
            }
            Text(
                text = "Enter Your Details",
                modifier = Modifier.align(Alignment.Center),
                style = TextStyle(
                    fontFamily = nunito,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGrey
                )
            )
        }

        // user's input
        CustomTextField(
            input = viewModel.emailText.value,
            label = "Email",
            onValueChange = {
                viewModel.setEmailText(it)
            },
            modifier = Modifier.padding(16.dp)
        )
        CustomTextField(
            input = viewModel.passwordText.value,
            label = "Password",
            onValueChange = {
                viewModel.setPasswordText(it)
            },
            keyboardType = KeyboardType.Password,
            isPassword = true,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 20.dp)
        )

        if (authState is UiState.Loading) {
            CircularProgressIndicator(
                color = Orange,
                modifier = Modifier.padding(8.dp)
            )
        }


        //sign in button
        CustomButton(
            text = "SIGN IN ",
            onClick = {
                if (authState !is UiState.Loading) viewModel.signIn()
            }
        )

        Text(
            text = "FORGOT PASSWORD",
            style = TextStyle(
                fontFamily = nunito,
                fontSize = 18.sp,
                color = DarkGrey,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .padding(top = 20.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { viewModel.forgotPassword() }
        )

        Spacer(modifier = Modifier.weight(1f))

        //bottom buttons(sign in with mobile,google)
        CustomButton(
            text = "SIGN IN WITH MOBILE",
            onClick = {},
            icon = Icons.Default.Phone,
            iconDescription = "phone"
        )
        CustomButton(
            text = "SIGN IN WITH GOOGLE",
            onClick = {},
            icon = Icons.Default.CorporateFare,
            iconDescription = "Google logo",
            modifier = Modifier.padding(top = 10.dp)
        )

    }
}
