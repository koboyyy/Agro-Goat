package com.agrogoat.app.ui.screens
import androidx.compose.material.icons.outlined.*

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrogoat.app.R
import com.agrogoat.app.data.*
import com.agrogoat.app.ui.components.*
import com.agrogoat.app.ui.components.detail.GoatDetailView
import com.agrogoat.app.viewmodel.AppTab
import com.agrogoat.app.viewmodel.AgroGoatViewModel
import kotlinx.coroutines.launch

enum class HomeSubScreen {
    HOME,
    DETAIL,
    BOOKING_STEP1,
    BOOKING_STEP2,
    BOOKING_STEP3,
    BOOKING_SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: AgroGoatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // States from VM
    val userName by viewModel.userName.collectAsState()
    val goats by viewModel.goats.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedHomeCategory by viewModel.selectedHomeCategory.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()

    // Booking Form Input States
    val currentUserName by viewModel.userName.collectAsState()
    val currentUserPhone by viewModel.userPhone.collectAsState()
    val currentUserEmail by viewModel.userEmail.collectAsState()

    var buyerName by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var buyerEmail by remember { mutableStateOf("") }
    var buyerNotes by remember { mutableStateOf("") }

    LaunchedEffect(currentUserName, currentUserPhone, currentUserEmail) {
        if (buyerName.isBlank() && currentUserName.isNotEmpty()) {
            buyerName = currentUserName
        }
        if (buyerPhone.isBlank() && currentUserPhone.isNotEmpty()) {
            buyerPhone = currentUserPhone
        }
        if (buyerEmail.isBlank() && currentUserEmail.isNotEmpty()) {
            buyerEmail = currentUserEmail
        }
    }

    var selectedDate by remember { mutableStateOf("20 Juni 2026") }
    var selectedTimeSlot by remember { mutableStateOf("09.00 - 10.00") }
    var bookingCode by remember { mutableStateOf("") }

    // Gender filter state
    var selectedGenderFilter by remember { mutableStateOf("Semua") }

    // Sheet states
    var currentSubScreen by remember {
        mutableStateOf(HomeSubScreen.HOME)
    }

    var selectedGoat by remember {
        mutableStateOf<GoatItem?>(null)
    }

    var showPromoCouponSheet by remember {
        mutableStateOf(false)
    }

    LaunchedEffect(currentSubScreen, currentTab) {
        if (currentTab == AppTab.BERANDA) {
            viewModel.setHideBottomBar(currentSubScreen != HomeSubScreen.HOME)
        }
    }

    // Filtered lists
    val filteredGoats = remember(goats, searchQuery, selectedHomeCategory, selectedGenderFilter) {
        goats.filter { goat ->
            val matchesSearch = goat.name.contains(searchQuery, ignoreCase = true) ||
                    goat.location.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedHomeCategory == null || goat.category == selectedHomeCategory
            val matchesGender = selectedGenderFilter == "Semua" || goat.gender.equals(selectedGenderFilter, ignoreCase = true)
            matchesSearch && matchesCategory && matchesGender && goat.isAvailable
        }
    }

