package com.example.ui.screens

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
import com.example.data.*
import com.example.ui.components.*
import com.example.viewmodel.AppTab
import com.example.viewmodel.AgroGoatViewModel
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
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()

    // Sheet states
    var selectedGoatForDetail by remember { mutableStateOf<GoatItem?>(null) }
    var showPromoCouponSheet by remember { mutableStateOf(false) }

    // Filtered lists
    val filteredGoats = goats.filter { goat ->
        val matchesSearch = goat.name.contains(searchQuery, ignoreCase = true) || 
                            goat.location.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedHomeCategory == null || goat.category == selectedHomeCategory
        matchesSearch && matchesCategory
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
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
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = { viewModel.toggleTheme() },
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
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Lihat Semua →",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            modifier = Modifier
                                .clickable {
                                    viewModel.setHomeCategory(null)
                                    viewModel.setTab(AppTab.KATALOG)
                                }
                                .testTag("view_all_categories")
                        )
                    }

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(GoatCategory.entries) { cat ->
                            CategoryCardItem(
                                category = cat,
                                isSelected = selectedHomeCategory == cat,
                                onClick = { viewModel.setHomeCategory(cat) }
                            )
                        }
                    }
                }
            }

            // Kambing Terbaru (Latest Goats) Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedHomeCategory == null) "Kambing Terbaru" else "Hasil Kategori ${selectedHomeCategory?.displayName}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
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
                        onClick = { selectedGoatForDetail = goat }
                    )
                }
            }
        }
    }

    // 1. PRODUCT DETAIL & WEIGHT ESTIMATOR SHEET
    if (selectedGoatForDetail != null) {
        val item = selectedGoatForDetail!!
        var currentTargetWeight by remember { mutableStateOf(item.weight.toFloat()) }

        // Pricing calculation: base price plus extra per-kg add-on
        val baseWeight = item.weight
        val pricePerKg = item.price / baseWeight
        val calculatedPrice = (item.price + (currentTargetWeight.toInt() - baseWeight) * pricePerKg).toLong()

        ModalBottomSheet(
            onDismissRequest = { selectedGoatForDetail = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 34.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header (Name & Close button)
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
                        modifier = Modifier
                            .background(Color(0xFFF5F5F5), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                // Silhouette Illustration Box Card
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

                // Technical Parameters Row
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
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F3))
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

                // Description
                Text(
                    text = item.description,
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color.Gray
                )

                HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))

                // INTERACTIVE WEIGHT SLIDER SECTION
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Estimasi Bobot Akhir",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${currentTargetWeight.toInt()} kg",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    
                    Slider(
                        value = currentTargetWeight,
                        onValueChange = { currentTargetWeight = it },
                        valueRange = item.weight.toFloat()..(item.weight.toFloat() + 40f),
                        colors = SliderDefaults.colors(
                            activeTrackColor = Color(0xFF2E7D32),
                            thumbColor = Color(0xFF2E7D32)
                        )
                    )
                    
                    Text(
                        text = "*Harga bertambah proporsional sesuai pertambahan berat pakan/usia timbang.",
                        fontSize = 10.sp,
                        color = Color.LightGray
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Bottom CTA details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Pembayaran", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            text = formatRupiah(calculatedPrice),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF2E7D32)
                        )
                    }

                    Button(
                        onClick = {
                            viewModel.createOrder(item, currentTargetWeight.toInt())
                            selectedGoatForDetail = null
                            viewModel.setTab(AppTab.PESANAN)
                            Toast.makeText(context, "Pesanan Berhasil Dibuat!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(52.dp)
                            .testTag("checkout_order_button")
                    ) {
                        Icon(Icons.Default.ShoppingCart, contentDescription = "Beli")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pesan Sekarang", fontWeight = FontWeight.Bold)
                    }
                }
            }
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
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
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
