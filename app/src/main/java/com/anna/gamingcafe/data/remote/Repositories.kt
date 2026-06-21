package com.anna.gamingcafe.data.remote

import com.anna.gamingcafe.data.model.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.realtime.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

private val supabase = SupabaseClient.client

// ═══════════════════════════════════════════════
// AUTH REPOSITORY
// ═══════════════════════════════════════════════
object AuthRepository {

    suspend fun sendOtp(phone: String) {
        supabase.auth.signInWith(OTP) {
            this.phone = phone
        }
    }

    suspend fun verifyOtp(phone: String, token: String) {
        supabase.auth.verifyPhoneOtp(
            type = OtpType.Phone.SMS,
            phone = phone,
            token = token
        )
    }

    suspend fun signUpWithEmail(email: String, password: String, fullName: String) {
        supabase.auth.signUpWith(io.github.jan.supabase.auth.providers.builtin.Email) {
            this.email = email
            this.password = password
            this.data = buildJsonObject { put("full_name", fullName) }
        }
    }

    suspend fun signInWithEmail(email: String, password: String) {
        supabase.auth.signInWith(io.github.jan.supabase.auth.providers.builtin.Email) {
            this.email = email
            this.password = password
        }
    }

    fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    fun isLoggedIn(): Boolean {
        return supabase.auth.currentUserOrNull() != null
    }

    suspend fun signOut() {
        supabase.auth.signOut()
    }
}

// ═══════════════════════════════════════════════
// PROFILE REPOSITORY
// ═══════════════════════════════════════════════
object ProfileRepository {

    suspend fun getProfile(userId: String): Profile? {
        return try {
            supabase.postgrest.from("profiles")
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<Profile>()
        } catch (e: Exception) { null }
    }

    suspend fun updateProfile(userId: String, fullName: String) {
        supabase.postgrest.from("profiles")
            .update({ set("full_name", fullName) }) { filter { eq("id", userId) } }
    }

    suspend fun updateWalletBalance(userId: String, newBalance: Double) {
        supabase.postgrest.from("profiles")
            .update({ set("wallet_balance", newBalance) }) { filter { eq("id", userId) } }
    }

    suspend fun addLoyaltyStars(userId: String, stars: Int) {
        val profile = getProfile(userId) ?: return
        supabase.postgrest.from("profiles")
            .update({ set("loyalty_stars", profile.loyaltyStars + stars) }) { filter { eq("id", userId) } }
    }

    suspend fun getAllCustomers(): List<Profile> {
        return supabase.postgrest.from("profiles")
            .select { filter { eq("role", "CUSTOMER") } }
            .decodeList<Profile>()
    }

    suspend fun makeOwner(userId: String) {
        supabase.postgrest.from("profiles")
            .update({ set("role", "OWNER") }) { filter { eq("id", userId) } }
    }
}

// ═══════════════════════════════════════════════
// STATION REPOSITORY
// ═══════════════════════════════════════════════
object StationRepository {

    suspend fun getAllStations(): List<Station> {
        return supabase.postgrest.from("stations")
            .select { filter { eq("is_active", true) }; order("display_order", Order.ASCENDING) }
            .decodeList<Station>()
    }

    suspend fun getStationsByType(type: String): List<Station> {
        return supabase.postgrest.from("stations")
            .select { filter { eq("type", type); eq("is_active", true) }; order("display_order", Order.ASCENDING) }
            .decodeList<Station>()
    }

    suspend fun updateStationStatus(stationId: String, status: String) {
        supabase.postgrest.from("stations")
            .update({ set("status", status) }) { filter { eq("id", stationId) } }
    }

    suspend fun createStation(name: String, type: String, pricePerHour: Double, specs: String) {
        supabase.postgrest.from("stations")
            .insert(buildJsonObject {
                put("name", name)
                put("type", type)
                put("price_per_hour", pricePerHour)
                put("specs", specs)
                put("status", "AVAILABLE")
            })
    }

    suspend fun deleteStation(stationId: String) {
        supabase.postgrest.from("stations")
            .update({ set("is_active", false) }) { filter { eq("id", stationId) } }
    }
}

// ═══════════════════════════════════════════════
// GAME REPOSITORY
// ═══════════════════════════════════════════════
object GameRepository {

    suspend fun getAllGames(): List<Game> {
        return supabase.postgrest.from("games")
            .select { filter { eq("is_active", true) } }
            .decodeList<Game>()
    }

    suspend fun getGamesByPlatform(platform: String): List<Game> {
        return if (platform == "ALL") getAllGames()
        else supabase.postgrest.from("games")
            .select { filter { eq("is_active", true) } }
            .decodeList<Game>()
            .filter { it.platform == platform || it.platform == "ALL" }
    }
}

// ═══════════════════════════════════════════════
// PACKAGE REPOSITORY
// ═══════════════════════════════════════════════
object PackageRepository {

    suspend fun getAllPackages(): List<GamingPackage> {
        return supabase.postgrest.from("packages")
            .select { filter { eq("is_active", true) }; order("display_order", Order.ASCENDING) }
            .decodeList<GamingPackage>()
    }
}

// ═══════════════════════════════════════════════
// PROMOTION REPOSITORY
// ═══════════════════════════════════════════════
object PromotionRepository {

    suspend fun getActivePromotions(): List<Promotion> {
        return supabase.postgrest.from("promotions")
            .select { filter { eq("is_active", true) } }
            .decodeList<Promotion>()
    }
}

