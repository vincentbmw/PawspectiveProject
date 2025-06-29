package com.ppb.pawspective

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
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

class ForgotPasswordActivity : ComponentActivity() {
    private lateinit var authManager: AuthManager
    private lateinit var themeManager: ThemeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        authManager = AuthManager(this)
        themeManager = ThemeManager.getInstance(this)
        
        setContent {
            PawspectiveTheme(darkTheme = themeManager.isDarkMode) {
                ForgotPasswordScreen(
                    onBackClick = {
                        finish()
                    },
                    onSendCodeClick = { email, onLoadingChange ->
                        lifecycleScope.launch {
                            onLoadingChange(true) // Set loading to true
                            when (val result = authManager.sendPasswordResetEmail(email)) {
                                is AuthManager.AuthResult.Success -> {
                                    onLoadingChange(false) // Reset loading
                                    // Navigate to Reset Email Sent screen
                                    val intent = Intent(this@ForgotPasswordActivity, ResetEmailSentActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                is AuthManager.AuthResult.Error -> {
                                    onLoadingChange(false) // Reset loading on error
                                    Toast.makeText(this@ForgotPasswordActivity, result.message, Toast.LENGTH_LONG).show()
                                }
                                is AuthManager.AuthResult.Loading -> {
                                    // Loading state is handled by onLoadingChange
                                }
                            }
                        }
                    },
                    onSignInClick = {
                        finish() // Go back to login page
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onBackClick: () -> Unit,
    onSendCodeClick: (String, (Boolean) -> Unit) -> Unit,
    onSignInClick: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var emailError by remember { mutableStateOf<String?>(null) }
    
    // Real-time email validation
    LaunchedEffect(email) {
        if (email.isNotEmpty()) {
            val result = ValidationUtils.validateEmail(email)
            emailError = if (result.isValid) null else result.errorMessage
        } else {
            emailError = null
        }
    }
    
    // Use Material3 color scheme
    val colorScheme = MaterialTheme.colorScheme
    val errorColor = Color(0xFFD32F2F)
    
    // Check if form is valid
    val isFormValid = email.isNotEmpty() && emailError == null
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Back Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = onBackClick,
                enabled = !isLoading
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back), // Using back.png from drawable
                    contentDescription = "Back",
                    tint = colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Title
        Text(
            text = "Forgot Password?",
            style = TextStyle(
                fontFamily = QuicksandRegular,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            ),
            color = colorScheme.onBackground,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Description
        Text(
            text = "Don't worry! It occurs. Please enter the email address linked with your account.",
            style = TextStyle(
                fontFamily = QuandoRegular,
                fontSize = 16.sp,
                lineHeight = 22.sp
            ),
            color = colorScheme.secondary,
            modifier = Modifier.padding(bottom = 40.dp)
        )
        
        // Email TextField
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
                    .padding(start = 16.dp, bottom = 16.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(20.dp))
        }
        
        // Send Code Button
        Button(
            onClick = { 
                if (isFormValid && !isLoading) {
                    onSendCodeClick(email) { isLoading = it }
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
                    text = "Send Code",
                    style = TextStyle(
                        fontFamily = QuandoRegular,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = colorScheme.onPrimary
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Sign In Link
        val annotatedText = buildAnnotatedString {
            append("Remember your password? ")
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        )
    }
}

@Preview(showBackground = true, name = "Forgot Password Screen Light")
@Composable
fun ForgotPasswordScreenLightPreview() {
    PawspectiveTheme(darkTheme = false) {
        ForgotPasswordScreen(
            onBackClick = { },
            onSendCodeClick = { _, _ -> },
            onSignInClick = { }
        )
    }
}

@Preview(showBackground = true, name = "Forgot Password Screen Dark")
@Composable
fun ForgotPasswordScreenDarkPreview() {
    PawspectiveTheme(darkTheme = true) {
        ForgotPasswordScreen(
            onBackClick = { },
            onSendCodeClick = { _, _ -> },
            onSignInClick = { }
        )
    }
} 