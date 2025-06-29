package com.ppb.pawspective

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ppb.pawspective.ui.theme.PawspectiveTheme
import com.ppb.pawspective.ui.theme.QuandoRegular
import com.ppb.pawspective.ui.theme.QuicksandRegular
import com.ppb.pawspective.utils.ThemeManager

class ResetEmailSentActivity : ComponentActivity() {
    private lateinit var themeManager: ThemeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        themeManager = ThemeManager.getInstance(this)
        
        setContent {
            PawspectiveTheme(darkTheme = themeManager.isDarkMode) {
                ResetEmailSentScreen(
                    onBackToLoginClick = {
                        // Navigate back to login and clear the stack
                        val intent = Intent(this@ResetEmailSentActivity, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun ResetEmailSentScreen(
    onBackToLoginClick: () -> Unit
) {
    // Use Material3 color scheme
    val colorScheme = MaterialTheme.colorScheme
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Back Button (top left)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(
                onClick = onBackToLoginClick
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back),
                    contentDescription = "Back",
                    tint = colorScheme.onBackground,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Success Icon in Circle
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(colorScheme.surface),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.confirm_icon),
                contentDescription = "Success",
                modifier = Modifier.size(60.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Title
        Text(
            text = "Reset Email Sent!",
            style = TextStyle(
                fontFamily = QuicksandRegular,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            ),
            color = colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Description
        Text(
            text = "We've sent a password reset link to your email address. Please check your inbox and follow the instructions to reset your password.",
            style = TextStyle(
                fontFamily = QuandoRegular,
                fontSize = 16.sp,
                lineHeight = 22.sp
            ),
            color = colorScheme.secondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        )
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Back to Login Button
        Button(
            onClick = onBackToLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary
            )
        ) {
            Text(
                text = "Back to Login",
                style = TextStyle(
                    fontFamily = QuandoRegular,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = colorScheme.onPrimary
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true, name = "Reset Email Sent Screen Light")
@Composable
fun ResetEmailSentScreenLightPreview() {
    PawspectiveTheme(darkTheme = false) {
        ResetEmailSentScreen(
            onBackToLoginClick = { }
        )
    }
}

@Preview(showBackground = true, name = "Reset Email Sent Screen Dark")
@Composable
fun ResetEmailSentScreenDarkPreview() {
    PawspectiveTheme(darkTheme = true) {
        ResetEmailSentScreen(
            onBackToLoginClick = { }
        )
    }
} 