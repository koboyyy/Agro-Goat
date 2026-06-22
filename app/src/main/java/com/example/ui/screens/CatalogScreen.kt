package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.*
import com.example.viewmodel.AgroGoatViewModel
import com.example.viewmodel.CatalogSort

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    viewModel: AgroGoatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Collect from VM
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

    // Bottom sheet details target
    var selectedGoatForDetail by remember { mutableStateOf<GoatItem?>(null) }

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
            // Search query search match (name or location)
            (searchQuery.isBlank() || 
             goat.name.contains(searchQuery, ignoreCase = true) || 
             goat.location.contains(searchQuery, ignoreCase = true)) &&
            // Gender filter matching
            (selectedGender == null || 
             (selectedGender == "Jantan" && goat.gender.equals("Jantan", ignoreCase = true)) ||
             (selectedGender == "Betina" && goat.gender.equals("Betina", ignoreCase = true)) ||
             (selectedGender == "Lainnya" && !goat.gender.equals("Jantan", ignoreCase = true) && !goat.gender.equals("Betina", ignoreCase = true))) &&
            // Favorites filter toggle
            (!showFavsOnly || goat.isFavorite) &&
            // Price Filter matching
            (selectedPriceFilter == "Semua" || when (selectedPriceFilter) {
                "< 2 Juta" -> goat.price < 2000000
                "2 - 5 Juta" -> goat.price in 2000000..5000000
                "> 5 Juta" -> goat.price > 5000000
                else -> true
            }) &&
            // Age Filter matching
            (selectedAgeFilter == "Semua" || when (selectedAgeFilter) {
                "< 1.5 Th" -> goat.age < 1.5
                "1.5 - 2 Th" -> goat.age in 1.5..2.0
                "> 2 Th" -> goat.age > 2.0
                else -> true
            }) &&
            // Weight Filter matching
            (selectedWeightFilter == "Semua" || when (selectedWeightFilter) {
                "< 50 kg" -> goat.weight < 50
                "50 - 60 kg" -> goat.weight in 50..60
                "> 60 kg" -> goat.weight > 60
                else -> true
            }) &&
            // Location Filter matching
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

    Scaffold(
        modifier = modifier.fillMaxSize(),
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
            // Interactive search, filters and sort controls at the top of the grid
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

                    // Gender Category Row with Jantan/Betina/Lainnya filters
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val genders = listOf(
                            null to "semua",
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

                    // Dropdown Filter Buttons (Harga, Umur, Berat, Lokasi)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. Harga Filter
                        Box(modifier = Modifier.weight(1f)) {
                            FilterDropdown(
                                label = "Harga",
                                selectedValue = selectedPriceFilter,
                                onClick = { showPriceDropdown = true }
                            )
                            DropdownMenu(
                                expanded = showPriceDropdown,
                                onDismissRequest = { showPriceDropdown = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
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

                        // 2. Umur Filter
                        Box(modifier = Modifier.weight(1f)) {
                            FilterDropdown(
                                label = "Umur",
                                selectedValue = selectedAgeFilter,
                                onClick = { showAgeDropdown = true }
                            )
                            DropdownMenu(
                                expanded = showAgeDropdown,
                                onDismissRequest = { showAgeDropdown = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
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

                        // 3. Berat Filter
                        Box(modifier = Modifier.weight(1f)) {
                            FilterDropdown(
                                label = "Berat",
                                selectedValue = selectedWeightFilter,
                                onClick = { showWeightDropdown = true }
                            )
                            DropdownMenu(
                                expanded = showWeightDropdown,
                                onDismissRequest = { showWeightDropdown = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
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

                        // 4. Lokasi Filter
                        Box(modifier = Modifier.weight(1f)) {
                            FilterDropdown(
                                label = "Lokasi",
                                selectedValue = selectedLocationFilter,
                                onClick = { showLocationDropdown = true }
                            )
                            DropdownMenu(
                                expanded = showLocationDropdown,
                                onDismissRequest = { showLocationDropdown = false },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
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

                    // Sorting Button "Urutkan: Terbaru ˅"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        val sortLabel = when (currentSort) {
                            CatalogSort.TERBARU -> "Terbaru"
                            CatalogSort.HARGA_RENDAH -> "Termurah"
                            CatalogSort.HARGA_TINGGI -> "Termahal"
                            CatalogSort.BOBOT_TERBESAR -> "Terberat"
                        }

                        val sortInteractionSource = remember { MutableInteractionSource() }
                        val isSortPressed by sortInteractionSource.collectIsPressedAsState()

                        Box(
                            modifier = Modifier
                                .shadow(1.dp, RoundedCornerShape(12.dp))
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSortPressed) Color(0xFFF5F5F5) else MaterialTheme.colorScheme.surface)
                                .clickable(
                                    interactionSource = sortInteractionSource,
                                    indication = null,
                                    onClick = { showSortingMenu = true }
                                )
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "Urutkan: $sortLabel",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Cards Grid List
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
                        onClick = { selectedGoatForDetail = goat }
                    )
                }
            }
        }
    }

    // SORTING MODAL OPTIONS SHEET
    if (showSortingMenu) {
        ModalBottomSheet(
            onDismissRequest = { showSortingMenu = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 40.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Urutkan Berdasarkan",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                CatalogSort.entries.forEach { sorting ->
                    val sortingLabel = when (sorting) {
                        CatalogSort.TERBARU -> "Terbaru / Pilihan Utama"
                        CatalogSort.HARGA_RENDAH -> "Harga: Terendah ke Tertinggi"
                        CatalogSort.HARGA_TINGGI -> "Harga: Tertinggi ke Terendah"
                        CatalogSort.BOBOT_TERBESAR -> "Bobot: Terberat ke Terringan"
                    }

                    val isSelected = currentSort == sorting

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setCatalogSort(sorting)
                                showSortingMenu = false
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sortingLabel,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // REUSED PRODUCT DETAIL SCREEN SHEET (exactly matching HomeScreen)
    if (selectedGoatForDetail != null) {
        val item = selectedGoatForDetail!!
        var currentTargetWeight by remember { mutableStateOf(item.weight.toFloat()) }

        val baseWeight = item.weight
        val pricePerKg = item.price / baseWeight
        val calculatedPrice = (item.price + (currentTargetWeight.toInt() - baseWeight) * pricePerKg).toLong()

        ModalBottomSheet(
            onDismissRequest = { selectedGoatForDetail = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 34.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFFECE5))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = item.category.displayName,
                                color = Color(0xFFF57C00),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    IconButton(
                        onClick = { selectedGoatForDetail = null },
                        modifier = Modifier.background(Color(0xFFF5F5F5), CircleShape).size(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            when (item.category) {
                                GoatCategory.POTONG -> Color(0xFFE8F5E9)
                                GoatCategory.ETAWA -> Color(0xFFFFF3E0)
                                GoatCategory.PERAH -> Color(0xFFE3F2FD)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    GoatSilhouette(modifier = Modifier.size(110.dp), Color.Black.copy(alpha = 0.82f))
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf(
                        "Kelamin" to item.gender,
                        "Bobot Awal" to "${item.weight} kg",
                        "Usia" to "${item.age} Tahun"
                    ).forEach { (label, value) ->
                        Card(
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(label, fontSize = 11.sp, color = Color.Gray)
                                Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Text(
                    text = item.description,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color.Gray
                )

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Estimasi Bobot Akhir", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("${currentTargetWeight.toInt()} kg", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                    Slider(
                        value = currentTargetWeight,
                        onValueChange = { currentTargetWeight = it },
                        valueRange = item.weight.toFloat()..(item.weight.toFloat() + 40f),
                        colors = SliderDefaults.colors(
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            thumbColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Pembayaran", fontSize = 11.sp, color = Color.Gray)
                        Text(formatRupiah(calculatedPrice), fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                    }
                    Button(
                        onClick = {
                            viewModel.createOrder(item, currentTargetWeight.toInt())
                            selectedGoatForDetail = null
                            viewModel.setTab(com.example.viewmodel.AppTab.PESANAN)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Pesan Sekarang", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Custom Tab component matching screenshot style (Active Green, Inactive Light Gray)
 */
@Composable
fun FilterTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Exact colors from screenshot: 
    // Active: Dark Green (Brand Theme), Inactive: Very light silver/gray
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isPressed -> MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
        else -> Color(0xFFDCDCDC) // Light gray matching screenshot
    }

    val textColor = when {
        isSelected || isPressed -> Color.White
        else -> Color(0xFF666666) // Darker gray for inactive text readability
    }
    
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null, 
                onClick = onClick
            )
            .padding(horizontal = 22.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

/**
 * Custom Dropdown button matching screenshot style (Gray outer with white inset box)
 */
@Composable
fun FilterDropdown(
    label: String,
    selectedValue: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // The "outer" part is gray, the "inner" part is white
    val outerColor = if (isPressed) Color(0xFFC0C0C0) else Color(0xFFDCDCDC)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(outerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(6.dp), // This padding creates the "border" effect
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                Text(
                    text = if (selectedValue == "Semua") label else selectedValue,
                    color = Color(0xFF666666),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color(0xFF666666),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
