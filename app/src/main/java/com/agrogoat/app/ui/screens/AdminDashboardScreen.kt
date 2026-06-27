package com.agrogoat.app.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrogoat.app.data.GoatCategory
import com.agrogoat.app.data.GoatItem
import com.agrogoat.app.ui.components.GoatLogo
import com.agrogoat.app.ui.components.formatRupiah
import com.agrogoat.app.ui.components.GoatImage
import com.agrogoat.app.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.launch
import com.agrogoat.app.viewmodel.AgroGoatViewModel
import java.util.UUID

enum class AdminTab {
    HOME,
    DATA,
    JUAL,
    PROFIL
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AgroGoatViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var currentTab by remember { mutableStateOf(AdminTab.HOME) }
    
    // Admin state
    var selectedGoatForDetail by remember { mutableStateOf<GoatItem?>(null) }
    var selectedGoatForEdit by remember { mutableStateOf<GoatItem?>(null) }
    
    // Jual/Add screen fields
    var nameInput by remember { mutableStateOf("") }
    var categoryInput by remember { mutableStateOf(GoatCategory.ETAWA) }
    var genderInput by remember { mutableStateOf("Jantan") }
    var ageInput by remember { mutableStateOf("") }
    var weightInput by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var locationInput by remember { mutableStateOf("Bengkalis") }
    var descInput by remember { mutableStateOf("Kambing sehat, sudah vaksin berkala.") }
    var healthInput by remember { mutableStateOf("Sehat") }
    
    var showSuccessSheet by remember { mutableStateOf(false) }
    var lastAddedGoat by remember { mutableStateOf<GoatItem?>(null) }

    val hasBackAction = selectedGoatForDetail != null || selectedGoatForEdit != null || currentTab != AdminTab.HOME
    if (hasBackAction) {
        androidx.activity.compose.BackHandler {
            if (selectedGoatForEdit != null) {
                // If editing, go back to detail view
                selectedGoatForDetail = selectedGoatForEdit
                selectedGoatForEdit = null
            } else if (selectedGoatForDetail != null) {
                // If viewing details, go back to home list
                selectedGoatForDetail = null
            } else {
                // If in another tab, go back to HOME tab
                currentTab = AdminTab.HOME
            }
        }
    }

