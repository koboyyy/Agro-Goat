package com.agrogoat.app.ui.screens
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*

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
import com.agrogoat.app.data.ChatRoom
import com.agrogoat.app.ui.components.GoatLogo
import com.agrogoat.app.ui.components.formatRupiah
import com.agrogoat.app.ui.components.GoatImage
import com.agrogoat.app.R
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.launch
import com.agrogoat.app.viewmodel.AgroGoatViewModel
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.Brush
import com.agrogoat.app.data.OrderStatus
import com.agrogoat.app.data.OrderItem
import java.util.UUID

enum class AdminTab {
    HOME,
    DATA,
    JUAL,
    PESANAN,
    PESAN,
    LAPORAN,
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
    val currentUserEmail by viewModel.userEmail.collectAsState()
    
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

    // Chat detail navigation state
    var selectedChatRoomForAdmin by remember { mutableStateOf<ChatRoom?>(null) }

    val hasBackAction = selectedGoatForDetail != null || selectedGoatForEdit != null || 
            selectedChatRoomForAdmin != null || currentTab != AdminTab.HOME
    if (hasBackAction) {
        androidx.activity.compose.BackHandler {
            if (selectedGoatForEdit != null) {
                // If editing, go back to detail view
                selectedGoatForDetail = selectedGoatForEdit
                selectedGoatForEdit = null
            } else if (selectedGoatForDetail != null) {
                // If viewing details, go back to home list
                selectedGoatForDetail = null
            } else if (selectedChatRoomForAdmin != null) {
                selectedChatRoomForAdmin = null
                viewModel.goBackToChatList()
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
                } else if (selectedChatRoomForAdmin != null) {
                    AdminChatDetailScreen(
                        chatRoom = selectedChatRoomForAdmin!!,
                        viewModel = viewModel,
                        onBack = {
                            selectedChatRoomForAdmin = null
                            viewModel.goBackToChatList()
                        }
                    )
                } else {
                    when (currentTab) {
                        AdminTab.HOME -> AdminHomeTab(
                            viewModel = viewModel,
                            onNavigateToData = { currentTab = AdminTab.DATA },
                            onNavigateToLaporan = { currentTab = AdminTab.LAPORAN }
                        )
                        AdminTab.DATA -> AdminDataTab(
                            viewModel = viewModel,
                            onGoatClick = { selectedGoatForDetail = it },
                            onEditClick = { selectedGoatForEdit = it },
                            onAddClick = { currentTab = AdminTab.JUAL }
                        )
                        AdminTab.JUAL -> AdminJualTab(
                            viewModel = viewModel,
                            onSave = { newGoat ->
                                val goatWithOwner = newGoat.copy(sellerEmail = currentUserEmail)
                                viewModel.addGoatItem(goatWithOwner)
                                triggerSuccess(newGoat)
                            },
                            onCancel = { currentTab = AdminTab.HOME }
                        )
                        AdminTab.PESANAN -> AdminPesananTab(
                            viewModel = viewModel,
                            onBack = { currentTab = AdminTab.HOME }
                        )
                        AdminTab.PESAN -> AdminChatDashboardScreen(
                            viewModel = viewModel,
                            onChatClick = { selectedChatRoomForAdmin = it },
                            onBack = { currentTab = AdminTab.HOME }
                        )
                        AdminTab.LAPORAN -> AdminSalesReportScreen(
                            onBack = { currentTab = AdminTab.HOME }
                        )
                        AdminTab.PROFIL -> AdminProfilScreen(
                            viewModel = viewModel,
                            onBack = { currentTab = AdminTab.HOME },
                            onLogout = onLogout
                        )
                    }
                }
            }

            // Bottom Navigation Bar
            if (selectedGoatForDetail == null && selectedGoatForEdit == null && selectedChatRoomForAdmin == null) {
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
                                    imageVector = Icons.Outlined.Check,
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

data class MockTransaction(
    val id: String,
    val date: String,
    val buyer: String,
    val price: String
)

fun formatShortRupiah(amount: Long): String {
    return if (amount >= 1_000_000) {
        val doubleVal = amount.toDouble() / 1_000_000.0
        val formatted = String.format(java.util.Locale.US, "%.1f", doubleVal)
        "Rp ${formatted.replace(".0", "")}jt"
    } else if (amount >= 1_000) {
        "Rp ${amount / 1000}rb"
    } else {
        "Rp $amount"
    }
}

@Composable
fun SalesTrendChart(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        
        val points = listOf(
            Offset(width * 0.05f, height * 0.85f),
            Offset(width * 0.20f, height * 0.55f),
            Offset(width * 0.35f, height * 0.65f),
            Offset(width * 0.50f, height * 0.40f),
            Offset(width * 0.65f, height * 0.48f),
            Offset(width * 0.80f, height * 0.25f),
            Offset(width * 0.95f, height * 0.18f)
        )
        
        // Path for background gradient fill under the chart curve
        val fillPath = Path().apply {
            moveTo(points.first().x, height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, height)
            close()
        }
        
        // Path for the trend line
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }
        
        // Draw the background fill gradient
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF2E7D32).copy(alpha = 0.25f), Color(0xFF2E7D32).copy(alpha = 0.0f))
            )
        )
        
        // Draw the main line path
        drawPath(
            path = linePath,
            color = Color(0xFF2E7D32),
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        
        // Draw circular markers at each point
        points.forEach { pt ->
            drawCircle(
                color = Color(0xFF1B5E20),
                radius = 5.dp.toPx(),
                center = pt
            )
            drawCircle(
                color = Color.White,
                radius = 2.dp.toPx(),
                center = pt
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeTab(
    viewModel: AgroGoatViewModel,
    onNavigateToData: () -> Unit,
    onNavigateToLaporan: () -> Unit
) {
    val userName by viewModel.userName.collectAsState()
    val goats by viewModel.myGoats.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val profiles by viewModel.usersProfiles.collectAsState()

    // Calculations for status metrics
    val completedOrders = orders.filter { it.status == OrderStatus.COMPLETED }
    val totalIncome = completedOrders.sumOf { it.totalPrice }
    val totalOrdersCount = orders.size
    val totalGoatsSold = completedOrders.size

    // Dynamic stats fallback to mockup figures if empty
    val incomeText = if (totalIncome > 0L) formatShortRupiah(totalIncome) else "Rp 12.5jt"
    val ordersText = if (totalOrdersCount > 0) "$totalOrdersCount Order" else "24 Order"
    val soldText = if (totalGoatsSold > 0) "$totalGoatsSold Ekor" else "32 Ekor"

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp),
                color = Color(0xFF2E7D32)
            ) {
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .height(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Dashboard Penjual",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        },
        containerColor = Color(0xFF4CAF50)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // 1. Stats card with 3 pills
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Stat 1: Pendapatan (Light green)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Pendapatan", fontSize = 11.sp, color = Color(0xFF757575))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = incomeText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                        }
                    }

                    // Stat 2: Transaksi (Light blue)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFE3F2FD))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Transaksi", fontSize = 11.sp, color = Color(0xFF757575))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = ordersText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                        }
                    }

                    // Stat 3: Terjual (Light yellow)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFFFF8E1))
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "Terjual", fontSize = 11.sp, color = Color(0xFF757575))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = soldText, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF57C00))
                        }
                    }
                }
            }

            // 2. Trend Sales Line Chart Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Tren Penjualan Minggu Ini",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Lihat Detail →",
                            color = Color(0xFF2E7D32),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable { onNavigateToLaporan() }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(18.dp))
                    
                    SalesTrendChart(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val labels = listOf("Sen", "Sel", "Rab", "Kam", "Jum")
                        labels.forEachIndexed { idx, day ->
                            val weightVal = if (idx == 0 || idx == labels.size - 1) 0.05f else 0.22f
                            Text(
                                text = day,
                                fontSize = 11.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // 3. Products Terlaris Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Produk Terlaris Bulan Ini",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )

                    // Card Item 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF5F5F5))
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "1. Kambing Etawa Jantan", fontSize = 13.sp, color = Color.Black)
                        Text(text = "12 Ekor", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }

                    // Card Item 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF5F5F5))
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "2. Kambing Boer Betina", fontSize = 13.sp, color = Color.Black)
                        Text(text = "8 Ekor", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }

                    // Card Item 3
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF5F5F5))
                            .padding(horizontal = 14.dp, vertical = 11.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "3. Kambing Kacang Muda", fontSize = 13.sp, color = Color.Black)
                        Text(text = "5 Ekor", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                    }
                }
            }

            // 4. Transaksi Terbaru Label
            Text(
                text = "Transaksi Terbaru",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Dynamic or fallback mockup Transactions List
            val transactionsList = if (orders.isEmpty()) {
                listOf(
                    MockTransaction("#INV-2506015", "28 Jun", "Budi Santoso", "Rp 3.5jt"),
                    MockTransaction("#INV-2506014", "27 Jun", "Siti Aisyah", "Rp 4.0jt")
                )
            } else {
                orders.take(3).map { order ->
                    val buyerName = profiles[order.buyerUid]?.get("name") as? String ?: "Pembeli"
                    MockTransaction(
                        id = "#INV-${order.id.take(8).uppercase()}",
                        date = order.orderDate,
                        buyer = buyerName,
                        price = formatShortRupiah(order.totalPrice)
                    )
                }
            }

            transactionsList.forEach { inv ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F5E9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "💰", fontSize = 20.sp)
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = inv.id,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${inv.date} • ${inv.buyer}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Text(
                            text = inv.price,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
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
    val goats by viewModel.myGoats.collectAsState()
    
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminJualTab(
    viewModel: AgroGoatViewModel,
    onSave: (GoatItem) -> Unit,
    onCancel: () -> Unit
) {
    var isAddingGoat by remember { mutableStateOf(false) }

    if (isAddingGoat) {
        AddGoatForm(
            onSave = { newGoat ->
                onSave(newGoat)
                isAddingGoat = false
            },
            onCancel = {
                isAddingGoat = false
            }
        )
    } else {
        val goats by viewModel.myGoats.collectAsState()
        val orders by viewModel.orders.collectAsState()
        val context = LocalContext.current

        // Calculations for status metrics
        val completedOrders = orders.filter { it.status == OrderStatus.COMPLETED }
        val totalIncome = completedOrders.sumOf { it.totalPrice }
        val totalGoatsSold = completedOrders.size

        val stockCount = goats.size
        val incomeText = if (totalIncome > 0L) formatShortRupiah(totalIncome) else "12.5jt"
        val soldText = if (totalGoatsSold > 0) "$totalGoatsSold" else "8"
        val averagePriceText = if (totalGoatsSold > 0) formatShortRupiah(totalIncome / totalGoatsSold) else "3.8jt"

        Scaffold(
            topBar = {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp),
                    color = Color(0xFF2E7D32)
                ) {
                    Box(
                        modifier = Modifier
                            .statusBarsPadding()
                            .fillMaxWidth()
                            .height(56.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Dashboard Jual Kambing",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            },
            containerColor = Color(0xFF4CAF50)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Alert Banner: Transaksi Etawa Gagal
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                    border = BorderStroke(1.dp, Color(0xFFFFCDD2))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "⚠️", fontSize = 16.sp)
                        Column {
                            Text(
                                text = "Transaksi Etawa Gagal",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828),
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Stok Sudah Terjual. Silakan pilih kambing lain di bawah.",
                                color = Color(0xFFC62828),
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                // 2. Stats cards in a single row
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Stok Jual
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Stok Jual", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = "$stockCount", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "▼ 1 Habis", fontSize = 9.sp, color = Color(0xFFD32F2F), fontWeight = FontWeight.Medium)
                            }
                        }

                        // Pendapatan
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Pendapatan", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = incomeText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "Bulan ini", fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        // Terjual
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFFFF8E1))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Terjual", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = soldText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF57C00))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "Ekor", fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        // Rata-rata
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE3F2FD))
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "Rata-rata", fontSize = 10.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(text = averagePriceText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1976D2))
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(text = "Per ekor", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                // 3. Stok Siap Jual Section Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stok Siap Jual",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                    
                    Button(
                        onClick = { isAddingGoat = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text("+ Tambah Stok", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // 4. List of Goat stock
                if (goats.isEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Belum ada stok kambing.", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                } else {
                    goats.forEach { goat ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
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
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                
                                Spacer(modifier = Modifier.width(14.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = goat.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "${goat.category.displayName} • ${goat.gender} • ${goat.weight} Kg",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFE8F5E9))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "✓ Sehat",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                                
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = formatShortRupiah(goat.price),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            Toast.makeText(context, "Kambing ${goat.name} dipromosikan untuk dijual!", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                                        modifier = Modifier.height(28.dp)
                                    ) {
                                        Text("Jual", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoatForm(
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
    val coroutineScope = rememberCoroutineScope()
    var imageUriStr by remember { mutableStateOf<String?>(null) }
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
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
                .background(Color(0xFF2E7D32))
                .statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onCancel, modifier = Modifier.size(36.dp)) {
                    Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Tambah Ternak Baru",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Upload Picture
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
                        contentDescription = null,
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
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(32.dp)
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
                            Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = null)
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
                            Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = null)
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
                                    imageUri = finalImageUri,
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

// AdminProfilTab has been replaced by the modular AdminProfilScreen Composable

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
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
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
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
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
                            Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = null)
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
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val activeColor = Color(0xFF1F6E35)
            val inactiveColor = Color.Gray.copy(alpha = 0.6f)

            listOf(
                Triple(AdminTab.HOME, Icons.Outlined.Home, "Home"),
                Triple(AdminTab.DATA, Icons.Outlined.Pets, "Data"),
                Triple(AdminTab.JUAL, Icons.Outlined.LocalOffer, "Jual"),
                Triple(AdminTab.PESANAN, Icons.Outlined.Receipt, "Pesanan"),
                Triple(AdminTab.PESAN, Icons.Outlined.Chat, "Chet"),
                Triple(AdminTab.LAPORAN, Icons.Outlined.Assessment, "Laporan"),
                Triple(AdminTab.PROFIL, Icons.Outlined.Person, "Profil")
            ).forEach { (tab, icon, label) ->
                val isSelected = activeTab == tab
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 2.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) activeColor.copy(alpha = 0.1f) else Color.Transparent,
                    onClick = { onTabSelected(tab) }
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = if (isSelected) activeColor else inactiveColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) activeColor else inactiveColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPesananTab(
    viewModel: AgroGoatViewModel,
    onBack: () -> Unit
) {
    val orders by viewModel.orders.collectAsState()
    val profiles by viewModel.usersProfiles.collectAsState()
    val context = LocalContext.current

    // Alert banner pending count
    val pendingOrders = orders.filter { it.status == OrderStatus.PENDING_PAYMENT }
    val pendingCount = pendingOrders.size

    // Accept / Reject Modal State
    var acceptedOrder by remember { mutableStateOf<OrderItem?>(null) }
    var rejectedOrder by remember { mutableStateOf<OrderItem?>(null) }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                color = Color(0xFF2E7D32)
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .height(56.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
                    Text(
                        text = "Konfirmasi Pesanan",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }
        },
        containerColor = Color(0xFF4CAF50)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Pending Banner
            if (pendingCount > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    border = BorderStroke(1.dp, Color(0xFFFFB74D))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF6C00)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$pendingCount",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Column {
                            Text(
                                text = "Menunggu Konfirmasi Anda",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFE65100),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Segera tolak atau terima agar pembeli tidak menunggu.",
                                color = Color(0xFFE65100),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // 2. Orders list
            if (orders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("📦", fontSize = 48.sp)
                        Text("Belum ada pesanan masuk.", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(orders) { order ->
                        val buyerName = profiles[order.buyerUid]?.get("name") as? String ?: "Pembeli"
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = buyerName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color.Black
                                    )
                                    Text(
                                        text = "10 menit lalu",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                                
                                Text(
                                    text = "Membeli 1x ${order.goat.name}",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )

                                // Payment summary box
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFF1F8E9))
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Total Pembayaran",
                                        fontSize = 12.sp,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = formatRupiah(order.totalPrice),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                
                                // Buttons Row (Terima and Tolak)
                                if (order.status == OrderStatus.PENDING_PAYMENT) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                viewModel.db.collection("orders").document(order.id).update("status", "REJECTED")
                                                rejectedOrder = order
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp),
                                            border = BorderStroke(1.dp, Color(0xFFE53935)),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE53935))
                                        ) {
                                            Text("Tolak ✕", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }

                                        Button(
                                            onClick = {
                                                viewModel.db.collection("orders").document(order.id).update("status", OrderStatus.COMPLETED.name)
                                                acceptedOrder = order
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(40.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text("Terima ✓", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.White)
                                        }
                                    }
                                } else {
                                    // Status Badge info
                                    val (statusText, badgeColor) = when(order.status) {
                                        OrderStatus.PACKING -> Pair("Diproses", Color(0xFFFFF3E0))
                                        OrderStatus.SHIPPING -> Pair("Dikirim", Color(0xFFE3F2FD))
                                        OrderStatus.COMPLETED -> Pair("Selesai", Color(0xFFE8F5E9))
                                        else -> Pair(order.status.name, Color(0xFFECEFF1))
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(badgeColor)
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(text = "Status: $statusText", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Accepted Overlay
    if (acceptedOrder != null) {
        val order = acceptedOrder!!
        val buyerName = profiles[order.buyerUid]?.get("name") as? String ?: "Pembeli"
        Dialog(onDismissRequest = { acceptedOrder = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Check icon circle
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Text(
                        text = "Pesanan Diterima! ✅",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Text(
                        text = "Anda telah menerima pesanan dari $buyerName",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    // Details Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFF1F8E9))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "DETAIL PESANAN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Pembeli",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = buyerName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Text(
                                text = "Item",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = "1x ${order.goat.name} (Si Belang)",
                                fontSize = 13.sp,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Total",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                            Text(
                                text = formatRupiah(order.totalPrice),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF4CAF50))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "✓ Diterima",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Button(
                        onClick = { acceptedOrder = null },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Kembali ke Daftar Pesanan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // Modal Rejected Overlay
    if (rejectedOrder != null) {
        val order = rejectedOrder!!
        val buyerName = profiles[order.buyerUid]?.get("name") as? String ?: "Pembeli"
        Dialog(onDismissRequest = { rejectedOrder = null }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Close icon circle
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE53935)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Text(
                        text = "Pesanan Ditolak ❌",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Text(
                        text = "Anda telah menolak pesanan dari $buyerName",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    // Details Card
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFFEBEE))
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "ALASAN PENOLAKAN",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Stok sudah terjual di kandang (Offline)",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Item: 1x ${order.goat.name}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Button(
                        onClick = { rejectedOrder = null },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Kembali ke Daftar Pesanan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
