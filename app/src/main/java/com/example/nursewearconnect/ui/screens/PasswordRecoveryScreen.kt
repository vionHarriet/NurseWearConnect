package com.example.nursewearconnect.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.ui.theme.*
import com.example.nursewearconnect.ui.components.PasswordStrengthSection

enum class RecoveryState {
    METHOD_SELECTION, OTP_VERIFICATION, NEW_PASSWORD, SUCCESS
}

@Composable
fun PasswordRecoveryScreen(
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var currentState by remember { mutableStateOf(RecoveryState.METHOD_SELECTION) }
    var selectedMethod by remember { mutableStateOf("sms") }
    var isPasswordValid by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
        // Decorative Gradients
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.4f)
                .align(Alignment.TopStart)
                .offset(x = (-80).dp, y = (-80).dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Brand100.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )
        Box(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.4f)
                .align(Alignment.BottomEnd)
                .offset(x = 80.dp, y = 80.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFDBEAFE).copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            if (currentState != RecoveryState.SUCCESS) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentState == RecoveryState.METHOD_SELECTION) {
                                onBack()
                            } else {
                                currentState = when (currentState) {
                                    RecoveryState.OTP_VERIFICATION -> RecoveryState.METHOD_SELECTION
                                    RecoveryState.NEW_PASSWORD -> RecoveryState.OTP_VERIFICATION
                                    else -> RecoveryState.METHOD_SELECTION
                                }
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.White, CircleShape)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Slate400,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(100.dp),
                        color = Brand50,
                    ) {
                        Text(
                            "Recovery",
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = Brand600,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                AnimatedContent(targetState = currentState, label = "recovery_state") { state ->
                    when (state) {
                        RecoveryState.METHOD_SELECTION -> MethodSelectionContent(
                            selectedMethod = selectedMethod,
                            onMethodSelected = { selectedMethod = it }
                        )
                        RecoveryState.OTP_VERIFICATION -> OtpVerificationContent(selectedMethod)
                        RecoveryState.NEW_PASSWORD -> NewPasswordContent(
                            onPasswordValid = { isPasswordValid = it }
                        )
                        RecoveryState.SUCCESS -> SuccessContent()
                    }
                }
            }

            // Footer
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.9f),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = {
                            currentState = when (currentState) {
                                RecoveryState.METHOD_SELECTION -> RecoveryState.OTP_VERIFICATION
                                RecoveryState.OTP_VERIFICATION -> RecoveryState.NEW_PASSWORD
                                RecoveryState.NEW_PASSWORD -> RecoveryState.SUCCESS
                                RecoveryState.SUCCESS -> {
                                    onSuccess()
                                    RecoveryState.SUCCESS
                                }
                            }
                        },
                        enabled = if (currentState == RecoveryState.NEW_PASSWORD) isPasswordValid else true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brand600,
                            disabledContainerColor = Brand200
                        )
                    ) {
                        Text(
                            text = when (currentState) {
                                RecoveryState.METHOD_SELECTION -> "Continue"
                                RecoveryState.OTP_VERIFICATION -> "Verify"
                                RecoveryState.NEW_PASSWORD -> "Reset Password"
                                RecoveryState.SUCCESS -> "Back to Log In"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (currentState != RecoveryState.SUCCESS) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Back to Login",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onBack() },
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Slate500
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MethodSelectionContent(
    selectedMethod: String,
    onMethodSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brand100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Shield,
                contentDescription = null,
                tint = Brand600,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Forgot Password?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Select which contact details should we use to reset your password.",
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            color = Slate500,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(32.dp))

        RecoveryMethodItem(
            icon = Icons.Default.Sms,
            title = "Via SMS:",
            subtitle = "••• ••• 4821",
            selected = selectedMethod == "sms",
            onClick = { onMethodSelected("sms") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        RecoveryMethodItem(
            icon = Icons.Default.Email,
            title = "Via Email:",
            subtitle = "j•••@hospital.com",
            selected = selectedMethod == "email",
            onClick = { onMethodSelected("email") }
        )
    }
}

@Composable
fun RecoveryMethodItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = if (selected) Brand500 else Slate100
        ),
        shadowElevation = if (selected) 4.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(if (selected) Brand50 else Slate50, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (selected) Brand500 else Slate400,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, color = Slate500)
                Text(subtitle, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Slate900)
            }
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(2.dp, if (selected) Brand500 else Slate200, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(Brand500, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
fun OtpVerificationContent(selectedMethod: String) {
    val contactInfo = if (selectedMethod == "sms") "••• ••• 4821" else "j•••@hospital.com"
    val contactLabel = if (selectedMethod == "sms") "Phone" else "Email"
    val icon = if (selectedMethod == "sms") Icons.Default.PhonelinkRing else Icons.Default.Email

    val otpValues = remember { mutableStateListOf("", "", "", "") }
    val focusRequesters = remember { List(4) { FocusRequester() } }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brand100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Brand600,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Check Your $contactLabel",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "We've sent a 4-digit code to",
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            color = Slate500
        )
        Text(
            contactInfo,
            fontWeight = FontWeight.SemiBold,
            fontSize = 15.sp,
            color = Slate800
        )
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            otpValues.forEachIndexed { index, value ->
                OtpDigitField(
                    value = value,
                    onValueChange = { newValue ->
                        if (newValue.length <= 1) {
                            otpValues[index] = newValue
                            if (newValue.isNotEmpty()) {
                                if (index < 3) {
                                    focusRequesters[index + 1].requestFocus()
                                } else {
                                    focusManager.clearFocus()
                                }
                            }
                        } else if (newValue.length == 2) {
                            // Handle case where user types quickly or pastes
                            val lastChar = newValue.last().toString()
                            otpValues[index] = lastChar
                            if (index < 3) {
                                focusRequesters[index + 1].requestFocus()
                            }
                        }
                    },
                    modifier = Modifier
                        .focusRequester(focusRequesters[index])
                        .onKeyEvent { keyEvent ->
                            if (keyEvent.type == KeyEventType.KeyDown && 
                                keyEvent.key == Key.Backspace && 
                                otpValues[index].isEmpty() && 
                                index > 0) {
                                focusRequesters[index - 1].requestFocus()
                                true
                            } else {
                                false
                            }
                        }
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Resend code in 00:59",
            fontSize = 14.sp,
            color = Slate500
        )
    }
}

@Composable
fun OtpDigitField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.size(64.dp),
        shape = RoundedCornerShape(16.dp),
        textStyle = androidx.compose.ui.text.TextStyle(
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = Color.White,
            focusedContainerColor = Color.White,
            unfocusedBorderColor = Slate200,
            focusedBorderColor = Brand500
        )
    )
}

