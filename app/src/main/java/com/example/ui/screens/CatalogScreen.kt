package com.example.ui.screens

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
import com.example.R
import com.example.data.*
import com.example.ui.components.*
import com.example.viewmodel.AgroGoatViewModel
import com.example.viewmodel.CatalogSort
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.ui.components.detail.GoatDetailView
import com.example.viewmodel.AppTab

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
    var buyerName by remember { mutableStateOf("Siti zahfia") }
    var buyerPhone by remember { mutableStateOf("0822-6883-0122") }
    var buyerEmail by remember { mutableStateOf("sitizahfia@gmail.com") }
    var buyerNotes by remember { mutableStateOf("") }

    // Date & Time selection states
    var selectedDate by remember { mutableStateOf("20 Juni 2026") }
    var selectedTimeSlot by remember { mutableStateOf("09.00 - 10.00") }
    var bookingCode by remember { mutableStateOf("") }

    LaunchedEffect(currentSubScreen) {
        viewModel.setHideBottomBar(currentSubScreen != CatalogSubScreen.LIST)
    }

    // Collect list parameters from VM for the list sub-screen
    val goats by viewModel.goats.collectAsState()
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
                                        onClick = { viewModel.setTab(com.example.viewmodel.AppTab.BERANDA) },
                                        modifier = Modifier.testTag("catalog_back_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
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
                                                    imageVector = Icons.Default.Search,
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
                                                imageVector = if (showFavsOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
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
                                                        imageVector = Icons.Default.ArrowDropDown,
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

                        GoatDetailView(

                            goat = goat,

                            onBack = {
                                currentSubScreen =
                                    CatalogSubScreen.LIST
                            },

                            onToggleFav = {
                                viewModel.toggleFavorite(goat.id)
                            },

                            onChat = {

                                viewModel.setTab(
                                    AppTab.CHAT
                                )

                            },

                            onOrder = {

                                currentSubScreen =
                                    CatalogSubScreen.BOOKING_STEP1

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
                                viewModel.createOrder(goat, goat.weight)
                                viewModel.setTab(com.example.viewmodel.AppTab.KATALOG)

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
                            viewModel.setTab(com.example.viewmodel.AppTab.PESANAN)
                        },
                        onBackToHome = {
                            currentSubScreen = CatalogSubScreen.LIST
                            viewModel.setTab(com.example.viewmodel.AppTab.BERANDA)
                        }
                    )
                }
            }
        }
    }
}


