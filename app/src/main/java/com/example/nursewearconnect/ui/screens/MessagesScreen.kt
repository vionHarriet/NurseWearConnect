package com.example.nursewearconnect.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nursewearconnect.ui.viewmodel.HomeViewModel
import com.example.nursewearconnect.ui.theme.*

data class Message(
    val id: Int,
    val text: String,
    val isFromMe: Boolean,
    val time: String
)

data class ChatPreview(
    val id: Int,
    val senderName: String,
    val lastMessage: String,
    val time: String,
    val unreadCount: Int = 0,
    val avatarEmoji: String,
    val messages: List<Message> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MessagesScreen(onBackClick: () -> Unit, viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedChatId by remember { mutableStateOf<Int?>(null) }
    
    // Transform Map to ChatPreview for the UI
    val chats = uiState.messages.map { msgMap ->
        ChatPreview(
            id = (msgMap["id"] as? Double)?.toInt() ?: 0,
            senderName = msgMap["senderName"] as? String ?: "Unknown",
            lastMessage = msgMap["text"] as? String ?: "",
            time = msgMap["time"] as? String ?: "",
            unreadCount = (msgMap["unreadCount"] as? Double)?.toInt() ?: 0,
            avatarEmoji = msgMap["avatarEmoji"] as? String ?: "👤"
        )
    }.ifEmpty {
        listOf(
            ChatPreview(1, "Elite Scrubs Vendor", "Sure, I can customize the embroidery for you.", "10:30 AM", 2, "🏪", listOf(
                Message(1, "Hello, can I customize the embroidery?", false, "10:25 AM"),
                Message(2, "Sure, I can customize the embroidery for you.", true, "10:30 AM")
            )),
            ChatPreview(2, "Support Team", "Your inquiry has been resolved.", "Yesterday", 0, "💬"),
            ChatPreview(3, "Modern Med Wear", "We have the blue sets back in stock.", "Mon", 0, "👕"),
            ChatPreview(4, "Dr. Michael Chen", "How do these fit compared to trends?", "Sun", 1, "👨‍⚕️")
        )
    }
    
    if (selectedChatId != null) {
        val chat = chats.find { it.id == selectedChatId }
        if (chat != null) {
            ChatDetailScreen(
                chat = chat,
                onBack = { selectedChatId = null },
                onSendMessage = { text ->
                    viewModel.sendMessage(text)
                    selectedChatId = null // Simple UI response for now
                }
            )
        }
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Messages", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { /* Search chats */ }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            },
            containerColor = Color.White
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(chats) { chat ->
                    ChatRow(chat) { selectedChatId = chat.id }
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp, color = Slate100)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatDetailScreen(chat: ChatPreview, onBack: () -> Unit, onSendMessage: (String) -> Unit) {
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(36.dp), shape = CircleShape, color = Slate50) {
                            Box(contentAlignment = Alignment.Center) { Text(chat.avatarEmoji, fontSize = 20.sp) }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(chat.senderName, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Surface(tonalElevation = 2.dp, color = Color.White) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        colors = TextFieldDefaults.colors(
                            unfocusedContainerColor = Slate50,
                            focusedContainerColor = Slate50,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    FloatingActionButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSendMessage(messageText)
                                messageText = ""
                            }
                        },
                        modifier = Modifier.size(48.dp),
                        shape = CircleShape,
                        containerColor = Brand600,
                        contentColor = Color.White
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(chat.messages) { msg ->
                MessageBubble(msg)
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (message.isFromMe) Alignment.End else Alignment.Start
    ) {
        Surface(
            color = if (message.isFromMe) Brand600 else Slate100,
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromMe) 16.dp else 0.dp,
                bottomEnd = if (message.isFromMe) 0.dp else 16.dp
            )
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                color = if (message.isFromMe) Color.White else Slate900,
                fontSize = 14.sp
            )
        }
        Text(
            text = message.time,
            fontSize = 10.sp,
            color = Slate400,
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}


@Composable
fun ChatRow(chat: ChatPreview, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(Slate50, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(chat.avatarEmoji, fontSize = 28.sp)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.senderName,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = chat.time,
                        fontSize = 12.sp,
                        color = if (chat.unreadCount > 0) Brand600 else Slate400,
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
                
                Spacer(Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chat.lastMessage,
                        fontSize = 14.sp,
                        color = if (chat.unreadCount > 0) Slate900 else Slate500,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                        fontWeight = if (chat.unreadCount > 0) FontWeight.Medium else FontWeight.Normal
                    )
                    
                    if (chat.unreadCount > 0) {
                        Surface(
                            modifier = Modifier.size(20.dp),
                            shape = CircleShape,
                            color = Brand500
                        ) {
                            Text(
                                text = chat.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.wrapContentSize()
                            )
                        }
                    }
                }
            }
        }
    }
}