    // Success popup helper
    val triggerSuccess = { goat: GoatItem ->
        lastAddedGoat = goat
        showSuccessSheet = true
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F4))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Main content based on current state / screen
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (selectedGoatForDetail != null) {
                    AdminGoatDetailView(
                        goat = selectedGoatForDetail!!,
                        onBack = { selectedGoatForDetail = null },
                        onEdit = {
                            selectedGoatForEdit = selectedGoatForDetail
                            selectedGoatForDetail = null
                        },
                        onDelete = {
                            viewModel.deleteGoatItem(selectedGoatForDetail!!.id)
                            selectedGoatForDetail = null
                        }
                    )
                } else if (selectedGoatForEdit != null) {
                    AdminEditGoatView(
                        goat = selectedGoatForEdit!!,
                        onBack = { selectedGoatForEdit = null },
                        onSave = { updatedGoat ->
                            viewModel.updateGoatItem(updatedGoat)
                            selectedGoatForEdit = null
                        }
                    )
                } else {
                    when (currentTab) {
                        AdminTab.HOME -> AdminHomeTab(viewModel, onNavigateToData = { currentTab = AdminTab.DATA })
                        AdminTab.DATA -> AdminDataTab(
                            viewModel = viewModel,
                            onGoatClick = { selectedGoatForDetail = it },
                            onEditClick = { selectedGoatForEdit = it },
                            onAddClick = { currentTab = AdminTab.JUAL }
                        )
                        AdminTab.JUAL -> AdminJualTab(
                            onSave = { newGoat ->
                                viewModel.addGoatItem(newGoat)
                                triggerSuccess(newGoat)
                            },
                            onCancel = { currentTab = AdminTab.HOME }
                        )
                        AdminTab.PROFIL -> AdminProfilTab(viewModel, onLogout)
                    }
                }
            }

            // Bottom Navigation Bar
            if (selectedGoatForDetail == null && selectedGoatForEdit == null) {
                AdminBottomNavigation(
                    activeTab = currentTab,
                    onTabSelected = { currentTab = it }
                )
            }
        }

        // Success Bottom Sheet / Dialog Overlay
        if (showSuccessSheet && lastAddedGoat != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showSuccessSheet = false },
                contentAlignment = Alignment.BottomCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                        .clickable(enabled = false) {},
                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Green circle checkmark
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1F6E35)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Text(
                            text = "Ternak Berhasil Ditambahkan!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111612),
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = "Data kambing baru telah berhasil disimpan ke sistem dan tersinkronisasi di Firestore.",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        // Info Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F5E9))
                                .border(1.dp, Color(0xFFC8E6C9), RoundedCornerShape(12.dp))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${lastAddedGoat!!.name} • ${lastAddedGoat!!.category.displayName} • ${lastAddedGoat!!.gender} • ${lastAddedGoat!!.weight} Kg",
                                color = Color(0xFF1F6E35),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                selectedGoatForDetail = lastAddedGoat
                                showSuccessSheet = false
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F6E35)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Lihat Detail Ternak 👁️", fontWeight = FontWeight.Bold, color = Color.White)
                        }

                        OutlinedButton(
                            onClick = {
                                showSuccessSheet = false
                                currentTab = AdminTab.DATA
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.Gray)
                        ) {
                            Text("Kembali ke Data", color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminHomeTab(viewModel: AgroGoatViewModel, onNavigateToData: () -> Unit) {
    val userName by viewModel.userName.collectAsState()
    val goats by viewModel.goats.collectAsState()
    val orders by viewModel.orders.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Top Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F6E35))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Dashboard Penjual",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "Selamat Datang, $userName 👋",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Kelola hewan ternak dan pantau pesanan Anda secara real-time.",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Stat 1: Total Ternak
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🐐", fontSize = 18.sp)
                        }
                        Text("Total Ternak", fontSize = 12.sp, color = Color.Gray)
                        Text("${goats.size} Ekor", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }

                // Stat 2: Pesanan Baru
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFF3E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📦", fontSize = 18.sp)
                        }
                        Text("Total Pesanan", fontSize = 12.sp, color = Color.Gray)
                        Text("${orders.size} Transaksi", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    }
                }
            }

            // Quick Actions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Navigasi Cepat",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = onNavigateToData,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F6E35)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Lihat Data Ternak", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }

            // Recent Goat Listings
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Ternak Terbaru",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                if (goats.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada data ternak", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    goats.take(3).forEach { goat ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                val picRes = when (goat.category) {
                                    GoatCategory.POTONG -> R.drawable.burawa
                                    GoatCategory.ETAWA -> R.drawable.etawa
                                    GoatCategory.PERAH -> R.drawable.kacang
                                }
                                GoatImage(
                                    imageUri = goat.imageUri,
                                    defaultImageRes = picRes,
                                    contentDescription = goat.name,
                                    modifier = Modifier
                                        .size(52.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = goat.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "${goat.category.displayName} • ${goat.gender}",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Text(
                                    text = formatRupiah(goat.price),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF1F6E35)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminDataTab(
    viewModel: AgroGoatViewModel,
    onGoatClick: (GoatItem) -> Unit,
    onEditClick: (GoatItem) -> Unit,
    onAddClick: () -> Unit
) {
    val goats by viewModel.goats.collectAsState()
    
    // Filters
    var genderFilter by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredGoats = goats.filter { goat ->
        val matchesGender = when (genderFilter) {
            "Jantan" -> goat.gender == "Jantan"
            "Betina" -> goat.gender == "Betina"
            else -> true
        }
        val matchesSearch = goat.name.contains(searchQuery, ignoreCase = true) || 
                            goat.category.displayName.contains(searchQuery, ignoreCase = true)
        matchesGender && matchesSearch
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1F6E35))
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Data Ternak Anda",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
                // Small search bar in header
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari...", color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                        focusedIndicatorColor = Color.White,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .width(140.dp)
                        .height(44.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Gender Segmented Buttons (Semua, Jantan, Betina)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Semua", "Jantan", "Betina").forEach { gender ->
                        val isSelected = genderFilter == gender
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { genderFilter = gender },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFF1F6E35) else Color.Transparent
                        ) {
                            Text(
                                text = gender,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                textAlign = TextAlign.Center,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isSelected) Color.White else Color.DarkGray
                            )
                        }
                    }
                }
            }

            // Goat List
            if (filteredGoats.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🔍", fontSize = 48.sp)
                        Text("Ternak tidak ditemukan", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredGoats) { goat ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onGoatClick(goat) },
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Picture box
                                val picRes = when (goat.category) {
                                    GoatCategory.POTONG -> R.drawable.burawa
                                    GoatCategory.ETAWA -> R.drawable.etawa
                                    GoatCategory.PERAH -> R.drawable.kacang
                                }
                                GoatImage(
                                    imageUri = goat.imageUri,
                                    defaultImageRes = picRes,
                                    contentDescription = goat.name,
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )

                                // Information details
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = goat.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Color(0xFF111612)
                                    )
                                    Text(
                                        text = "• ${goat.gender} • ${goat.age} Th",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Health Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFE8F5E9))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Sehat",
                                            color = Color(0xFF1F6E35),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = formatRupiah(goat.price),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF1F6E35)
                                    )
                                }

                                // Edit & Delete Action Buttons
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Edit Button
                                    IconButton(
                                        onClick = { onEditClick(goat) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFE8F5E9))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = "Edit",
                                            tint = Color(0xFF1F6E35),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Delete Button
                                    IconButton(
                                        onClick = { viewModel.deleteGoatItem(goat.id) },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFFFEBEE))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Hapus",
                                            tint = Color(0xFFC62828),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button to Add Goat
        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 20.dp)
                .shadow(8.dp, CircleShape),
            shape = CircleShape,
            containerColor = Color(0xFF1F6E35)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
                Text("Tambah", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminJualTab(
    onSave: (GoatItem) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(GoatCategory.ETAWA) }
    var gender by remember { mutableStateOf("Jantan") }
    var weightStr by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Bengkalis") }
    var desc by remember { mutableStateOf("Kambing sangat jinak, sehat, gemuk, makannya teratur.") }
    var health by remember { mutableStateOf("Sehat") }
    var imageUriStr by remember { mutableStateOf<String?>(null) }
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var isSaving by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            imageUriStr = uri.toString()
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    bitmap = android.graphics.ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    bitmap = android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F6E35))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Tambah Ternak Baru",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Upload Picture Placeholder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .border(
                        border = BorderStroke(1.5.dp, Color(0xFFC2D2C2)),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { launcher.launch("image/*") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                if (bitmap != null) {
                    androidx.compose.foundation.Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = null,
                            tint = Color(0xFF1F6E35),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "📷 Ketuk untuk upload foto",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF1F6E35)
                        )
                        Text(
                            text = "JPG, PNG (Maks. 5MB)",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            // Informasi Dasar Section
            Text("Informasi Dasar", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama / ID Kambing *") },
                placeholder = { Text("Contoh: Si Belang #001") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Category Dropdown Selection
            var catExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = category.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Jenis Ras *") },
                    trailingIcon = {
                        IconButton(onClick = { catExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = catExpanded,
                    onDismissRequest = { catExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    GoatCategory.values().forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.displayName) },
                            onClick = {
                                category = cat
                                catExpanded = false
                            }
                        )
                    }
                }
            }

            // Gender radio buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text("Jenis Kelamin *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = gender == "Jantan", onClick = { gender = "Jantan" })
                    Text("Jantan", fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = gender == "Betina", onClick = { gender = "Betina" })
                    Text("Betina", fontSize = 13.sp)
                }
            }

            // Data Fisik Section
            Text("Data Fisik", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = ageStr,
                    onValueChange = { ageStr = it },
                    label = { Text("Umur (Tahun) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = weightStr,
                    onValueChange = { weightStr = it },
                    label = { Text("Berat (Kg) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = priceStr,
                onValueChange = { priceStr = it },
                label = { Text("Harga (Rp) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Health Status Dropdown
            var healthExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = health,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status Kesehatan *") },
                    trailingIcon = {
                        IconButton(onClick = { healthExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = healthExpanded,
                    onDismissRequest = { healthExpanded = false }
                ) {
                    listOf("Sehat", "Sakit", "Karantina").forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status) },
                            onClick = {
                                health = status
                                healthExpanded = false
                            }
                        )
                    }
                }
            }

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Catatan Tambahan") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    enabled = !isSaving,
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.Gray)
                ) {
                    Text("Batal", color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    enabled = !isSaving,
                    onClick = {
                        val weight = weightStr.toIntOrNull()
                        val age = ageStr.toDoubleOrNull()
                        val price = priceStr.toLongOrNull()
                        if (name.isBlank() || weight == null || age == null || price == null) {
                            Toast.makeText(context, "Harap isi semua kolom dengan benar!", Toast.LENGTH_SHORT).show()
                        } else {
                            coroutineScope.launch {
                                isSaving = true
                                var finalImageUri: String? = imageUriStr
                                
                                if (imageUriStr != null && !imageUriStr!!.startsWith("http")) {
                                    Toast.makeText(context, "Mengunggah foto ke Cloudinary...", Toast.LENGTH_SHORT).show()
                                    val cloudinaryUrl = com.agrogoat.app.utils.CloudinaryUploader.uploadImage(
                                        context,
                                        android.net.Uri.parse(imageUriStr!!)
                                    )
                                    if (cloudinaryUrl != null) {
                                        finalImageUri = cloudinaryUrl
                                        Toast.makeText(context, "Foto berhasil diunggah ke Cloudinary!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Gagal mengunggah ke Cloudinary. Menggunakan penyimpanan lokal.", Toast.LENGTH_LONG).show()
                                    }
                                }
                                
                                val newGoat = GoatItem(
                                    name = name,
                                    category = category,
                                    gender = gender,
                                    weight = weight,
                                    age = age,
                                    price = price,
                                    location = location,
                                    description = desc,
                                    isNew = true,
                                    imageUri = finalImageUri
                                )
                                onSave(newGoat)
                                // Clear form
                                name = ""
                                weightStr = ""
                                ageStr = ""
                                priceStr = ""
                                imageUriStr = null
                                bitmap = null
                                isSaving = false
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F6E35)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Simpan Ternak 🐐", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminProfilTab(viewModel: AgroGoatViewModel, onLogout: () -> Unit) {
    val userName by viewModel.userName.collectAsState()
    val userAddress by viewModel.userAddress.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F6E35))
                .statusBarsPadding()
                .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = null,
                        tint = Color(0xFF1F6E35),
                        modifier = Modifier.size(72.dp)
                    )
                }
                Text(userName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("Role: Penjual (Admin)", color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Pengaturan Akun", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Nama", color = Color.Gray)
                        Text(userName, fontWeight = FontWeight.SemiBold)
                    }
                    Divider(color = Color(0xFFE2E8F0))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Lokasi Peternakan", color = Color.Gray)
                        Text(userAddress, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Keluar dari Akun", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminGoatDetailView(
    goat: GoatItem,
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF4F6F4))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F6E35))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Kembali",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "Detail Kambing",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                val picRes = when (goat.category) {
                    GoatCategory.POTONG -> R.drawable.burawa
                    GoatCategory.ETAWA -> R.drawable.etawa
                    GoatCategory.PERAH -> R.drawable.kacang
                }
                GoatImage(
                    imageUri = goat.imageUri,
                    defaultImageRes = picRes,
                    contentDescription = goat.name,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Description card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = goat.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Text(
                        text = "${goat.category.displayName} • ${goat.gender} • ${goat.age} Tahun",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )

                    Divider(color = Color(0xFFE2E8F0))

                    Text("Spesifikasi Fisik", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Berat Badan", color = Color.Gray, fontSize = 12.sp)
                            Text("${goat.weight} Kg", color = Color(0xFF1F6E35), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column {
                            Text("Warna Bulu", color = Color.Gray, fontSize = 12.sp)
                            Text("Putih Bercak Hitam", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    Text("Riwayat Kesehatan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Sangat Sehat", color = Color(0xFF1F6E35), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }

                    Divider(color = Color(0xFFE2E8F0))

                    Text("Catatan Tambahan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
                    Text(
                        text = goat.description,
                        fontSize = 13.sp,
                        color = Color.DarkGray
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFC62828))
                ) {
                    Text("Hapus Ternak", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F6E35)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Edit Detail", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun AdminEditGoatView(
    goat: GoatItem,
    onBack: () -> Unit,
    onSave: (GoatItem) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(goat.name) }
    var category by remember { mutableStateOf(goat.category) }
    var gender by remember { mutableStateOf(goat.gender) }
    var weightStr by remember { mutableStateOf(goat.weight.toString()) }
    var ageStr by remember { mutableStateOf(goat.age.toString()) }
    var priceStr by remember { mutableStateOf(goat.price.toString()) }
    var desc by remember { mutableStateOf(goat.description) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color(0xFFF4F6F4))
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF1F6E35))
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Batal",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "Edit Data Ternak",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama / ID Kambing *") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Category Dropdown
            var catExpanded by remember { mutableStateOf(false) }
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = category.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Jenis Ras *") },
                    trailingIcon = {
                        IconButton(onClick = { catExpanded = true }) {
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                DropdownMenu(
                    expanded = catExpanded,
                    onDismissRequest = { catExpanded = false }
                ) {
                    GoatCategory.values().forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.displayName) },
                            onClick = {
                                category = cat
                                catExpanded = false
                            }
                        )
                    }
                }
            }

            // Gender Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Text("Jenis Kelamin *", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Gray)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = gender == "Jantan", onClick = { gender = "Jantan" })
                    Text("Jantan", fontSize = 13.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = gender == "Betina", onClick = { gender = "Betina" })
                    Text("Betina", fontSize = 13.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = ageStr,
                    onValueChange = { ageStr = it },
                    label = { Text("Umur (Th) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = weightStr,
                    onValueChange = { weightStr = it },
                    label = { Text("Berat (Kg) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            OutlinedTextField(
                value = priceStr,
                onValueChange = { priceStr = it },
                label = { Text("Harga (Rp) *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Catatan Tambahan") },
                maxLines = 3,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    val weight = weightStr.toIntOrNull()
                    val age = ageStr.toDoubleOrNull()
                    val price = priceStr.toLongOrNull()
                    if (name.isBlank() || weight == null || age == null || price == null) {
                        Toast.makeText(context, "Harap isi semua kolom dengan benar!", Toast.LENGTH_SHORT).show()
                    } else {
                        val updatedGoat = goat.copy(
                            name = name,
                            category = category,
                            gender = gender,
                            weight = weight,
                            age = age,
                            price = price,
                            description = desc
                        )
                        onSave(updatedGoat)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F6E35)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Perubahan 💾", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AdminBottomNavigation(
    activeTab: AdminTab,
    onTabSelected: (AdminTab) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(76.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeColor = Color(0xFF1F6E35)
            val inactiveColor = Color.Gray.copy(alpha = 0.6f)

            // Tab 1: Home
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (activeTab == AdminTab.HOME) activeColor.copy(alpha = 0.1f) else Color.Transparent,
                onClick = { onTabSelected(AdminTab.HOME) }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = "Home",
                        tint = if (activeTab == AdminTab.HOME) activeColor else inactiveColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Home",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == AdminTab.HOME) activeColor else inactiveColor
                    )
                }
            }

            // Tab 2: Data
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (activeTab == AdminTab.DATA) activeColor.copy(alpha = 0.1f) else Color.Transparent,
                onClick = { onTabSelected(AdminTab.DATA) }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Pets, // Goat representation
                        contentDescription = "Data",
                        tint = if (activeTab == AdminTab.DATA) activeColor else inactiveColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Data",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == AdminTab.DATA) activeColor else inactiveColor
                    )
                }
            }

            // Tab 3: Jual
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (activeTab == AdminTab.JUAL) activeColor.copy(alpha = 0.1f) else Color.Transparent,
                onClick = { onTabSelected(AdminTab.JUAL) }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalOffer, // Money/tag icon
                        contentDescription = "Jual",
                        tint = if (activeTab == AdminTab.JUAL) activeColor else inactiveColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Jual",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == AdminTab.JUAL) activeColor else inactiveColor
                    )
                }
            }

            // Tab 4: Profil
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                shape = RoundedCornerShape(16.dp),
                color = if (activeTab == AdminTab.PROFIL) activeColor.copy(alpha = 0.1f) else Color.Transparent,
                onClick = { onTabSelected(AdminTab.PROFIL) }
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profil",
                        tint = if (activeTab == AdminTab.PROFIL) activeColor else inactiveColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Profil",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (activeTab == AdminTab.PROFIL) activeColor else inactiveColor
                    )
                }
            }
        }
    }
}
