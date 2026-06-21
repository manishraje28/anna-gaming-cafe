package com.anna.gamingcafe.feature.customer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anna.gamingcafe.data.model.ChatMessage
import com.anna.gamingcafe.data.model.ChatRoom
import com.anna.gamingcafe.data.remote.AuthRepository
import com.anna.gamingcafe.data.remote.ChatRepository
import com.anna.gamingcafe.data.remote.SupabaseClient
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.RealtimeChannel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement


class ChatViewModel : ViewModel() {
    var messages by mutableStateOf<List<ChatMessage>>(emptyList()); private set
    var room by mutableStateOf<ChatRoom?>(null); private set
    var isLoading by mutableStateOf(true); private set
    var currentUserId by mutableStateOf(""); private set
    var inputText by mutableStateOf(""); private set
    var isSending by mutableStateOf(false); private set

    private var channel: RealtimeChannel? = null

    init { initialize() }

    private fun initialize() {
        viewModelScope.launch {
            try {
                currentUserId = AuthRepository.getCurrentUserId() ?: return@launch
                room = ChatRepository.getOrCreateRoom(currentUserId)
                room?.let { r ->
                    messages = ChatRepository.getMessages(r.id)
                    ChatRepository.markMessagesAsRead(r.id, currentUserId)
                    subscribeToMessages(r.id)
                }
            } catch (_: Exception) { }
            isLoading = false
        }
    }

    private fun subscribeToMessages(roomId: String) {
        viewModelScope.launch {
            try {
                channel = ChatRepository.subscribeToMessages(roomId)
                val flow = channel!!.postgresChangeFlow<PostgresAction.Insert>(schema = "public") {
                    table = "chat_messages"
                }
                flow.onEach { action ->
                    try {
                        val msg = Json.decodeFromJsonElement<ChatMessage>(action.record)
                        if (msg.roomId == roomId && messages.none { it.id == msg.id }) {
                            messages = messages + msg
                            ChatRepository.markMessagesAsRead(roomId, currentUserId)
                        }
                    } catch (_: Exception) { }
                }.launchIn(viewModelScope)
                channel!!.subscribe()
            } catch (_: Exception) { }
        }
    }


    fun updateInput(text: String) { inputText = text }

    fun sendMessage() {
        val text = inputText.trim()
        if (text.isEmpty() || isSending) return
        val r = room ?: return

        viewModelScope.launch {
            isSending = true
            inputText = ""
            try {
                val msg = ChatRepository.sendMessage(r.id, currentUserId, text)
                if (messages.none { it.id == msg.id }) {
                    messages = messages + msg
                }
            } catch (_: Exception) { inputText = text }
            isSending = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            try { channel?.unsubscribe() } catch (_: Exception) { }
        }
    }
}
