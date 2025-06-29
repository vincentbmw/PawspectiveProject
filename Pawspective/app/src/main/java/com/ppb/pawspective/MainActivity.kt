package com.ppb.pawspective

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.ppb.pawspective.ui.theme.PawspectiveTheme
import com.ppb.pawspective.utils.ThemeManager

class MainActivity : ComponentActivity() {
    private lateinit var themeManager: ThemeManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        themeManager = ThemeManager.getInstance(this)
        
        enableEdgeToEdge()
        setContent {
            PawspectiveTheme(darkTheme = themeManager.isDarkMode) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        color = MaterialTheme.colorScheme.onBackground,
        modifier = modifier
    )
}

@Preview(showBackground = true, name = "MainActivity Light")
@Composable
fun GreetingLightPreview() {
    PawspectiveTheme(darkTheme = false) {
        Greeting("Android")
    }
}

@Preview(showBackground = true, name = "MainActivity Dark")
@Composable
fun GreetingDarkPreview() {
    PawspectiveTheme(darkTheme = true) {
        Greeting("Android")
    }
}