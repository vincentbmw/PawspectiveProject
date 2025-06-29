package com.ppb.pawspective

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.ppb.pawspective.data.model.User
import com.ppb.pawspective.data.repository.UserRepository
import com.ppb.pawspective.ui.theme.PawspectiveTheme
import com.ppb.pawspective.ui.theme.QuandoRegular
import com.ppb.pawspective.ui.theme.QuicksandRegular
import com.ppb.pawspective.utils.ThemeManager

class ProfileActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var userRepository: UserRepository
    private lateinit var themeManager: ThemeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
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
        
        // Check if user signed in with Google
        val isGoogleUser = currentUser.providerData.any { it.providerId == "google.com" }
        val isEmailUser = currentUser.providerData.any { it.providerId == "password" }

        setContent {
            var user by remember {
                mutableStateOf<User?>(null)
            }
            var isLoading by remember { mutableStateOf(true) }

            // Load user data from local database
            LaunchedEffect(Unit) {
                try {
                    user = userRepository.getUserOrCreate(uid, email)
                } catch (e: Exception) {
                    Toast.makeText(this@ProfileActivity, "Error loading profile: ${e.message}", Toast.LENGTH_SHORT).show()
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
                        ProfileScreen(
                            user = userData,
                            isGoogleUser = isGoogleUser,
                            isEmailUser = isEmailUser,
                            onBackClick = { finish() },
                            onEditProfileClick = { 
                                startActivity(Intent(this@ProfileActivity, EditProfileActivity::class.java))
                            },
                            onChangePasswordClick = { 
                                // Only navigate to ChangePasswordActivity for email users
                                if (isEmailUser && !isGoogleUser) {
                                    startActivity(Intent(this@ProfileActivity, ChangePasswordActivity::class.java))
                                }
                                // Google users will be handled by the dialog in ProfileScreen
                            },
                            onProfilePictureClick = {
                                // Handle profile picture selection
                                Toast.makeText(this@ProfileActivity, "Profile picture selection coming soon!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    user: User,
    isGoogleUser: Boolean,
    isEmailUser: Boolean,
    onBackClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onChangePasswordClick: () -> Unit,
    onProfilePictureClick: () -> Unit
) {
    val context = LocalContext.current
    val colorScheme = MaterialTheme.colorScheme
    var showGooglePasswordDialog by remember { mutableStateOf(false) }
    
    // Permission launcher for photo selection
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onProfilePictureClick()
        } else {
            Toast.makeText(context, "Permission denied to access photos", Toast.LENGTH_SHORT).show()
        }
    }
    
    // Photo picker launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // For now, just show a message since we can't use Firebase Storage
            Toast.makeText(context, "Photo selected! (Storage functionality disabled)", Toast.LENGTH_SHORT).show()
        }
    }

    // Google Password Dialog
    if (showGooglePasswordDialog) {
        Dialog(onDismissRequest = { showGooglePasswordDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surface
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Google Icon
                    Icon(
                        painter = painterResource(id = R.drawable.google_icon),
                        contentDescription = "Google",
                        modifier = Modifier.size(48.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Google Account",
                        style = TextStyle(
                            fontFamily = QuicksandRegular,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "You signed in with Google. To change your password, please visit your Google Account settings.",
                        style = TextStyle(
                            fontFamily = QuandoRegular,
                            fontSize = 14.sp
                        ),
                        color = colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showGooglePasswordDialog = false },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Cancel",
                                style = TextStyle(
                                    fontFamily = QuandoRegular,
                                    fontSize = 14.sp
                                )
                            )
                        }
                        
                        Button(
                            onClick = { 
                                showGooglePasswordDialog = false
                                // You could open Google Account settings here if needed
                                Toast.makeText(context, "Please visit myaccount.google.com to change your password", Toast.LENGTH_LONG).show()
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colorScheme.primary
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Got it",
                                style = TextStyle(
                                    fontFamily = QuandoRegular,
                                    fontSize = 14.sp
                                ),
                                color = colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }
    }

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
                    text = "Profile",
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
            Box(
                modifier = Modifier
                    .size(120.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                // Profile Picture
                AsyncImage(
                    model = "https://firebasestorage.googleapis.com/v0/b/gotravel-9fad0.appspot.com/o/profile_pictures%2Ffemale.png?alt=media&token=4f6f872c-f971-42c8-b526-a39ac604ddb5",
                    contentDescription = "Profile Picture",
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable {
                            // Check permission and launch photo picker
                            when (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )) {
                                PackageManager.PERMISSION_GRANTED -> {
                                    photoPickerLauncher.launch("image/*")
                                }
                                else -> {
                                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                            }
                        },
                    contentScale = ContentScale.Crop,
                    error = painterResource(id = R.drawable.content), // Fallback image
                    placeholder = painterResource(id = R.drawable.content)
                )
                
                // Camera Icon
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable {
                            // Same click handler as profile picture
                            when (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            )) {
                                PackageManager.PERMISSION_GRANTED -> {
                                    photoPickerLauncher.launch("image/*")
                                }
                                else -> {
                                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.camera),
                        contentDescription = "Change Picture",
                        modifier = Modifier.size(20.dp),
                        tint = Color.Black
                    )
                }
            }
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
                ProfileField(
                    label = "What should we call you?",
                    value = if (user.username.isBlank()) "Your nickname here" else user.username,
                    isPlaceholder = user.username.isBlank()
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Full Name Field
                ProfileField(
                    label = "Full Name",
                    value = if (user.fullName.isBlank()) "Your name here" else user.fullName,
                    isPlaceholder = user.fullName.isBlank()
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // Email Field
                ProfileField(
                    label = "Email",
                    value = user.email,
                    isPlaceholder = false
                )

                Spacer(modifier = Modifier.weight(1f))

                // Edit Profile Button
                Button(
                    onClick = onEditProfileClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Edit Profile",
                        style = TextStyle(
                            fontFamily = QuandoRegular,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = colorScheme.onPrimary
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Change Password Button
                Button(
                    onClick = {
                        if (isGoogleUser && !isEmailUser) {
                            showGooglePasswordDialog = true
                        } else {
                            onChangePasswordClick()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.primary
                    )
                ) {
                    Text(
                        text = "Change Password",
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
fun ProfileField(
    label: String,
    value: String,
    isPlaceholder: Boolean = false
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
                    color = colorScheme.surfaceVariant,
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
                color = if (isPlaceholder) 
                    colorScheme.onSurface.copy(alpha = 0.6f) 
                else 
                    colorScheme.onSurface
            )
        }
    }
}

@Preview(showBackground = true, name = "Profile Screen Light")
@Composable
fun ProfileScreenLightPreview() {
    val sampleUser = User(
        userId = "sample_id",
        email = "user@example.com",
        username = "",
        fullName = "",
        profileCompleted = false
    )
    
    PawspectiveTheme(darkTheme = false) {
        ProfileScreen(
            user = sampleUser,
            isGoogleUser = false,
            isEmailUser = true,
            onBackClick = { },
            onEditProfileClick = { },
            onChangePasswordClick = { },
            onProfilePictureClick = { }
        )
    }
}

@Preview(showBackground = true, name = "Profile Screen Dark")
@Composable
fun ProfileScreenDarkPreview() {
    val sampleUser = User(
        userId = "sample_id",
        email = "user@example.com",
        username = "JohnDoe",
        fullName = "John Doe",
        profileCompleted = true
    )
    
    PawspectiveTheme(darkTheme = true) {
        ProfileScreen(
            user = sampleUser,
            isGoogleUser = false,
            isEmailUser = true,
            onBackClick = { },
            onEditProfileClick = { },
            onChangePasswordClick = { },
            onProfilePictureClick = { }
        )
    }
}