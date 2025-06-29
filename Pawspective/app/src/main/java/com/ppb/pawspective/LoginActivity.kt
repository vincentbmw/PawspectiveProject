package com.ppb.pawspective

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

class LoginActivity : ComponentActivity() {
    private lateinit var authManager: AuthManager
    private lateinit var themeManager: ThemeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        authManager = AuthManager(this)
        themeManager = ThemeManager.getInstance(this)
        
        setContent {
            PawspectiveTheme(darkTheme = themeManager.isDarkMode) {
                LoginScreen(
                    onLoginClick = { email, password, rememberMe, onLoadingChange ->
                        lifecycleScope.launch {
                            onLoadingChange(true) // Set loading to true
                            when (val result = authManager.signInWithEmailAndPassword(email, password, rememberMe)) {
                                is AuthManager.AuthResult.Success -> {
                                    onLoadingChange(false) // Reset loading
                                    Toast.makeText(this@LoginActivity, "Login successful!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                    finish()
                                }
                                is AuthManager.AuthResult.Error -> {
                                    onLoadingChange(false) // Reset loading on error
                                    Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_LONG).show()
                                }
                                is AuthManager.AuthResult.Loading -> {
                                    // Loading state is handled by onLoadingChange
                                }
                            }
                        }
                    },
                    onGoogleSignInClick = { rememberMe, onLoadingChange ->
                        lifecycleScope.launch {
                            onLoadingChange(true) // Set loading to true
                            when (val result = authManager.signInWithGoogle(rememberMe)) {
                                is AuthManager.AuthResult.Success -> {
                                    onLoadingChange(false) // Reset loading
                                    Toast.makeText(this@LoginActivity, "Google sign in successful!", Toast.LENGTH_SHORT).show()
                                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                                    finish()
                                }
                                is AuthManager.AuthResult.Error -> {
                                    onLoadingChange(false) // Reset loading on error
                                    Toast.makeText(this@LoginActivity, result.message, Toast.LENGTH_LONG).show()
                                }
                                is AuthManager.AuthResult.Loading -> {
                                    // Loading state is handled by onLoadingChange
                                }
                            }
                        }
                    },
                    onForgotPasswordClick = { email ->
                        startActivity(Intent(this@LoginActivity, ForgotPasswordActivity::class.java))
                    },
                    onSignUpClick = {
                        startActivity(Intent(this, RegisterActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginClick: (String, String, Boolean, (Boolean) -> Unit) -> Unit,
    onGoogleSignInClick: (Boolean, (Boolean) -> Unit) -> Unit,
    onForgotPasswordClick: (String) -> Unit,
    onSignUpClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(true) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Validation states
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    
    // Real-time validation for email
    LaunchedEffect(email) {
        if (email.isNotEmpty()) {
            val result = ValidationUtils.validateEmail(email)
            emailError = if (result.isValid) null else result.errorMessage
        } else {
            emailError = null
        }
    }
    
    // Basic password validation for login (less strict than registration)
    LaunchedEffect(password) {
        if (password.isNotEmpty()) {
            passwordError = if (password.length < 6) "Password is too short" else null
        } else {
            passwordError = null
        }
    }
    
    // Use Material3 color scheme
    val colorScheme = MaterialTheme.colorScheme
    val errorColor = Color(0xFFD32F2F)
    
    // Check if form is valid (basic validation for login)
    val isFormValid = email.isNotEmpty() && password.isNotEmpty() && 
            emailError == null && passwordError == null
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))
        
        // Logo
        Image(
            painter = painterResource(id = R.drawable.paws_only),
            contentDescription = "App Logo",
            modifier = Modifier.size(80.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Title
        Text(
            text = "Welcome Back!",
            style = TextStyle(
                fontFamily = QuicksandRegular,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            ),
            color = colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Sign in to continue",
            style = TextStyle(
                fontFamily = QuandoRegular,
                fontSize = 16.sp
            ),
            color = colorScheme.secondary
        )
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { 
                Text(
                    "Email",
                    fontFamily = QuandoRegular,
                    fontSize = 16.sp
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (emailError != null) errorColor else colorScheme.primary,
                unfocusedBorderColor = if (emailError != null) errorColor else colorScheme.outline,
                focusedLabelColor = if (emailError != null) errorColor else colorScheme.primary,
                unfocusedLabelColor = colorScheme.secondary,
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
            isError = emailError != null
        )
        
        // Email Error Message
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
                    .padding(start = 16.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { 
                Text(
                    "Password",
                    fontFamily = QuandoRegular,
                    fontSize = 16.sp
                )
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (isPasswordVisible) R.drawable.visibility_off else R.drawable.visibility
                        ),
                        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
                        tint = colorScheme.secondary
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (passwordError != null) errorColor else colorScheme.primary,
                unfocusedBorderColor = if (passwordError != null) errorColor else colorScheme.outline,
                focusedLabelColor = if (passwordError != null) errorColor else colorScheme.primary,
                unfocusedLabelColor = colorScheme.secondary,
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
            isError = passwordError != null
        )
        
        // Password Error Message
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
                    .padding(start = 16.dp, top = 4.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Remember Me Checkbox
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { rememberMe = !rememberMe }
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colorScheme.primary,
                        uncheckedColor = colorScheme.outline,
                        checkmarkColor = colorScheme.onPrimary
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Remember me",
                    style = TextStyle(
                        fontFamily = QuandoRegular,
                        fontSize = 14.sp
                    ),
                    color = colorScheme.onBackground
                )
            }
            
            // Forgot Password
            Text(
                text = "Forgot Password?",
                style = TextStyle(
                    fontFamily = QuandoRegular,
                    fontSize = 14.sp
                ),
                color = colorScheme.primary,
                modifier = Modifier.clickable {
                    onForgotPasswordClick(email)
                }
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Login Button
        Button(
            onClick = {
                if (isFormValid && !isLoading) {
                    onLoginClick(email, password, rememberMe) { isLoading = it }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary,
                disabledContainerColor = colorScheme.outline
            ),
            enabled = isFormValid && !isLoading,
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text(
                    text = "Sign In",
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
        
        // Divider with "OR"
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
                text = " OR ",
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
        
        // Google Sign In Button
        OutlinedButton(
            onClick = {
                if (!isLoading) {
                    onGoogleSignInClick(rememberMe) { isLoading = it }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            border = BorderStroke(1.dp, colorScheme.outline),
            shape = RoundedCornerShape(8.dp),
            enabled = !isLoading
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.google_icon),
                    contentDescription = "Google Icon",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Continue with Google",
                    style = TextStyle(
                        fontFamily = QuandoRegular,
                        fontSize = 16.sp
                    ),
                    color = colorScheme.onBackground
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Sign Up Link
        val annotatedString = buildAnnotatedString {
            withStyle(style = SpanStyle(color = colorScheme.secondary)) {
                append("Don't have an account? ")
            }
            pushStringAnnotation(tag = "SIGN_UP", annotation = "sign_up")
            withStyle(style = SpanStyle(color = colorScheme.primary, fontWeight = FontWeight.Medium)) {
                append("Sign Up")
            }
            pop()
        }
        
        ClickableText(
            text = annotatedString,
            style = TextStyle(
                fontFamily = QuandoRegular,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            ),
            onClick = { offset ->
                annotatedString.getStringAnnotations(tag = "SIGN_UP", start = offset, end = offset)
                    .firstOrNull()?.let {
                        onSignUpClick()
                    }
            }
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, name = "Login Screen Light")
@Composable
fun LoginScreenLightPreview() {
    PawspectiveTheme(darkTheme = false) {
        LoginScreen(
            onLoginClick = { _, _, _, _ -> },
            onGoogleSignInClick = { _, _ -> },
            onForgotPasswordClick = { },
            onSignUpClick = { }
        )
    }
}

@Preview(showBackground = true, name = "Login Screen Dark")
@Composable
fun LoginScreenDarkPreview() {
    PawspectiveTheme(darkTheme = true) {
        LoginScreen(
            onLoginClick = { _, _, _, _ -> },
            onGoogleSignInClick = { _, _ -> },
            onForgotPasswordClick = { },
            onSignUpClick = { }
        )
    }
} 