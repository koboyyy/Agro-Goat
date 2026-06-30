package com.agrogoat.feature.dashboard.ui
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.agrogoat.core.designsystem.R
import com.agrogoat.core.model.*
import com.agrogoat.core.designsystem.components.*
import com.agrogoat.core.designsystem.components.*
import com.agrogoat.core.shared.AgroGoatViewModel
import com.agrogoat.core.shared.CatalogSort
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.agrogoat.core.designsystem.components.detail.GoatDetailView
import com.agrogoat.core.shared.AppTab

enum class CatalogSubScreen {
    LIST,
    DETAIL,
    BOOKING_STEP1,
    BOOKING_STEP2,
    BOOKING_STEP3,
    BOOKING_SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: AgroGoatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    // Sub-Screen Router
    var currentSubScreen by remember { mutableStateOf(CatalogSubScreen.LIST) }
    var selectedGoat by remember { mutableStateOf<GoatItem?>(null) }

    // Booking Form Input States
    val currentUserName by viewModel.userName.collectAsState()
    val currentUserPhone by viewModel.userPhone.collectAsState()
    val currentUserEmail by viewModel.userEmail.collectAsState()

    var buyerName by remember { mutableStateOf("") }
    var buyerPhone by remember { mutableStateOf("") }
    var buyerEmail by remember { mutableStateOf("") }
    var buyerNotes by remember { mutableStateOf("") }

    // Seller info for detail screen
    val sellerNameForDetail by viewModel.userName.collectAsState()
    val sellerPhotoForDetail by viewModel.userPhotoUrl.collectAsState()

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

    // Date & Time selection states
    var selectedDate by remember { mutableStateOf("20 Juni 2026") }
    var selectedTimeSlot by remember { mutableStateOf("09.00 - 10.00") }
    var bookingCode by remember { mutableStateOf("") }

    val currentTab by viewModel.currentTab.collectAsState()

    LaunchedEffect(currentSubScreen, currentTab) {
        if (currentTab == AppTab.KATALOG) {
            viewModel.setHideBottomBar(currentSubScreen != CatalogSubScreen.LIST)
        }
    }

    // Collect list parameters from VM for the list sub-screen
    val goats by viewModel.goats.collectAsState()
    val usersProfiles by viewModel.usersProfiles.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentSort by viewModel.catalogSort.collectAsState()
    val showFavsOnly by viewModel.showFavoritesOnly.collectAsState()

    // Gender filter state (null = semua, "Jantan" = Jantan, "Betina" = Betina, "Lainnya" = Other)
    var selectedGender by remember { mutableStateOf<String?>(null) }

    // Dropdown open states
    var showPriceDropdown by remember { mutableStateOf(false) }
    var showAgeDropdown by remember { mutableStateOf(false) }
    var showWeightDropdown by remember { mutableStateOf(false) }
    var showLocationDropdown by remember { mutableStateOf(false) }

    // Dropdown selected values
    var selectedPriceFilter by remember { mutableStateOf("Semua") }
    var selectedAgeFilter by remember { mutableStateOf("Semua") }
    var selectedWeightFilter by remember { mutableStateOf("Semua") }
    var selectedLocationFilter by remember { mutableStateOf("Semua") }

    // Sorting sheet toggle
    var showSortingMenu by remember { mutableStateOf(false) }

