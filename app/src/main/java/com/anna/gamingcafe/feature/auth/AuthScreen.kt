package com.anna.gamingcafe.feature.auth

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anna.gamingcafe.core.theme.*

@Composable
fun AuthScreen(viewModel: AuthViewModel) {
    val state = viewModel.uiState
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = BackBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))

            // ── Logo ──
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .background(
                        Brush.linearGradient(PremiumGradient),
                        shape = RoundedCornerShape(20.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsEsports,
                    contentDescription = "Logo",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Anna Gaming Cafe",
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )

            Text(
                text = "Your premium gaming destination",
                color = TextSecondary,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 6.dp, bottom = 40.dp)
            )

            // ── Content based on step ──
            AnimatedContent(
                targetState = when {
                    state.isNewUser -> "name"
                    state.isOtpSent -> "otp"
                    else -> "phone"
                },
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                },
                label = "auth_step"
            ) { step ->
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (step) {
                        "phone" -> PhoneInputStep(
                            phone = state.phone,
                            onPhoneChange = viewModel::updatePhone,
                            onSubmit = { viewModel.sendOtp(); focusManager.clearFocus() },
                            isLoading = state.isLoading
                        )
                        "otp" -> OtpInputStep(
                            otp = state.otp,
                            phone = state.phone,
                            onOtpChange = viewModel::updateOtp,
                            onVerify = { viewModel.verifyOtp(); focusManager.clearFocus() },
                            onBack = { viewModel.resetOtpState() },
                            isLoading = state.isLoading
                        )
                        "name" -> NameInputStep(
                            name = state.fullName,
                            onNameChange = viewModel::updateFullName,
                            onSubmit = { viewModel.completeProfile(); focusManager.clearFocus() },
                            isLoading = state.isLoading
                        )
                    }
                }
            }

            // ── Error ──
            AnimatedVisibility(visible = state.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.12f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = state.error ?: "",
                            color = ErrorRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // ── Footer ──
            Text(
                text = "By continuing, you agree to our Terms of Service",
                color = TextMuted,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        }
    }
}

@Composable
private fun PhoneInputStep(
    phone: String,
    onPhoneChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Enter your phone number",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "We'll send you a 6-digit verification code",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Phone input with +91 prefix
        OutlinedTextField(
            value = phone,
            onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) onPhoneChange(it) },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("9876543210", color = TextMuted) },
            prefix = {
                Text(
                    text = "+91  ",
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            },
            leadingIcon = {
                Icon(Icons.Default.Phone, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = EdgeBorder,
                cursorColor = NeonCyan,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = phone.length == 10 && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                disabledContainerColor = NeonCyan.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = BackBg,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Send Verification Code",
                    color = BackBg,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
private fun OtpInputStep(
    otp: String,
    phone: String,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onBack() }
                .padding(bottom = 16.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = NeonCyan,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text("Change number", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }

        Text(
            text = "Verify your number",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "Enter the 6-digit code sent to +91 $phone",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // OTP input — individual boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(6) { index ->
                val char = otp.getOrNull(index)?.toString() ?: ""
                val isFocused = otp.length == index

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(54.dp)
                        .background(
                            if (char.isNotEmpty()) NeonCyan.copy(alpha = 0.08f) else SurfaceDark,
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.dp,
                            when {
                                char.isNotEmpty() -> NeonCyan
                                isFocused -> NeonCyan.copy(alpha = 0.5f)
                                else -> EdgeBorder
                            },
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        color = TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Hidden text field for keyboard
        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 6 && it.all { c -> c.isDigit() }) onOtpChange(it) },
            modifier = Modifier
                .fillMaxWidth()
                .height(0.dp)
                .offset(y = (-60).dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { if (otp.length == 6) onVerify() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onVerify,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = otp.length == 6 && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                disabledContainerColor = NeonCyan.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BackBg, strokeWidth = 2.dp)
            } else {
                Text("Verify & Continue", color = BackBg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun NameInputStep(
    name: String,
    onNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Welcome to Anna! 🎮",
            color = TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            text = "What should we call you?",
            color = TextSecondary,
            fontSize = 12.sp,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Your name", color = TextMuted) },
            leadingIcon = {
                Icon(Icons.Default.Person, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(20.dp))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSubmit() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = EdgeBorder,
                cursorColor = NeonCyan,
                focusedContainerColor = SurfaceDark,
                unfocusedContainerColor = SurfaceDark
            ),
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = name.isNotBlank() && !isLoading,
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                disabledContainerColor = NeonCyan.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = BackBg, strokeWidth = 2.dp)
            } else {
                Text("Get Started", color = BackBg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
