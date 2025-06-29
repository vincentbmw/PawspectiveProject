package com.ppb.pawspective

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.ppb.pawspective.data.model.User
import com.ppb.pawspective.data.repository.UserRepository
import com.ppb.pawspective.ui.theme.PawspectiveTheme
import com.ppb.pawspective.ui.theme.QuandoRegular
import com.ppb.pawspective.ui.theme.QuicksandRegular
import com.ppb.pawspective.utils.ThemeManager

class EditProfileActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var userRepository: UserRepository
    private lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        userRepository = UserRepository(this)
        themeManager = ThemeManager.getInstance(this)

        // Check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // User not logged in, redirect to login
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        val uid = currentUser.uid
        val email = currentUser.email ?: "No email"

        setContent {
            var user by remember {
                mutableStateOf<User?>(null)
            }
            var isLoading by remember { mutableStateOf(true) }
            var nickname by remember { mutableStateOf(TextFieldValue("")) }
            var fullName by remember { mutableStateOf(TextFieldValue("")) }

            // Load user data from local database
            LaunchedEffect(Unit) {
                try {
                    val userData = userRepository.getUserOrCreate(uid, email)
                    user = userData
                    nickname = TextFieldValue(userData.username)
                    fullName = TextFieldValue(userData.fullName)
                } catch (e: Exception) {
                    Toast.makeText(this@EditProfileActivity, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
                } finally {
                    isLoading = false
                }
            }

            PawspectiveTheme(darkTheme = themeManager.isDarkMode) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    user?.let { userData ->
                        EditProfileScreen(
                            user = userData,
                            nickname = nickname,
                            fullName = fullName,
                            onNicknameChange = { nickname = it },
                            onFullNameChange = { fullName = it },
                            onBackClick = { finish() },
                            onSaveChangesClick = {
                                // Update user profile in local database
                                val success = userRepository.updateUserProfile(
                                    userId = userData.userId,
                                    username = nickname.text.trim(),
                                    fullName = fullName.text.trim()
                                )

                                if (success) {
                                    Toast.makeText(this@EditProfileActivity, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                    // Redirect to ProfileActivity
                                    startActivity(Intent(this@EditProfileActivity, ProfileActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this@EditProfileActivity, "Failed to update profile", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditProfileScreen(
    user: User,
    nickname: TextFieldValue,
    fullName: TextFieldValue,
    onNicknameChange: (TextFieldValue) -> Unit,
    onFullNameChange: (TextFieldValue) -> Unit,
    onBackClick: () -> Unit,
    onSaveChangesClick: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        // Top Bar with Back Button and Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
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
                    text = "Edit Profile",
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

        // Profile Picture Section
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp)
        ) {
            AsyncImage(
                model = "https://firebasestorage.googleapis.com/v0/b/gotravel-9fad0.appspot.com/o/profile_pictures%2Ffemale.png?alt=media&token=4f6f872c-f971-42c8-b526-a39ac604ddb5",
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.content),
                placeholder = painterResource(id = R.drawable.content)
            )
        }

        // Content Section
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 220.dp)
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                
                // Nickname Field
                EditableField("What should we call you?", nickname, onNicknameChange)
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Full Name Field
                EditableField("Full Name", fullName, onFullNameChange)
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Email Field (Read-only)
                ProfileFieldReadOnly("Email", user.email)

                Spacer(modifier = Modifier.weight(1f))

                // Save Changes Button
                Button(
                    onClick = onSaveChangesClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Save Changes",
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
    }
}

@Composable
fun EditableField(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = QuicksandRegular,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            color = colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontFamily = QuandoRegular,
                fontSize = 16.sp
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colorScheme.surfaceVariant,
                unfocusedContainerColor = colorScheme.surfaceVariant,
                focusedIndicatorColor = colorScheme.primary,
                unfocusedIndicatorColor = colorScheme.outline
            ),
            shape = RoundedCornerShape(8.dp),
            singleLine = true
        )
    }
}

@Composable
fun ProfileFieldReadOnly(
    label: String,
    value: String
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontFamily = QuicksandRegular,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            ),
            color = colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(16.dp)
        ) {
            Text(
                text = value,
                style = TextStyle(
                    fontFamily = QuandoRegular,
                    fontSize = 16.sp
                ),
                color = colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
    }
}

@Preview(showBackground = true, name = "Edit Profile Screen Light Preview")
@Composable
fun EditProfileScreenLightPreview() {
    PawspectiveTheme(darkTheme = false) {
        EditProfileScreen(
            user = User(userId = "1", email = "user@example.com"),
            nickname = TextFieldValue("User's Nickname"),
            fullName = TextFieldValue("User's Fullname"),
            onNicknameChange = { },
            onFullNameChange = { },
            onBackClick = { },
            onSaveChangesClick = { }
        )
    }
}

@Preview(showBackground = true, name = "Edit Profile Screen Dark Preview")
@Composable
fun EditProfileScreenDarkPreview() {
    PawspectiveTheme(darkTheme = true) {
        EditProfileScreen(
            user = User(userId = "1", email = "user@example.com"),
            nickname = TextFieldValue("User's Nickname"),
            fullName = TextFieldValue("User's Fullname"),
            onNicknameChange = { },
            onFullNameChange = { },
            onBackClick = { },
            onSaveChangesClick = { }
        )
    }
}

