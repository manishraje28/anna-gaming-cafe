package com.anna.gamingcafe.feature.customer

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anna.gamingcafe.core.components.LoadingScreen
import com.anna.gamingcafe.core.theme.*
import com.anna.gamingcafe.data.model.ChatMessage
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: ChatViewModel) {
    if (viewModel.isLoading) { LoadingScreen(); return }

    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    // Auto-scroll on new messages
    LaunchedEffect(viewModel.messages.size) {
        if (viewModel.messages.isNotEmpty()) {
            listState.animateScrollToItem(viewModel.messages.size - 1)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().imePadding()
    ) {
        // Chat Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = SurfaceCard,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(36.dp).background(NeonCyan.copy(0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SupportAgent, null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Anna Support", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).background(LiveGreen, CircleShape))
                        Spacer(Modifier.width(4.dp))
                        Text("Online", color = LiveGreen, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Messages
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth().background(BackBg),
            state = listState,
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Welcome message if empty
            if (viewModel.messages.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(Modifier.size(56.dp).background(NeonCyan.copy(0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Forum, null, tint = NeonCyan, modifier = Modifier.size(28.dp))
                            }
                            Spacer(Modifier.height(12.dp))
                            Text("Start a conversation", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Ask us anything about gaming or bookings", color = TextSecondary, fontSize = 11.sp,
                                modifier = Modifier.padding(top = 4.dp), textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            items(viewModel.messages, key = { it.id }) { msg ->
                ChatBubble(
                    message = msg,
                    isMe = msg.senderId == viewModel.currentUserId
                )
            }
        }

        // Quick Replies
        if (viewModel.messages.size < 2) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("Is a PS5 free?", "Booking help", "Promotions?").forEach { q ->
                    Box(
                        Modifier.background(SurfaceElevated, RoundedCornerShape(16.dp))
                            .border(1.dp, EdgeBorder, RoundedCornerShape(16.dp))
                            .clickable { viewModel.updateInput(q) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(q, color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Input Bar
        Surface(color = SurfaceCard, shadowElevation = 4.dp) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                // Text Input
                OutlinedTextField(
                    value = viewModel.inputText,
                    onValueChange = viewModel::updateInput,
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Type a message...", color = TextMuted, fontSize = 14.sp) },
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = SurfaceElevated,
                        unfocusedContainerColor = SurfaceElevated,
                        cursorColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(22.dp)
                )

                Spacer(Modifier.width(6.dp))

                // Send Button
                val canSend = viewModel.inputText.isNotBlank() && !viewModel.isSending
                Box(
                    modifier = Modifier.size(44.dp)
                        .clip(CircleShape)
                        .background(if (canSend) NeonCyan else NeonCyan.copy(0.3f))
                        .clickable(enabled = canSend) {
                            viewModel.sendMessage()
                            scope.launch {
                                if (viewModel.messages.isNotEmpty()) {
                                    listState.animateScrollToItem(viewModel.messages.size - 1)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (viewModel.isSending) {
                        CircularProgressIndicator(Modifier.size(18.dp), color = BackBg, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.AutoMirrored.Filled.Send, null, tint = BackBg, modifier = Modifier.size(20.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage, isMe: Boolean) {
    val bubbleColor = if (isMe) NeonCyan.copy(0.12f) else SurfaceCard
    val borderColor = if (isMe) NeonCyan.copy(0.3f) else EdgeBorder
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp, topEnd = 16.dp,
        bottomStart = if (isMe) 16.dp else 4.dp,
        bottomEnd = if (isMe) 4.dp else 16.dp
    )

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        // Avatar for support
        if (!isMe) {
            Box(
                Modifier.size(28.dp).clip(CircleShape).background(NeonCyan.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.SupportAgent, null, tint = NeonCyan, modifier = Modifier.size(14.dp))
            }
            Spacer(Modifier.width(6.dp))
        }

        Surface(
            modifier = Modifier.widthIn(max = 280.dp),
            color = bubbleColor,
            border = BorderStroke(0.5.dp, borderColor),
            shape = bubbleShape,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                // Attachment
                message.attachmentUrl?.let {
                    Box(
                        Modifier.fillMaxWidth().height(80.dp).background(BackBg, RoundedCornerShape(8.dp))
                            .border(1.dp, EdgeBorder, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Image, null, tint = NeonCyan, modifier = Modifier.size(24.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                }

                Text(message.content, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)

                // Timestamp + read
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val time = message.createdAt?.let {
                        try { it.substringAfter("T").take(5) } catch (_: Exception) { "" }
                    } ?: ""
                    Text(time, color = TextMuted, fontSize = 9.sp)

                    if (isMe) {
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            if (message.isRead) Icons.Default.DoneAll else Icons.Default.Done,
                            null, tint = if (message.isRead) NeonCyan else TextMuted, modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }

        if (isMe) Spacer(Modifier.width(4.dp))
    }
}
