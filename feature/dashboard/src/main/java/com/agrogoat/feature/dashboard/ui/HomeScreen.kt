package com.agrogoat.feature.dashboard.ui
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
import com.agrogoat.core.designsystem.R
import com.agrogoat.core.designsystem.components.*
import com.agrogoat.core.model.*
import com.agrogoat.core.designsystem.components.*
import com.agrogoat.core.designsystem.components.*
import com.agrogoat.core.designsystem.components.detail.GoatDetailView
import com.agrogoat.core.shared.AppTab
import com.agrogoat.core.shared.AppSubScreen
import com.agrogoat.core.shared.AgroGoatViewModel
import kotlinx.coroutines.launch



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
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount = remember(notifications) { notifications.count { !it.isRead } }

    // Booking Form Input States
    val currentUserName by viewModel.userName.collectAsState()
    val currentUserPhone by viewModel.userPhone.collectAsState()
    val currentUserEmail by viewModel.userEmail.collectAsState()
    val usersProfiles by viewModel.usersProfiles.collectAsState()

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
    val currentSubScreen by viewModel.homeSubScreen.collectAsState()

    val selectedGoat by viewModel.homeSelectedGoat.collectAsState()


    LaunchedEffect(currentSubScreen, currentTab) {
        if (currentTab == AppTab.BERANDA) {
            viewModel.setHideBottomBar(currentSubScreen != AppSubScreen.LIST)
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

        AppSubScreen.LIST -> {
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
                            unreadCount = unreadCount,
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
                            onClick = {}
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
                            val categories = listOf("Semua", "Potong", "Etawa", "Perah")
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
                                    viewModel.setHomeSelectedGoat(goat)
                                    viewModel.setHomeSubScreen(AppSubScreen.DETAIL)
                                }
                            )
                        }
                    }
                }
            }
        }

        AppSubScreen.DETAIL -> {
            selectedGoat?.let { goat ->
                val sellerProfile = goat.sellerEmail?.lowercase()?.let { usersProfiles[it] }
                    ?: goat.sellerUid?.let { usersProfiles[it] }
                    ?: goat.sellerEmail?.let { email -> usersProfiles.values.find { it["email"] == email } }

                val sellerLocationStr = sellerProfile?.get("address") as? String
                val sellerMapsUrl = sellerProfile?.get("mapsUrl") as? String
                val sellerLat = (sellerProfile?.get("locationLat") as? Number)?.toDouble()
                val sellerLng = (sellerProfile?.get("locationLng") as? Number)?.toDouble()

                GoatDetailView(
                    goat = goat,
                    sellerName = sellerProfile?.get("name") as? String ?: goat.sellerEmail?.substringBefore("@") ?: "",
                    sellerPhotoUrl = sellerProfile?.get("photoUrl") as? String,
                    sellerLocationStr = sellerLocationStr,
                    sellerMapsUrl = sellerMapsUrl,
                    sellerLat = sellerLat,
                    sellerLng = sellerLng,
                    onBack = {
                        viewModel.setHomeSubScreen(AppSubScreen.LIST)
                    },
                    onToggleFav = {
                        viewModel.toggleFavorite(goat.id)
                    },
                    onChat = {
                        val emailTujuan = goat.sellerEmail?.takeIf { email -> email.isNotBlank() } ?: "admin@agrogoat.com"
                        viewModel.setReturnTabAfterChat(AppTab.BERANDA)
                        viewModel.startChatWith(emailTujuan)
                        viewModel.sendMessage("[PRODUCT_CARD]${goat.id}", recipientUid = emailTujuan)
                    },
                    onOrder = {
                        viewModel.setHomeSubScreen(AppSubScreen.BOOKING_STEP1)
                    }
                )
            }
        }

        AppSubScreen.BOOKING_STEP1 -> {
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
                    onBack = { viewModel.setHomeSubScreen(AppSubScreen.DETAIL) },
                    onNext = {
                        if (buyerName.isBlank() || buyerPhone.isBlank()) {
                            Toast.makeText(context, "Nama Lengkap dan Nomor HP harus diisi!", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.setHomeSubScreen(AppSubScreen.BOOKING_STEP2)
                        }
                    }
                )
            }
        }

        AppSubScreen.BOOKING_STEP2 -> {
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
                    onBack = { viewModel.setHomeSubScreen(AppSubScreen.BOOKING_STEP1) },
                    onNext = { viewModel.setHomeSubScreen(AppSubScreen.BOOKING_STEP3) }
                )
            }
        }

        AppSubScreen.BOOKING_STEP3 -> {
            selectedGoat?.let { goat ->
                BookingStep3View(
                    goat = goat,
                    name = buyerName,
                    phone = buyerPhone,
                    email = buyerEmail,
                    date = selectedDate,
                    timeSlot = selectedTimeSlot,
                    notes = buyerNotes,
                    onBack = { viewModel.setHomeSubScreen(AppSubScreen.BOOKING_STEP2) },
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
                        viewModel.setHomeSubScreen(AppSubScreen.BOOKING_SUCCESS)
                    }
                )
            }
        }

        AppSubScreen.BOOKING_SUCCESS -> {
            val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current
            BookingSuccessView(
                bookingCode = bookingCode,
                onCopyCode = {
                    clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(bookingCode))
                    Toast.makeText(context, "Kode Booking disalin!", Toast.LENGTH_SHORT).show()
                },
                onViewOrders = {
                    viewModel.setHomeSubScreen(AppSubScreen.LIST)
                    viewModel.setTab(AppTab.PESANAN)
                },
                onBackToHome = {
                    viewModel.setHomeSubScreen(AppSubScreen.LIST)
                    viewModel.setTab(AppTab.BERANDA)
                }
            )
        }
    }



}

