package com.anna.gamingcafe.feature.owner

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anna.gamingcafe.data.model.*
import com.anna.gamingcafe.data.remote.*
import kotlinx.coroutines.launch

class OwnerDashboardViewModel : ViewModel() {
    var bookings by mutableStateOf<List<Booking>>(emptyList()); private set
    var stations by mutableStateOf<List<Station>>(emptyList()); private set
    var customers by mutableStateOf<List<Profile>>(emptyList()); private set
    var transactions by mutableStateOf<List<Transaction>>(emptyList()); private set
    var chatRooms by mutableStateOf<List<ChatRoom>>(emptyList()); private set
    var isLoading by mutableStateOf(true); private set

    val todayRevenue: Double get() = transactions.filter { it.status == "SUCCESS" }.sumOf { it.amount }
    val activeBookings: Int get() = bookings.count { it.status in listOf("CONFIRMED", "ACTIVE") }
    val availableStations: Int get() = stations.count { it.status == "AVAILABLE" }

    init { loadAll() }

    fun loadAll() {
        viewModelScope.launch {
            isLoading = true
            try {
                bookings = BookingRepository.getAllBookings()
                stations = StationRepository.getAllStations()
                customers = ProfileRepository.getAllCustomers()
                transactions = TransactionRepository.getAllTransactions()
                chatRooms = ChatRepository.getAllOpenRooms()
            } catch (_: Exception) { }
            isLoading = false
        }
    }

    fun updateBookingStatus(bookingId: String, status: String) {
        viewModelScope.launch {
            try {
                BookingRepository.updateBookingStatus(bookingId, status)
                if (status == "COMPLETED" || status == "CANCELLED") {
                    val bk = bookings.find { it.id == bookingId }
                    if (bk != null) StationRepository.updateStationStatus(bk.stationId, "AVAILABLE")
                }
                loadAll()
            } catch (_: Exception) { }
        }
    }

    fun updatePaymentStatus(bookingId: String, paymentStatus: String) {
        viewModelScope.launch {
            try {
                BookingRepository.updatePaymentStatus(bookingId, paymentStatus)
                if (paymentStatus == "PAID") BookingRepository.updateBookingStatus(bookingId, "CONFIRMED")
                loadAll()
            } catch (_: Exception) { }
        }
    }

    fun updateStationStatus(stationId: String, status: String) {
        viewModelScope.launch {
            try { StationRepository.updateStationStatus(stationId, status); loadAll() } catch (_: Exception) { }
        }
    }

    fun addStation(name: String, type: String, price: Double, specs: String) {
        viewModelScope.launch {
            try { StationRepository.createStation(name, type, price, specs); loadAll() } catch (_: Exception) { }
        }
    }
}

class OwnerChatViewModel : ViewModel() {
    var rooms by mutableStateOf<List<ChatRoom>>(emptyList()); private set
    var selectedRoom by mutableStateOf<ChatRoom?>(null); private set
    var messages by mutableStateOf<List<ChatMessage>>(emptyList()); private set
    var inputText by mutableStateOf(""); private set
    var isLoading by mutableStateOf(true); private set
    var currentUserId by mutableStateOf(""); private set

    init { loadRooms() }

    fun loadRooms() {
        viewModelScope.launch {
            isLoading = true
            try {
                currentUserId = AuthRepository.getCurrentUserId() ?: ""
                rooms = ChatRepository.getAllOpenRooms()
            } catch (_: Exception) { }
            isLoading = false
        }
    }

    fun selectRoom(room: ChatRoom) {
        selectedRoom = room
        viewModelScope.launch {
            try {
                messages = ChatRepository.getMessages(room.id)
                ChatRepository.markMessagesAsRead(room.id, currentUserId)
            } catch (_: Exception) { }
        }
    }

    fun backToList() { selectedRoom = null; messages = emptyList() }

    fun updateInput(text: String) { inputText = text }

    fun sendReply() {
        val text = inputText.trim()
        if (text.isEmpty()) return
        val r = selectedRoom ?: return
        viewModelScope.launch {
            inputText = ""
            try {
                val msg = ChatRepository.sendMessage(r.id, currentUserId, text)
                messages = messages + msg
            } catch (_: Exception) { inputText = text }
        }
    }
}