// -------------------------------------------------------------
// SUB-SCREEN 2: STEP 1 (DATA DIRI FORM)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingStep1View(
    goat: GoatItem,
    name: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Pesan Kambing",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        containerColor = Color(0xFFF9FBF9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            // Steps indicator
            BookingProgressHeader(currentStep = 1)

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                "Data Pembeli",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Nama Lengkap Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Nama Lengkap", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E7D32),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Nomor HP Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Nomor HP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E7D32),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Email Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Alamat Email (Opsional)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E7D32),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Catatan Field (max 200 characters)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Catatan (Opsional)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { if (it.length <= 200) onNotesChange(it) },
                    placeholder = {
                        Text(
                            "Contoh: Kondisi kandang sudah siap, mohon...",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E7D32),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Counter
                Text(
                    text = "${notes.length}/200",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Next CTA Button
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Selanjutnya", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -------------------------------------------------------------
// SUB-SCREEN 3: STEP 2 (JADWAL & LOKASI)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingStep2View(
    selectedDate: String,
    onDateClick: () -> Unit,
    selectedTimeSlot: String,
    onTimeSlotSelect: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Pesan Kambing",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        containerColor = Color(0xFFF9FBF9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            BookingProgressHeader(currentStep = 2)

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Pilih Tanggal Pengambilan
            Text("Pilih Tanggal Pengambilan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .clickable { onDateClick() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedDate,
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )

                    CustomCalendarIcon(
                        modifier = Modifier.size(20.dp),
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Pilih Jam Pengambilan
            Text("Pilih Jam Pengambilan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))

            val timeSlots = listOf(
                "08.00 - 09.00", "09.00 - 10.00", "10.00 - 11.00",
                "13.00 - 14.00", "14.00 - 15.00", "15.00 - 16.00"
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                timeSlots.chunked(3).forEach { rowSlots ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowSlots.forEach { slot ->
                            val isSelected = selectedTimeSlot == slot
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF2E7D32) else Color.White)
                                    .border(1.dp, if (isSelected) Color(0xFF2E7D32) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    .clickable { onTimeSlotSelect(slot) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = slot,
                                    color = if (isSelected) Color.White else Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Lokasi Pengambilan Card
            Text("Lokasi Pengambilan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        CustomLocationPinIcon(modifier = Modifier.size(22.dp).padding(top = 2.dp), tint = Color.Black)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "AGRO GOAT BENGKALIS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            Text(
                                "Jl. Sudirman, Senggoro, Kec. Bengkalis\nKabupaten Bengkalis, Riau",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // "Lihat di Maps" Pill Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFE8F5E9))
                            .clickable {
                                Toast.makeText(context, "Membuka Google Maps...", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CustomLocationPinIcon(modifier = Modifier.size(14.dp), tint = Color(0xFF2E7D32))
                        Text(
                            "Lihat di Maps",
                            color = Color(0xFF2E7D32),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            // Navigation Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Outlined Kembali
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF2E7D32)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Kembali", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Filled Selanjutnya
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Selanjutnya", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -------------------------------------------------------------
// SUB-SCREEN 4: STEP 3 (KONFIRMASI PESANAN)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingStep3View(
    goat: GoatItem,
    name: String,
    phone: String,
    email: String,
    date: String,
    timeSlot: String,
    notes: String,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Pesan Kambing",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        containerColor = Color(0xFFF9FBF9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            BookingProgressHeader(currentStep = 3)

            Spacer(modifier = Modifier.height(18.dp))

            Text("Konfirmasi Pesanan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(10.dp))

            // Main Card Summary Info Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    
                    // 1. Goat Info (Header row)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFF3E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            GoatSilhouette(modifier = Modifier.size(46.dp), tint = Color.Black.copy(alpha = 0.8f))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(goat.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                            Text("Jantan • ${goat.weight} kg • ${goat.age} Th", fontSize = 11.sp, color = Color.Gray)
                            Text(formatRupiah(goat.price), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // 2. Data Pembeli
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Data Pembeli", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                        Text(name, fontSize = 12.sp, color = Color.Gray)
                        Text(phone, fontSize = 12.sp, color = Color.Gray)
                        if (email.isNotEmpty()) {
                            Text(email, fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // 3. Jadwal Pengambilan
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Jadwal Pengambilan", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CustomCalendarIcon(modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Text(date, fontSize = 12.sp, color = Color.Gray)
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CustomClockIcon(modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Text(timeSlot, fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // 4. Lokasi Pengambilan
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Lokasi Pengambilan", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            CustomLocationPinIcon(modifier = Modifier.size(14.dp).padding(top = 2.dp), tint = Color.Black)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("AGRO GOAT BENGKALIS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                                Text("Jl. Sudirman, Senggoro, Kec. Bengkalis Kabupaten Bengkalis, Riau", fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // 5. Catatan
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Catatan", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                        Text(notes.ifBlank { "-" }, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            // Navigation Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Outlined Kembali
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF2E7D32)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Kembali", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Filled Selanjutnya
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Selanjutnya", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -------------------------------------------------------------
// SUB-SCREEN 5: STEP 4 (SUCCESS SCREEN)
// -------------------------------------------------------------
@Composable
fun BookingSuccessView(
    bookingCode: String,
    onCopyCode: () -> Unit,
    onViewOrders: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Concentric Circle Green checkmark
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                // Checkmark
                val p = remember { Path() }
                Canvas(modifier = Modifier.size(34.dp)) {
                    p.reset()
                    p.moveTo(size.width * 0.15f, size.height * 0.52f)
                    p.lineTo(size.width * 0.44f, size.height * 0.8f)
                    p.lineTo(size.width * 0.88f, size.height * 0.24f)
                    drawPath(p, color = Color.White, style = Stroke(width = 5.dp.toPx()))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Pesanan Berhasil!",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Kambing Anda telah dipesan.\nSilahkan ambil sesuai jadwal yang telah dipilih.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Booking Code Card Box
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F6F4)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Kode Booking",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = bookingCode,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF1B5E20)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = onCopyCode,
                        modifier = Modifier.size(24.dp)
                    ) {
                        CustomCopyIcon(modifier = Modifier.fillMaxSize(), tint = Color(0xFF2E7D32))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))

        // Action Buttons Row/Column
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "Lihat Pesanan Saya" button
            Button(
                onClick = onViewOrders,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Lihat Pesanan Saya", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // "Kembali ke Beranda" button
            Button(
                onClick = onBackToHome,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF2E7D32)
                ),
                border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Kembali ke Beranda", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// PROGRESS INDICATOR HEADER COMPOSABLE
// -------------------------------------------------------------
@Composable
fun BookingProgressHeader(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        StepCircleItem(step = 1, label = "Data Diri", isActive = currentStep >= 1, isCompleted = currentStep > 1)
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(1.5.dp)
                .background(if (currentStep >= 2) Color(0xFF66BB6A) else Color(0xFFE2E8F0))
        )
        StepCircleItem(step = 2, label = "Jadwal", isActive = currentStep >= 2, isCompleted = currentStep > 2)
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(1.5.dp)
                .background(if (currentStep >= 3) Color(0xFF66BB6A) else Color(0xFFE2E8F0))
        )
        StepCircleItem(step = 3, label = "Konfirmasi", isActive = currentStep >= 3, isCompleted = false)
    }
}

@Composable
fun StepCircleItem(
    step: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted || isActive) Color(0xFF66BB6A) else Color(0xFFE2E8F0)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            } else {
                Text(
                    text = step.toString(),
                    color = if (isActive) Color.White else Color(0xFF888888),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) Color.Black else Color.LightGray
        )
    }
}

// -------------------------------------------------------------
// CUSTOM COMPOSABLE VECTOR ICONS DRAWING
// -------------------------------------------------------------
@Composable
fun CustomCalendarIcon(modifier: Modifier = Modifier, tint: Color = Color.Gray) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.08f

        // Outline rectangle
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.1f, h * 0.2f),
            size = Size(w * 0.8f, h * 0.7f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = stroke)
        )

        // Top horizontal dividing line
        drawLine(
            color = tint,
            start = Offset(w * 0.1f, h * 0.42f),
            end = Offset(w * 0.9f, h * 0.42f),
            strokeWidth = stroke
        )

        // Two little binder loops at top
        drawLine(
            color = tint,
            start = Offset(w * 0.3f, h * 0.08f),
            end = Offset(w * 0.3f, h * 0.22f),
            strokeWidth = stroke
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.7f, h * 0.08f),
            end = Offset(w * 0.7f, h * 0.22f),
            strokeWidth = stroke
        )
    }
}

@Composable
fun CustomClockIcon(modifier: Modifier = Modifier, tint: Color = Color.Gray) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.42f
        val stroke = w * 0.08f

        // Dial Circle
        drawCircle(
            color = tint,
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = stroke)
        )

        // Center dot
        drawCircle(
            color = tint,
            radius = w * 0.06f,
            center = Offset(cx, cy)
        )

        // Hour hand
        drawLine(
            color = tint,
            start = Offset(cx, cy),
            end = Offset(cx, cy - r * 0.5f),
            strokeWidth = stroke
        )

        // Minute hand
        drawLine(
            color = tint,
            start = Offset(cx, cy),
            end = Offset(cx + r * 0.45f, cy),
            strokeWidth = stroke
        )
    }
}

@Composable
fun CustomCopyIcon(modifier: Modifier = Modifier, tint: Color = Color.Gray) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.08f

        // Back page outline
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.24f, h * 0.1f),
            size = Size(w * 0.62f, h * 0.65f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = stroke)
        )

        // Front page white mask
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.1f, h * 0.26f),
            size = Size(w * 0.62f, h * 0.65f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )

        // Front page outline
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.1f, h * 0.26f),
            size = Size(w * 0.62f, h * 0.65f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = stroke)
        )
    }
}

