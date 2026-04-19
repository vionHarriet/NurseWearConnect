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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.text.ClickableText
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.ui.theme.*
import com.example.nursewearconnect.ui.components.PasswordStrengthSection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onBack: () -> Unit,
    onRegisterSuccess: (String, String, String, String, String, String?, String?, String?) -> Unit,
    onNavigateToLogin: () -> Unit,
    isLoading: Boolean = false,
    externalError: String? = null,
    isExternalSuccess: Boolean = false
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var businessName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var businessDescription by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("student") }
    var agreeToTerms by remember { mutableStateOf(false) }
    var receiveUpdates by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showTerms by remember { mutableStateOf(false) }
    var showPrivacy by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current

    // Password validation logic
    val hasMinLength = password.length >= 8
    val hasUppercase = password.any { it.isUpperCase() }
    val hasNumber = password.any { it.isDigit() }
    val hasSpecialChar = password.any { !it.isLetterOrDigit() }
    val isPasswordValid = hasMinLength && hasUppercase && hasNumber && hasSpecialChar

    val isFormValid = fullName.isNotBlank() && 
                     email.contains("@") && 
                     phoneNumber.isNotBlank() &&
                     isPasswordValid && 
                     password == confirmPassword && 
                     agreeToTerms &&
                     (if (selectedRole == "vendor") businessName.isNotBlank() && location.isNotBlank() else true)

    val handleRegister = {
        if (!isPasswordValid) {
            errorMessage = "Please meet all password requirements"
        } else if (password != confirmPassword) {
            errorMessage = "Passwords do not match"
        } else {
            errorMessage = null
            onRegisterSuccess(
                selectedRole,
                fullName,
                email,
                phoneNumber,
                password,
                if (selectedRole == "vendor") businessName else null,
                if (selectedRole == "vendor") location else null,
                if (selectedRole == "vendor") businessDescription else null
            )
        }
    }

    val displayError = externalError ?: errorMessage

    if (showTerms) {
        TermsOfServiceScreen(onBack = { showTerms = false })
    } else if (showPrivacy) {
        PrivacyPolicyScreen(onBack = { showPrivacy = false })
    } else if (isExternalSuccess && selectedRole == "vendor") {
        VendorAwaitingApproval(onBackToLogin = onNavigateToLogin)
    } else {
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

                // Display Error if exists
                displayError?.let {
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Error, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                            Text(it, color = Color.Red, fontSize = 14.sp)
                        }
                    }
                }

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
                        title = "Student (Nurse)",
                        icon = Icons.Default.School,
                        isSelected = selectedRole == "student",
                        onClick = { selectedRole = "student" },
                        accentColor = Brand600,
                        bgColor = Brand100
                    )
                    RoleItem(
                        modifier = Modifier.weight(1f),
                        title = "Vendor (Tailor)",
                        icon = Icons.Default.Store,
                        isSelected = selectedRole == "vendor",
                        onClick = { selectedRole = "vendor" },
                        accentColor = Color(0xFF3B82F6),
                        bgColor = Color(0xFFDBEAFE)
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
                    "Email Address",
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
                    placeholder = { Text("jane@example.com", color = Slate400) },
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
                    "Phone Number",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Slate700,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("+254 700 000 000", color = Slate400) },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Slate400)
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
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                if (selectedRole == "vendor") {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "Business Name",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate700,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = businessName,
                        onValueChange = { businessName = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Baraton Tailors", color = Slate400) },
                        leadingIcon = {
                            Icon(Icons.Default.Store, contentDescription = null, tint = Slate400)
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
                        "Location",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate700,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Baraton Center, Nandi", color = Slate400) },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate400)
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
                        "Business Description",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Slate700,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = businessDescription,
                        onValueChange = { businessDescription = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Tell us about your services...", color = Slate400) },
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = Slate50,
                            focusedContainerColor = Slate50,
                            unfocusedBorderColor = Slate200,
                            focusedBorderColor = Brand500
                        ),
                        minLines = 3,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                }

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

                // Advanced Password Strength
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    PasswordStrengthSection(
                        hasMinLength = hasMinLength,
                        hasUppercase = hasUppercase,
                        hasNumber = hasNumber,
                        hasSpecialChar = hasSpecialChar
                    )
                    
                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text(
                            "Passwords do not match",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                if (displayError != null) {
                    // Handled above in the Scrollable Column for better visibility
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
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Checkbox(
                        checked = agreeToTerms,
                        onCheckedChange = { agreeToTerms = it },
                        colors = CheckboxDefaults.colors(checkedColor = Brand600),
                        modifier = Modifier.size(24.dp).padding(top = 2.dp)
                    )
                    
                    val annotatedString = buildAnnotatedString {
                        append("I agree to the ")
                        pushStringAnnotation(tag = "terms", annotation = "terms")
                        withStyle(style = SpanStyle(color = Brand600, fontWeight = FontWeight.Bold)) {
                            append("Terms of Service")
                        }
                        pop()
                        append(" and ")
                        pushStringAnnotation(tag = "privacy", annotation = "privacy")
                        withStyle(style = SpanStyle(color = Brand600, fontWeight = FontWeight.Bold)) {
                            append("Privacy Policy")
                        }
                        pop()
                        append(".")
                    }

                    ClickableText(
                        text = annotatedString,
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = Slate600,
                            lineHeight = 18.sp
                        ),
                        onClick = { offset ->
                            annotatedString.getStringAnnotations(tag = "terms", start = offset, end = offset)
                                .firstOrNull()?.let { showTerms = true }
                            annotatedString.getStringAnnotations(tag = "privacy", start = offset, end = offset)
                                .firstOrNull()?.let { showPrivacy = true }
                        }
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

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Footer
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .navigationBarsPadding()
                ) {
                    Button(
                        onClick = handleRegister,
                        enabled = isFormValid && !isLoading,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Brand600,
                            disabledContainerColor = Slate200
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Create Account", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }
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
fun VendorAwaitingApproval(onBackToLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(Brand100, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.HourglassEmpty,
                contentDescription = null,
                tint = Brand600,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            "Registration Received!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Slate900,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            "Your vendor account is currently pending verification. " +
            "Our admin team will review your business details and approve your account within 24-48 hours.",
            fontSize = 16.sp,
            color = Slate600,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onBackToLogin,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Brand600)
        ) {
            Text("Back to Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun LegalScreen(
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate50)
    ) {
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
                
                Text(
                    title,
                    modifier = Modifier.padding(start = 16.dp),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                content()
            }
        }
    }
}

@Composable
fun TermsOfServiceScreen(onBack: () -> Unit) {
    val date = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())
    LegalScreen(
        title = "Terms of Service",
        onBack = onBack
    ) {
        Text("NurseWear Connect (NWC)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Brand600)
        Text("Effective Date: $date", fontSize = 14.sp, color = Slate500, modifier = Modifier.padding(vertical = 8.dp))
        
        LegalSection("1. Introduction", "Welcome to NurseWear Connect (NWC), a digital platform designed to connect student nurses with verified uniform vendors for the purpose of browsing, customizing, ordering, and purchasing nursing uniforms. By accessing or using this platform, you agree to comply with and be bound by these Terms of Service. If you do not agree, you must not use the system.")
        LegalSection("2. Definitions", "• Platform refers to NurseWear Connect (mobile and web applications).\n• User refers to any individual accessing the system.\n• Student refers to a registered nursing student using the platform.\n• Vendor refers to a registered tailor or uniform seller.\n• Admin refers to authorized system administrators managing the platform.")
        LegalSection("3. Eligibility", "Users must:\n• Be at least 18 years old or have appropriate institutional authorization\n• Provide accurate and complete registration information\n• Use the platform only for lawful purposes")
        LegalSection("4. User Accounts", "• Users are responsible for maintaining the confidentiality of their login credentials\n• Users must provide accurate and up-to-date information\n• The system reserves the right to suspend or terminate accounts that violate these terms")
        LegalSection("5. Vendor Verification", "• Vendors must undergo an approval process before accessing full system features\n• The platform reserves the right to approve, reject, or suspend vendors at its discretion\n• Vendors are responsible for the accuracy of their product listings and services")
        LegalSection("6. Orders and Transactions", "• All orders placed through the platform are binding once confirmed\n• Prices and product descriptions are provided by vendors and may vary\n• The platform facilitates transactions but does not manufacture or physically supply uniforms")
        LegalSection("7. Payments", "• Payments must be made through approved payment methods integrated into the platform\n• Users agree to provide accurate payment information\n• The platform is not responsible for payment failures caused by third-party providers")
        LegalSection("8. Reviews and Ratings", "• Users may leave reviews based on genuine experiences\n• False, misleading, or abusive reviews are prohibited\n• The platform reserves the right to remove inappropriate content")
        LegalSection("9. User Conduct", "Users agree NOT to:\n• Provide false or misleading information\n• Engage in fraudulent activities\n• Abuse, harass, or threaten other users\n• Attempt to compromise system security")
        LegalSection("10. Intellectual Property", "All content on the platform, including logos, system design, and software, is the property of NurseWear Connect or its licensors and is protected by applicable intellectual property laws.")
        LegalSection("11. Limitation of Liability", "• The platform acts as an intermediary between students and vendors\n• NurseWear Connect is not liable for:\n  o Product quality issues\n  o Delivery delays\n  o Disputes between users and vendors\n• Users engage with vendors at their own risk")
        LegalSection("12. Termination", "The platform reserves the right to:\n• Suspend or terminate user accounts for violations\n• Remove content that breaches these terms\n• Restrict access without prior notice if necessary")
        LegalSection("13. Changes to Terms", "These Terms of Service may be updated at any time. Continued use of the platform constitutes acceptance of the updated terms.")
        LegalSection("14. Governing Law", "These terms shall be governed by the laws of Kenya.")
        LegalSection("15. Contact Information", "For inquiries regarding these Terms:\n• Email: nwc-support@baraton.ac.ke\n• Institution: University of Eastern Africa, Baraton")
    }
}

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val date = java.text.SimpleDateFormat("MMMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date())
    LegalScreen(
        title = "Privacy Policy",
        onBack = onBack
    ) {
        Text("NurseWear Connect (NWC)", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Brand600)
        Text("Effective Date: $date", fontSize = 14.sp, color = Slate500, modifier = Modifier.padding(vertical = 8.dp))

        LegalSection("1. Introduction", "NurseWear Connect (NWC) is committed to protecting the privacy and personal data of its users. This Privacy Policy explains how we collect, use, and safeguard your information.")
        LegalSection("2. Information We Collect", "We may collect the following data:\na) Personal Information: Full name, Email address, Phone number, Password (encrypted)\nb) Vendor Information: Business name, Location, Service descriptions\nc) Transaction Data: Orders and purchase history, Payment confirmations\nd) Communication Data: Chat messages between users and vendors")
        LegalSection("3. How We Use Information", "We use collected data to:\n• Create and manage user accounts\n• Process orders and transactions\n• Facilitate communication between users\n• Improve system functionality and user experience\n• Ensure system security and prevent fraud")
        LegalSection("4. Data Sharing", "We do NOT sell user data. However, data may be shared with:\n• Vendors (for order fulfillment)\n• Payment service providers (for transaction processing)\n• System administrators (for monitoring and support)")
        LegalSection("5. Data Security", "We implement security measures including:\n• Password encryption\n• Secure authentication (JWT)\n• SSL encryption for data transmission\nDespite these measures, no system is completely secure, and users share information at their own risk.")
        LegalSection("6. User Rights", "Users have the right to:\n• Access their personal data\n• Update or correct their information\n• Request deletion of their account\n• Withdraw consent where applicable")
        LegalSection("7. Data Retention", "• User data is retained as long as the account is active\n• Transaction records may be retained for legal and auditing purposes")
        LegalSection("8. Cookies and Tracking", "The platform may use cookies or similar technologies to:\n• Enhance user experience\n• Maintain login sessions\n• Analyze system usage")
        LegalSection("9. Third-Party Services", "The platform may integrate third-party services (e.g., payment gateways). These services have their own privacy policies, and we are not responsible for their practices.")
        LegalSection("10. Children’s Privacy", "The platform is not intended for users under the age of 18 without supervision or institutional authorization.")
        LegalSection("11. Changes to Privacy Policy", "We may update this Privacy Policy periodically. Users will be notified of significant changes where necessary.")
        LegalSection("12. Contact Information", "For privacy-related inquiries:\n• Email: sainska@proton.me\n• Institution: University of Eastern Africa, Baraton")
    }
}

@Composable
fun LegalSection(title: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Slate900)
        Spacer(modifier = Modifier.height(4.dp))
        Text(content, fontSize = 14.sp, color = Slate600, lineHeight = 20.sp)
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    NurseWearConnectTheme {
        RegisterScreen({}, { _, _, _, _, _, _, _, _ -> }, {})
    }
}
