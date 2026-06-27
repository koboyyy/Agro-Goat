package com.agrogoat.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agrogoat.app.data.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class AppTab {
    BERANDA,
    KATALOG,
    CHAT,
    PESANAN,
    PROFIL,
    LACAK_PESANAN,
    NOTIFIKASI,
    PEMBAYARAN
}

enum class CatalogSort {
    TERBARU,
    HARGA_RENDAH,
    HARGA_TINGGI,
    BOBOT_TERBESAR
}

class AgroGoatViewModel : ViewModel() {

    // Internet connectivity state
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    fun setOnlineStatus(online: Boolean) {
        _isOnline.value = online
    }

    private val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            null
        }
    }

    private var goatsListener: ListenerRegistration? = null
    private var ordersListener: ListenerRegistration? = null
    private var messagesListener: ListenerRegistration? = null
    private var notificationsListener: ListenerRegistration? = null
    private var profileListener: ListenerRegistration? = null

    // Theme state (false = light theme, true = dark theme)
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }

    // Bottom bar visibility override
    private val _hideBottomBar = MutableStateFlow(false)
    val hideBottomBar: StateFlow<Boolean> = _hideBottomBar.asStateFlow()

    fun setHideBottomBar(hide: Boolean) {
        _hideBottomBar.value = hide
    }

    // Tab Navigation
    private val _currentTab = MutableStateFlow(AppTab.BERANDA)
    val currentTab: StateFlow<AppTab> = _currentTab.asStateFlow()

    // State for specific flows
    private val _selectedOrderForTracking = MutableStateFlow<OrderItem?>(null)
    val selectedOrderForTracking: StateFlow<OrderItem?> = _selectedOrderForTracking.asStateFlow()

    private val _selectedOrderForPayment = MutableStateFlow<OrderItem?>(null)
    val selectedOrderForPayment: StateFlow<OrderItem?> = _selectedOrderForPayment.asStateFlow()

    // User details
    private val _userName = MutableStateFlow("Siti Zahfia")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userBalance = MutableStateFlow(12500000L) // in IDR
    val userBalance: StateFlow<Long> = _userBalance.asStateFlow()

    private val _userAddress = MutableStateFlow("Bengkalis, Riau")
    val userAddress: StateFlow<String> = _userAddress.asStateFlow()

    private val _userRole = MutableStateFlow("Pedagang") // default role (Pedagang / Penjual)
    val userRole: StateFlow<String> = _userRole.asStateFlow()

    // Search & Filter state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedHomeCategory = MutableStateFlow<GoatCategory?>(null)
    val selectedHomeCategory: StateFlow<GoatCategory?> = _selectedHomeCategory.asStateFlow()

    // Catalog state
    private val _selectedCatalogCategory = MutableStateFlow<GoatCategory?>(null)
    val selectedCatalogCategory: StateFlow<GoatCategory?> = _selectedCatalogCategory.asStateFlow()

    private val _catalogSort = MutableStateFlow(CatalogSort.TERBARU)
    val catalogSort: StateFlow<CatalogSort> = _catalogSort.asStateFlow()

    private val _showFavoritesOnly = MutableStateFlow(false)
    val showFavoritesOnly: StateFlow<Boolean> = _showFavoritesOnly.asStateFlow()

    // Data lists
    private val _goats = MutableStateFlow<List<GoatItem>>(emptyList())
    val goats: StateFlow<List<GoatItem>> = _goats.asStateFlow()

    private val _orders = MutableStateFlow<List<OrderItem>>(emptyList())
    val orders: StateFlow<List<OrderItem>> = _orders.asStateFlow()

    private val _messages = MutableStateFlow<List<MessageItem>>(emptyList())
    val messages: StateFlow<List<MessageItem>> = _messages.asStateFlow()

    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // UI state loaders/triggerers
    init {
        try {
            db?.clearPersistence()
        } catch (e: Exception) {
            // Ignore
        }
        setupFirestoreListeners()
    }

    fun clearAllFirestoreData() {
        val database = db ?: return
        val collections = listOf("goats", "orders", "messages", "notifications", "users_profiles", "users")
        for (col in collections) {
            database.collection(col).get().addOnSuccessListener { snapshot ->
                for (doc in snapshot.documents) {
                    database.collection(col).document(doc.id).delete()
                }
            }
        }
    }

    private fun setupFirestoreListeners() {
        val database = db ?: return

        // 1. Goats Listener
        goatsListener = database.collection("goats").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { mapToGoatItem(it) }
                }
                _goats.value = list
            }
        }

        // 2. Orders Listener
        ordersListener = database.collection("orders").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { mapToOrderItem(it) }
                }.sortedByDescending { it.orderDate }
                _orders.value = list
            }
        }

        // 3. Messages Listener
        messagesListener = database.collection("messages").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { mapToMessageItem(it) }
                }.sortedWith(compareBy({ it.timestamp }, { it.id }))
                _messages.value = list
            }
        }

        // 4. Notifications Listener
        notificationsListener = database.collection("notifications").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null) {
                val list = snapshot.documents.mapNotNull { doc ->
                    doc.data?.let { mapToNotificationItem(it) }
                }.sortedByDescending { it.id }
                _notifications.value = list
            }
        }

        // 5. Profile Listener
        profileListener = database.collection("users").document("current_user").addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                _userName.value = snapshot.getString("name") ?: ""
                _userAddress.value = snapshot.getString("address") ?: ""
                _userBalance.value = snapshot.getLong("balance") ?: 0L
                _userRole.value = snapshot.getString("role") ?: "Pedagang"
            }
        }
    }

    fun setTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun trackOrder(order: OrderItem) {
        _selectedOrderForTracking.value = order
        setTab(AppTab.LACAK_PESANAN)
    }

    fun openPayment(order: OrderItem) {
        _selectedOrderForPayment.value = order
        setTab(AppTab.PEMBAYARAN)
    }

    fun markNotificationAsRead(notifId: String) {
        val database = db
        if (database == null) {
            _notifications.value = _notifications.value.map {
                if (it.id == notifId) it.copy(isRead = true) else it
            }
            return
        }
        database.collection("notifications").document(notifId).update("isRead", true)
    }

    fun markAllNotificationsAsRead() {
        val database = db
        if (database == null) {
            _notifications.value = _notifications.value.map { it.copy(isRead = true) }
            return
        }
        for (notif in _notifications.value) {
            if (!notif.isRead) {
                database.collection("notifications").document(notif.id).update("isRead", true)
            }
        }
    }

    fun updateProfile(name: String, address: String) {
        val database = db
        if (database == null) {
            if (name.isNotBlank()) _userName.value = name
            if (address.isNotBlank()) _userAddress.value = address
            return
        }
        val updates = mutableMapOf<String, Any>()
        if (name.isNotBlank()) updates["name"] = name
        if (address.isNotBlank()) updates["address"] = address
        if (updates.isNotEmpty()) {
            database.collection("users").document("current_user").update(updates)
        }
    }

    fun setUserProfile(name: String, address: String, balance: Long, role: String, email: String, phone: String) {
        val database = db
        if (database == null) {
            _userName.value = name
            _userAddress.value = address
            _userBalance.value = balance
            _userRole.value = role
            return
        }
        val profile = mapOf(
            "name" to name,
            "address" to address,
            "balance" to balance,
            "role" to role,
            "email" to email,
            "phone" to phone
        )
        database.collection("users").document("current_user").set(profile)
    }

    fun topUpBalance(amount: Long) {
        val database = db
        if (database == null) {
            _userBalance.value += amount
            return
        }
        database.collection("users").document("current_user")
            .update("balance", FieldValue.increment(amount))
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setHomeCategory(category: GoatCategory?) {
        _selectedHomeCategory.value = if (_selectedHomeCategory.value == category) null else category
    }

    fun setCatalogCategory(category: GoatCategory?) {
        _selectedCatalogCategory.value = if (_selectedCatalogCategory.value == category) null else category
    }

    fun setCatalogSort(sort: CatalogSort) {
        _catalogSort.value = sort
    }

    fun toggleFavoritesOnly() {
        _showFavoritesOnly.value = !_showFavoritesOnly.value
    }

    fun toggleFavorite(goatId: String) {
        val goat = _goats.value.find { it.id == goatId } ?: return
        val database = db
        if (database == null) {
            _goats.value = _goats.value.map {
                if (it.id == goatId) it.copy(isFavorite = !it.isFavorite) else it
            }
            return
        }
        database.collection("goats").document(goatId).update("isFavorite", !goat.isFavorite)
    }

    fun addGoatItem(goat: GoatItem) {
        val database = db
        if (database == null) {
            _goats.value = _goats.value + goat
            return
        }
        database.collection("goats").document(goat.id).set(goat.toMap())
    }

    fun updateGoatItem(goat: GoatItem) {
        val database = db
        if (database == null) {
            _goats.value = _goats.value.map { if (it.id == goat.id) goat else it }
            return
        }
        database.collection("goats").document(goat.id).set(goat.toMap())
    }

    fun deleteGoatItem(goatId: String) {
        val database = db
        if (database == null) {
            _goats.value = _goats.value.filter { it.id != goatId }
            return
        }
        database.collection("goats").document(goatId).delete()
    }

    fun createOrder(goat: GoatItem, targetWeight: Int) {
        val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
        
        val baseWeight = goat.weight
        val basePrice = goat.price
        val pricePerKg = basePrice / baseWeight
        val finalPrice = basePrice + (targetWeight - baseWeight) * pricePerKg

        val newOrder = OrderItem(
            goat = goat,
            selectedWeight = targetWeight,
            totalPrice = finalPrice,
            orderDate = dateStr,
            status = OrderStatus.PENDING_PAYMENT
        )

        val database = db
        if (database == null) {
            _orders.value = listOf(newOrder) + _orders.value
            val newNotif = NotificationItem(
                title = "Pesanan Dibuat",
                message = "Pesanan ${goat.name} menunggu pembayaran transfer bank.",
                type = NotificationType.ORDER_STATUS,
                timestamp = "Baru saja"
            )
            _notifications.value = listOf(newNotif) + _notifications.value
            openPayment(newOrder)
            return
        }

        database.collection("orders").document(newOrder.id).set(newOrder.toMap())
        
        val newNotif = NotificationItem(
            title = "Pesanan Dibuat",
            message = "Pesanan ${goat.name} menunggu pembayaran transfer bank.",
            type = NotificationType.ORDER_STATUS,
            timestamp = "Baru saja"
        )
        database.collection("notifications").document(newNotif.id).set(newNotif.toMap())
        
        openPayment(newOrder)
    }

    fun processBankTransferPayment(orderId: String) {
        val database = db
        if (database == null) {
            _orders.value = _orders.value.map { ord ->
                if (ord.id == orderId && ord.status == OrderStatus.PENDING_PAYMENT) {
                    ord.copy(status = OrderStatus.PACKING)
                } else {
                    ord
                }
            }
            val notif = NotificationItem(
                title = "Bukti Terkirim ✅",
                message = "Bukti transfer telah diterima admin. Pesanan Anda sedang diverifikasi.",
                type = NotificationType.ORDER_STATUS,
                timestamp = "Baru saja"
            )
            _notifications.value = listOf(notif) + _notifications.value
            setTab(AppTab.PESANAN)
            viewModelScope.launch {
                delay(8000)
                _orders.value = _orders.value.map { ord ->
                    if (ord.id == orderId && ord.status == OrderStatus.PACKING) {
                        ord.copy(status = OrderStatus.SHIPPING)
                    } else {
                        ord
                    }
                }
            }
            return
        }

        database.collection("orders").document(orderId).update("status", OrderStatus.PACKING.name)

        val notif = NotificationItem(
            title = "Bukti Terkirim ✅",
            message = "Bukti transfer telah diterima admin. Pesanan Anda sedang diverifikasi.",
            type = NotificationType.ORDER_STATUS,
            timestamp = "Baru saja"
        )
        database.collection("notifications").document(notif.id).set(notif.toMap())

        setTab(AppTab.PESANAN)
        
        viewModelScope.launch {
            delay(8000)
            database.collection("orders").document(orderId).update("status", OrderStatus.SHIPPING.name)
        }
    }

    fun processOrderPayment(orderId: String) {
        processBankTransferPayment(orderId)
    }

    fun sendMessage(content: String) {
        if (content.isBlank()) return

        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val messageId = "MSG_${System.currentTimeMillis()}"
        val userMsg = MessageItem(
            id = messageId,
            content = content,
            sender = MessageSender.USER,
            timestamp = timeStr
        )

        val database = db
        if (database == null) {
            _messages.value = _messages.value + userMsg
            viewModelScope.launch {
                delay(1200)
                val query = content.lowercase()
                val (replyText, sender) = when {
                    query.contains("bukti_transfer") || query.contains("sudah transfer") -> {
                        Pair(
                            "Terima kasih mas! Bukti transfer sudah kami terima. Pesanan mas sedang kami verifikasi dan akan segera masuk proses pengemasan. Tunggu update selanjutnya ya! 🙏",
                            MessageSender.SYSTEM
                        )
                    }
                    query.contains("ready") || query.contains("ada") -> {
                        Pair(
                            "Halo mas! Semua kambing yang ada di katalog berstatus ready dan sehat walafiat. Bisa langsung dicek detailnya di tab katalog ya.",
                            MessageSender.BREEDER_ETAWA
                        )
                    }
                    else -> {
                        Pair(
                            "Baik mas, tim kami akan segera merespon pesan Anda secepatnya. 😊",
                            MessageSender.BREEDER_POTONG
                        )
                    }
                }
                val replyMsg = MessageItem(
                    id = "MSG_${System.currentTimeMillis()}",
                    content = replyText,
                    sender = sender,
                    timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
                )
                _messages.value = _messages.value + replyMsg
            }
            return
        }

        database.collection("messages").document(userMsg.id).set(userMsg.toMap())

        viewModelScope.launch {
            delay(1200)

            val query = content.lowercase()
            val (replyText, sender) = when {
                query.contains("bukti_transfer") || query.contains("sudah transfer") -> {
                    Pair(
                        "Terima kasih mas! Bukti transfer sudah kami terima. Pesanan mas sedang kami verifikasi dan akan segera masuk proses pengemasan. Tunggu update selanjutnya ya! 🙏",
                        MessageSender.SYSTEM
                    )
                }
                query.contains("ready") || query.contains("ada") -> {
                    Pair(
                        "Halo mas! Semua kambing yang ada di katalog berstatus ready dan sehat walafiat. Bisa langsung dicek detailnya di tab katalog ya.",
                        MessageSender.BREEDER_ETAWA
                    )
                }
                else -> {
                    Pair(
                        "Baik mas, tim kami akan segera merespon pesan Anda secepatnya. 😊",
                        MessageSender.BREEDER_POTONG
                    )
                }
            }

            val replyMsg = MessageItem(
                id = "MSG_${System.currentTimeMillis()}",
                content = replyText,
                sender = sender,
                timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            )
            database.collection("messages").document(replyMsg.id).set(replyMsg.toMap())
        }
    }

    private fun seedDefaultGoats() {
        val database = db ?: return
        val defaultGoats = listOf(
            GoatItem(
                name = "Kambing Etawa",
                category = GoatCategory.ETAWA,
                gender = "Betina",
                weight = 45,
                age = 1.5,
                price = 3500000,
                location = "Bengkalis",
                description = "Kambing Etawa betina berkualitas tinggi, sangat bagus untuk indukan perah maupun pembibitan lanjutan."
            ),
            GoatItem(
                name = "Kambing PE",
                category = GoatCategory.ETAWA,
                gender = "Betina",
                weight = 45,
                age = 2.0,
                price = 4000000,
                location = "Bengkalis",
                description = "Kambing Peranakan Etawa betina unggulan, sehat dan siap dikawinkan."
            ),
            GoatItem(
                name = "Kambing Etawa",
                category = GoatCategory.ETAWA,
                gender = "Jantan",
                weight = 55,
                age = 1.5,
                price = 5500000,
                location = "Bengkalis",
                description = "Kambing Etawa unggulan dengan postur tubuh besar, tegap, telinga melipat dengan baik, dan bulu yang subur. Sangat cocok sebagai hewan indukan pejantan tangguh maupun untuk keperluan kontes.",
                isNew = true
            ),
            GoatItem(
                name = "Kambing PE",
                category = GoatCategory.ETAWA,
                gender = "Jantan",
                weight = 40,
                age = 2.0,
                price = 1550000,
                location = "Bengkalis",
                description = "Kambing Peranakan Etawa (PE) lokal yang memiliki daya tahan tubuh tinggi terhadap iklim tropis. Makannya sangat lahap dan rajin, menjadikannya pilihan investasi peternakan mandiri yang hemat biaya.",
                isFavorite = true
            ),
            GoatItem(
                name = "Kambing Boer Karkas",
                category = GoatCategory.POTONG,
                gender = "Jantan",
                weight = 68,
                age = 1.2,
                price = 4800000,
                location = "Bandar Laksamana",
                description = "Kambing Boer asli keturunan pedaging tinggi karkas. Otot tebal di pundak dan kaki, daging empuk rendah kolesterol. Sempurna untuk keperluan pesta keagamaan, aqiqah, maupun qurban.",
                isNew = true
            ),
            GoatItem(
                name = "Kambing Kacang Super",
                category = GoatCategory.POTONG,
                gender = "Jantan",
                weight = 32,
                age = 1.0,
                price = 1950000,
                location = "Bengkalis",
                description = "Kambing Kacang lokal lincah, aktif, dipelihara dengan metode rumping rumput hijauan alam liar. Kualitas daging sangat padat dan gurih, sangat digemari restoran kuliner sate."
            ),
            GoatItem(
                name = "Saanen Perah Unggul",
                category = GoatCategory.PERAH,
                gender = "Betina",
                weight = 45,
                age = 1.8,
                price = 6200000,
                location = "Bukit Batu",
                description = "Kambing impor Saanen murni putih mulus, berkemampuan memproduksi susu murni hingga 2,5 liter per hari. Sangat jinak, ramah, dan dipelihara dalam sanitasi kandang modern yang steril.",
                isNew = true
            ),
            GoatItem(
                name = "Anglo Nubian Pedigree",
                category = GoatCategory.PERAH,
                gender = "Betina",
                weight = 50,
                age = 2.1,
                price = 7800000,
                location = "Bengkalis",
                description = "Kambing Anglo Nubian ras premium. Struktur glandula mamae sangat sempurna, menghasilkan produksi susu gurih dengan kadar lemak butterfat tinggi yang sangat cocok diproses menjadi keju premium."
            )
        )
        for (goat in defaultGoats) {
            database.collection("goats").document(goat.id).set(goat.toMap())
        }
    }

    private fun seedDefaultMessages() {
        val database = db ?: return
        val defaultMessages = listOf(
            MessageItem(
                id = "MSG_101",
                content = "Hari ini",
                sender = MessageSender.SYSTEM,
                timestamp = ""
            ),
            MessageItem(
                id = "MSG_102",
                content = "Assalamualaikum, Pak Budi. Stok Kambing Etawa tersedia?",
                sender = MessageSender.BREEDER_ETAWA,
                timestamp = "09:15"
            ),
            MessageItem(
                id = "MSG_103",
                content = "Waalaikumussalam! Alhamdulillah tersedia, ada 5 ekor Etawa jantan.",
                sender = MessageSender.USER,
                timestamp = "09:17"
            ),
            MessageItem(
                id = "MSG_104",
                content = "Berapa harga per ekornya, Pak?",
                sender = MessageSender.BREEDER_ETAWA,
                timestamp = "09:18"
            ),
            MessageItem(
                id = "MSG_105",
                content = "[PRODUCT_CARD]Harga Rp 5,5 juta/ekor. Bisa nego",
                sender = MessageSender.USER,
                timestamp = "09:20"
            ),
            MessageItem(
                id = "MSG_106",
                content = "Kalau ambil 3 ekor bisa Rp 4,8 juta per ekor?",
                sender = MessageSender.BREEDER_ETAWA,
                timestamp = "09:22"
            ),
            MessageItem(
                id = "MSG_107",
                content = "Boleh, saya kirimkan detailnya",
                sender = MessageSender.USER,
                timestamp = "09:23"
            ),
            MessageItem(
                id = "MSG_108",
                content = "...",
                sender = MessageSender.BREEDER_ETAWA,
                timestamp = "09:24"
            )
        )
        for (msg in defaultMessages) {
            database.collection("messages").document(msg.id).set(msg.toMap())
        }
    }

    private fun seedDefaultOrders() {
        val database = db ?: return
        val goatEtawa1 = GoatItem(
            name = "Kambing Etawa",
            category = GoatCategory.ETAWA,
            gender = "Betina",
            weight = 45,
            age = 1.5,
            price = 4800000,
            location = "Bengkalis",
            description = ""
        )

        val goatPE = GoatItem(
            name = "Kambing PE",
            category = GoatCategory.ETAWA,
            gender = "Betina",
            weight = 45,
            age = 1.5,
            price = 4800000,
            location = "Bengkalis",
            description = ""
        )

        val goatEtawa2 = GoatItem(
            name = "Kambing Etawa",
            category = GoatCategory.ETAWA,
            gender = "Betina",
            weight = 45,
            age = 1.0,
            price = 4800000,
            location = "Bengkalis",
            description = ""
        )

        val defaultOrders = listOf(
            OrderItem(
                id = "AG-200626-001",
                goat = goatEtawa1,
                selectedWeight = 45,
                totalPrice = 4800000,
                status = OrderStatus.SHIPPING,
                orderDate = "20 Jun 2026"
            ),
            OrderItem(
                id = "AG-180626-002",
                goat = goatPE,
                selectedWeight = 45,
                totalPrice = 4800000,
                status = OrderStatus.SHIPPING,
                orderDate = "18 Jun 2026"
            ),
            OrderItem(
                id = "AG-150626-003",
                goat = goatEtawa2,
                selectedWeight = 45,
                totalPrice = 4800000,
                status = OrderStatus.COMPLETED,
                orderDate = "15 Jun 2024"
            ),
            OrderItem(
                id = "AG-100626-004",
                goat = goatEtawa2,
                selectedWeight = 45,
                totalPrice = 4800000,
                status = OrderStatus.COMPLETED,
                orderDate = "10 Jun 2026"
            )
        )
        for (order in defaultOrders) {
            database.collection("orders").document(order.id).set(order.toMap())
        }
    }

    private fun seedDefaultNotifications() {
        val database = db ?: return
        val defaultNotifications = listOf(
            NotificationItem(
                id = "NT_101",
                title = "Pesanan Dikirim 🚚",
                message = "Pesanan AG-2024-0870 sedang dalam perjalanan ke lokasi Anda.",
                type = NotificationType.ORDER_STATUS,
                timestamp = "2 jam yang lalu",
                isRead = false
            ),
            NotificationItem(
                id = "NT_102",
                title = "Promo Spesial Hari Raya! 🎉",
                message = "Dapatkan diskon hingga 20% untuk pembelian Kambing Potong khusus minggu ini.",
                type = NotificationType.PROMO,
                timestamp = "5 jam yang lalu",
                isRead = false
            ),
            NotificationItem(
                id = "NT_103",
                title = "Pembayaran Berhasil ✅",
                message = "Pembayaran untuk pesanan AG-2024-0871 telah kami terima. Pesanan sedang diproses.",
                type = NotificationType.ORDER_STATUS,
                timestamp = "Kemarin",
                isRead = true
            ),
            NotificationItem(
                id = "NT_104",
                title = "Selamat Datang di Agro Goat!",
                message = "Temukan berbagai jenis kambing berkualitas dengan harga terbaik langsung dari peternak.",
                type = NotificationType.SYSTEM,
                timestamp = "2 hari yang lalu",
                isRead = true
            )
        )
        for (notif in defaultNotifications) {
            database.collection("notifications").document(notif.id).set(notif.toMap())
        }
    }

    private fun seedDefaultUsers() {
        val database = db ?: return
        val defaultProfiles = listOf(
            mapOf(
                "name" to "Akun Ganda",
                "address" to "Bengkalis, Riau",
                "balance" to 15000000L,
                "role" to "Pedagang",
                "roles" to listOf("Pedagang", "Penjual"),
                "email" to "multi@agrogoat.com",
                "phone" to "08123456789",
                "password" to "password123"
            ),
            mapOf(
                "name" to "Pak Pedagang",
                "address" to "Bengkalis, Riau",
                "balance" to 12500000L,
                "role" to "Pedagang",
                "roles" to listOf("Pedagang"),
                "email" to "pedagang@agrogoat.com",
                "phone" to "08123456789",
                "password" to "password123"
            ),
            mapOf(
                "name" to "Pak Peternak",
                "address" to "Bengkalis, Riau",
                "balance" to 0L,
                "role" to "Penjual",
                "roles" to listOf("Penjual"),
                "email" to "penjual@agrogoat.com",
                "phone" to "08123456789",
                "password" to "password123"
            )
        )
        for (profile in defaultProfiles) {
            val email = profile["email"] as String
            database.collection("users_profiles").document(email).set(profile)
        }
    }

    override fun onCleared() {
        super.onCleared()
        goatsListener?.remove()
        ordersListener?.remove()
        messagesListener?.remove()
        notificationsListener?.remove()
        profileListener?.remove()
    }
}
