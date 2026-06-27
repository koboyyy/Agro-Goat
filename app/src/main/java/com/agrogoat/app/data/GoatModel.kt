package com.agrogoat.app.data

import java.util.UUID
import androidx.annotation.DrawableRes

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
    val weight: Int,
    val age: Double,
    val price: Long,
    val location: String = "Bengkalis",
    val description: String,
    val isNew: Boolean = false,
    val isFavorite: Boolean = false,
    @DrawableRes val imageRes: Int? = null,
    val imageUri: String? = null,
    val sellerEmail: String? = null,
    val sellerUid: String? = null
)

data class ChatRoom(
    val id: String,
    val name: String,
    val lastMessage: String,
    val initials: String,
    val hasCheckmark: Boolean,
    val hasBorder: Boolean = false,
    val sellerEmail: String,
    val buyerEmail: String = "",
    val participants: List<String> = emptyList()
)

enum class OrderStatus(val displayName: String, val stepIndex: Int) {
    PENDING_PAYMENT("Menunggu Konfirmasi", 0),
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
    val orderDate: String,
    val buyerUid: String? = null,
    val buyerName: String? = null,
    val buyerPhone: String? = null,
    val buyerEmail: String? = null,
    val buyerNotes: String? = null,
    val bookingDate: String? = null,
    val bookingTimeSlot: String? = null
)

data class MessageItem(
    val id: String = UUID.randomUUID().toString(),
    val chatRoomId: String = "",
    val content: String,
    val sender: MessageSender,
    val timestamp: String,
    val senderEmail: String = "",
    val participants: List<String> = emptyList()
)

enum class MessageSender { USER, SYSTEM, BREEDER_ETAWA, BREEDER_POTONG }

enum class NotificationType { ORDER_STATUS, PROMO, SYSTEM }

data class NotificationItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val message: String,
    val type: NotificationType,
    val timestamp: String,
    val isRead: Boolean = false,
    val userId: String? = null
)

// Mapping helpers
fun GoatItem.toMap(): Map<String, Any?> = mapOf(
    "id" to id, "name" to name, "category" to category.name, "gender" to gender,
    "weight" to weight.toLong(), "age" to age, "price" to price, "location" to location,
    "description" to description, "isNew" to isNew, "isFavorite" to isFavorite,
    "imageRes" to imageRes?.toLong(), "imageUri" to imageUri, "sellerEmail" to sellerEmail, "sellerUid" to sellerUid
)

@Suppress("UNCHECKED_CAST")
fun mapToGoatItem(map: Map<String, Any?>): GoatItem = GoatItem(
    id = map["id"] as? String ?: UUID.randomUUID().toString(),
    name = map["name"] as? String ?: "",
    category = try { GoatCategory.valueOf(map["category"] as? String ?: "ETAWA") } catch(e: Exception) { GoatCategory.ETAWA },
    gender = map["gender"] as? String ?: "Jantan",
    weight = (map["weight"] as? Long ?: 0L).toInt(),
    age = when(val ageVal = map["age"]) { 
        is Double -> ageVal 
        is Long -> ageVal.toDouble() 
        else -> 0.0 
    },
    price = map["price"] as? Long ?: 0L,
    location = map["location"] as? String ?: "Bengkalis",
    description = map["description"] as? String ?: "",
    isNew = map["isNew"] as? Boolean ?: false,
    isFavorite = map["isFavorite"] as? Boolean ?: false,
    imageRes = (map["imageRes"] as? Long)?.toInt(),
    imageUri = map["imageUri"] as? String,
    sellerEmail = map["sellerEmail"] as? String,
    sellerUid = map["sellerUid"] as? String
)

fun MessageItem.toMap(): Map<String, Any?> = mapOf(
    "id" to id, "chatRoomId" to chatRoomId, "content" to content, "sender" to sender.name,
    "timestamp" to timestamp, "senderEmail" to senderEmail, "participants" to participants
)

@Suppress("UNCHECKED_CAST")
fun mapToMessageItem(map: Map<String, Any?>): MessageItem = MessageItem(
    id = map["id"] as? String ?: UUID.randomUUID().toString(),
    chatRoomId = map["chatRoomId"] as? String ?: "",
    content = map["content"] as? String ?: "",
    sender = try { MessageSender.valueOf(map["sender"] as? String ?: "SYSTEM") } catch(e: Exception) { MessageSender.SYSTEM },
    timestamp = map["timestamp"] as? String ?: "",
    senderEmail = map["senderEmail"] as? String ?: "",
    participants = (map["participants"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
)

fun OrderItem.toMap(): Map<String, Any?> = mapOf(
    "id" to id, "goat" to goat.toMap(), "selectedWeight" to selectedWeight.toLong(),
    "totalPrice" to totalPrice, "status" to status.name, "orderDate" to orderDate, "buyerUid" to buyerUid,
    "buyerName" to buyerName, "buyerPhone" to buyerPhone, "buyerEmail" to buyerEmail, "buyerNotes" to buyerNotes,
    "bookingDate" to bookingDate, "bookingTimeSlot" to bookingTimeSlot
)

@Suppress("UNCHECKED_CAST")
fun mapToOrderItem(map: Map<String, Any?>): OrderItem = OrderItem(
    id = map["id"] as? String ?: UUID.randomUUID().toString(),
    goat = mapToGoatItem(map["goat"] as? Map<String, Any?> ?: emptyMap()),
    selectedWeight = (map["selectedWeight"] as? Long ?: 0L).toInt(),
    totalPrice = map["totalPrice"] as? Long ?: 0L,
    status = try { OrderStatus.valueOf(map["status"] as? String ?: "PENDING_PAYMENT") } catch(e: Exception) { OrderStatus.PENDING_PAYMENT },
    orderDate = map["orderDate"] as? String ?: "",
    buyerUid = map["buyerUid"] as? String,
    buyerName = map["buyerName"] as? String,
    buyerPhone = map["buyerPhone"] as? String,
    buyerEmail = map["buyerEmail"] as? String,
    buyerNotes = map["buyerNotes"] as? String,
    bookingDate = map["bookingDate"] as? String,
    bookingTimeSlot = map["bookingTimeSlot"] as? String
)

fun NotificationItem.toMap(): Map<String, Any?> = mapOf(
    "id" to id, "title" to title, "message" to message, "type" to type.name,
    "timestamp" to timestamp, "isRead" to isRead, "userId" to userId
)

@Suppress("UNCHECKED_CAST")
fun mapToNotificationItem(map: Map<String, Any?>): NotificationItem = NotificationItem(
    id = map["id"] as? String ?: UUID.randomUUID().toString(),
    title = map["title"] as? String ?: "",
    message = map["message"] as? String ?: "",
    type = try { NotificationType.valueOf(map["type"] as? String ?: "SYSTEM") } catch(e: Exception) { NotificationType.SYSTEM },
    timestamp = map["timestamp"] as? String ?: "",
    isRead = map["isRead"] as? Boolean ?: false,
    userId = map["userId"] as? String
)
