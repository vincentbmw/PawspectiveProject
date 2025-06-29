package com.ppb.pawspective

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.ppb.pawspective.auth.AuthManager
import com.ppb.pawspective.ui.theme.PawspectiveTheme
import com.ppb.pawspective.ui.theme.QuandoRegular
import com.ppb.pawspective.ui.theme.QuicksandRegular
import com.ppb.pawspective.utils.ThemeManager
import com.ppb.pawspective.utils.ValidationUtils
import kotlinx.coroutines.launch

class RegisterActivity : ComponentActivity() {
    private lateinit var authManager: AuthManager
    private lateinit var themeManager: ThemeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        authManager = AuthManager(this)
        themeManager = ThemeManager.getInstance(this)
        
        setContent {
            PawspectiveTheme(darkTheme = themeManager.isDarkMode) {
                RegisterScreen(
                    onRegisterClick = { email, password, onLoadingChange ->
                        lifecycleScope.launch {
                            onLoadingChange(true) // Set loading to true
                            when (val result = authManager.registerWithEmailAndPassword(email, password)) {
                                is AuthManager.AuthResult.Success -> {
                                    onLoadingChange(false) // Reset loading
                                    Toast.makeText(this@RegisterActivity, "Registration successful!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                                    finish()
                                }
                                is AuthManager.AuthResult.Error -> {
                                    onLoadingChange(false) // Reset loading on error
                                    Toast.makeText(this@RegisterActivity, result.message, Toast.LENGTH_LONG).show()
                                }
                                is AuthManager.AuthResult.Loading -> {
                                    // Loading state is handled by onLoadingChange
                                }
                            }
                        }
                    },
                    onGoogleSignInClick = { onLoadingChange ->
                        lifecycleScope.launch {
                            onLoadingChange(true) // Set loading to true
                            when (val result = authManager.signInWithGoogle()) {
                                is AuthManager.AuthResult.Success -> {
                                    onLoadingChange(false) // Reset loading
                                    Toast.makeText(this@RegisterActivity, "Google sign in successful!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@RegisterActivity, HomeActivity::class.java))
                                    finish()
                                }
                                is AuthManager.AuthResult.Error -> {
                                    onLoadingChange(false) // Reset loading on error
                                    Toast.makeText(this@RegisterActivity, result.message, Toast.LENGTH_LONG).show()
                                }
                                is AuthManager.AuthResult.Loading -> {
                                    // Loading state is handled by onLoadingChange
                                }
                            }
                        }
                    },
                    onSignInClick = {
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterClick: (String, String, (Boolean) -> Unit) -> Unit,
    onGoogleSignInClick: ((Boolean) -> Unit) -> Unit,
    onSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Validation states
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    
    // Real-time validation
    LaunchedEffect(email) {
        if (email.isNotEmpty()) {
            val result = ValidationUtils.validateEmail(email)
            emailError = if (result.isValid) null else result.errorMessage
        } else {
            emailError = null
        }
    }
    
    LaunchedEffect(password) {
        if (password.isNotEmpty()) {
            val result = ValidationUtils.validatePassword(password)
            passwordError = if (result.isValid) null else result.errorMessage
        } else {
            passwordError = null
        }
    }
    
    LaunchedEffect(confirmPassword, password) {
        if (confirmPassword.isNotEmpty()) {
            val result = ValidationUtils.validatePasswordConfirmation(password, confirmPassword)
            confirmPasswordError = if (result.isValid) null else result.errorMessage
        } else {
            confirmPasswordError = null
        }
    }
    
    // Use Material3 color scheme
    val colorScheme = MaterialTheme.colorScheme
    val errorColor = Color(0xFFD32F2F)
    
    // Check if form is valid
    val isFormValid = email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() &&
            emailError == null && passwordError == null && confirmPasswordError == null
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Title
        Text(
            text = buildAnnotatedString {
                append("Create an Account\nfor ")
                withStyle(style = SpanStyle(color = colorScheme.primary)) {
                    append("Pawspective")
                }
            },
            style = TextStyle(
                fontFamily = QuicksandRegular,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                lineHeight = 30.sp
            ),
            color = colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        // Email TextField with validation
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { 
                Text(
                    "Email",
                    fontFamily = QuandoRegular,
                    fontSize = 14.sp,
                    color = if (emailError != null) errorColor else colorScheme.secondary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (emailError != null) errorColor else colorScheme.primary,
                unfocusedBorderColor = if (emailError != null) errorColor else colorScheme.outline,
                focusedLabelColor = if (emailError != null) errorColor else colorScheme.primary,
                unfocusedLabelColor = if (emailError != null) errorColor else colorScheme.secondary,
                cursorColor = colorScheme.primary,
                focusedTextColor = colorScheme.onSurface,
                unfocusedTextColor = colorScheme.onSurface,
                disabledTextColor = colorScheme.onSurface
            ),
            textStyle = TextStyle(
                fontFamily = QuandoRegular,
                fontSize = 16.sp,
                color = colorScheme.onSurface
            ),
            singleLine = true,
            isError = emailError != null,
            enabled = !isLoading
        )
        
        // Email error message
        if (emailError != null) {
            Text(
                text = emailError!!,
                color = errorColor,
                style = TextStyle(
                    fontFamily = QuandoRegular,
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Password TextField with validation
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { 
                Text(
                    "Password",
                    fontFamily = QuandoRegular,
                    fontSize = 14.sp,
                    color = if (passwordError != null) errorColor else colorScheme.secondary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (passwordError != null) errorColor else colorScheme.primary,
                unfocusedBorderColor = if (passwordError != null) errorColor else colorScheme.outline,
                focusedLabelColor = if (passwordError != null) errorColor else colorScheme.primary,
                unfocusedLabelColor = if (passwordError != null) errorColor else colorScheme.secondary,
                cursorColor = colorScheme.primary,
                focusedTextColor = colorScheme.onSurface,
                unfocusedTextColor = colorScheme.onSurface,
                disabledTextColor = colorScheme.onSurface
            ),
            textStyle = TextStyle(
                fontFamily = QuandoRegular,
                fontSize = 16.sp,
                color = colorScheme.onSurface
            ),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (isPasswordVisible) android.R.drawable.ic_menu_view 
                            else android.R.drawable.ic_secure
                        ),
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                        tint = colorScheme.secondary
                    )
                }
            },
            singleLine = true,
            isError = passwordError != null,
            enabled = !isLoading
        )
        
        // Password strength indicator
        if (password.isNotEmpty()) {
            val strength = ValidationUtils.getPasswordStrength(password)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 4.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Strength: ",
                    style = TextStyle(
                        fontFamily = QuandoRegular,
                        fontSize = 12.sp
                    ),
                    color = colorScheme.secondary
                )
                Text(
                    text = strength.label,
                    style = TextStyle(
                        fontFamily = QuandoRegular,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = strength.color
                )
            }
        }
        
        // Password error message
        if (passwordError != null) {
            Text(
                text = passwordError!!,
                color = errorColor,
                style = TextStyle(
                    fontFamily = QuandoRegular,
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 8.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Confirm Password TextField
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { 
                Text(
                    "Confirm Password",
                    fontFamily = QuandoRegular,
                    fontSize = 14.sp,
                    color = if (confirmPasswordError != null) errorColor else colorScheme.secondary
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (confirmPasswordError != null) errorColor else colorScheme.primary,
                unfocusedBorderColor = if (confirmPasswordError != null) errorColor else colorScheme.outline,
                focusedLabelColor = if (confirmPasswordError != null) errorColor else colorScheme.primary,
                unfocusedLabelColor = if (confirmPasswordError != null) errorColor else colorScheme.secondary,
                cursorColor = colorScheme.primary,
                focusedTextColor = colorScheme.onSurface,
                unfocusedTextColor = colorScheme.onSurface,
                disabledTextColor = colorScheme.onSurface
            ),
            textStyle = TextStyle(
                fontFamily = QuandoRegular,
                fontSize = 16.sp,
                color = colorScheme.onSurface
            ),
            visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (isConfirmPasswordVisible) android.R.drawable.ic_menu_view 
                            else android.R.drawable.ic_secure
                        ),
                        contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password",
                        tint = colorScheme.secondary
                    )
                }
            },
            singleLine = true,
            isError = confirmPasswordError != null,
            enabled = !isLoading
        )
        
        // Confirm password error message
        if (confirmPasswordError != null) {
            Text(
                text = confirmPasswordError!!,
                color = errorColor,
                style = TextStyle(
                    fontFamily = QuandoRegular,
                    fontSize = 12.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 16.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        // Create Account Button
        Button(
            onClick = { 
                if (isFormValid && !isLoading) {
                    isLoading = true
                    onRegisterClick(email, password) { isLoading = it }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid && !isLoading) colorScheme.primary else colorScheme.outline,
                disabledContainerColor = colorScheme.outline
            ),
            enabled = isFormValid && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Create my Account!",
                    style = TextStyle(
                        fontFamily = QuandoRegular,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = colorScheme.onPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // OR Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = colorScheme.outline,
                thickness = 1.dp
            )
            Text(
                text = "OR",
                style = TextStyle(
                    fontFamily = QuandoRegular,
                    fontSize = 14.sp
                ),
                color = colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = colorScheme.outline,
                thickness = 1.dp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Google Sign-In Button
        Button(
            onClick = { 
                if (!isLoading) {
                    isLoading = true
                    onGoogleSignInClick { isLoading = it }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.surface,
                disabledContainerColor = colorScheme.outline
            ),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = colorScheme.primary,
                    strokeWidth = 2.dp
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.google_icon),
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Sign In Link
        val annotatedText = buildAnnotatedString {
            append("Already have an account? ")
            pushStringAnnotation(tag = "SIGN_IN", annotation = "sign_in")
            withStyle(style = SpanStyle(color = colorScheme.primary, fontWeight = FontWeight.Medium)) {
                append("Sign In")
            }
            pop()
        }
        
        ClickableText(
            text = annotatedText,
            style = TextStyle(
                fontFamily = QuandoRegular,
                fontSize = 14.sp,
                color = colorScheme.onBackground,
                textAlign = TextAlign.Center
            ),
            onClick = { offset ->
                if (!isLoading) {
                    annotatedText.getStringAnnotations(tag = "SIGN_IN", start = offset, end = offset)
                        .firstOrNull()?.let {
                            onSignInClick()
                        }
                }
            },
            modifier = Modifier.padding(bottom = 24.dp)
        )
    }
}

@Preview(showBackground = true, name = "Register Screen Light")
@Composable
fun RegisterScreenLightPreview() {
    PawspectiveTheme(darkTheme = false) {
        RegisterScreen(
            onRegisterClick = { _, _, _ -> },
            onGoogleSignInClick = { _ -> },
            onSignInClick = { }
        )
    }
}

@Preview(showBackground = true, name = "Register Screen Dark")
@Composable
fun RegisterScreenDarkPreview() {
    PawspectiveTheme(darkTheme = true) {
        RegisterScreen(
            onRegisterClick = { _, _, _ -> },
            onGoogleSignInClick = { _ -> },
            onSignInClick = { }
        )
    }
} 