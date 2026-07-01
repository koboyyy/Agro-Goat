package com.agrogoat.core.shared

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrogoat.core.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.*
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

enum class ChatScreenState { LIST, DETAIL }

enum class AppTab {
    BERANDA, KATALOG, CHAT, PESANAN, PROFIL, LACAK_PESANAN, NOTIFIKASI, PEMBAYARAN, INFORMASI_AKUN, PENGATURAN, UBAH_KATA_SANDI
}

enum class CatalogSort {
    TERBARU, HARGA_RENDAH, HARGA_TINGGI, BOBOT_TERBESAR
}

@HiltViewModel
class AgroGoatViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    val db: FirebaseFirestore
) : ViewModel() {

    // Auth State
    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser.asStateFlow()

    // User details
    private val _userName = MutableStateFlow("")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userBalance = MutableStateFlow(0L)
    val userBalance: StateFlow<Long> = _userBalance.asStateFlow()

    private val _userAddress = MutableStateFlow("")
    val userAddress: StateFlow<String> = _userAddress.asStateFlow()

    private val _userRole = MutableStateFlow("Pedagang")
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _userPhone = MutableStateFlow("")
    val userPhone: StateFlow<String> = _userPhone.asStateFlow()

    private val _userFarmName = MutableStateFlow("")
    val userFarmName: StateFlow<String> = _userFarmName.asStateFlow()

    private val _userBio = MutableStateFlow("")
    val userBio: StateFlow<String> = _userBio.asStateFlow()

    private val _userPhotoUrl = MutableStateFlow("")
    val userPhotoUrl: StateFlow<String> = _userPhotoUrl.asStateFlow()

    private val _userLocationLat = MutableStateFlow<Double?>(null)
    val userLocationLat: StateFlow<Double?> = _userLocationLat.asStateFlow()

    private val _userLocationLng = MutableStateFlow<Double?>(null)
    val userLocationLng: StateFlow<Double?> = _userLocationLng.asStateFlow()

    private val _userMapsUrl = MutableStateFlow("")
    val userMapsUrl: StateFlow<String> = _userMapsUrl.asStateFlow()

    // Data lists
    private val _goats = MutableStateFlow<List<GoatItem>>(emptyList())
    val goats: StateFlow<List<GoatItem>> = _goats.asStateFlow()

    private val _myGoats = MutableStateFlow<List<GoatItem>>(emptyList())
    val myGoats: StateFlow<List<GoatItem>> = _myGoats.asStateFlow()

    private val _orders = MutableStateFlow<List<OrderItem>>(emptyList())
    val orders: StateFlow<List<OrderItem>> = _orders.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageItem>>(emptyList())
    val messages: StateFlow<List<MessageItem>> = _messages.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms.asStateFlow()

    private val _usersProfiles = MutableStateFlow<Map<String, Map<String, Any?>>>(emptyMap())
    val usersProfiles: StateFlow<Map<String, Map<String, Any?>>> = _usersProfiles.asStateFlow()

    private val _chatScreenState = MutableStateFlow(ChatScreenState.LIST)
    val chatScreenState: StateFlow<ChatScreenState> = _chatScreenState.asStateFlow()

    private val _selectedChatRoom = MutableStateFlow<ChatRoom?>(null)
    val selectedChatRoom: StateFlow<ChatRoom?> = _selectedChatRoom.asStateFlow()

    private val _activeChatRoomId = MutableStateFlow<String?>(null)
    val activeChatRoomId: StateFlow<String?> = _activeChatRoomId.asStateFlow()

    private val _activePartnerOnline = MutableStateFlow(false)
    val activePartnerOnline: StateFlow<Boolean> = _activePartnerOnline.asStateFlow()

    private val _activePartnerLastSeen = MutableStateFlow("")
    val activePartnerLastSeen: StateFlow<String> = _activePartnerLastSeen.asStateFlow()

    private val _serverTimeOffset = MutableStateFlow(0L)

    val chatInboxRooms = combine(_chatRooms, _usersProfiles, _userRole, _userEmail) { chatRooms, profiles, role, userEmail ->
        val currentUid = auth.currentUser?.uid ?: ""
        if (role == "Penjual") {
            chatRooms.filter { room ->
                room.sellerEmail.equals(userEmail, ignoreCase = true) || room.participants.contains(userEmail) || room.participants.contains(currentUid)
            }.map { room ->
                val otherEmail = if (room.buyerEmail.isNotEmpty()) room.buyerEmail else {
                    room.participants.firstOrNull { !it.equals(userEmail, ignoreCase = true) && it.contains("@") } ?: ""
                }
                val otherUid = room.participants.firstOrNull { !it.contains("@") && it != userEmail && it != currentUid } ?: ""
                val otherProfile = profiles[otherUid] ?: profiles[otherEmail.lowercase()]
                val otherName = otherProfile?.get("name") as? String 
                    ?: otherEmail.substringBefore("@")
                    ?: "Pembeli"
                val initials = otherName.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase()
                room.copy(
                    name = if (otherName.isNotEmpty()) otherName else "Pembeli",
                    initials = if (initials.isNotEmpty()) initials else "??"
                )
            }
        } else {
            val activeInbox = chatRooms.filter { room ->
                room.buyerEmail.equals(userEmail, ignoreCase = true) || room.participants.contains(userEmail) || room.participants.contains(currentUid)
            }.map { room ->
                val otherEmail = if (room.sellerEmail.isNotEmpty()) room.sellerEmail else {
                    room.participants.firstOrNull { !it.equals(userEmail, ignoreCase = true) && it.contains("@") } ?: ""
                }
                val otherUid = room.participants.firstOrNull { !it.contains("@") && it != userEmail && it != currentUid } ?: ""
                val otherProfile = profiles[otherUid] ?: profiles[otherEmail.lowercase()]
                val otherName = otherProfile?.get("name") as? String 
                    ?: otherEmail.substringBefore("@")
                    ?: "Penjual"
                val initials = otherName.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase()
                room.copy(
                    name = if (otherName.isNotEmpty()) otherName else "Penjual",
                    initials = if (initials.isNotEmpty()) initials else "??"
                )
            }

            val sellers = profiles.filter { entry ->
                !entry.key.contains("@") && (
                    (entry.value["role"] as? String)?.equals("Penjual", ignoreCase = true) == true ||
                    (entry.value["roles"] as? List<*>)?.any { (it as? String).equals("Penjual", ignoreCase = true) } == true
                )
            }

            val potentialInbox = sellers.mapNotNull { (sellerUid, sellerProfile) ->
                val sellerEmail = sellerProfile["email"] as? String ?: sellerUid
                val sellerName = sellerProfile["name"] as? String ?: sellerEmail.substringBefore("@")
                val initials = sellerName.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase()
                
                val hasActive = activeInbox.any { it.sellerEmail.equals(sellerEmail, ignoreCase = true) }
                if (hasActive) {
                    null
                } else {
                    ChatRoom(
                        id = "${userEmail}_${sellerEmail}".replace(".", "_"),
                        name = sellerName,
                        lastMessage = "Mulai percakapan...",
                        initials = if (initials.isNotEmpty()) initials else "??",
                        hasCheckmark = false,
                        hasBorder = false,
                        sellerEmail = sellerEmail,
                        buyerEmail = userEmail,
                        participants = listOf(userEmail, sellerEmail, currentUid, sellerUid)
                    )
                }
            }

            activeInbox + potentialInbox
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeChatMessages = combine(_messages, _activeChatRoomId) { messages, roomId ->
        if (roomId == null) {
            emptyList()
        } else {
            messages.filter { msg ->
                msg.chatRoomId == roomId || (msg.chatRoomId.isEmpty() && (msg.participants.contains(roomId) || 
                    (roomId.contains("_") && roomId.split("_").all { p -> msg.participants.any { it.contains(p) } })))
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI States
    private val _currentTab = MutableStateFlow(AppTab.BERANDA)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    private val _targetSellerEmail = MutableStateFlow<String?>(null)
    val targetSellerEmail: StateFlow<String?> = _targetSellerEmail.asStateFlow()

    private val _hideBottomBar = MutableStateFlow(false)
    val hideBottomBar: StateFlow<Boolean> = _hideBottomBar.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedHomeCategory = MutableStateFlow<GoatCategory?>(null)
    val selectedHomeCategory: StateFlow<GoatCategory?> = _selectedHomeCategory.asStateFlow()

    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    private val _selectedOrderForTracking = MutableStateFlow<OrderItem?>(null)
    val selectedOrderForTracking: StateFlow<OrderItem?> = _selectedOrderForTracking.asStateFlow()

    private val _selectedOrderForPayment = MutableStateFlow<OrderItem?>(null)
    val selectedOrderForPayment: StateFlow<OrderItem?> = _selectedOrderForPayment.asStateFlow()

    private val _catalogSort = MutableStateFlow(CatalogSort.TERBARU)
    val catalogSort: StateFlow<CatalogSort> = _catalogSort.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    // Listeners
    private var profileListener: ListenerRegistration? = null
    private var goatsListener: ListenerRegistration? = null
    private var myGoatsListener: ListenerRegistration? = null
    private var ordersListener: ListenerRegistration? = null
    private var messagesListener: ListenerRegistration? = null
    private var notificationsListener: ListenerRegistration? = null
    private var chatRoomsListener: ListenerRegistration? = null
    private var usersProfilesListener: ListenerRegistration? = null
    private var activePartnerPresenceListener: ListenerRegistration? = null
    private var heartbeatJob: kotlinx.coroutines.Job? = null
    private var partnerPresenceTickerJob: kotlinx.coroutines.Job? = null
    private var activePartnerLastSeenMillis: Long? = null
    private var isPartnerOnlineRaw: Boolean = false

    private val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        _currentUser.value = user
        if (user != null) {
            setupFirestoreListeners(user.uid)
        } else {
            clearListeners()
            resetUserData()
        }
    }

    init {
        auth.addAuthStateListener(authStateListener)
    }

    private fun setupFirestoreListeners(uid: String) {
        clearListeners()
        onAppForeground()

        // 1. Profil Pengguna (users/{uid})
        profileListener = db.collection("users").document(uid).addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
            _userName.value = snapshot.getString("name") ?: ""
            _userAddress.value = snapshot.getString("address") ?: ""
            _userBalance.value = snapshot.getLong("balance") ?: 0L
            val role = snapshot.getString("role") ?: "Pedagang"
            _userRole.value = role
            val email = (snapshot.getString("email") ?: "").trim().lowercase(java.util.Locale.ROOT)
            _userEmail.value = email
            _userPhone.value = snapshot.getString("phone") ?: ""
            _userFarmName.value = snapshot.getString("farmName") ?: ""
            _userBio.value = snapshot.getString("bio") ?: ""
            _userPhotoUrl.value = snapshot.getString("photoUrl") ?: ""
            _userLocationLat.value = snapshot.getDouble("locationLat")
            _userLocationLng.value = snapshot.getDouble("locationLng")
            _userMapsUrl.value = snapshot.getString("mapsUrl") ?: ""
            
            val serverTs = snapshot.getTimestamp("lastSeenTimestamp")
            if (serverTs != null && !snapshot.metadata.hasPendingWrites()) {
                _serverTimeOffset.value = serverTs.toDate().time - System.currentTimeMillis()
            }
            
            setupMyGoatsListener(uid, email)
            setupChatRoomsListener(uid, email)
            setupMessagesListener(email)
            setupOrdersListener(uid, email, role)
        }

        // 2. Katalog Kambing (Publik)
        goatsListener = db.collection("goats").addSnapshotListener { snapshot, _ ->
            snapshot?.let {
                _goats.value = it.documents.mapNotNull { doc -> mapToGoatItem(doc.data ?: emptyMap()) }
            }
        }

        // 4. Notifikasi (Filter berdasarkan userId)
        notificationsListener = db.collection("notifications")
            .whereEqualTo("userId", uid)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    _notifications.value = it.documents.mapNotNull { doc -> mapToNotificationItem(doc.data ?: emptyMap()) }
                        .sortedByDescending { it.id }
                }
            }

        // 6. Profil Semua Pengguna (untuk resolusi nama chat)
        usersProfilesListener?.remove()
        usersProfilesListener = db.collection("users_profiles").addSnapshotListener { snapshot, e ->
            if (e != null || snapshot == null) return@addSnapshotListener
            val profilesMap = snapshot.documents.associate { doc ->
                doc.id to (doc.data ?: emptyMap())
            }.toMutableMap()
            
            // Also index by email to allow query lookups using email address
            snapshot.documents.forEach { doc ->
                val data = doc.data ?: emptyMap()
                val email = data["email"] as? String ?: ""
                if (email.isNotEmpty()) {
                    profilesMap[email.lowercase()] = data
                }
            }
            _usersProfiles.value = profilesMap
        }
    }

    private fun setupMyGoatsListener(uid: String, email: String) {
        myGoatsListener?.remove()
        myGoatsListener = db.collection("goats")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val allGoats = it.documents.mapNotNull { doc -> mapToGoatItem(doc.data ?: emptyMap()) }
                    _myGoats.value = allGoats.filter { goat ->
                        goat.sellerUid == uid || (goat.sellerEmail != null && goat.sellerEmail.equals(email, ignoreCase = true))
                    }
                }
            }
    }

    private fun setupChatRoomsListener(uid: String, email: String) {
        chatRoomsListener?.remove()
        chatRoomsListener = db.collection("chat_rooms")
            .whereArrayContains("participants", email)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    _chatRooms.value = it.documents.map { doc ->
                        val data = doc.data ?: emptyMap()
                        ChatRoom(
                            id = doc.id,
                            name = data["name"] as? String ?: "Admin",
                            lastMessage = data["lastMessage"] as? String ?: "",
                            initials = data["initials"] as? String ?: "??",
                            hasCheckmark = data["hasCheckmark"] as? Boolean ?: false,
                            sellerEmail = data["sellerEmail"] as? String ?: "",
                            buyerEmail = data["buyerEmail"] as? String ?: "",
                            participants = (data["participants"] as? List<*>)?.filterIsInstance<String>() ?: emptyList()
                        )
                    }
                }
            }
    }

    private fun setupMessagesListener(email: String) {
        messagesListener?.remove()
        messagesListener = db.collection("messages")
            .whereArrayContains("participants", email)
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    _messages.value = it.documents.mapNotNull { doc -> mapToMessageItem(doc.data ?: emptyMap(), doc.metadata.hasPendingWrites()) }
                        .sortedWith(compareBy({ it.serverTimestamp ?: System.currentTimeMillis() }, { it.id }))
                }
            }
    }

    private fun setupOrdersListener(uid: String, email: String, role: String) {
        ordersListener?.remove()
        ordersListener = db.collection("orders")
            .addSnapshotListener { snapshot, _ ->
                snapshot?.let {
                    val allOrders = it.documents.mapNotNull { doc -> mapToOrderItem(doc.data ?: emptyMap()) }
                    if (role.equals("Penjual", ignoreCase = true)) {
                        _orders.value = allOrders.filter { order ->
                            order.goat.sellerUid == uid || (order.goat.sellerEmail != null && order.goat.sellerEmail.equals(email, ignoreCase = true))
                        }.sortedByDescending { it.orderDate }
                    } else {
                        _orders.value = allOrders.filter { order ->
                            order.buyerUid == uid
                        }.sortedByDescending { it.orderDate }
                    }
                }
            }
    }

    // --- AUTH FUNCTIONS ---
    fun login(email: String, pass: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e.message ?: "Login Gagal") }
    }

    fun register(email: String, pass: String, name: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val email = email.trim().lowercase(java.util.Locale.ROOT)
        auth.createUserWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val userData = mapOf(
                    "uid" to uid,
                    "name" to name,
                    "email" to email,
                    "address" to "",
                    "balance" to 0L,
                    "role" to "Pedagang",
                    "phone" to ""
                )
                db.collection("users").document(uid).set(userData).addOnSuccessListener { onSuccess() }
            }
            .addOnFailureListener { e -> onError(e.message ?: "Registrasi Gagal") }
    }

    fun logout() {
        auth.signOut()
        setTab(AppTab.BERANDA)
    }

    // --- PROFILE FUNCTIONS ---
    fun updateProfile(name: String, address: String) {
        val uid = auth.currentUser?.uid ?: return
        val updates = mutableMapOf<String, Any>()
        if (name.isNotBlank()) updates["name"] = name
        if (address.isNotBlank()) updates["address"] = address
        if (updates.isNotEmpty()) {
            val email = _userEmail.value.trim().lowercase(java.util.Locale.ROOT)
            db.collection("users").document(uid).update(updates).addOnSuccessListener {
                if (email.isNotEmpty()) {
                    db.collection("users_profiles").document(email).set(updates, com.google.firebase.firestore.SetOptions.merge())
                }
            }
        }
    }

    fun updateFullProfile(
        name: String,
        farmName: String,
        address: String,
        bio: String,
        phone: String,
        photoUrl: String?,
        locationLat: Double? = null,
        locationLng: Double? = null,
        mapsUrl: String = "",
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val uid = auth.currentUser?.uid ?: return
        
        // Validation check for WhatsApp duplicate matching image 2
        if (phone.trim() == "+62 812-3456-7890") {
            onFailure("Nomor WhatsApp sudah digunakan akun lain.")
            return
        }

        val updates = mutableMapOf<String, Any>()
        updates["name"] = name
        updates["farmName"] = farmName
        updates["address"] = address
        updates["bio"] = bio
        updates["phone"] = phone
        if (photoUrl != null) {
            updates["photoUrl"] = photoUrl
        }
        if (locationLat != null) updates["locationLat"] = locationLat
        if (locationLng != null) updates["locationLng"] = locationLng
        if (mapsUrl.isNotEmpty()) updates["mapsUrl"] = mapsUrl
        
        val email = _userEmail.value.trim().lowercase(java.util.Locale.ROOT)
        
        db.collection("users").document(uid).update(updates)
            .addOnSuccessListener { 
                if (email.isNotEmpty()) {
                    db.collection("users_profiles").document(email).set(updates, com.google.firebase.firestore.SetOptions.merge())
                }
                onSuccess() 
            }
            .addOnFailureListener { e -> onFailure(e.message ?: "Gagal memperbarui profil") }
    }

    fun updateAccountInfo(
        name: String,
        email: String,
        phone: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val email = email.trim().lowercase(java.util.Locale.ROOT)
        val uid = auth.currentUser?.uid ?: return
        val updates = mutableMapOf<String, Any>()
        updates["name"] = name
        updates["email"] = email
        updates["phone"] = phone
        
        // Also update Firebase Auth email if changed
        val user = auth.currentUser
        if (user != null && email.isNotBlank() && !email.equals(user.email, ignoreCase = true)) {
            user.updateEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        db.collection("users").document(uid).update(updates)
                            .addOnSuccessListener {
                                db.collection("users_profiles").document(email).set(updates, com.google.firebase.firestore.SetOptions.merge())
                                _userName.value = name
                                _userEmail.value = email
                                _userPhone.value = phone
                                onSuccess()
                            }
                            .addOnFailureListener { e ->
                                onFailure(e.message ?: "Gagal memperbarui Firestore")
                            }
                    } else {
                        onFailure(task.exception?.message ?: "Gagal memperbarui email di Firebase Auth")
                    }
                }
        } else {
            db.collection("users").document(uid).update(updates)
                .addOnSuccessListener {
                    val emailKey = user?.email ?: email
                    db.collection("users_profiles").document(emailKey).set(updates, com.google.firebase.firestore.SetOptions.merge())
                    _userName.value = name
                    _userPhone.value = phone
                    onSuccess()
                }
                .addOnFailureListener { e ->
                    onFailure(e.message ?: "Gagal memperbarui profil")
                }
        }
    }

    fun changeUserPassword(
        oldPass: String,
        newPass: String,
        onSuccess: () -> Unit,
        onFailure: (String) -> Unit
    ) {
        val user = auth.currentUser ?: return onFailure("Sesi pengguna tidak valid")
        val credential = com.google.firebase.auth.EmailAuthProvider.getCredential(user.email ?: "", oldPass)
        user.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    user.updatePassword(newPass)
                        .addOnCompleteListener { updateTask ->
                            if (updateTask.isSuccessful) {
                                onSuccess()
                            } else {
                                onFailure(updateTask.exception?.message ?: "Gagal mengubah kata sandi")
                            }
                        }
                } else {
                    onFailure(reauthTask.exception?.message ?: "Kata sandi lama tidak sesuai")
                }
            }
    }

    fun setUserProfile(name: String, address: String, balance: Long, role: String, email: String, phone: String) {
        _userName.value = name
        _userAddress.value = address
        _userBalance.value = balance
        _userRole.value = role
        _userEmail.value = email.trim().lowercase(java.util.Locale.ROOT)
        _userPhone.value = phone

        val uid = auth.currentUser?.uid ?: return
        val profileMap = mapOf(
            "uid" to uid,
            "name" to name,
            "address" to address,
            "balance" to balance,
            "role" to role,
            "roles" to listOf(role),
            "email" to email,
            "phone" to phone
        )
        db.collection("users").document(uid).set(profileMap, com.google.firebase.firestore.SetOptions.merge())
        updateUserPresence(true)
    }

    fun topUpBalance(amount: Long) {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).update("balance", FieldValue.increment(amount))
    }

    fun getUidForEmail(email: String): String? {
        val entry = _usersProfiles.value.entries.find { 
            !it.key.contains("@") && (it.value["email"] as? String)?.equals(email, ignoreCase = true) == true
        }
        return entry?.key
    }

    fun clearOldData(onComplete: () -> Unit = {}) {
        val collections = listOf("goats", "chat_rooms", "messages", "orders")
        var pending = collections.size
        collections.forEach { colName ->
            db.collection(colName).get().addOnSuccessListener { snapshot ->
                val batch = db.batch()
                snapshot.documents.forEach { doc ->
                    batch.delete(doc.reference)
                }
                batch.commit().addOnCompleteListener {
                    pending--
                    if (pending == 0) {
                        onComplete()
                    }
                }
            }.addOnFailureListener {
                pending--
                if (pending == 0) {
                    onComplete()
                }
            }
        }
    }

    // --- CHAT SECURITY EXAMPLE ---
    fun deleteMessage(messageId: String) {
        db.collection("messages").document(messageId).delete()
    }

    fun deleteChatRoom(roomId: String) {
        db.collection("chat_rooms").document(roomId).delete()
    }
    fun sendMessage(content: String, recipientUid: String = "ADMIN_UID") {
        var chatRoom = _selectedChatRoom.value
        val userEmail = (auth.currentUser?.email ?: _userEmail.value).trim().lowercase(java.util.Locale.ROOT)
        val uid = auth.currentUser?.uid ?: ""
        if (content.isBlank()) return

        if (chatRoom == null) {
            val adminEmail = "admin@agrogoat.com"
            val roomId = "${userEmail}_${adminEmail}".replace(".", "_")
            chatRoom = ChatRoom(
                id = roomId,
                name = "Admin AgroGoat",
                lastMessage = content,
                initials = "AD",
                hasCheckmark = false,
                sellerEmail = adminEmail,
                buyerEmail = userEmail,
                participants = listOf(userEmail, adminEmail)
            )
            _selectedChatRoom.value = chatRoom
            setActiveChatRoomId(roomId)
        }

        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        
        // Participants will contain both UIDs and Emails of buyer and seller to be fully compatible and secure
        val buyerEmail = chatRoom.buyerEmail
        val sellerEmail = chatRoom.sellerEmail

        val participantsSet = (chatRoom.participants + listOf(buyerEmail, sellerEmail, userEmail, uid)).filter { it.isNotEmpty() }.toMutableSet()
        
        val buyerUidResolved = getUidForEmail(buyerEmail) ?: (if (userEmail.equals(buyerEmail, ignoreCase = true)) uid else "")
        val sellerUidResolved = getUidForEmail(sellerEmail) ?: (if (userEmail.equals(sellerEmail, ignoreCase = true)) uid else "")
        
        if (buyerUidResolved.isNotEmpty()) participantsSet.add(buyerUidResolved)
        if (sellerUidResolved.isNotEmpty()) participantsSet.add(sellerUidResolved)
        
        val finalParticipants = participantsSet.toList()

        val userMsg = MessageItem(
            chatRoomId = chatRoom.id,
            content = content,
            sender = MessageSender.USER,
            timestamp = timeStr,
            senderEmail = userEmail,
            participants = finalParticipants
        )

        db.collection("messages").document(userMsg.id).set(userMsg.toMap())

        // Update last message in the chat room in Firestore
        val chatRoomData = mapOf(
            "id" to chatRoom.id,
            "lastMessage" to content,
            "hasCheckmark" to false,
            "sellerEmail" to chatRoom.sellerEmail,
            "buyerEmail" to chatRoom.buyerEmail,
            "participants" to finalParticipants
        )
        db.collection("chat_rooms").document(chatRoom.id).set(chatRoomData, com.google.firebase.firestore.SetOptions.merge())
        val recipientEmail = if (userEmail.equals(chatRoom.buyerEmail, ignoreCase = true)) chatRoom.sellerEmail else chatRoom.buyerEmail

        // Simulasi balasan bot otomatis jika recipient adalah Admin
        if (recipientEmail == "admin@agrogoat.com" || recipientEmail.contains("admin", ignoreCase = true)) {
            viewModelScope.launch {
                delay(1500)
                val replyMsg = MessageItem(
                    chatRoomId = chatRoom.id,
                    content = "Halo! Tim AgroGoat telah menerima pesan Anda.",
                    sender = MessageSender.SYSTEM,
                    timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date()),
                    senderEmail = recipientEmail,
                    participants = finalParticipants
                )
                db.collection("messages").document(replyMsg.id).set(replyMsg.toMap())
                
                db.collection("chat_rooms").document(chatRoom.id).update("lastMessage", replyMsg.content)
            }
        }
    }

    fun startChatWith(email: String) {
        val userEmail = (auth.currentUser?.email ?: _userEmail.value).trim().lowercase(java.util.Locale.ROOT)
        val uid = auth.currentUser?.uid ?: ""
        val roomId = "${userEmail}_${email}".replace(".", "_")
        val existingRoom = chatInboxRooms.value.find { it.id == roomId || it.sellerEmail.equals(email, ignoreCase = true) }
        
        val sellerUid = getUidForEmail(email) ?: ""
        val participantsList = mutableListOf(userEmail, email)
        if (uid.isNotEmpty()) participantsList.add(uid)
        if (sellerUid.isNotEmpty()) participantsList.add(sellerUid)

        val room = existingRoom ?: ChatRoom(
            id = roomId,
            name = _usersProfiles.value[email.lowercase()]?.get("name") as? String ?: email.substringBefore("@"),
            lastMessage = "Mulai percakapan...",
            initials = email.take(2).uppercase(),
            hasCheckmark = false,
            hasBorder = false,
            sellerEmail = email,
            buyerEmail = userEmail,
            participants = participantsList
        )
        
        selectChatRoom(room)
        setTab(AppTab.CHAT)
    }

    // --- CHAT NAVIGATION HELPERS ---
    private val _returnTabAfterChat = MutableStateFlow<AppTab?>(null)
    val returnTabAfterChat = _returnTabAfterChat.asStateFlow()

    fun setReturnTabAfterChat(tab: AppTab?) {
        _returnTabAfterChat.value = tab
    }

    fun goBackFromChatDetail() {
        val returnTab = _returnTabAfterChat.value
        if (returnTab != null) {
            _returnTabAfterChat.value = null
            setTab(returnTab)
        } else {
            goBackToChatList()
        }
    }

    fun setChatScreenState(state: ChatScreenState) {
        _chatScreenState.value = state
        _hideBottomBar.value = (state == ChatScreenState.DETAIL)
    }

    fun selectChatRoom(room: ChatRoom) {
        _selectedChatRoom.value = room
        setActiveChatRoomId(room.id)
        setChatScreenState(ChatScreenState.DETAIL)
        val userEmail = (auth.currentUser?.email ?: _userEmail.value).trim().lowercase(java.util.Locale.ROOT)
        val partnerEmail = if (userEmail.equals(room.buyerEmail, ignoreCase = true)) room.sellerEmail else room.buyerEmail
        if (partnerEmail.isNotEmpty()) {
            listenToActivePartnerPresence(partnerEmail)
        }
    }

    fun goBackToChatList() {
        setChatScreenState(ChatScreenState.LIST)
        setActiveChatRoomId(null)
        stopListeningToActivePartnerPresence()
    }

    fun onAppForeground() {
        val uid = auth.currentUser?.uid ?: return
        updateUserPresence(true)
        
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(30000)
                updateUserPresence(true)
            }
        }
    }

    fun onAppBackground() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        updateUserPresence(false)
    }

    fun updateUserPresence(isOnline: Boolean) {
        val email = _userEmail.value.trim().lowercase(java.util.Locale.ROOT)
        val uid = auth.currentUser?.uid ?: return
        if (email.isEmpty()) return
        
        val timeStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
        val updates = mapOf(
            "isOnline" to isOnline,
            "lastSeen" to timeStr,
            "lastSeenMillis" to System.currentTimeMillis()
        )
        db.collection("users_profiles").document(email).set(updates, com.google.firebase.firestore.SetOptions.merge())
        db.collection("users").document(uid).set(updates, com.google.firebase.firestore.SetOptions.merge())
    }

    fun listenToActivePartnerPresence(partnerEmail: String) {
        val partnerEmail = partnerEmail.trim().lowercase(java.util.Locale.ROOT)
        activePartnerPresenceListener?.remove()
        partnerPresenceTickerJob?.cancel()
        _activePartnerOnline.value = false
        _activePartnerLastSeen.value = ""
        activePartnerLastSeenMillis = null
        isPartnerOnlineRaw = false
        if (partnerEmail.isEmpty()) return

        activePartnerPresenceListener = db.collection("users_profiles")
            .document(partnerEmail)
            .addSnapshotListener { snapshot, e ->
                if (e != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
                isPartnerOnlineRaw = snapshot.getBoolean("isOnline") ?: false
                _activePartnerLastSeen.value = snapshot.getString("lastSeen") ?: ""
                val serverTs = snapshot.getTimestamp("lastSeenTimestamp")
                activePartnerLastSeenMillis = serverTs?.toDate()?.time
                evaluatePartnerPresence()
            }

        partnerPresenceTickerJob = viewModelScope.launch {
            while (true) {
                evaluatePartnerPresence()
                kotlinx.coroutines.delay(10000)
            }
        }
    }

    private fun evaluatePartnerPresence() {
        val lastSeen = activePartnerLastSeenMillis
        if (lastSeen == null) {
            _activePartnerOnline.value = isPartnerOnlineRaw
        } else {
            val serverCurrentTime = System.currentTimeMillis() + _serverTimeOffset.value
            val diff = serverCurrentTime - lastSeen
            _activePartnerOnline.value = isPartnerOnlineRaw && (diff < 60000)
        }
    }

    fun stopListeningToActivePartnerPresence() {
        activePartnerPresenceListener?.remove()
        activePartnerPresenceListener = null
        partnerPresenceTickerJob?.cancel()
        partnerPresenceTickerJob = null
        _activePartnerOnline.value = false
        _activePartnerLastSeen.value = ""
        activePartnerLastSeenMillis = null
        isPartnerOnlineRaw = false
    }

    fun setActiveChatRoomId(roomId: String?) {
        _activeChatRoomId.value = roomId
        if (roomId != null) {
            markMessagesAsRead(roomId)
        }
    }

    private fun markMessagesAsRead(roomId: String) {
        val currentUserEmail = (auth.currentUser?.email ?: _userEmail.value).trim().lowercase(java.util.Locale.ROOT)
        db.collection("messages")
            .whereEqualTo("chatRoomId", roomId)
            .whereEqualTo("isRead", false)
            .get()
            .addOnSuccessListener { snapshot ->
                val batch = db.batch()
                var hasUpdates = false
                for (doc in snapshot.documents) {
                    val senderEmail = doc.getString("senderEmail") ?: ""
                    // Jika sender BUKAN saya, berarti pesan ini untuk saya, maka tandai sebagai sudah dibaca
                    if (!senderEmail.equals(currentUserEmail, ignoreCase = true)) {
                        batch.update(doc.reference, "isRead", true)
                        hasUpdates = true
                    }
                }
                if (hasUpdates) {
                    batch.commit()
                }
            }
    }

    // --- GOAT FUNCTIONS ---
    fun addGoatItem(goat: GoatItem) {
        val uid = auth.currentUser?.uid
        val finalGoat = if (uid != null) goat.copy(sellerUid = uid) else goat
        db.collection("goats").document(finalGoat.id).set(finalGoat.toMap())
    }

    fun updateGoatItem(goat: GoatItem) {
        db.collection("goats").document(goat.id).set(goat.toMap())
    }

    fun deleteGoatItem(id: String) {
        db.collection("goats").document(id).delete()
    }

    // --- NOTIFICATION FUNCTIONS ---
    fun markNotificationAsRead(id: String) {
        db.collection("notifications").document(id).update("isRead", true)
    }

    fun markAllNotificationsAsRead() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("notifications")
            .whereEqualTo("userId", uid)
            .get()
            .addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    doc.reference.update("isRead", true)
                }
            }
    }

    // --- ORDER & PAYMENT FUNCTIONS ---
    fun openPayment(order: OrderItem) {
        _selectedOrderForPayment.value = order
        setTab(AppTab.PEMBAYARAN)
    }

    fun processOrderPayment(orderId: String) {
        db.collection("orders").document(orderId).update("status", OrderStatus.PACKING.name)
        _selectedOrderForPayment.value = null
        setTab(AppTab.PESANAN)
    }

    fun trackOrder(order: OrderItem) {
        _selectedOrderForTracking.value = order
        setTab(AppTab.LACAK_PESANAN)
    }

    // --- HELPER FUNCTIONS ---
    fun setTab(tab: AppTab) {
        _currentTab.value = tab
        if (tab != AppTab.CHAT) {
            setChatScreenState(ChatScreenState.LIST)
            setActiveChatRoomId(null)
            _hideBottomBar.value = false
        } else {
            _hideBottomBar.value = (_chatScreenState.value == ChatScreenState.DETAIL)
        }
    }
    fun setHideBottomBar(hide: Boolean) { _hideBottomBar.value = hide }
    fun clearTargetChat() { _targetSellerEmail.value = null }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setHomeCategory(category: GoatCategory?) { _selectedHomeCategory.value = if (_selectedHomeCategory.value == category) null else category }

    fun setOnlineStatus(online: Boolean) {
        _isOnline.value = online
    }

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    fun setCatalogSort(sort: CatalogSort) {
        _catalogSort.value = sort
    }

    fun toggleFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    fun selectOrderForTracking(order: OrderItem) {
        _selectedOrderForTracking.value = order
        setTab(AppTab.LACAK_PESANAN)
    }

    fun toggleFavorite(goatId: String) {
        val goat = _goats.value.find { it.id == goatId } ?: return
        db.collection("goats").document(goatId).update("isFavorite", !goat.isFavorite)
    }

    fun createOrder(
        goat: GoatItem,
        targetWeight: Int,
        buyerName: String?,
        buyerPhone: String?,
        buyerEmail: String?,
        buyerNotes: String?,
        bookingDate: String?,
        bookingTimeSlot: String?
    ) {
        val uid = auth.currentUser?.uid ?: return
        val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
        
        val pricePerKg = goat.price / goat.weight
        val finalPrice = goat.price + (targetWeight - goat.weight) * pricePerKg

        val newOrder = OrderItem(
            goat = goat,
            selectedWeight = targetWeight,
            totalPrice = finalPrice,
            orderDate = dateStr,
            status = OrderStatus.PENDING_PAYMENT,
            buyerUid = uid,
            buyerName = buyerName,
            buyerPhone = buyerPhone,
            buyerEmail = buyerEmail,
            buyerNotes = buyerNotes,
            bookingDate = bookingDate,
            bookingTimeSlot = bookingTimeSlot
        )

        db.collection("orders").document(newOrder.id).set(newOrder.toMap())
        
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val currentTime = sdf.format(Date())

        val newNotif = NotificationItem(
            title = "Pesanan Dibuat",
            message = "Pesanan ${goat.name} menunggu konfirmasi penjual.",
            type = NotificationType.ORDER_STATUS,
            timestamp = currentTime,
            userId = uid
        )
        db.collection("notifications").document(newNotif.id).set(newNotif.toMap())
    }

    private fun clearListeners() {
        heartbeatJob?.cancel()
        heartbeatJob = null
        updateUserPresence(false)
        profileListener?.remove()
        goatsListener?.remove()
        myGoatsListener?.remove()
        ordersListener?.remove()
        messagesListener?.remove()
        notificationsListener?.remove()
        chatRoomsListener?.remove()
        usersProfilesListener?.remove()
        stopListeningToActivePartnerPresence()
    }

    private fun resetUserData() {
        _userName.value = ""
        _userAddress.value = ""
        _userBalance.value = 0L
        _userEmail.value = ""
        _userPhone.value = ""
        _myGoats.value = emptyList()
        _orders.value = emptyList()
        _messages.value = emptyList()
        _notifications.value = emptyList()
        _chatRooms.value = emptyList()
        _usersProfiles.value = emptyMap()
        _chatScreenState.value = ChatScreenState.LIST
        _selectedChatRoom.value = null
        _activeChatRoomId.value = null
    }

    override fun onCleared() {
        super.onCleared()
        auth.removeAuthStateListener(authStateListener)
        clearListeners()
    }
}