/**
 * Custom Tab component matching style
 */
@Composable
fun FilterTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFF2E7D32) else Color.White
    val textColor = if (isSelected) Color.White else Color(0xFF757575)
    val borderStroke = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0))
    
    Box(
        modifier = modifier
            .let { if (borderStroke != null) it.border(borderStroke, CircleShape) else it }
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

/**
 * Custom Dropdown button matching style
 */
@Composable
fun FilterDropdown(
    label: String,
    selectedValue: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFiltering = selectedValue != "Semua"
    val borderColor = if (isFiltering) Color(0xFF2E7D32) else Color(0xFFE2E8F0)
    val textColor = if (isFiltering) Color(0xFF2E7D32) else Color(0xFF757575)
    
    Box(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isFiltering) selectedValue else label,
            color = textColor,
            fontWeight = if (isFiltering) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            maxLines = 1
        )
    }
}

@Composable
fun SortDropdownItem(
    label: String,
    isActive: Boolean,
    isTerbaru: Boolean,
    onClick: () -> Unit
) {
    val textToShow = if (isActive) "✓ $label" else label
    
    val (bgColor, borderStroke, textColor) = when {
        isActive && isTerbaru -> Triple(
            Color(0xFFE8F5E9), // light green
            null,
            Color(0xFF2E7D32)  // brand green
        )
        isActive && !isTerbaru -> Triple(
            Color(0xFFFFF3E0), // light orange
            BorderStroke(1.dp, Color(0xFFF57C00)), // orange border
            Color(0xFFF57C00)  // orange text
        )
        else -> Triple(
            Color.Transparent,
            null,
            Color.Black
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .let { modifier ->
                if (borderStroke != null) {
                    modifier.border(borderStroke, RoundedCornerShape(8.dp))
                } else {
                    modifier
                }
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = textToShow,
            color = textColor,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}