    // Dynamic list calculations based on all interactive filters
    val processedGoats = remember(
        goats,
        searchQuery,
        selectedGender,
        currentSort,
        showFavsOnly,
        selectedPriceFilter,
        selectedAgeFilter,
        selectedWeightFilter,
        selectedLocationFilter
    ) {
        var list = goats.filter { goat ->
            goat.isAvailable &&
            // Search query match
            (searchQuery.isBlank() || 
             goat.name.contains(searchQuery, ignoreCase = true) || 
             goat.location.contains(searchQuery, ignoreCase = true)) &&
            // Gender match
            (selectedGender == null || 
             (selectedGender == "Jantan" && goat.gender.equals("Jantan", ignoreCase = true)) ||
             (selectedGender == "Betina" && goat.gender.equals("Betina", ignoreCase = true)) ||
             (selectedGender == "Lainnya" && !goat.gender.equals("Jantan", ignoreCase = true) && !goat.gender.equals("Betina", ignoreCase = true))) &&
            // Favorites filter
            (!showFavsOnly || goat.isFavorite) &&
            // Price Filter
            (selectedPriceFilter == "Semua" || when (selectedPriceFilter) {
                "< 2 Juta" -> goat.price < 2000000
                "2 - 5 Juta" -> goat.price in 2000000..5000000
                "> 5 Juta" -> goat.price > 5000000
                else -> true
            }) &&
            // Age Filter
            (selectedAgeFilter == "Semua" || when (selectedAgeFilter) {
                "< 1.5 Th" -> goat.age < 1.5
                "1.5 - 2 Th" -> goat.age in 1.5..2.0
                "> 2 Th" -> goat.age > 2.0
                else -> true
            }) &&
            // Weight Filter
            (selectedWeightFilter == "Semua" || when (selectedWeightFilter) {
                "< 50 kg" -> goat.weight < 50
                "50 - 60 kg" -> goat.weight in 50..60
                "> 60 kg" -> goat.weight > 60
                else -> true
            }) &&
            // Location Filter
            (selectedLocationFilter == "Semua" || goat.location.contains(selectedLocationFilter, ignoreCase = true))
        }

        list = when (currentSort) {
            CatalogSort.TERBARU -> list.sortedWith(compareByDescending { it.isNew })
            CatalogSort.HARGA_RENDAH -> list.sortedBy { it.price }
            CatalogSort.HARGA_TINGGI -> list.sortedByDescending { it.price }
            CatalogSort.BOBOT_TERBESAR -> list.sortedByDescending { it.weight }
        }
        list
    }

