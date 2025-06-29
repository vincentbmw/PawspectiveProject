package com.ppb.pawspective

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.ppb.pawspective.data.api.ApiClient
import com.ppb.pawspective.data.repository.ChatRepository
import com.ppb.pawspective.ui.theme.PawspectiveTheme
import com.ppb.pawspective.ui.theme.QuandoRegular
import com.ppb.pawspective.ui.theme.QuicksandRegular
import com.ppb.pawspective.utils.ThemeManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// Data class for chat messages
data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var chatRepository: ChatRepository
    private lateinit var themeManager: ThemeManager
    private val TAG = "ChatActivity"
    
    // Track if chat was created during this session
    private var chatWasCreated = false
    private var chatWasModified = false
    private var existingChatId: String? = null
    private var existingChatTitle: String? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display and handle window insets
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        Log.d(TAG, "onCreate: Initializing ChatActivity")
        
        auth = FirebaseAuth.getInstance()
        chatRepository = ChatRepository(ApiClient.apiService, this)
        themeManager = ThemeManager.getInstance(this)
        
        // Check if user is logged in
        val currentUser = auth.currentUser
        if (currentUser == null) {
            // User not logged in, redirect to login
            Log.d(TAG, "onCreate: No user logged in, redirecting to LoginActivity")
            finish()
            return
        }
        
        // Check if this is an existing chat
        existingChatId = intent.getStringExtra("CHAT_ID")
        existingChatTitle = intent.getStringExtra("CHAT_TITLE")
        
        Log.d(TAG, "onCreate: Existing chat ID: $existingChatId, Title: $existingChatTitle")
        
        setContent {
            PawspectiveTheme(darkTheme = themeManager.isDarkMode) {
                ChatScreen(
                    userId = currentUser.uid,
                    chatRepository = chatRepository,
                    existingChatId = existingChatId,
                    existingChatTitle = existingChatTitle,
                    onBackClick = {
                        handleBackNavigation()
                    },
                    onChatCreated = {
                        chatWasCreated = true
                        chatWasModified = true
                    },
                    onChatModified = {
                        chatWasModified = true
                    },
                    onMenuClick = {
                        // Menu click is now handled within ChatScreen
                    }
                )
            }
        }
    }
    
    private fun handleBackNavigation() {
        // Send result back to HomeActivity if chat was created or modified
        val resultIntent = Intent().apply {
            putExtra("CHAT_CREATED", chatWasCreated)
            putExtra("CHAT_MODIFIED", chatWasModified)
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
    
    override fun onBackPressed() {
        super.onBackPressed()
        handleBackNavigation()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    userId: String,
    chatRepository: ChatRepository,
    existingChatId: String?,
    existingChatTitle: String?,
    onBackClick: () -> Unit,
    onChatCreated: () -> Unit,
    onChatModified: () -> Unit,
    onMenuClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    var messageText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showPlusMenu by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var chatTitle by remember { mutableStateOf(existingChatTitle ?: "Pawspective") }
    var isStarred by remember { mutableStateOf(false) }
    var currentChatId by remember { mutableStateOf(existingChatId) }
    var isDeleting by remember { mutableStateOf(false) }
    var isRenaming by remember { mutableStateOf(false) }
    var isStarring by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    
    // Load existing chat messages if this is an existing chat
    LaunchedEffect(existingChatId) {
        existingChatId?.let { chatId ->
            Log.d("ChatScreen", "Loading existing chat messages for chat: $chatId")
            isLoading = true
            
            when (val result = chatRepository.getChatMessages(userId, chatId)) {
                is ChatRepository.Result.Success -> {
                    isLoading = false
                    Log.d("ChatScreen", "Successfully loaded ${result.data.size} messages")
                    
                    // Convert API ChatMessage to local ChatMessage format
                    val loadedMessages = result.data.map { apiMessage ->
                        ChatMessage(
                            id = apiMessage.id,
                            content = if (apiMessage.sender == "user") 
                                apiMessage.message 
                            else 
                                cleanMarkdown(apiMessage.message),
                            isFromUser = apiMessage.sender == "user",
                            timestamp = parseTimestamp(apiMessage.timestamp ?: "")
                        )
                    }
                    
                    messages = loadedMessages
                    Log.d("ChatScreen", "Chat messages loaded: ${messages.size} messages")
                }
                is ChatRepository.Result.Error -> {
                    isLoading = false
                    Log.e("ChatScreen", "Failed to load chat messages: ${result.message}")
                    
                    // Add error message to chat
                    val errorMessage = ChatMessage(
                        content = "Failed to load chat history. Please try again.",
                        isFromUser = false
                    )
                    messages = listOf(errorMessage)
                }
                is ChatRepository.Result.Loading -> {
                    isLoading = true
                }
            }
        }
    }
    
    // Auto scroll to bottom when new message is added
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .windowInsetsPadding(WindowInsets.systemBars)
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
                IconButton(onClick = onBackClick) {
                    Icon(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Back",
                        tint = colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Text(
                    text = chatTitle,
                    style = TextStyle(
                        fontFamily = QuicksandRegular,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                
                Box {
                    IconButton(onClick = { showDropdownMenu = true }) {
                        Icon(
                            painter = painterResource(id = R.drawable.menu_dots_icon),
                            contentDescription = "Menu",
                            tint = colorScheme.onBackground,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showDropdownMenu,
                        onDismissRequest = { showDropdownMenu = false },
                        modifier = Modifier.background(colorScheme.surface)
                    ) {
                        if (currentChatId != null) {
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Rename Chat",
                                        fontFamily = QuandoRegular,
                                        color = colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    showRenameDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.edit_icon),
                                        contentDescription = "Rename",
                                        tint = colorScheme.onSurface
                                    )
                                },
                                enabled = !isRenaming
                            )
                            
                            DropdownMenuItem(
                                text = { 
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            if (isStarred) "Unstar Chat" else "Star Chat",
                                            fontFamily = QuandoRegular,
                                            color = colorScheme.onSurface
                                        )
                                        if (isStarring) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = colorScheme.primary,
                                                strokeWidth = 2.dp
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    currentChatId?.let { chatId ->
                                        coroutineScope.launch {
                                            isStarring = true
                                            val newStarredState = !isStarred
                                            when (chatRepository.starChat(userId, chatId, newStarredState)) {
                                                is ChatRepository.Result.Success -> {
                                                    isStarred = newStarredState
                                                    onChatModified()
                                                    isStarring = false
                                                }
                                                is ChatRepository.Result.Error -> {
                                                    isStarring = false
                                                    // TODO: Show error message
                                                }
                                                is ChatRepository.Result.Loading -> {
                                                    // Keep loading state
                                                }
                                            }
                                        }
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(
                                            id = if (isStarred) R.drawable.star_filled 
                                            else R.drawable.star_outline
                                        ),
                                        contentDescription = if (isStarred) "Unstar" else "Star",
                                        tint = if (isStarred) Color(0xFFFFD700) else colorScheme.onSurface
                                    )
                                },
                                enabled = !isStarring
                            )
                            
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Delete Chat",
                                        fontFamily = QuandoRegular,
                                        color = Color(0xFFD32F2F)
                                    )
                                },
                                onClick = {
                                    showDropdownMenu = false
                                    showDeleteDialog = true
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.delete_icon),
                                        contentDescription = "Delete",
                                        tint = Color(0xFFD32F2F)
                                    )
                                },
                                enabled = !isDeleting
                            )
                        }
                    }
                }
            }
            
            // Messages List or Welcome Screen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (messages.isEmpty() && !isLoading) {
                    // Welcome Screen
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.paws_only),
                            contentDescription = "Pawspective",
                            tint = colorScheme.primary,
                            modifier = Modifier.size(80.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            text = "How can I help you today?",
                            style = TextStyle(
                                fontFamily = QuicksandRegular,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Messages List
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(messages) { message ->
                            MessageBubble(
                                message = message,
                                isFromUser = message.isFromUser
                            )
                        }
                        
                        // Show loading indicator when waiting for response
                        if (isLoading) {
                            item {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.Start
                                ) {
                                    Card(
                                        modifier = Modifier.widthIn(max = 280.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = colorScheme.surface
                                        ),
                                        shape = RoundedCornerShape(
                                            topStart = 16.dp,
                                            topEnd = 16.dp,
                                            bottomEnd = 16.dp,
                                            bottomStart = 4.dp
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(16.dp),
                                                color = colorScheme.primary,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Thinking...",
                                                style = TextStyle(
                                                    fontFamily = QuandoRegular,
                                                    fontSize = 14.sp
                                                ),
                                                color = colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Message Input with keyboard padding
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .windowInsetsPadding(WindowInsets.ime),
                colors = CardDefaults.cardColors(
                    containerColor = colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        // Plus icon in circular background
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(colorScheme.surface)
                                .clickable { showPlusMenu = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.plus_icon),
                                contentDescription = "Add attachment",
                                tint = colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        
                        DropdownMenu(
                            expanded = showPlusMenu,
                            onDismissRequest = { showPlusMenu = false },
                            modifier = Modifier.background(colorScheme.surface)
                        ) {
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Camera",
                                        fontFamily = QuandoRegular,
                                        color = colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    showPlusMenu = false
                                    // TODO: Implement camera functionality
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.camera_icon),
                                        contentDescription = "Camera",
                                        tint = colorScheme.onSurface
                                    )
                                }
                            )
                            
                            DropdownMenuItem(
                                text = { 
                                    Text(
                                        "Microphone",
                                        fontFamily = QuandoRegular,
                                        color = colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    showPlusMenu = false
                                    // TODO: Implement voice input functionality
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.mic_icon),
                                        contentDescription = "Microphone",
                                        tint = colorScheme.onSurface
                                    )
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Text input area
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp, max = 120.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (messageText.isEmpty()) {
                            Text(
                                text = "Chat with Pawspective",
                                style = TextStyle(
                                    fontFamily = QuandoRegular,
                                    fontSize = 16.sp,
                                    color = colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        
                        BasicTextField(
                            value = messageText,
                            onValueChange = { messageText = it },
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = TextStyle(
                                fontFamily = QuandoRegular,
                                fontSize = 16.sp,
                                color = colorScheme.onSurface
                            ),
                            enabled = !isLoading,
                            cursorBrush = SolidColor(colorScheme.primary)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Send icon in circular background
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                if (messageText.isNotBlank() && !isLoading) 
                                    colorScheme.primary 
                                else 
                                    colorScheme.surface
                            )
                            .clickable(enabled = messageText.isNotBlank() && !isLoading) {
                                sendMessage(
                                    messageText = messageText,
                                    userId = userId,
                                    chatRepository = chatRepository,
                                    currentChatId = currentChatId,
                                    onMessageSent = { newMessage ->
                                        messages = messages + newMessage
                                        messageText = ""
                                    },
                                    onResponseReceived = { response ->
                                        messages = messages + response
                                    },
                                    onChatCreated = { chatId ->
                                        currentChatId = chatId
                                        onChatCreated()
                                    },
                                    onLoadingChange = { loading ->
                                        isLoading = loading
                                    },
                                    coroutineScope = coroutineScope
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.send_icon),
                            contentDescription = "Send",
                            tint = if (messageText.isNotBlank() && !isLoading) 
                                colorScheme.onPrimary 
                            else 
                                colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
        
        // Rename Dialog
        if (showRenameDialog) {
            RenameDialog(
                currentTitle = chatTitle,
                isRenaming = isRenaming,
                onDismiss = { showRenameDialog = false },
                onConfirm = { newTitle ->
                    currentChatId?.let { chatId ->
                        coroutineScope.launch {
                            isRenaming = true
                            when (chatRepository.renameChat(userId, chatId, newTitle)) {
                                is ChatRepository.Result.Success -> {
                                    chatTitle = newTitle
                                    onChatModified()
                                    isRenaming = false
                                    showRenameDialog = false
                                }
                                is ChatRepository.Result.Error -> {
                                    isRenaming = false
                                    // TODO: Show error message
                                }
                                is ChatRepository.Result.Loading -> {
                                    // Keep loading state
                                }
                            }
                        }
                    }
                }
            )
        }
        
        // Delete Dialog
        if (showDeleteDialog) {
            DeleteDialog(
                chatTitle = chatTitle,
                isDeleting = isDeleting,
                onDismiss = { showDeleteDialog = false },
                onConfirm = {
                    currentChatId?.let { chatId ->
                        coroutineScope.launch {
                            isDeleting = true
                            when (chatRepository.deleteChat(userId, chatId)) {
                                is ChatRepository.Result.Success -> {
                                    isDeleting = false
                                    showDeleteDialog = false
                                    onChatModified()
                                    onBackClick()
                                }
                                is ChatRepository.Result.Error -> {
                                    isDeleting = false
                                    // TODO: Show error message
                                }
                                is ChatRepository.Result.Loading -> {
                                    // Keep loading state
                                }
                            }
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun MessageBubble(
    message: ChatMessage,
    isFromUser: Boolean
) {
    // Use Material3 color scheme
    val colorScheme = MaterialTheme.colorScheme
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isFromUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isFromUser) {
            // AI Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.paws_only),
                    contentDescription = "AI",
                    tint = colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }
        
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isFromUser) colorScheme.primary else colorScheme.surface
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomEnd = if (isFromUser) 4.dp else 16.dp,
                bottomStart = if (isFromUser) 16.dp else 4.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    style = TextStyle(
                        fontFamily = QuandoRegular,
                        fontSize = 14.sp
                    ),
                    color = if (isFromUser) colorScheme.onPrimary else colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatMessageTime(message.timestamp),
                    style = TextStyle(
                        fontFamily = QuandoRegular,
                        fontSize = 10.sp
                    ),
                    color = if (isFromUser) 
                        colorScheme.onPrimary.copy(alpha = 0.7f) 
                    else 
                        colorScheme.onSurface.copy(alpha = 0.7f),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
        
        if (isFromUser) {
            Spacer(modifier = Modifier.width(8.dp))
            // User Avatar
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(colorScheme.secondary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.profile_icon),
                    contentDescription = "User",
                    tint = colorScheme.onSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun RenameDialog(
    currentTitle: String,
    isRenaming: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newTitle by remember { mutableStateOf(currentTitle) }
    val colorScheme = MaterialTheme.colorScheme
    
    AlertDialog(
        onDismissRequest = if (!isRenaming) onDismiss else { {} },
        title = {
            Text(
                text = "Rename Chat",
                style = TextStyle(
                    fontFamily = QuicksandRegular,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = colorScheme.onSurface
            )
        },
        text = {
            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { 
                    Text(
                        "Chat Title",
                        fontFamily = QuandoRegular,
                        color = colorScheme.secondary
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorScheme.primary,
                    unfocusedBorderColor = colorScheme.outline,
                    cursorColor = colorScheme.primary,
                    focusedTextColor = colorScheme.onSurface,
                    unfocusedTextColor = colorScheme.onSurface
                ),
                textStyle = TextStyle(
                    fontFamily = QuandoRegular,
                    fontSize = 16.sp,
                    color = colorScheme.onSurface
                ),
                singleLine = true,
                enabled = !isRenaming
            )
        },
        confirmButton = {
            Button(
                onClick = { 
                    if (newTitle.isNotBlank() && newTitle != currentTitle) {
                        onConfirm(newTitle)
                    }
                },
                enabled = !isRenaming && newTitle.isNotBlank() && newTitle != currentTitle
            ) {
                if (isRenaming) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Rename",
                        fontFamily = QuandoRegular,
                        color = colorScheme.onPrimary
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isRenaming
            ) {
                Text(
                    "Cancel",
                    fontFamily = QuandoRegular,
                    color = colorScheme.secondary
                )
            }
        },
        containerColor = colorScheme.surface
    )
}

@Composable
fun DeleteDialog(
    chatTitle: String,
    isDeleting: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    
    AlertDialog(
        onDismissRequest = if (!isDeleting) onDismiss else { {} },
        title = {
            Text(
                text = "Delete Chat",
                style = TextStyle(
                    fontFamily = QuicksandRegular,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = colorScheme.onSurface
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete \"$chatTitle\"? This action cannot be undone.",
                style = TextStyle(
                    fontFamily = QuandoRegular,
                    fontSize = 14.sp
                ),
                color = colorScheme.onSurface
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isDeleting,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFD32F2F)
                )
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Delete",
                        fontFamily = QuandoRegular,
                        color = Color.White
                    )
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text(
                    "Cancel",
                    fontFamily = QuandoRegular,
                    color = colorScheme.secondary
                )
            }
        },
        containerColor = colorScheme.surface
    )
}

// Helper functions remain the same...
private fun sendMessage(
    messageText: String,
    userId: String,
    chatRepository: ChatRepository,
    currentChatId: String?,
    onMessageSent: (ChatMessage) -> Unit,
    onResponseReceived: (ChatMessage) -> Unit,
    onChatCreated: (String) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    val userMessage = ChatMessage(
        content = messageText,
        isFromUser = true
    )
    
    onMessageSent(userMessage)
    onLoadingChange(true)
    
    coroutineScope.launch {
        try {
            if (currentChatId != null) {
                // Send message to existing chat
                val result = chatRepository.sendChatMessage(userId, currentChatId, messageText)
                when (result) {
                    is ChatRepository.Result.Success -> {
                        onLoadingChange(false)
                        val aiResponse = ChatMessage(
                            content = cleanMarkdown(result.data.response ?: "No response received"),
                            isFromUser = false
                        )
                        onResponseReceived(aiResponse)
                    }
                    is ChatRepository.Result.Error -> {
                        onLoadingChange(false)
                        val errorMessage = ChatMessage(
                            content = "Sorry, I couldn't process your message. Please try again.",
                            isFromUser = false
                        )
                        onResponseReceived(errorMessage)
                    }
                    is ChatRepository.Result.Loading -> {
                        // Keep loading
                    }
                }
            } else {
                // Create new chat and send message
                val result = chatRepository.sendMessage(userId, messageText)
                when (result) {
                    is ChatRepository.Result.Success -> {
                        onLoadingChange(false)
                        
                        if (result.data.chatId != null) {
                            onChatCreated(result.data.chatId)
                        }
                        
                        val aiResponse = ChatMessage(
                            content = cleanMarkdown(result.data.response ?: "No response received"),
                            isFromUser = false
                        )
                        onResponseReceived(aiResponse)
                    }
                    is ChatRepository.Result.Error -> {
                        onLoadingChange(false)
                        val errorMessage = ChatMessage(
                            content = "Sorry, I couldn't process your message. Please try again.",
                            isFromUser = false
                        )
                        onResponseReceived(errorMessage)
                    }
                    is ChatRepository.Result.Loading -> {
                        // Keep loading
                    }
                }
            }
        } catch (e: Exception) {
            onLoadingChange(false)
            val errorMessage = ChatMessage(
                content = "An error occurred. Please check your connection and try again.",
                isFromUser = false
            )
            onResponseReceived(errorMessage)
        }
    }
}

private fun cleanMarkdown(text: String): String {
    return text
        // Remove bold formatting (**text** or __text__)
        .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
        .replace(Regex("__(.*?)__"), "$1")
        // Remove italic formatting (*text* or _text_)
        .replace(Regex("\\*(.*?)\\*"), "$1")
        .replace(Regex("_(.*?)_"), "$1")
        // Remove code formatting (`text`)
        .replace(Regex("`(.*?)`"), "$1")
        // Remove code blocks (```text```)
        .replace(Regex("```[\\s\\S]*?```"), "")
        // Remove headers (# ## ### etc.)
        .replace(Regex("^#{1,6}\\s+"), "")
        // Remove links [text](url)
        .replace(Regex("\\[(.*?)\\]\\(.*?\\)"), "$1")
        // Remove strikethrough (~~text~~)
        .replace(Regex("~~(.*?)~~"), "$1")
        // Remove blockquotes (> text)
        .replace(Regex("^>\\s+", RegexOption.MULTILINE), "")
        // Remove horizontal rules (--- or ***)
        .replace(Regex("^[-*]{3,}$", RegexOption.MULTILINE), "")
        // Clean up extra whitespace
        .replace(Regex("\\n{3,}"), "\n\n")
        .trim()
}

private fun parseTimestamp(timestamp: String): Long {
    return try {
        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        format.parse(timestamp)?.time ?: System.currentTimeMillis()
    } catch (e: Exception) {
        System.currentTimeMillis()
    }
}

private fun formatMessageTime(timestamp: Long): String {
    val format = SimpleDateFormat("HH:mm", Locale.getDefault())
    return format.format(Date(timestamp))
}

@Preview(showBackground = true, name = "Chat Screen Light")
@Composable
fun ChatScreenLightPreview() {
    PawspectiveTheme(darkTheme = false) {
        // Preview with mock messages
        val mockMessages = listOf(
            ChatMessage(
                content = "Hello! I'm looking for advice on adopting a dog.",
                isFromUser = true
            ),
            ChatMessage(
                content = "I'd be happy to help you with dog adoption advice! What specific questions do you have about adopting a dog?",
                isFromUser = false
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            mockMessages.forEach { message ->
                MessageBubble(
                    message = message,
                    isFromUser = message.isFromUser
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Preview(showBackground = true, name = "Chat Screen Dark")
@Composable
fun ChatScreenDarkPreview() {
    PawspectiveTheme(darkTheme = true) {
        // Preview with mock messages
        val mockMessages = listOf(
            ChatMessage(
                content = "Hello! I'm looking for advice on adopting a dog.",
                isFromUser = true
            ),
            ChatMessage(
                content = "I'd be happy to help you with dog adoption advice! What specific questions do you have about adopting a dog?",
                isFromUser = false
            )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            mockMessages.forEach { message ->
                MessageBubble(
                    message = message,
                    isFromUser = message.isFromUser
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
} 