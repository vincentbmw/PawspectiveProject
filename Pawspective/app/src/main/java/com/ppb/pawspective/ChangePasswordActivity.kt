package com.ppb.pawspective

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.ppb.pawspective.ui.theme.PawspectiveTheme
import com.ppb.pawspective.ui.theme.QuandoRegular
import com.ppb.pawspective.ui.theme.QuicksandRegular
import com.ppb.pawspective.utils.ThemeManager
import kotlinx.coroutines.launch

class ChangePasswordActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var themeManager: ThemeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        themeManager = ThemeManager.getInstance(this)
        
        // Check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please log in first", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setContent {
            PawspectiveTheme(darkTheme = themeManager.isDarkMode) {
                ChangePasswordScreen(
                    currentUser = currentUser,
                    onBackClick = { finish() },
                    onPasswordChanged = {
                        Toast.makeText(this@ChangePasswordActivity, "Password changed successfully!", Toast.LENGTH_SHORT).show()
                        finish()
                    },
                    onError = { error ->
                        Toast.makeText(this@ChangePasswordActivity, error, Toast.LENGTH_LONG).show()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    currentUser: com.google.firebase.auth.FirebaseUser,
    onBackClick: () -> Unit,
    onPasswordChanged: () -> Unit,
    onError: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val coroutineScope = rememberCoroutineScope()
    
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isOldPasswordVisible by remember { mutableStateOf(false) }
    var isNewPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    
    // Real-time password matching validation
    LaunchedEffect(newPassword, confirmPassword) {
        if (confirmPassword.isNotEmpty()) {
            passwordError = if (newPassword == confirmPassword) {
                if (newPassword.length < 6) {
                    "Password must be at least 6 characters"
                } else null
            } else {
                "Passwords do not match"
            }
        } else {
            passwordError = null
        }
    }
    
    fun changePassword() {
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            onError("Please fill in all fields")
            return
        }
        
        if (passwordError != null) {
            onError(passwordError!!)
            return
        }
        
        isLoading = true
        
        coroutineScope.launch {
            try {
                // Re-authenticate user with old password
                val email = currentUser.email ?: ""
                val credential = EmailAuthProvider.getCredential(email, oldPassword)
                
                currentUser.reauthenticate(credential)
                    .addOnSuccessListener {
                        // Re-authentication successful, now update password
                        currentUser.updatePassword(newPassword)
                            .addOnSuccessListener {
                                isLoading = false
                                onPasswordChanged()
                            }
                            .addOnFailureListener { exception ->
                                isLoading = false
                                when (exception) {
                                    is FirebaseAuthWeakPasswordException -> {
                                        onError("Password is too weak. Please choose a stronger password.")
                                    }
                                    else -> {
                                        onError("Failed to update password: ${exception.message}")
                                    }
                                }
                            }
                    }
                    .addOnFailureListener { exception ->
                        isLoading = false
                        when (exception) {
                            is FirebaseAuthInvalidCredentialsException -> {
                                onError("Current password is incorrect")
                            }
                            else -> {
                                onError("Authentication failed: ${exception.message}")
                            }
                        }
                    }
            } catch (e: Exception) {
                isLoading = false
                onError("An error occurred: ${e.message}")
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Back arrow and title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Back",
                        tint = colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Change Password",
                    style = TextStyle(
                        fontFamily = QuicksandRegular,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.onBackground
                )
                Spacer(modifier = Modifier.width(48.dp)) // Balance row
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            // Old Password field
            Text(
                text = "Current Password",
                style = TextStyle(
                    fontFamily = QuicksandRegular,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = oldPassword,
                onValueChange = { oldPassword = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isOldPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isOldPasswordVisible = !isOldPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (isOldPasswordVisible) R.drawable.visibility else R.drawable.visibility_off
                            ),
                            contentDescription = if (isOldPasswordVisible) "Hide password" else "Show password",
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = TextStyle(
                    fontFamily = QuandoRegular, 
                    fontSize = 16.sp,
                    color = colorScheme.onSurface
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // New Password field
            Text(
                text = "New Password",
                style = TextStyle(
                    fontFamily = QuicksandRegular,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = newPassword,
                onValueChange = { newPassword = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isNewPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isNewPasswordVisible = !isNewPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (isNewPasswordVisible) R.drawable.visibility else R.drawable.visibility_off
                            ),
                            contentDescription = if (isNewPasswordVisible) "Hide password" else "Show password",
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = TextStyle(
                    fontFamily = QuandoRegular, 
                    fontSize = 16.sp,
                    color = colorScheme.onSurface
                ),
                enabled = !isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm New Password field
            Text(
                text = "Confirm New Password",
                style = TextStyle(
                    fontFamily = QuicksandRegular,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { isConfirmPasswordVisible = !isConfirmPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (isConfirmPasswordVisible) R.drawable.visibility else R.drawable.visibility_off
                            ),
                            contentDescription = if (isConfirmPasswordVisible) "Hide password" else "Show password",
                            tint = colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (passwordError != null) colorScheme.error else colorScheme.primary,
                    unfocusedBorderColor = if (passwordError != null) colorScheme.error else colorScheme.outline
                ),
                shape = RoundedCornerShape(8.dp),
                textStyle = TextStyle(
                    fontFamily = QuandoRegular, 
                    fontSize = 16.sp,
                    color = colorScheme.onSurface
                ),
                isError = passwordError != null,
                enabled = !isLoading
            )
            
            if (passwordError != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.warning_icon),
                        contentDescription = "Error",
                        tint = colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = passwordError!!,
                        color = colorScheme.error,
                        style = TextStyle(
                            fontFamily = QuandoRegular, 
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Save Changes button
            Button(
                onClick = { changePassword() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading && oldPassword.isNotEmpty() && newPassword.isNotEmpty() && 
                         confirmPassword.isNotEmpty() && passwordError == null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Change Password",
                        style = TextStyle(
                            fontFamily = QuandoRegular,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = colorScheme.onPrimary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}