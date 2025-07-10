package com.ppb.pawspective

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.ppb.pawspective.data.api.ApiClient
import com.ppb.pawspective.data.model.FeedbackRequest
import com.ppb.pawspective.ui.theme.PawspectiveTheme
import com.ppb.pawspective.ui.theme.QuandoRegular
import com.ppb.pawspective.ui.theme.QuicksandRegular
import com.ppb.pawspective.utils.ThemeManager
import kotlinx.coroutines.launch

class FeedbackActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var themeManager: ThemeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        themeManager = ThemeManager.getInstance(this)
        
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Please log in to send feedback", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        
        setContent {
            PawspectiveTheme(darkTheme = themeManager.isDarkMode) {
                FeedbackScreen(
                    userId = currentUser.uid,
                    userEmail = currentUser.email ?: "Unknown",
                    onBackClick = { finish() },
                    onSubmitFeedback = { feedback ->
                        submitFeedback(currentUser.uid, feedback)
                    }
                )
            }
        }
    }
    
    private fun submitFeedback(userId: String, feedback: String) {
        lifecycleScope.launch {
            try {
                val response = ApiClient.apiService.submitFeedback(
                    userId = userId,
                    request = FeedbackRequest(feedback = feedback)
                )
                
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.success == true) {
                        Toast.makeText(
                            this@FeedbackActivity, 
                            "Thank you! Your feedback has been sent successfully.", 
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(
                            this@FeedbackActivity, 
                            body?.message ?: "Failed to send feedback", 
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@FeedbackActivity, 
                        "Network error. Please check your connection and try again.", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@FeedbackActivity, 
                    "Error: ${e.message ?: "Something went wrong"}", 
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    userId: String,
    userEmail: String,
    onBackClick: () -> Unit,
    onSubmitFeedback: (String) -> Unit
) {
    var feedbackText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val colorScheme = MaterialTheme.colorScheme
    val scrollState = rememberScrollState()
    
    val maxCharacters = 2000
    val remainingCharacters = maxCharacters - feedbackText.length
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Header with back button and title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    tint = colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Feedback",
                    style = TextStyle(
                        fontFamily = QuicksandRegular,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.onBackground
                )
            }
            
            // Empty space to balance the back button
            Spacer(modifier = Modifier.width(48.dp))
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Introduction card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.feedback_icon),
                            contentDescription = "Feedback",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Share Your Thoughts",
                            style = TextStyle(
                                fontFamily = QuicksandRegular,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colorScheme.onSurface
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "We value your feedback! Help us improve Pawspective by sharing your thoughts, suggestions, or reporting any issues you've encountered.",
                        style = TextStyle(
                            fontFamily = QuandoRegular,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        color = colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Feedback input section
            Text(
                text = "Your Feedback",
                style = TextStyle(
                    fontFamily = QuicksandRegular,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            OutlinedTextField(
                value = feedbackText,
                onValueChange = { 
                    if (it.length <= maxCharacters) {
                        feedbackText = it
                    }
                },
                label = { 
                    Text(
                        "Tell us what you think...",
                        style = TextStyle(fontFamily = QuandoRegular)
                    ) 
                },
                placeholder = { 
                    Text(
                        "Share your experience, suggestions, or report issues here.",
                        style = TextStyle(fontFamily = QuandoRegular)
                    ) 
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                textStyle = TextStyle(
                    fontFamily = QuandoRegular,
                    fontSize = 14.sp
                ),
                maxLines = 10,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline,
                    focusedLabelColor = colorScheme.primary
                )
            )
            
            // Character counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "$remainingCharacters characters remaining",
                    style = TextStyle(
                        fontFamily = QuandoRegular,
                        fontSize = 12.sp
                    ),
                    color = if (remainingCharacters < 100) 
                        colorScheme.error 
                    else 
                        colorScheme.onBackground.copy(alpha = 0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Submit button
            Button(
                onClick = {
                    if (feedbackText.isNotBlank() && !isSubmitting) {
                        isSubmitting = true
                        onSubmitFeedback(feedbackText.trim())
                    }
                },
                enabled = feedbackText.isNotBlank() && !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    disabledContainerColor = colorScheme.primary.copy(alpha = 0.5f)
                )
            ) {
                if (isSubmitting) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = "Sending...",
                            style = TextStyle(
                                fontFamily = QuicksandRegular,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = colorScheme.onPrimary
                        )
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.send_icon),
                            contentDescription = "Send",
                            tint = colorScheme.onPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Send Feedback",
                            style = TextStyle(
                                fontFamily = QuicksandRegular,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = colorScheme.onPrimary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Privacy note
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "📧 Your feedback will be sent directly to our team. We respect your privacy and will only use this information to improve our app.",
                    style = TextStyle(
                        fontFamily = QuandoRegular,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    color = colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, name = "Feedback Screen Light")
@Composable
fun FeedbackScreenLightPreview() {
    PawspectiveTheme(darkTheme = false) {
        FeedbackScreen(
            userId = "preview_user",
            userEmail = "user@example.com",
            onBackClick = { },
            onSubmitFeedback = { }
        )
    }
}

@Preview(showBackground = true, name = "Feedback Screen Dark")
@Composable
fun FeedbackScreenDarkPreview() {
    PawspectiveTheme(darkTheme = true) {
        FeedbackScreen(
            userId = "preview_user",
            userEmail = "user@example.com",
            onBackClick = { },
            onSubmitFeedback = { }
        )
    }
} 