package com.ppb.pawspective

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.ppb.pawspective.ui.theme.PawspectiveTheme
import com.ppb.pawspective.ui.theme.QuandoRegular
import com.ppb.pawspective.ui.theme.QuicksandRegular
import com.ppb.pawspective.utils.SessionManager
import com.ppb.pawspective.utils.ThemeManager

class OnboardingActivity : ComponentActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var themeManager: ThemeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        sessionManager = SessionManager(this)
        themeManager = ThemeManager.getInstance(this)
        
        setContent {
            PawspectiveTheme(darkTheme = themeManager.isDarkMode) {
                OnboardingScreen(
                    onGetStartedClick = {
                        // Mark onboarding as completed using SessionManager
                        sessionManager.setOnboardingCompleted(true)
                        
                        // Navigate to RegisterActivity
                        startActivity(Intent(this, RegisterActivity::class.java))
                        finish()
                    },
                    onSkipClick = {
                        // Mark onboarding as completed using SessionManager
                        sessionManager.setOnboardingCompleted(true)
                        
                        // Navigate to LoginActivity
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun OnboardingScreen(
    onGetStartedClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    // Use Material3 color scheme
    val colorScheme = MaterialTheme.colorScheme
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Paw print image positioned at the left edge
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.paws_splash_sec),
                    contentDescription = "Paw Print",
                    modifier = Modifier
                        .size(350.dp)
                        .align(Alignment.TopStart)
                        .offset((-80).dp, 40.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // Heading text with left alignment and horizontal padding
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Find your",
                    style = TextStyle(
                        fontFamily = QuicksandRegular,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.onBackground
                )
                
                Text(
                    text = "bestfriend",
                    style = TextStyle(
                        fontFamily = QuicksandRegular, 
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.primary
                )
                
                Text(
                    text = buildAnnotatedString {
                        append("with ")
                        withStyle(style = SpanStyle(color = colorScheme.primary)) {
                            append("Pawspective")
                        }
                    },
                    style = TextStyle(
                        fontFamily = QuicksandRegular,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.onBackground
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description text with left alignment
            Text(
                text = "You can find what type of pet matches your lifestyle and preferences for the best adoption experience! We'll help you discover your perfect furry companion through our personalized matching system.",
                style = TextStyle(
                    fontFamily = QuandoRegular,
                    fontSize = 14.sp
                ),
                color = colorScheme.onBackground,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                textAlign = TextAlign.Start
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Get Started button
            Button(
                onClick = onGetStartedClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary
                )
            ) {
                Text(
                    text = "Get Started!",
                    style = TextStyle(
                        fontFamily = QuandoRegular,
                        fontSize = 16.sp
                    ),
                    color = colorScheme.onPrimary
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Skip button
            Button(
                onClick = onSkipClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.surface
                )
            ) {
                Text(
                    text = "Skip",
                    style = TextStyle(
                        fontFamily = QuandoRegular,
                        fontSize = 16.sp
                    ),
                    color = colorScheme.onSurface
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Preview(showBackground = true, name = "Onboarding Screen Light")
@Composable
fun OnboardingScreenLightPreview() {
    PawspectiveTheme(darkTheme = false) {
        OnboardingScreen(
            onGetStartedClick = { },
            onSkipClick = { }
        )
    }
}

@Preview(showBackground = true, name = "Onboarding Screen Dark")
@Composable
fun OnboardingScreenDarkPreview() {
    PawspectiveTheme(darkTheme = true) {
        OnboardingScreen(
            onGetStartedClick = { },
            onSkipClick = { }
        )
    }
}