@Composable
fun NewPasswordContent(
    onPasswordValid: (Boolean) -> Unit
) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasNumber = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }

    val isValid = hasMinLength && hasUppercase && hasNumber && hasSpecialChar && 
                  password == confirmPassword && password.isNotEmpty()

    LaunchedEffect(isValid) {
        onPasswordValid(isValid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Brand100),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Key,
                contentDescription = null,
                tint = Brand600,
                modifier = Modifier.size(32.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            "Create New Password",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Your new password must be different from previous used passwords.",
            textAlign = TextAlign.Center,
            fontSize = 15.sp,
            color = Slate500,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))

        Column(modifier = Modifier.fillMaxWidth()) {
            Text("New Password", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Slate700)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("••••••••", color = Slate400) },
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = Slate400) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null, tint = Slate400)
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Slate200,
                    focusedBorderColor = Brand500
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Password Requirements Grid
            PasswordStrengthSection(
                hasMinLength = hasMinLength,
                hasUppercase = hasUppercase,
                hasNumber = hasNumber,
                hasSpecialChar = hasSpecialChar
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text("Confirm Password", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Slate700)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("••••••••", color = Slate400) },
                leadingIcon = { Icon(Icons.Default.LockReset, null, tint = Slate400) },
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(16.dp),
                isError = confirmPassword.isNotEmpty() && password != confirmPassword,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White,
                    unfocusedBorderColor = Slate200,
                    focusedBorderColor = Brand500,
                    errorBorderColor = Color.Red
                )
            )
            if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                Text(
                    "Passwords do not match",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                )
            }
        }
    }
}

@Composable
fun SuccessContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(Brand500, RoundedCornerShape(32.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            "Password Reset!",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Your password has been successfully reset. Click below to log in magically.",
            textAlign = TextAlign.Center,
            fontSize = 16.sp,
            color = Slate500,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PasswordRecoveryPreview() {
    NurseWearConnectTheme {
        PasswordRecoveryScreen({}, {})
    }
}