// ═══════════════════════════════════════════════
// BOOKING REPOSITORY
// ═══════════════════════════════════════════════
object BookingRepository {

    suspend fun createBooking(
        customerId: String,
        stationId: String,
        packageId: String?,
        bookingDate: String,
        durationHours: Int,
        totalPrice: Double,
        paymentMethod: String,
        paymentStatus: String = "PENDING"
    ): Booking {
        val ref = "ANNA-B${(1000..9999).random()}"
        return supabase.postgrest.from("bookings")
            .insert(buildJsonObject {
                put("booking_ref", ref)
                put("customer_id", customerId)
                put("station_id", stationId)
                if (packageId != null) put("package_id", packageId)
                put("booking_date", bookingDate)
                put("duration_hours", durationHours)
                put("total_price", totalPrice)
                put("payment_method", paymentMethod)
                put("payment_status", paymentStatus)
                put("status", if (paymentStatus == "PAID") "CONFIRMED" else "PENDING")
            }) { select() }
            .decodeSingle<Booking>()
    }

    suspend fun getCustomerBookings(customerId: String): List<Booking> {
        return supabase.postgrest.from("bookings")
            .select { filter { eq("customer_id", customerId) }; order("created_at", Order.DESCENDING) }
            .decodeList<Booking>()
    }

    suspend fun getAllBookings(): List<Booking> {
        return supabase.postgrest.from("bookings")
            .select { order("created_at", Order.DESCENDING) }
            .decodeList<Booking>()
    }

    suspend fun updateBookingStatus(bookingId: String, status: String) {
        supabase.postgrest.from("bookings")
            .update({ set("status", status) }) { filter { eq("id", bookingId) } }
    }

    suspend fun updatePaymentStatus(bookingId: String, paymentStatus: String) {
        supabase.postgrest.from("bookings")
            .update({ set("payment_status", paymentStatus) }) { filter { eq("id", bookingId) } }
    }
}

// ═══════════════════════════════════════════════
// TRANSACTION REPOSITORY
// ═══════════════════════════════════════════════
object TransactionRepository {

    suspend fun createTransaction(
        userId: String,
        type: String,
        amount: Double,
        paymentMethod: String,
        bookingId: String? = null,
        status: String = "SUCCESS"
    ) {
        supabase.postgrest.from("transactions")
            .insert(buildJsonObject {
                put("user_id", userId)
                put("type", type)
                put("amount", amount)
                put("payment_method", paymentMethod)
                if (bookingId != null) put("booking_id", bookingId)
                put("status", status)
            })
    }

    suspend fun getUserTransactions(userId: String): List<Transaction> {
        return supabase.postgrest.from("transactions")
            .select { filter { eq("user_id", userId) }; order("created_at", Order.DESCENDING) }
            .decodeList<Transaction>()
    }

    suspend fun getAllTransactions(): List<Transaction> {
        return supabase.postgrest.from("transactions")
            .select { order("created_at", Order.DESCENDING) }
            .decodeList<Transaction>()
    }
}

// ═══════════════════════════════════════════════
// CHAT REPOSITORY
// ═══════════════════════════════════════════════
object ChatRepository {

    suspend fun getOrCreateRoom(customerId: String): ChatRoom {
        // Try to find existing open room
        val existing = supabase.postgrest.from("chat_rooms")
            .select { filter { eq("customer_id", customerId); eq("status", "OPEN") } }
            .decodeList<ChatRoom>()
            .firstOrNull()

        if (existing != null) return existing

        // Create new room
        return supabase.postgrest.from("chat_rooms")
            .insert(buildJsonObject {
                put("customer_id", customerId)
                put("status", "OPEN")
            }) { select() }
            .decodeSingle<ChatRoom>()
    }

    suspend fun getAllOpenRooms(): List<ChatRoom> {
        return supabase.postgrest.from("chat_rooms")
            .select { filter { eq("status", "OPEN") }; order("last_message_at", Order.DESCENDING) }
            .decodeList<ChatRoom>()
    }

    suspend fun getMessages(roomId: String): List<ChatMessage> {
        return supabase.postgrest.from("chat_messages")
            .select { filter { eq("room_id", roomId) }; order("created_at", Order.ASCENDING) }
            .decodeList<ChatMessage>()
    }

    suspend fun sendMessage(roomId: String, senderId: String, content: String, attachmentUrl: String? = null): ChatMessage {
        // Update room's last message timestamp
        supabase.postgrest.from("chat_rooms")
            .update({ set("last_message_at", "now()") }) { filter { eq("id", roomId) } }

        return supabase.postgrest.from("chat_messages")
            .insert(buildJsonObject {
                put("room_id", roomId)
                put("sender_id", senderId)
                put("content", content)
                if (attachmentUrl != null) put("attachment_url", attachmentUrl)
            }) { select() }
            .decodeSingle<ChatMessage>()
    }

    suspend fun markMessagesAsRead(roomId: String, readerId: String) {
        supabase.postgrest.from("chat_messages")
            .update({ set("is_read", true) }) {
                filter {
                    eq("room_id", roomId)
                    neq("sender_id", readerId)
                    eq("is_read", false)
                }
            }
    }

    fun subscribeToMessages(roomId: String): RealtimeChannel {
        return supabase.realtime.channel("chat-$roomId")
    }
}
