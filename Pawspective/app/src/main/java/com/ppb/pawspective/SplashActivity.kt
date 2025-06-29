package com.ppb.pawspective

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ppb.pawspective.auth.AuthManager
import com.ppb.pawspective.ui.theme.PawspectiveTheme
import com.ppb.pawspective.utils.SessionManager
import com.ppb.pawspective.utils.ThemeManager

class SplashActivity : ComponentActivity() {
    
    private lateinit var authManager: AuthManager
    private lateinit var sessionManager: SessionManager
    private lateinit var themeManager: ThemeManager
    private val TAG = "SplashActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "onCreate: Initializing SplashActivity")
        
        authManager = AuthManager(this)
        sessionManager = authManager.getSessionManager()
        themeManager = ThemeManager.getInstance(this)

        setContent {
            PawspectiveTheme(darkTheme = themeManager.isDarkMode) {
                SplashScreen()
            }
        }

        // Navigate after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            navigateToNextScreen()
        }, 3000)
    }
    
    private fun navigateToNextScreen() {
        Log.d(TAG, "navigateToNextScreen: Determining next screen")
        
        // Check if user should auto-login
        if (authManager.shouldAutoLogin()) {
            Log.d(TAG, "navigateToNextScreen: User has valid session, auto-login to HomeActivity")
            
            // Extend session since user is active
            authManager.extendSession()
            
            // Log session info for debugging
            Log.d(TAG, sessionManager.getSessionInfo())
            
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }
        
        // Check Firebase Auth state (fallback)
        val currentUser = authManager.getCurrentUser()
        if (currentUser != null) {
            Log.d(TAG, "navigateToNextScreen: Firebase user exists but no valid session, going to HomeActivity")
            
            // Create session for existing Firebase user
            sessionManager.createLoginSession(
                userId = currentUser.uid,
                email = currentUser.email ?: "",
                name = currentUser.displayName,
                loginMethod = if (currentUser.providerData.any { it.providerId == "google.com" }) 
                    SessionManager.LOGIN_METHOD_GOOGLE else SessionManager.LOGIN_METHOD_EMAIL,
                rememberMe = true
            )
            
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
            return
        }
        
        // No valid session, check onboarding status
        if (sessionManager.isOnboardingCompleted()) {
            Log.d(TAG, "navigateToNextScreen: Onboarding completed, going to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            Log.d(TAG, "navigateToNextScreen: First time user, going to OnboardingActivity")
            startActivity(Intent(this, OnboardingActivity::class.java))
        }
        finish()
    }
}

@Composable
fun SplashScreen() {
    val colorScheme = MaterialTheme.colorScheme
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.paws_splash),
            contentDescription = "App Logo",
            tint = Color.Unspecified,
            modifier = Modifier.size(120.dp)
        )
    }
}

@Preview(showBackground = true, name = "Splash Screen Light")
@Composable
fun SplashScreenLightPreview() {
    PawspectiveTheme(darkTheme = false) {
        SplashScreen()
    }
}

@Preview(showBackground = true, name = "Splash Screen Dark")
@Composable
fun SplashScreenDarkPreview() {
    PawspectiveTheme(darkTheme = true) {
        SplashScreen()
    }
}