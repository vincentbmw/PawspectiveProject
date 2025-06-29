package com.ppb.pawspective

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.ppb.pawspective.data.api.ApiClient
import com.ppb.pawspective.data.model.Chat
import com.ppb.pawspective.data.repository.ChatRepository
import com.ppb.pawspective.ui.theme.PawspectiveTheme
import com.ppb.pawspective.ui.theme.QuandoRegular
import com.ppb.pawspective.ui.theme.QuicksandRegular
import com.ppb.pawspective.utils.ThemeManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class HomeActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var chatRepository: ChatRepository
    private lateinit var themeManager: ThemeManager
    private val TAG = "HomeActivity"
    
    // Activity Result Launcher for ChatActivity
    private val chatActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Check if we need to refresh chat list
        val chatCreated = result.data?.getBooleanExtra("CHAT_CREATED", false) ?: false
        val chatModified = result.data?.getBooleanExtra("CHAT_MODIFIED", false) ?: false
        
        if (chatCreated || chatModified) {
            Log.d(TAG, "Chat was ${if (chatCreated) "created" else "modified"}, refreshing chat list")
            // Trigger refresh in compose
            refreshTrigger = !refreshTrigger
        }
    }
    
    private var refreshTrigger by mutableStateOf(false)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d(TAG, "onCreate: Initializing HomeActivity")
        
        auth = FirebaseAuth.getInstance()
        chatRepository = ChatRepository(ApiClient.apiService, this)
        themeManager = ThemeManager.getInstance(this)
        
        // Check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // User not logged in, redirect to login
            Log.d(TAG, "onCreate: No user logged in, redirecting to LoginActivity")
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        setContent {
            PawspectiveTheme(darkTheme = themeManager.isDarkMode) {
                HomeScreen(
                    userId = currentUser.uid,
                    chatRepository = chatRepository,
                    refreshTrigger = refreshTrigger,
                    onSettingsClick = {
                        startActivity(Intent(this@HomeActivity, SettingsActivity::class.java))
                    },
                    onNewChatClick = {
                        val intent = Intent(this@HomeActivity, ChatActivity::class.java)
                        chatActivityLauncher.launch(intent)
                    },
                    onChatItemClick = { chat ->
                        // TODO: Navigate to specific chat with chat ID
                        val intent = Intent(this@HomeActivity, ChatActivity::class.java).apply {
                            putExtra("CHAT_ID", chat.id)
                            putExtra("CHAT_TITLE", chat.title)
                        }
                        chatActivityLauncher.launch(intent)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userId: String,
    chatRepository: ChatRepository,
    refreshTrigger: Boolean,
    onSettingsClick: () -> Unit,
    onNewChatClick: () -> Unit,
    onChatItemClick: (Chat) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    var chats by remember { mutableStateOf<List<Chat>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    
    // Function to retry loading chats
    fun retryLoadChats() {
        Log.d("HomeScreen", "Retrying to load chats...")
        isLoading = true
        errorMessage = null
        
        // Use coroutine scope to make API call
        coroutineScope.launch {
            try {
                when (val result = chatRepository.getUserChats(userId)) {
                    is ChatRepository.Result.Success -> {
                        chats = result.data
                        isLoading = false
                        Log.d("HomeScreen", "Retry successful: loaded ${chats.size} chats")
                    }
                    is ChatRepository.Result.Error -> {
                        errorMessage = result.message
                        isLoading = false
                        Log.e("HomeScreen", "Retry failed: ${result.message}")
                    }
                    is ChatRepository.Result.Loading -> {
                        isLoading = true
                    }
                }
            } catch (e: Exception) {
                Log.e("HomeScreen", "Exception in retry: ${e.message}", e)
                errorMessage = "Retry failed: ${e.message}"
                isLoading = false
            }
        }
    }
    
    // Fetch chats when the screen loads
    LaunchedEffect(userId, refreshTrigger) {
        Log.d("HomeScreen", "LaunchedEffect: Fetching chats for user: $userId (refresh: $refreshTrigger)")
        isLoading = true
        errorMessage = null
        
        try {
            when (val result = chatRepository.getUserChats(userId)) {
                is ChatRepository.Result.Success -> {
                    chats = result.data
                    isLoading = false
                    Log.d("HomeScreen", "Successfully loaded ${chats.size} chats")
                    Log.d("HomeScreen", "Chat data: $chats")
                }
                is ChatRepository.Result.Error -> {
                    errorMessage = result.message
                    isLoading = false
                    Log.e("HomeScreen", "Error loading chats: ${result.message}")
                }
                is ChatRepository.Result.Loading -> {
                    isLoading = true
                    Log.d("HomeScreen", "Still loading...")
                }
            }
        } catch (e: Exception) {
            Log.e("HomeScreen", "Exception in LaunchedEffect: ${e.message}", e)
            errorMessage = "Unexpected error: ${e.message}"
            isLoading = false
        }
    }
    
    // Filter chats based on search query
    val filteredChats = remember(chats, searchQuery) {
        if (searchQuery.isBlank()) {
            chats
        } else {
            chats.filter { 
                it.title.contains(searchQuery, ignoreCase = true) ||
                it.preview.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Home",
                    style = TextStyle(
                        fontFamily = QuicksandRegular,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.onBackground
                )
                
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.setting_icon),
                        contentDescription = "Settings",
                        tint = colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            // Search Bar
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.search_icon),
                        contentDescription = "Search",
                        tint = colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { newValue -> searchQuery = newValue },
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(
                            fontFamily = QuandoRegular,
                            fontSize = 16.sp,
                            color = colorScheme.onSurface
                        ),
                        singleLine = true,
                        decorationBox = @Composable { innerTextField ->
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search",
                                    style = TextStyle(
                                        fontFamily = QuandoRegular,
                                        fontSize = 16.sp,
                                        color = colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }
            
            // Content Area
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when {
                    isLoading -> {
                        // Loading State
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = colorScheme.primary,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Loading your chats...",
                                style = TextStyle(
                                    fontFamily = QuandoRegular,
                                    fontSize = 16.sp
                                ),
                                color = colorScheme.secondary
                            )
                        }
                    }
                    
                    errorMessage != null -> {
                        // Error State
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.feedback_icon),
                                contentDescription = "Error",
                                tint = colorScheme.error,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Oops! Something went wrong",
                                style = TextStyle(
                                    fontFamily = QuicksandRegular,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage!!,
                                style = TextStyle(
                                    fontFamily = QuandoRegular,
                                    fontSize = 14.sp
                                ),
                                color = colorScheme.secondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { retryLoadChats() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colorScheme.primary
                                )
                            ) {
                                Text(
                                    text = "Try Again",
                                    style = TextStyle(
                                        fontFamily = QuandoRegular,
                                        fontSize = 16.sp
                                    ),
                                    color = colorScheme.onPrimary
                                )
                            }
                        }
                    }
                    
                    filteredChats.isEmpty() && !isLoading -> {
                        // Empty State
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.chat_icon),
                                contentDescription = "No chats",
                                tint = colorScheme.secondary,
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (searchQuery.isBlank()) "No chats yet" else "No chats found",
                                style = TextStyle(
                                    fontFamily = QuicksandRegular,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (searchQuery.isBlank()) 
                                    "Start a conversation to see your chats here" 
                                else 
                                    "Try a different search term",
                                style = TextStyle(
                                    fontFamily = QuandoRegular,
                                    fontSize = 14.sp
                                ),
                                color = colorScheme.secondary,
                                textAlign = TextAlign.Center
                            )
                            if (searchQuery.isBlank()) {
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = onNewChatClick,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = "Start New Chat",
                                        style = TextStyle(
                                            fontFamily = QuandoRegular,
                                            fontSize = 16.sp
                                        ),
                                        color = colorScheme.onPrimary
                                    )
                                }
                            }
                        }
                    }
                    
                    else -> {
                        // Chat List
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp)
                        ) {
                            items(filteredChats) { chat ->
                                ChatItem(
                                    chat = chat,
                                    onClick = { onChatItemClick(chat) }
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // New Chat Button
        Button(
            onClick = onNewChatClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.primary
            ),
            shape = RoundedCornerShape(24.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.chat_icon),
                contentDescription = "New Chat",
                tint = colorScheme.onPrimary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "New Chat",
                style = TextStyle(
                    fontFamily = QuandoRegular,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                ),
                color = colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun ChatItem(
    chat: Chat,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = chat.title,
                style = TextStyle(
                    fontFamily = QuicksandRegular,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            if (chat.preview.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = chat.preview,
                    style = TextStyle(
                        fontFamily = QuandoRegular,
                        fontSize = 14.sp
                    ),
                    color = colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = formatTimestamp(chat.updatedAt ?: ""),
                style = TextStyle(
                    fontFamily = QuandoRegular,
                    fontSize = 12.sp
                ),
                color = colorScheme.secondary
            )
        }
    }
}

// Helper function to format timestamp
private fun formatTimestamp(timestamp: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(timestamp)
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        "Recent"
    }
}

@Preview(showBackground = true, name = "Home Screen Light")
@Composable
fun HomeScreenLightPreview() {
    PawspectiveTheme(darkTheme = false) {
        // Preview with mock data
        val mockChats = listOf(
            Chat(
                id = "1",
                title = "Pet Adoption Recommendation...",
                preview = "Lorem Ipsum",
                lastMessage = "What should I consider when adopting a dog?",
                lastSender = "user",
                messageCount = 1,
                createdAt = "2024-01-15T10:30:00.000Z",
                updatedAt = "2024-01-15T10:30:00.000Z"
            ),
            Chat(
                id = "2", 
                title = "What is German Shepherd?",
                preview = "",
                lastMessage = "How often should I feed my cat?",
                lastSender = "user",
                messageCount = 1,
                createdAt = "2024-01-14T15:20:00.000Z",
                updatedAt = "2024-01-14T15:20:00.000Z"
            ),
            Chat(
                id = "3", 
                title = "The best dog that can pet and...",
                preview = "Lorem Ipsum",
                lastMessage = "What are the best dog breeds for families?",
                lastSender = "user",
                messageCount = 1,
                createdAt = "2024-01-13T09:15:00.000Z",
                updatedAt = "2024-01-13T09:15:00.000Z"
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            mockChats.forEach { chat ->
                ChatItem(
                    chat = chat,
                    onClick = { }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Preview(showBackground = true, name = "Home Screen Dark")
@Composable
fun HomeScreenDarkPreview() {
    PawspectiveTheme(darkTheme = true) {
        // Preview with mock data
        val mockChats = listOf(
            Chat(
                id = "1",
                title = "Pet Adoption Recommendation...",
                preview = "Lorem Ipsum",
                lastMessage = "What should I consider when adopting a dog?",
                lastSender = "user",
                messageCount = 1,
                createdAt = "2024-01-15T10:30:00.000Z",
                updatedAt = "2024-01-15T10:30:00.000Z"
            ),
            Chat(
                id = "2", 
                title = "What is German Shepherd?",
                preview = "",
                lastMessage = "How often should I feed my cat?",
                lastSender = "user",
                messageCount = 1,
                createdAt = "2024-01-14T15:20:00.000Z",
                updatedAt = "2024-01-14T15:20:00.000Z"
            ),
            Chat(
                id = "3", 
                title = "The best dog that can pet and...",
                preview = "Lorem Ipsum",
                lastMessage = "What are the best dog breeds for families?",
                lastSender = "user",
                messageCount = 1,
                createdAt = "2024-01-13T09:15:00.000Z",
                updatedAt = "2024-01-13T09:15:00.000Z"
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            mockChats.forEach { chat ->
                ChatItem(
                    chat = chat,
                    onClick = { }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
} 