    when (currentSubScreen) {

        HomeSubScreen.HOME -> {
            Scaffold(
                modifier = modifier.fillMaxSize(),
                containerColor = Color(0xFFF5F6F5) // light gray dashboard background
            ) { innerPadding ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentPadding = PaddingValues(bottom = 90.dp) // buffer for nav bar
                ) {
                    // Header Info Bar
                    item {
                        AppHeader(
                            userName = userName,
                            onNotificationClick = {
                                viewModel.setTab(AppTab.NOTIFIKASI)
                            },
                            onProfileClick = {
                                viewModel.setTab(AppTab.PROFIL)
                            }
                        )
                    }

                    // Search and Filter Bar
                    item {
                        SearchAndFilterBar(
                            query = searchQuery,
                            onQueryChange = { viewModel.setSearchQuery(it) },
                            onFilterClick = {
                                // Switch to catalog with sorting menu visible
                                viewModel.setTab(AppTab.KATALOG)
                                Toast.makeText(context, "Membuka katalog dengan filter penyortiran aktif!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    // Special Promo Banner
                    item {
                        PromoSpecialBanner(
                            onClick = { showPromoCouponSheet = true }
                        )
                    }

                    // Kategori (Categories) Section
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp, bottom = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Kategori",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.3).sp
                                    ),
                                    color = Color.Black
                                )
                                Text(
                                    text = "Lihat Semua →",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier
                                        .clickable {
                                            selectedGenderFilter = "Semua"
                                        }
                                        .testTag("view_all_categories")
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CategoryCardItem(
                                    label = "Semua",
                                    iconBgColor = Color(0xFFE8F5E9),
                                    iconRes = R.drawable.icon_kategori_semua,
                                    isSelected = selectedGenderFilter == "Semua",
                                    onClick = { selectedGenderFilter = "Semua" },
                                    modifier = Modifier.weight(1f)
                                )
                                CategoryCardItem(
                                    label = "Jantan",
                                    iconBgColor = Color(0xFFFFF3E0),
                                    iconRes = R.drawable.icon_kambing_kategori_jantan,
                                    isSelected = selectedGenderFilter == "Jantan",
                                    onClick = { selectedGenderFilter = "Jantan" },
                                    modifier = Modifier.weight(1f)
                                )
                                CategoryCardItem(
                                    label = "Betina",
                                    iconBgColor = Color(0xFFFFFDE7),
                                    iconRes = R.drawable.icon_kambing_kategori_betina,
                                    isSelected = selectedGenderFilter == "Betina",
                                    onClick = { selectedGenderFilter = "Betina" },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Rekomendasi (Recommendations) Section
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rekomendasi",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.3).sp
                                    ),
                                    color = Color.Black
                                )
                                Text(
                                    text = "Lihat Semua →",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier
                                        .clickable {
                                            viewModel.setTab(AppTab.KATALOG)
                                        }
                                        .testTag("view_all_goats")
                                )
                            }

                            // Active filter tag pill
                            val tagLabel = when (selectedGenderFilter) {
                                "Jantan" -> "Kambing Jantan"
                                "Betina" -> "Kambing Betina"
                                else -> "Kambing Semua"
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = tagLabel,
                                    color = Color(0xFF2E7D32),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // List of goats matching category or search query
                    if (filteredGoats.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 40.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFFF5F5F5)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    GoatSilhouette(Modifier.size(50.dp), Color.LightGray)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Kambing tidak ditemukan",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Gray,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Cobalah kata kunci pencarian atau kategori lain.",
                                    color = Color.LightGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    } else {
                        items(filteredGoats) { goat ->
                            GoatVerticalRowItem(
                                goat = goat,
                                onFavoriteToggle = { viewModel.toggleFavorite(goat.id) },
                                onClick = {
                                    selectedGoat = goat
                                    currentSubScreen = HomeSubScreen.DETAIL
                                }
                            )
                        }
                    }
                }
            }
        }

        HomeSubScreen.DETAIL -> {
            selectedGoat?.let {
                GoatDetailView(
                    goat = it,
                    onBack = {
                        currentSubScreen = HomeSubScreen.HOME
                    },
                    onToggleFav = {
                        viewModel.toggleFavorite(it.id)
                    },
                    onChat = {
                        val emailTujuan = if (!it.sellerEmail.isNullOrBlank()) it.sellerEmail else "admin@agrogoat.com"
                        viewModel.startChatWith(emailTujuan)
                        currentSubScreen = HomeSubScreen.HOME
                    },
                    onOrder = {
                        currentSubScreen = HomeSubScreen.BOOKING_STEP1
                    }
                )
            }
        }

        HomeSubScreen.BOOKING_STEP1 -> {
            selectedGoat?.let { goat ->
                BookingStep1View(
                    goat = goat,
                    name = buyerName,
                    onNameChange = { buyerName = it },
                    phone = buyerPhone,
                    onPhoneChange = { buyerPhone = it },
                    email = buyerEmail,
                    onEmailChange = { buyerEmail = it },
                    notes = buyerNotes,
                    onNotesChange = { buyerNotes = it },
                    onBack = { currentSubScreen = HomeSubScreen.DETAIL },
                    onNext = {
                        if (buyerName.isBlank() || buyerPhone.isBlank()) {
                            Toast.makeText(context, "Nama Lengkap dan Nomor HP harus diisi!", Toast.LENGTH_SHORT).show()
                        } else {
                            currentSubScreen = HomeSubScreen.BOOKING_STEP2
                        }
                    }
                )
            }
        }

        HomeSubScreen.BOOKING_STEP2 -> {
            selectedGoat?.let { goat ->
                BookingStep2View(
                    selectedDate = selectedDate,
                    onDateClick = {
                        val calendar = java.util.Calendar.getInstance()
                        val datePickerDialog = android.app.DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val cal = java.util.Calendar.getInstance().apply {
                                    set(java.util.Calendar.YEAR, year)
                                    set(java.util.Calendar.MONTH, month)
                                    set(java.util.Calendar.DAY_OF_MONTH, dayOfMonth)
                                }
                                val sdf = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale("id", "ID"))
                                selectedDate = sdf.format(cal.time)
                            },
                            2026, 5, 20
                        )
                        datePickerDialog.show()
                    },
                    selectedTimeSlot = selectedTimeSlot,
                    onTimeSlotSelect = { selectedTimeSlot = it },
                    onBack = { currentSubScreen = HomeSubScreen.BOOKING_STEP1 },
                    onNext = { currentSubScreen = HomeSubScreen.BOOKING_STEP3 }
                )
            }
        }

        HomeSubScreen.BOOKING_STEP3 -> {
            selectedGoat?.let { goat ->
                BookingStep3View(
                    goat = goat,
                    name = buyerName,
                    phone = buyerPhone,
                    email = buyerEmail,
                    date = selectedDate,
                    timeSlot = selectedTimeSlot,
                    notes = buyerNotes,
                    onBack = { currentSubScreen = HomeSubScreen.BOOKING_STEP2 },
                    onNext = {
                        viewModel.createOrder(
                            goat = goat,
                            targetWeight = goat.weight,
                            buyerName = buyerName,
                            buyerPhone = buyerPhone,
                            buyerEmail = buyerEmail,
                            buyerNotes = buyerNotes,
                            bookingDate = selectedDate,
                            bookingTimeSlot = selectedTimeSlot
                        )
                        
                        val formattedDate = try {
                            val sdfInput = java.text.SimpleDateFormat("dd MMMM yyyy", java.util.Locale("id", "ID"))
                            val dateObj = sdfInput.parse(selectedDate) ?: java.util.Date()
                            val sdfOutput = java.text.SimpleDateFormat("ddMMyy", java.util.Locale.getDefault())
                            sdfOutput.format(dateObj)
                        } catch (e: Exception) {
                            "260620"
                        }
                        bookingCode = "AG-$formattedDate-001"
                        currentSubScreen = HomeSubScreen.BOOKING_SUCCESS
                    }
                )
            }
        }

        HomeSubScreen.BOOKING_SUCCESS -> {
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            BookingSuccessView(
                bookingCode = bookingCode,
                onCopyCode = {
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(bookingCode))
                    Toast.makeText(context, "Kode Booking disalin!", Toast.LENGTH_SHORT).show()
                },
                onViewOrders = {
                    currentSubScreen = HomeSubScreen.HOME
                    viewModel.setTab(AppTab.PESANAN)
                },
                onBackToHome = {
                    currentSubScreen = HomeSubScreen.HOME
                    viewModel.setTab(AppTab.BERANDA)
                }
            )
        }
    }



    // 2. PROMO BANNER SPECIAL SHEEET
    if (showPromoCouponSheet) {
        ModalBottomSheet(
            onDismissRequest = { showPromoCouponSheet = false },
            containerColor = Color(0xFF1B5E20), // matching promo visual green
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with White Close Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(
                        onClick = { showPromoCouponSheet = false },
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.1f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Outlined.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Visual elements
                Box(
                    modifier = Modifier.size(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CutePromoGoat(Modifier.fillMaxSize())
                }

                Text(
                    text = "Selamat! Anda Mendapatkan Kupon",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )

                Text(
                    text = "Kambing Sehat & Berkualitas",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = Color.White
                )

                // The Promo Code Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.15f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp).fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "KODE KUPON ANDA:",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "GOATBARU20",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFFD54F), // Amber color matching golden coupon style
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Potongan diskon langsung 20% otomatis diaplikasikan ke pembelian kambing pertama Anda.",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Button(
                    onClick = {
                        showPromoCouponSheet = false
                        viewModel.setTab(AppTab.KATALOG)
                        Toast.makeText(context, "Kupon diskon diaktifkan!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Text(
                        text = "Gunakan Sekarang",
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

