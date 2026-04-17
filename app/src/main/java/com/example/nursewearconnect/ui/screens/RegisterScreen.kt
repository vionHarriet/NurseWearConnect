package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("nurse") }
    var agreeToTerms by remember { mutableStateOf(false) }
    var receiveUpdates by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val focusManager = LocalFocusManager.current

    val isFormValid = fullName.isNotBlank() && 
                     email.isNotBlank() && 
                     password.length >= 8 && 
                     password == confirmPassword && 
                     agreeToTerms

    val handleRegister = {
        if (password != confirmPassword) {
            errorMessage = "Passwords do not match"
        } else if (password.length < 8) {
            errorMessage = "Password must be at least 8 characters"
        } else {
            errorMessage = null
            onRegisterSuccess()
        }
    }

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.White, CircleShape)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
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
                        "Create Account",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = Brand600,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Join NurseWear Connect",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )

                Text(
                    "Create your account to start customizing your perfect scrubs.",
                    fontSize = 15.sp,
                    color = Slate500,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Role Selection
                Text(
                    "Select your role",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate700,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RoleItem(
                        modifier = Modifier.weight(1f),
                        title = "Nurse",
                        icon = Icons.Default.Person,
                        isSelected = selectedRole == "nurse",
                        onClick = { selectedRole = "nurse" },
                        accentColor = Brand600,
                        bgColor = Brand100
                    )
                    RoleItem(
                        modifier = Modifier.weight(1f),
                        title = "Clinician",
                        icon = Icons.Default.MedicalServices,
                        isSelected = selectedRole == "clinician",
                        onClick = { selectedRole = "clinician" },
                        accentColor = Color(0xFF3B82F6),
                        bgColor = Color(0xFFDBEAFE)
                    )
                    RoleItem(
                        modifier = Modifier.weight(1f),
                        title = "Student",
                        icon = Icons.Default.MenuBook,
                        isSelected = selectedRole == "student",
                        onClick = { selectedRole = "student" },
                        accentColor = Slate500,
                        bgColor = Slate100
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Form
                Text(
                    "Full Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate700,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Jane Doe", color = Slate400) },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null, tint = Slate400)
                    },
                    trailingIcon = {
                        if (fullName.length > 2) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Brand500)
                        }
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Slate50,
                        focusedContainerColor = Slate50,
                        unfocusedBorderColor = Slate200,
                        focusedBorderColor = Brand500
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Email or Phone",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate700,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it.trim()
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("jane@hospital.com", color = Slate400) },
                    leadingIcon = {
                        Icon(Icons.Default.Email, contentDescription = null, tint = Slate400)
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Slate50,
                        focusedContainerColor = Slate50,
                        unfocusedBorderColor = Slate200,
                        focusedBorderColor = Brand500
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate700,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        password = it.trim()
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("••••••••", color = Slate400) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Slate400)
                    },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = Slate400)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Slate50,
                        focusedContainerColor = Slate50,
                        unfocusedBorderColor = Slate200,
                        focusedBorderColor = Brand500
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    "Confirm Password",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate700,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { 
                        confirmPassword = it.trim()
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("••••••••", color = Slate400) },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Slate400)
                    },
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = null, tint = Slate400)
                        }
                    },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Slate50,
                        focusedContainerColor = Slate50,
                        unfocusedBorderColor = Slate200,
                        focusedBorderColor = Brand500
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { 
                        focusManager.clearFocus()
                        handleRegister()
                    })
                )

                // Password Strength
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val strength = if (password.length >= 8) 2 else if (password.isNotEmpty()) 1 else 0
                        Box(modifier = Modifier.weight(1f).height(4.dp).background(if (strength >= 1) Brand500 else Slate200, CircleShape))
                        Box(modifier = Modifier.weight(1f).height(4.dp).background(if (strength >= 2) Brand500 else Slate200, CircleShape))
                        Box(modifier = Modifier.weight(1f).height(4.dp).background(Slate200, CircleShape))
                        Box(modifier = Modifier.weight(1f).height(4.dp).background(Slate200, CircleShape))
                    }
                    Text(
                        if (password.length >= 8) "Good password" else if (password.isNotEmpty()) "Weak password" else "Enter password",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (password.length >= 8) Brand600 else Slate500,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PasswordRule(text = "Minimum 8 characters", isMet = password.length >= 8)
                    PasswordRule(text = "Passwords must match", isMet = password.isNotEmpty() && password == confirmPassword)
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 12.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Measurement Callout
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Brand50,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Brand100)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = Color.White,
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Straighten, contentDescription = null, tint = Brand600, modifier = Modifier.size(20.dp))
                            }
                        }
                        Column {
                            Text("Know your measurements?", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Brand900)
                            Text(
                                "Add them later to get perfectly tailored scrubs on your first order.",
                                fontSize = 12.sp,
                                color = Brand700,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Checkboxes
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { agreeToTerms = !agreeToTerms },
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Checkbox(
                        checked = agreeToTerms,
                        onCheckedChange = { agreeToTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = Brand600),
                        modifier = Modifier.size(24.dp).padding(top = 2.dp)
                    )
                    Text(
                        text = "I agree to the Terms of Service and Privacy Policy.",
                        fontSize = 13.sp,
                        color = Slate600,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth().clickable { receiveUpdates = !receiveUpdates },
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Checkbox(
                        checked = receiveUpdates,
                        onCheckedChange = { receiveUpdates = it },
                        colors = CheckboxDefaults.colors(checkedColor = Brand600),
                        modifier = Modifier.size(24.dp).padding(top = 2.dp)
                    )
                    Text(
                        text = "Send me updates on new scrub styles and exclusive offers.",
                        fontSize = 13.sp,
                        color = Slate600,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(140.dp))
            }
        }

        // Footer
        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            color = Color.White, // Solid color
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = handleRegister,
                    enabled = isFormValid,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Brand600,
                        disabledContainerColor = Slate200
                    )
                ) {
                    Text("Create Account", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Already have an account?", fontSize = 13.sp, color = Slate500)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Log in",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Brand600,
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                }
            }
        }
    }
}

@Composable
fun RoleItem(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    bgColor: Color
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Brand50 else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = 2.dp,
            color = if (isSelected) Brand500 else Slate100
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(if (isSelected) Brand200 else bgColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isSelected) Brand600 else accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Brand700 else Slate600
            )
        }
    }
}

@Composable
fun PasswordRule(text: String, isMet: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        if (isMet) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Brand600, modifier = Modifier.size(14.dp))
        } else {
            Box(modifier = Modifier.size(4.dp).background(Slate300, CircleShape).offset(x = 5.dp))
            Spacer(modifier = Modifier.width(10.dp))
        }
        Text(text, fontSize = 11.sp, color = if (isMet) Brand600 else Slate500)
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    NurseWearConnectTheme {
        RegisterScreen({}, {}, {})
    }
}
