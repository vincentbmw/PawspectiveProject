package com.ppb.pawspective

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.google.firebase.auth.FirebaseAuth
import com.ppb.pawspective.ProfileActivity
import com.ppb.pawspective.ui.theme.PawspectiveTheme
import com.ppb.pawspective.ui.theme.QuandoRegular
import com.ppb.pawspective.ui.theme.QuicksandRegular
import com.ppb.pawspective.utils.ThemeManager

class SettingsActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var themeManager: ThemeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        themeManager = ThemeManager.getInstance(this)
        
        // Check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // User not logged in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        setContent {
            // Use ThemeManager's isDarkMode state
            PawspectiveTheme(darkTheme = themeManager.isDarkMode) {
                SettingsScreen(
                    userEmail = currentUser.email ?: "No email",
                    isDarkMode = themeManager.isDarkMode,
                    onBackClick = {
                        finish()
                    },
                    onProfileClick = {
                        // TODO: Navigate to profile screen
                        // Toast.makeText(this@SettingsActivity, "Profile feature coming soon!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@SettingsActivity, ProfileActivity::class.java))
                    },
                    onFeedbackClick = {
                        // TODO: Navigate to feedback screen
                        Toast.makeText(this@SettingsActivity, "Feedback feature coming soon!", Toast.LENGTH_SHORT).show()
                    },
                    onThemeToggle = { isDark ->
                        themeManager.updateDarkMode(isDark)
                        // Recreate activity to apply theme change
                        recreate()
                    },
                    onLogoutClick = {
                        auth.signOut()
                        
                        // Clear session using SessionManager
                        val authManager = com.ppb.pawspective.auth.AuthManager(this@SettingsActivity)
                        authManager.signOut()
                        
                        Toast.makeText(this@SettingsActivity, "Logged out successfully", Toast.LENGTH_SHORT).show()
                        
                        // Navigate to login screen
                        val intent = Intent(this@SettingsActivity, LoginActivity::class.java)
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
fun SettingsScreen(
    userEmail: String,
    isDarkMode: Boolean,
    onBackClick: () -> Unit,
    onProfileClick: () -> Unit,
    onFeedbackClick: () -> Unit,
    onThemeToggle: (Boolean) -> Unit,
    onLogoutClick: () -> Unit
) {
    // Use Material3 color scheme
    val colorScheme = MaterialTheme.colorScheme
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        
        // Top Bar with Back Button and Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
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
                    text = "Settings",
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
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // User Email Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userEmail,
                    style = TextStyle(
                        fontFamily = QuandoRegular,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Settings Menu Items
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Item
            SettingsMenuItem(
                icon = R.drawable.profile_icon,
                title = "Profile",
                onClick = onProfileClick,
                iconTint = colorScheme.onBackground,
                textColor = colorScheme.onBackground
            )
            
            // Feedback Item
            SettingsMenuItem(
                icon = R.drawable.feedback_icon,
                title = "Feedback",
                onClick = onFeedbackClick,
                iconTint = colorScheme.onBackground,
                textColor = colorScheme.onBackground
            )
            
            // Dark Mode Toggle Item
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.mode_icon),
                        contentDescription = "Dark Mode",
                        tint = colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = if (isDarkMode) "Dark Mode" else "Light Mode",
                        style = TextStyle(
                            fontFamily = QuandoRegular,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = colorScheme.onBackground
                    )
                }
                
                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onThemeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = colorScheme.onPrimary,
                        checkedTrackColor = colorScheme.primary,
                        uncheckedThumbColor = colorScheme.onSurface,
                        uncheckedTrackColor = colorScheme.outline
                    )
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Logout Button
        Button(
            onClick = onLogoutClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary
            )
        ) {
            Text(
                text = "Logout",
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

@Composable
fun SettingsMenuItem(
    icon: Int,
    title: String,
    onClick: () -> Unit,
    iconTint: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(id = icon),
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = title,
            style = TextStyle(
                fontFamily = QuandoRegular,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            ),
            color = textColor
        )
    }
}

@Preview(showBackground = true, name = "Settings Screen Light Preview")
@Composable
fun SettingsScreenLightPreview() {
    PawspectiveTheme(darkTheme = false) {
        SettingsScreen(
            userEmail = "user@example.com",
            isDarkMode = false,
            onBackClick = { },
            onProfileClick = { },
            onFeedbackClick = { },
            onThemeToggle = { },
            onLogoutClick = { }
        )
    }
}

@Preview(showBackground = true, name = "Settings Screen Dark Preview")
@Composable
fun SettingsScreenDarkPreview() {
    PawspectiveTheme(darkTheme = true) {
        SettingsScreen(
            userEmail = "user@example.com",
            isDarkMode = true,
            onBackClick = { },
            onProfileClick = { },
            onFeedbackClick = { },
            onThemeToggle = { },
            onLogoutClick = { }
        )
    }
} 