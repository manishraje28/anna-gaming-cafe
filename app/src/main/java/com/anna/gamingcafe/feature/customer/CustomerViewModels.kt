package com.anna.gamingcafe.feature.customer

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.anna.gamingcafe.data.model.*
import com.anna.gamingcafe.data.remote.*
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════
// HOME VIEW MODEL
// ═══════════════════════════════════════════════
class HomeViewModel : ViewModel() {
    var profile by mutableStateOf<Profile?>(null); private set
    var promotions by mutableStateOf<List<Promotion>>(emptyList()); private set
    var isLoading by mutableStateOf(true); private set

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            isLoading = true
            try {
                val userId = AuthRepository.getCurrentUserId() ?: return@launch
                profile = ProfileRepository.getProfile(userId)
                promotions = PromotionRepository.getActivePromotions()
            } catch (_: Exception) { }
            isLoading = false
        }
    }
}

// ═══════════════════════════════════════════════
// BOOKING VIEW MODEL
// ═══════════════════════════════════════════════
data class BookingUiState(
    val stations: List<Station> = emptyList(),
    val packages: List<GamingPackage> = emptyList(),
    val selectedPlatform: String = "PS5",
    val selectedDateIndex: Int = 0,
    val selectedPackageIndex: Int = 0,
    val selectedStationId: String = "",
    val selectedPaymentMethod: String = "UPI",
    val isLoading: Boolean = true,
    val isBooking: Boolean = false,
    val confirmedBooking: Booking? = null,
    val error: String? = null,
    val walletBalance: Double = 0.0
)

class BookingViewModel : ViewModel() {
    var uiState by mutableStateOf(BookingUiState()); private set

    val dateOptions = listOf("Today", "Tomorrow", "Day After", "+3 Days", "+4 Days")

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val stations = StationRepository.getAllStations()
                val packages = PackageRepository.getAllPackages()
                val userId = AuthRepository.getCurrentUserId()
                val balance = if (userId != null) ProfileRepository.getProfile(userId)?.walletBalance ?: 0.0 else 0.0
                uiState = uiState.copy(
                    stations = stations,
                    packages = packages,
                    walletBalance = balance,
                    isLoading = false
                )
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun selectPlatform(platform: String) {
        uiState = uiState.copy(selectedPlatform = platform, selectedStationId = "")
    }

    fun selectDate(index: Int) { uiState = uiState.copy(selectedDateIndex = index) }
    fun selectPackage(index: Int) { uiState = uiState.copy(selectedPackageIndex = index) }
    fun selectStation(id: String) { uiState = uiState.copy(selectedStationId = id) }
    fun selectPaymentMethod(method: String) { uiState = uiState.copy(selectedPaymentMethod = method) }

    fun confirmBooking() {
        val stationId = uiState.selectedStationId
        if (stationId.isEmpty()) return
        val pkg = uiState.packages.getOrNull(uiState.selectedPackageIndex) ?: return

        viewModelScope.launch {
            uiState = uiState.copy(isBooking = true, error = null)
            try {
                val userId = AuthRepository.getCurrentUserId() ?: return@launch
                val paymentStatus = if (uiState.selectedPaymentMethod == "WALLET") {
                    if (uiState.walletBalance < pkg.basePrice) {
                        uiState = uiState.copy(isBooking = false, error = "Insufficient wallet balance. Please top up.")
                        return@launch
                    }
                    ProfileRepository.updateWalletBalance(userId, uiState.walletBalance - pkg.basePrice)
                    "PAID"
                } else "PENDING"

                val booking = BookingRepository.createBooking(
                    customerId = userId,
                    stationId = stationId,
                    packageId = pkg.id,
                    bookingDate = dateOptions[uiState.selectedDateIndex],
                    durationHours = pkg.durationHours,
                    totalPrice = pkg.basePrice,
                    paymentMethod = uiState.selectedPaymentMethod,
                    paymentStatus = paymentStatus
                )

                // Update station status
                StationRepository.updateStationStatus(stationId, "BOOKED")

                // Add loyalty stars
                ProfileRepository.addLoyaltyStars(userId, 20 * pkg.durationHours)

                // Create transaction record
                TransactionRepository.createTransaction(
                    userId = userId,
                    type = "BOOKING_PAYMENT",
                    amount = pkg.basePrice,
                    paymentMethod = uiState.selectedPaymentMethod,
                    bookingId = booking.id,
                    status = paymentStatus
                )

                uiState = uiState.copy(isBooking = false, confirmedBooking = booking)
            } catch (e: Exception) {
                uiState = uiState.copy(isBooking = false, error = e.message ?: "Booking failed")
            }
        }
    }

    fun dismissConfirmation() {
        uiState = uiState.copy(confirmedBooking = null)
        loadData()  // Refresh station statuses
    }
}

// ═══════════════════════════════════════════════
// GAMES VIEW MODEL
// ═══════════════════════════════════════════════
class GamesViewModel : ViewModel() {
    var games by mutableStateOf<List<Game>>(emptyList()); private set
    var isLoading by mutableStateOf(true); private set
    var searchQuery by mutableStateOf(""); private set
    var platformFilter by mutableStateOf("ALL"); private set

    init { loadGames() }

    private fun loadGames() {
        viewModelScope.launch {
            isLoading = true
            try { games = GameRepository.getAllGames() } catch (_: Exception) { }
            isLoading = false
        }
    }

    fun updateSearch(query: String) { searchQuery = query }
    fun updatePlatformFilter(platform: String) { platformFilter = platform }

    val filteredGames: List<Game>
        get() = games.filter { game ->
            val matchesSearch = searchQuery.isEmpty() ||
                game.title.contains(searchQuery, ignoreCase = true) ||
                game.category.contains(searchQuery, ignoreCase = true)
            val matchesPlatform = platformFilter == "ALL" || game.platform == "ALL" || game.platform == platformFilter
            matchesSearch && matchesPlatform
        }
}

// ═══════════════════════════════════════════════
// WALLET VIEW MODEL
// ═══════════════════════════════════════════════
class WalletViewModel : ViewModel() {
    var profile by mutableStateOf<Profile?>(null); private set
    var transactions by mutableStateOf<List<Transaction>>(emptyList()); private set
    var bookings by mutableStateOf<List<Booking>>(emptyList()); private set
    var isLoading by mutableStateOf(true); private set
    var selectedTopUp by mutableStateOf(500); private set
    var showTopUpDialog by mutableStateOf(false); private set

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            isLoading = true
            try {
                val userId = AuthRepository.getCurrentUserId() ?: return@launch
                profile = ProfileRepository.getProfile(userId)
                transactions = TransactionRepository.getUserTransactions(userId)
                bookings = BookingRepository.getCustomerBookings(userId)
            } catch (_: Exception) { }
            isLoading = false
        }
    }

    fun selectTopUp(amount: Int) { selectedTopUp = amount }
    fun toggleTopUpDialog() { showTopUpDialog = !showTopUpDialog }

    fun confirmTopUp() {
        viewModelScope.launch {
            try {
                val userId = AuthRepository.getCurrentUserId() ?: return@launch
                val currentBalance = profile?.walletBalance ?: 0.0
                ProfileRepository.updateWalletBalance(userId, currentBalance + selectedTopUp)
                TransactionRepository.createTransaction(
                    userId = userId, type = "WALLET_TOPUP",
                    amount = selectedTopUp.toDouble(), paymentMethod = "UPI"
                )
                showTopUpDialog = false
                loadData()
            } catch (_: Exception) { }
        }
    }
}