    // Sub-Screen Router Body
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF9FBF9))
    ) {
        Crossfade(targetState = currentSubScreen, label = "subscreen_router") { subScreen ->
            when (subScreen) {
                CatalogSubScreen.LIST -> {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        "Katalog Kambing",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                },
                                navigationIcon = {
                                    IconButton(
                                        onClick = { viewModel.setTab(com.agrogoat.core.shared.AppTab.BERANDA) },
                                        modifier = Modifier.testTag("catalog_back_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                            contentDescription = "Kembali",
                                            tint = MaterialTheme.colorScheme.onBackground,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.background
                                )
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.background
                    ) { innerPadding ->
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(1),
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            contentPadding = PaddingValues(bottom = 100.dp)
                        ) {
                            item {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp)
                                ) {
                                    // Search and Filter Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        TextField(
                                            value = searchQuery,
                                            onValueChange = { viewModel.setSearchQuery(it) },
                                            placeholder = {
                                                Text(
                                                    "Cari Kambing...",
                                                    color = Color.Gray.copy(alpha = 0.8f),
                                                    fontSize = 14.sp
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Outlined.Search,
                                                    contentDescription = null,
                                                    tint = Color.Gray,
                                                    modifier = Modifier.size(22.dp)
                                                )
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(52.dp)
                                                .shadow(elevation = 1.dp, shape = CircleShape)
                                                .testTag("catalog_search_input"),
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                                focusedIndicatorColor = Color.Transparent,
                                                unfocusedIndicatorColor = Color.Transparent,
                                                disabledIndicatorColor = Color.Transparent
                                            ),
                                            shape = CircleShape,
                                            singleLine = true
                                        )

                                        // Forest Green Filter Box
                                        val filterInteractionSource = remember { MutableInteractionSource() }
                                        val isFilterPressed by filterInteractionSource.collectIsPressedAsState()

                                        IconButton(
                                            onClick = { 
                                                viewModel.toggleFavoritesOnly()
                                                val msg = if (!showFavsOnly) "Menampilkan Kambing Favorit" else "Menampilkan Semua Kambing"
                                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                            },
                                            interactionSource = filterInteractionSource,
                                            modifier = Modifier
                                                .size(50.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (isFilterPressed) MaterialTheme.colorScheme.primary.copy(alpha = 0.85f) 
                                                    else MaterialTheme.colorScheme.primary
                                                )
                                                .testTag("catalog_green_filter_btn")
                                        ) {
                                            Icon(
                                                imageVector = if (showFavsOnly) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                                                contentDescription = "Saring Favorit",
                                                tint = Color.White,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Gender Category Row
                                    LazyRow(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp),
                                        contentPadding = PaddingValues(horizontal = 20.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                         val genders = listOf(
                                             null to "Semua",
                                             "Jantan" to "Jantan",
                                             "Betina" to "Betina",
                                             "Lainnya" to "Lainnya"
                                         )

                                        items(genders) { (genderVal, label) ->
                                            FilterTab(
                                                label = label,
                                                isSelected = selectedGender == genderVal,
                                                onClick = { selectedGender = genderVal }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(2.dp))

                                    // Dropdown Filters Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 6.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Harga
                                        Box(modifier = Modifier.weight(1f)) {
                                            FilterDropdown(
                                                label = "Harga",
                                                selectedValue = selectedPriceFilter,
                                                onClick = { showPriceDropdown = true },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            DropdownMenu(
                                                expanded = showPriceDropdown,
                                                onDismissRequest = { showPriceDropdown = false }
                                            ) {
                                                listOf("Semua", "< 2 Juta", "2 - 5 Juta", "> 5 Juta").forEach { opt ->
                                                    DropdownMenuItem(
                                                        text = { Text(opt, fontSize = 12.sp) },
                                                        onClick = {
                                                            selectedPriceFilter = opt
                                                            showPriceDropdown = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Umur
                                        Box(modifier = Modifier.weight(1f)) {
                                            FilterDropdown(
                                                label = "Umur",
                                                selectedValue = selectedAgeFilter,
                                                onClick = { showAgeDropdown = true },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            DropdownMenu(
                                                expanded = showAgeDropdown,
                                                onDismissRequest = { showAgeDropdown = false }
                                            ) {
                                                listOf("Semua", "< 1.5 Th", "1.5 - 2 Th", "> 2 Th").forEach { opt ->
                                                    DropdownMenuItem(
                                                        text = { Text(opt, fontSize = 12.sp) },
                                                        onClick = {
                                                            selectedAgeFilter = opt
                                                            showAgeDropdown = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Berat
                                        Box(modifier = Modifier.weight(1f)) {
                                            FilterDropdown(
                                                label = "Berat",
                                                selectedValue = selectedWeightFilter,
                                                onClick = { showWeightDropdown = true },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            DropdownMenu(
                                                expanded = showWeightDropdown,
                                                onDismissRequest = { showWeightDropdown = false }
                                            ) {
                                                listOf("Semua", "< 50 kg", "50 - 60 kg", "> 60 kg").forEach { opt ->
                                                    DropdownMenuItem(
                                                        text = { Text(opt, fontSize = 12.sp) },
                                                        onClick = {
                                                            selectedWeightFilter = opt
                                                            showWeightDropdown = false
                                                        }
                                                    )
                                                }
                                            }
                                        }

                                        // Lokasi
                                        Box(modifier = Modifier.weight(1f)) {
                                            FilterDropdown(
                                                label = "Lokasi",
                                                selectedValue = selectedLocationFilter,
                                                onClick = { showLocationDropdown = true },
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                            DropdownMenu(
                                                expanded = showLocationDropdown,
                                                onDismissRequest = { showLocationDropdown = false }
                                            ) {
                                                listOf("Semua", "Bengkalis", "Bukit Batu", "Bandar Laksamana").forEach { opt ->
                                                    DropdownMenuItem(
                                                        text = { Text(opt, fontSize = 12.sp) },
                                                        onClick = {
                                                            selectedLocationFilter = opt
                                                            showLocationDropdown = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    // Sorting Button
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 20.dp, vertical = 6.dp)
                                    ) {
                                        val sortLabel = when (currentSort) {
                                            CatalogSort.TERBARU -> "Terbaru"
                                            CatalogSort.HARGA_RENDAH -> "Harga: Terendah"
                                            CatalogSort.HARGA_TINGGI -> "Harga: Tertinggi"
                                            CatalogSort.BOBOT_TERBESAR -> "Berat: Terbesar"
                                        }

                                        Box {
                                            Box(
                                                modifier = Modifier
                                                    .border(1.dp, Color(0xFF2E7D32), RoundedCornerShape(12.dp))
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .clickable(onClick = { showSortingMenu = !showSortingMenu })
                                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                            ) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                ) {
                                                    Text(
                                                        text = "Urutkan: ",
                                                        color = Color.Black,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp
                                                    )
                                                    Text(
                                                        text = sortLabel,
                                                        color = Color(0xFF2E7D32),
                                                        fontWeight = FontWeight.Medium,
                                                        fontSize = 13.sp
                                                    )
                                                    Icon(
                                                        imageVector = Icons.Outlined.ArrowDropDown,
                                                        contentDescription = null,
                                                        tint = Color(0xFF2E7D32),
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }

                                            if (showSortingMenu) {
                                                Popup(
                                                    onDismissRequest = { showSortingMenu = false },
                                                    offset = androidx.compose.ui.unit.IntOffset(
                                                        0,
                                                        with(androidx.compose.ui.platform.LocalDensity.current) { 46.dp.roundToPx() }
                                                    ),
                                                    properties = PopupProperties(focusable = true)
                                                ) {
                                                    Card(
                                                        modifier = Modifier
                                                            .width(220.dp)
                                                            .padding(top = 4.dp),
                                                        shape = RoundedCornerShape(16.dp),
                                                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                                                        colors = CardDefaults.cardColors(
                                                            containerColor = Color.White
                                                        ),
                                                        elevation = CardDefaults.cardElevation(
                                                            defaultElevation = 8.dp
                                                        )
                                                    ) {
                                                        Column(
                                                            modifier = Modifier.padding(12.dp),
                                                            verticalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            val options = listOf(
                                                                CatalogSort.TERBARU to "Terbaru",
                                                                CatalogSort.HARGA_RENDAH to "Harga: Terendah",
                                                                CatalogSort.HARGA_TINGGI to "Harga: Tertinggi",
                                                                CatalogSort.BOBOT_TERBESAR to "Berat: Terbesar"
                                                            )
                                                            options.forEach { (sortType, label) ->
                                                                val isActive = currentSort == sortType
                                                                SortDropdownItem(
                                                                    label = label,
                                                                    isActive = isActive,
                                                                    isTerbaru = sortType == CatalogSort.TERBARU,
                                                                    onClick = {
                                                                        viewModel.setCatalogSort(sortType)
                                                                        showSortingMenu = false
                                                                    }
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (processedGoats.isEmpty()) {
                                item {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 100.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(90.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            GoatSilhouette(Modifier.size(54.dp), Color.LightGray)
                                        }
                                        Spacer(modifier = Modifier.height(14.dp))
                                        Text(
                                            text = "Tidak ada hasil ditemukan",
                                            fontWeight = FontWeight.Bold,
                                            color = Color.Gray,
                                            fontSize = 15.sp
                                        )
                                        Text(
                                            text = "Atur kembali kombinasi filter Anda.",
                                            color = Color.LightGray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            } else {
                                items(processedGoats) { goat ->
                                    GoatVerticalRowItem(
                                        goat = goat,
                                        onFavoriteToggle = { viewModel.toggleFavorite(goat.id) },
                                        onClick = {
                                            selectedGoat = goat
                                            currentSubScreen = CatalogSubScreen.DETAIL
                                        }
                                    )
                                }
                            }
                        }
                    }


                }

                CatalogSubScreen.DETAIL -> {
                    selectedGoat?.let { goat ->
                        val sellerProfile = goat.sellerUid?.let { usersProfiles[it] } 
                            ?: goat.sellerEmail?.let { email -> usersProfiles.values.find { it["email"] == email } }
                        
                        val sellerLocationStr = sellerProfile?.get("address") as? String
                        val sellerMapsUrl = sellerProfile?.get("mapsUrl") as? String
                        val sellerLat = sellerProfile?.get("locationLat") as? Double
                        val sellerLng = sellerProfile?.get("locationLng") as? Double

                        GoatDetailView(
                            goat = goat,
                            sellerName = sellerNameForDetail,
                            sellerPhotoUrl = sellerPhotoForDetail,
                            sellerLocationStr = sellerLocationStr,
                            sellerMapsUrl = sellerMapsUrl,
                            sellerLat = sellerLat,
                            sellerLng = sellerLng,
                            onBack = {
                                currentSubScreen = CatalogSubScreen.LIST
                            },
                            onToggleFav = {
                                viewModel.toggleFavorite(goat.id)
                            },
                            onChat = {
                                val targetEmail = goat.sellerEmail?.takeIf { it.isNotBlank() } ?: "admin@agrogoat.com"
                                viewModel.startChatWith(targetEmail)
                                currentSubScreen = CatalogSubScreen.LIST
                            },
                            onOrder = {
                                currentSubScreen = CatalogSubScreen.BOOKING_STEP1
                            }
                        )
                    }
                }
                CatalogSubScreen.BOOKING_STEP1 -> {
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
                            onBack = { currentSubScreen = CatalogSubScreen.DETAIL },
                            onNext = {
                                if (buyerName.isBlank() || buyerPhone.isBlank()) {
                                    Toast.makeText(context, "Nama Lengkap dan Nomor HP harus diisi!", Toast.LENGTH_SHORT).show()
                                } else {
                                    currentSubScreen = CatalogSubScreen.BOOKING_STEP2
                                }
                            }
                        )
                    }
                }

                CatalogSubScreen.BOOKING_STEP2 -> {
                    selectedGoat?.let { goat ->
                        BookingStep2View(
                            selectedDate = selectedDate,
                            onDateClick = {
                                val calendar = Calendar.getInstance()
                                val datePickerDialog = DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val cal = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, year)
                                            set(Calendar.MONTH, month)
                                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                        }
                                        val sdf = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                                        selectedDate = sdf.format(cal.time)
                                    },
                                    2026, 5, 20 // June is 5 in Calendar API (0-indexed)
                                )
                                datePickerDialog.show()
                            },
                            selectedTimeSlot = selectedTimeSlot,
                            onTimeSlotSelect = { selectedTimeSlot = it },
                            onBack = { currentSubScreen = CatalogSubScreen.BOOKING_STEP1 },
                            onNext = { currentSubScreen = CatalogSubScreen.BOOKING_STEP3 }
                        )
                    }
                }

                CatalogSubScreen.BOOKING_STEP3 -> {
                    selectedGoat?.let { goat ->
                        BookingStep3View(
                            goat = goat,
                            name = buyerName,
                            phone = buyerPhone,
                            email = buyerEmail,
                            date = selectedDate,
                            timeSlot = selectedTimeSlot,
                            notes = buyerNotes,
                            onBack = { currentSubScreen = CatalogSubScreen.BOOKING_STEP2 },
                            onNext = {
                                // Create order and transition to success
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
                                viewModel.setTab(com.agrogoat.core.shared.AppTab.KATALOG)

                                // Generate booking code based on date
                                val formattedDate = try {
                                    val sdfInput = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
                                    val dateObj = sdfInput.parse(selectedDate) ?: Date()
                                    val sdfOutput = SimpleDateFormat("ddMMyy", Locale.getDefault())
                                    sdfOutput.format(dateObj)
                                } catch (e: Exception) {
                                    "260620"
                                }
                                bookingCode = "AG-$formattedDate-001"
                                currentSubScreen = CatalogSubScreen.BOOKING_SUCCESS
                            }
                        )
                    }
                }

                CatalogSubScreen.BOOKING_SUCCESS -> {
                    BookingSuccessView(
                        bookingCode = bookingCode,
                        onCopyCode = {
                            clipboardManager.setText(AnnotatedString(bookingCode))
                            Toast.makeText(context, "Kode Booking disalin!", Toast.LENGTH_SHORT).show()
                        },
                        onViewOrders = {
                            currentSubScreen = CatalogSubScreen.LIST
                            viewModel.setTab(com.agrogoat.core.shared.AppTab.PESANAN)
                        },
                        onBackToHome = {
                            currentSubScreen = CatalogSubScreen.LIST
                            viewModel.setTab(com.agrogoat.core.shared.AppTab.BERANDA)
                        }
                    )
                }
            }
        }
    }
}


