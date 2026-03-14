package com.signsathi.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.signsathi.R
import com.signsathi.navigationGraph.Screens
import com.signsathi.ui.theme.Background
import com.signsathi.ui.theme.DarkGrey
import com.signsathi.ui.theme.Grey
import com.signsathi.ui.theme.LightBlue
import com.signsathi.ui.theme.Orange
import com.signsathi.ui.theme.TextBlack
import com.signsathi.ui.theme.nunito

@Composable
fun GetStartedScreen(
    navController: NavController,
    onGetStarted: () -> Unit = { navController.navigate(Screens.SignUp.route) },
    onAlreadyHaveAccount: () -> Unit = { navController.navigate(Screens.Login.route) },
    onTermsOfService: () -> Unit = {},
    onPrivacyPolicy: () -> Unit = {}
) {
    var termsAccepted by remember { mutableStateOf(true) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp)
                .padding(top = 80.dp, bottom = 36.dp),
//                .background(Color.Red),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = TextBlack)) {
                        append("Welcome\nto SignSathi")
                    }
                    append("\uD83D\uDC4B\uFE0F")
                },
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,

                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Unlock the world of sign language.",
                style = MaterialTheme.typography.bodyLarge,
                color = DarkGrey,
                textAlign = TextAlign.Center,
            )
            Spacer(modifier = Modifier.height(40.dp))

            Image(
                painter = painterResource(id = R.drawable.on_boarding_image),
                contentDescription = "Welcome Image",
                modifier = Modifier.size(380.dp)
            )
            Spacer(modifier = Modifier.weight(1f))

            // ── Get Started Button ────────────────────────────────────────────
            Button(
                onClick = onGetStarted,
                enabled = termsAccepted,
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Orange,
                    contentColor = TextBlack,
                    disabledContainerColor = Orange.copy(alpha = 0.4f),
                    disabledContentColor = TextBlack.copy(alpha = 0.4f)
                ),
                contentPadding = PaddingValues(vertical = 11.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Get started",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = nunito
                    )

                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Already Have Account ──────────────────────────────────────────
            Text(
                text = "I already have an account",
                style = TextStyle(
                    fontFamily = nunito,
                    fontSize = 18.sp,
                    color = DarkGrey,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onAlreadyHaveAccount() }
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ── Terms & Privacy Checkbox ──────────────────────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 5.dp)
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = LightBlue,
                        checkmarkColor = Color.White,
                        uncheckedColor = LightBlue
                    ),
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(10.dp))


                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = Grey, fontSize = 12.sp)) {
                            append("I accept ")
                        }
                        withStyle(
                            SpanStyle(
                                fontFamily = nunito,
                                color = Grey,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("Terms Of Service")
                        }
                        withStyle(SpanStyle(color = Grey, fontSize = 14.sp, fontFamily = nunito)) {
                            append(" and ")
                        }
                        withStyle(
                            SpanStyle(
                                fontFamily = nunito,
                                color = Grey,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                textDecoration = TextDecoration.Underline
                            )
                        ) {
                            append("Privacy Policy")
                        }
                    },
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        // Ideally detect which span was tapped using ClickableText
                        // For simplicity, route both to respective callbacks
                    }
                )
            }
        }
    }
}