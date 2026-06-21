package com.anna.gamingcafe.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String = "",
    @SerialName("full_name") val fullName: String = "",
    val email: String? = null,
    val phone: String? = null,
    val role: String = "CUSTOMER",
    @SerialName("loyalty_stars") val loyaltyStars: Int = 0,
    @SerialName("wallet_balance") val walletBalance: Double = 0.0,
    @SerialName("referral_code") val referralCode: String? = null,
    @SerialName("referred_by") val referredBy: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("total_visits") val totalVisits: Int = 0,
    @SerialName("total_spend") val totalSpend: Double = 0.0,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("last_visit_at") val lastVisitAt: String? = null
)

@Serializable
data class Station(
    val id: String = "",
    val name: String = "",
    val type: String = "PS5",
    val status: String = "AVAILABLE",
    @SerialName("price_per_hour") val pricePerHour: Double = 0.0,
    val specs: String = "",
    @SerialName("display_order") val displayOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Game(
    val id: String = "",
    val title: String = "",
    val platform: String = "ALL",
    val category: String = "",
    @SerialName("cover_image_url") val coverImageUrl: String? = null,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class GamingPackage(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    @SerialName("duration_hours") val durationHours: Int = 1,
    @SerialName("base_price") val basePrice: Double = 0.0,
    val perks: String = "[]",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("display_order") val displayOrder: Int = 0
)

@Serializable
data class Promotion(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    @SerialName("badge_text") val badgeText: String = "",
    @SerialName("badge_color") val badgeColor: String = "#00E5FF",
    @SerialName("stars_multiplier") val starsMultiplier: Double = 1.0,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
data class Booking(
    val id: String = "",
    @SerialName("booking_ref") val bookingRef: String = "",
    @SerialName("customer_id") val customerId: String = "",
    @SerialName("station_id") val stationId: String = "",
    @SerialName("package_id") val packageId: String? = null,
    @SerialName("booking_date") val bookingDate: String = "",
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    @SerialName("duration_hours") val durationHours: Int = 1,
    @SerialName("total_price") val totalPrice: Double = 0.0,
    val status: String = "PENDING",
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("payment_status") val paymentStatus: String = "PENDING",
    @SerialName("cancellation_reason") val cancellationReason: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    // Joined fields for display
    @SerialName("station_name") val stationName: String? = null,
    @SerialName("customer_name") val customerName: String? = null
)

@Serializable
data class Transaction(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("booking_id") val bookingId: String? = null,
    val type: String = "",
    val amount: Double = 0.0,
    @SerialName("payment_method") val paymentMethod: String? = null,
    @SerialName("upi_transaction_id") val upiTransactionId: String? = null,
    val status: String = "PENDING",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class ChatRoom(
    val id: String = "",
    @SerialName("customer_id") val customerId: String = "",
    val status: String = "OPEN",
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("last_message_at") val lastMessageAt: String? = null,
    // Joined for owner view
    @SerialName("customer_name") val customerName: String? = null
)

@Serializable
data class ChatMessage(
    val id: String = "",
    @SerialName("room_id") val roomId: String = "",
    @SerialName("sender_id") val senderId: String = "",
    val content: String = "",
    @SerialName("attachment_url") val attachmentUrl: String? = null,
    @SerialName("is_read") val isRead: Boolean = false,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Review(
    val id: String = "",
    @SerialName("customer_id") val customerId: String = "",
    @SerialName("booking_id") val bookingId: String = "",
    val rating: Int = 5,
    val comment: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)
