package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

    // Theme state (false = light theme, true = dark theme)
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
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
    private val _userName = MutableStateFlow("Eko Prasetyo")
    val userName: StateFlow<String> = _userName.asStateFlow()

    private val _userBalance = MutableStateFlow(12500000L) // in IDR
    val userBalance: StateFlow<Long> = _userBalance.asStateFlow()

    private val _userAddress = MutableStateFlow("Bengkalis, Riau")
    val userAddress: StateFlow<String> = _userAddress.asStateFlow()

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
        setupDefaultGoats()
        setupDefaultMessages()
        setupDefaultOrders()
        setupDefaultNotifications()
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
        _notifications.value = _notifications.value.map {
            if (it.id == notifId) it.copy(isRead = true) else it
        }
    }

    fun markAllNotificationsAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    fun updateProfile(name: String, address: String) {
        if (name.isNotBlank()) _userName.value = name
        if (address.isNotBlank()) _userAddress.value = address
    }

    fun topUpBalance(amount: Long) {
        _userBalance.value += amount
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
        _goats.value = _goats.value.map {
            if (it.id == goatId) {
                it.copy(isFavorite = !it.isFavorite)
            } else {
                it
            }
        }
    }

    /**
     * Places a real order in the Orders list
     */
    fun createOrder(goat: GoatItem, targetWeight: Int) {
        val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID")).format(Date())
        
        // Calculate dynamic price based on weight adjustment
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

        _orders.value = listOf(newOrder) + _orders.value
        
        // Add notification for new order
        val newNotif = NotificationItem(
            title = "Pesanan Dibuat",
            message = "Pesanan ${goat.name} menunggu pembayaran transfer bank.",
            type = NotificationType.ORDER_STATUS,
            timestamp = "Baru saja"
        )
        _notifications.value = listOf(newNotif) + _notifications.value
        
        openPayment(newOrder)
    }

    /**
     * Completes the payment process (manual bank transfer version)
     */
    fun processBankTransferPayment(orderId: String) {
        _orders.value = _orders.value.map { ord ->
            if (ord.id == orderId && ord.status == OrderStatus.PENDING_PAYMENT) {
                ord.copy(status = OrderStatus.PACKING)
            } else {
                ord
            }
        }

        // Success notification
        val notif = NotificationItem(
            title = "Bukti Terkirim ✅",
            message = "Bukti transfer telah diterima admin. Pesanan Anda sedang diverifikasi.",
            type = NotificationType.ORDER_STATUS,
            timestamp = "Baru saja"
        )
        _notifications.value = listOf(notif) + _notifications.value

        setTab(AppTab.PESANAN)
        
        // Simulate progress to shipment
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
    }

    /**
     * Original balance payment (Legacy)
     */
    fun processOrderPayment(orderId: String) {
        processBankTransferPayment(orderId)
    }

    /**
     * Sends custom user message and triggers smart breeder replies
     */
    fun sendMessage(content: String) {
        if (content.isBlank()) return

        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val userMsg = MessageItem(
            content = content,
            sender = MessageSender.USER,
            timestamp = timeStr
        )

        _messages.value = _messages.value + userMsg

        // Trigger smart automated reply from breeders based on text
        viewModelScope.launch {
            delay(1200) // Realistic delay

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
                content = replyText,
                sender = sender,
                timestamp = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            )
            _messages.value = _messages.value + replyMsg
        }
    }

    private fun setupDefaultGoats() {
        _goats.value = listOf(
            GoatItem(
                name = "Kambing Etawa",
                category = GoatCategory.ETAWA,
                gender = "Jantan",
                weight = 55,
                age = 1.5,
                price = 5500000,
                location = "Bengkalis",
                description = "Kambing Etawa unggulan dengan postur tubuh besar, tegap, telinga melipat dengan baik, dan bulu yang subur. Sangat cocok sebagai hewan indukan pejantan tangguh maupun untuk keperluan kontes.",
                isNew = true,
                isFavorite = false
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
                isNew = false,
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
                isNew = true,
                isFavorite = false
            ),
            GoatItem(
                name = "Kambing Kacang Super",
                category = GoatCategory.POTONG,
                gender = "Jantan",
                weight = 32,
                age = 1.0,
                price = 1950000,
                location = "Bengkalis",
                description = "Kambing Kacang lokal lincah, aktif, dipelihara dengan metode rumping rumput hijauan alam liar. Kualitas daging sangat padat dan gurih, sangat digemari restoran kuliner sate.",
                isNew = false,
                isFavorite = false
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
                isNew = true,
                isFavorite = false
            ),
            GoatItem(
                name = "Anglo Nubian Pedigree",
                category = GoatCategory.PERAH,
                gender = "Betina",
                weight = 50,
                age = 2.1,
                price = 7800000,
                location = "Bengkalis",
                description = "Kambing Anglo Nubian ras premium. Struktur glandula mamae sangat sempurna, menghasilkan produksi susu gurih dengan kadar lemak butterfat tinggi yang sangat cocok diproses menjadi keju premium.",
                isNew = false,
                isFavorite = false
            )
        )
    }

    private fun setupDefaultMessages() {
        _messages.value = listOf(
            MessageItem(
                content = "Hari ini",
                sender = MessageSender.SYSTEM,
                timestamp = ""
            ),
            MessageItem(
                content = "Assalamualaikum, Pak Budi. Stok Kambing Etawa tersedia?",
                sender = MessageSender.USER,
                timestamp = "09:15"
            ),
            MessageItem(
                content = "Waalaikumussalam! Alhamdulillah tersedia, ada 5 ekor Etawa jantan.",
                sender = MessageSender.BREEDER_ETAWA,
                timestamp = "09:17"
            ),
            MessageItem(
                content = "Berapa harga per ekornya, Pak?",
                sender = MessageSender.USER,
                timestamp = "09:18"
            ),
            MessageItem(
                content = "[PRODUCT_CARD]Harga Rp 5,5 juta/ekor. Bisa nego",
                sender = MessageSender.BREEDER_ETAWA,
                timestamp = "09:20"
            ),
            MessageItem(
                content = "Kalau ambil 3 ekor bisa Rp 4,8 juta per ekor?",
                sender = MessageSender.USER,
                timestamp = "09:22"
            ),
            MessageItem(
                content = "Boleh, saya kirimkan detailnya",
                sender = MessageSender.BREEDER_ETAWA,
                timestamp = "09:23"
            ),
            MessageItem(
                content = "...",
                sender = MessageSender.USER,
                timestamp = "09:24"
            )
        )
    }

    private fun setupDefaultOrders() {
        val goatEtawaJantan = GoatItem(
            name = "Kambing Etawa",
            category = GoatCategory.ETAWA,
            gender = "Jantan",
            weight = 55,
            age = 1.5,
            price = 5500000,
            location = "Bengkalis",
            description = ""
        )

        val goatPEJantan = GoatItem(
            name = "Kambing PE",
            category = GoatCategory.ETAWA,
            gender = "Jantan",
            weight = 40,
            age = 2.0,
            price = 1550000,
            location = "Bengkalis",
            description = ""
        )

        val goatEtawaBetina = GoatItem(
            name = "Kambing Etawa",
            category = GoatCategory.ETAWA,
            gender = "Betina",
            weight = 45,
            age = 1.5,
            price = 4800000,
            location = "Bengkalis",
            description = ""
        )

        val goatBoerJantan = GoatItem(
            name = "Kambing Boer",
            category = GoatCategory.POTONG,
            gender = "Jantan",
            weight = 60,
            age = 2.0,
            price = 4000000,
            location = "Bengkalis",
            description = ""
        )

        _orders.value = listOf(
            OrderItem(
                id = "AG-2024-0871",
                goat = goatEtawaJantan,
                selectedWeight = 55,
                totalPrice = 5500000,
                status = OrderStatus.PACKING,
                orderDate = "20 Jun 2024, 08:30"
            ),
            OrderItem(
                id = "AG-2024-0870",
                goat = goatPEJantan,
                selectedWeight = 40,
                totalPrice = 3100000,
                status = OrderStatus.SHIPPING,
                orderDate = "18 Jun 2024, 14:20"
            ),
            OrderItem(
                id = "AG-2024-0869",
                goat = goatEtawaBetina,
                selectedWeight = 45,
                totalPrice = 4800000,
                status = OrderStatus.COMPLETED,
                orderDate = "15 Jun 2024, 10:15"
            ),
            OrderItem(
                id = "AG-2024-0868",
                goat = goatBoerJantan,
                selectedWeight = 60,
                totalPrice = 4000000,
                status = OrderStatus.COMPLETED,
                orderDate = "10 Jun 2024, 16:45"
            )
        )
    }

    private fun setupDefaultNotifications() {
        _notifications.value = listOf(
            NotificationItem(
                title = "Pesanan Dikirim 🚚",
                message = "Pesanan AG-2024-0870 sedang dalam perjalanan ke lokasi Anda.",
                type = NotificationType.ORDER_STATUS,
                timestamp = "2 jam yang lalu",
                isRead = false
            ),
            NotificationItem(
                title = "Promo Spesial Hari Raya! 🎉",
                message = "Dapatkan diskon hingga 20% untuk pembelian Kambing Potong khusus minggu ini.",
                type = NotificationType.PROMO,
                timestamp = "5 jam yang lalu",
                isRead = false
            ),
            NotificationItem(
                title = "Pembayaran Berhasil ✅",
                message = "Pembayaran untuk pesanan AG-2024-0871 telah kami terima. Pesanan sedang diproses.",
                type = NotificationType.ORDER_STATUS,
                timestamp = "Kemarin",
                isRead = true
            ),
            NotificationItem(
                title = "Selamat Datang di Agro Goat!",
                message = "Temukan berbagai jenis kambing berkualitas dengan harga terbaik langsung dari peternak.",
                type = NotificationType.SYSTEM,
                timestamp = "2 hari yang lalu",
                isRead = true
            )
        )
    }
}
