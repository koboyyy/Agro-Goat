package com.example.data

import java.util.UUID

enum class GoatCategory(val displayName: String, val description: String) {
    POTONG("Kambing Potong", "Potong"),
    ETAWA("Kambing Etawa", "Etawa"),
    PERAH("Kambing Perah", "Perah")
}

data class GoatItem(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val category: GoatCategory,
    val gender: String = "Jantan",
    val weight: Int, // in kg
    val age: Double, // in years
    val price: Long, // in IDR
    val location: String = "Bengkalis",
    val description: String,
    val isNew: Boolean = false,
    val isFavorite: Boolean = false
)

enum class OrderStatus(val displayName: String, val stepIndex: Int) {
    PENDING_PAYMENT("Menunggu Pembayaran", 0),
    PACKING("Dikemas", 1),
    SHIPPING("Dikirim", 2),
    COMPLETED("Selesai", 3)
}

data class OrderItem(
    val id: String = UUID.randomUUID().toString(),
    val goat: GoatItem,
    val selectedWeight: Int,
    val totalPrice: Long,
    val status: OrderStatus = OrderStatus.PENDING_PAYMENT,
    val orderDate: String
)

data class MessageItem(
    val id: String = UUID.randomUUID().toString(),
    val content: String,
    val sender: MessageSender,
    val timestamp: String
)

enum class MessageSender {
    USER,
    SYSTEM,
    BREEDER_ETAWA,
    BREEDER_POTONG
}

enum class NotificationType {
    ORDER_STATUS,
    PROMO,
    SYSTEM
}

data class NotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: String,
    val isRead: Boolean = false
)
