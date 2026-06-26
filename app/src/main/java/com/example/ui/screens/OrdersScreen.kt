package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.GoatSilhouette
import com.example.ui.components.formatRupiah
import com.example.viewmodel.AgroGoatViewModel
import com.example.viewmodel.AppTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersScreen(
    viewModel: AgroGoatViewModel,
    modifier: Modifier = Modifier
) {
    val orders by viewModel.orders.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTabFilter by remember { mutableStateOf("Semua") }

    val filteredOrders = remember(orders, searchQuery, selectedTabFilter) {
        orders.filter { order ->
            (searchQuery.isBlank() || 
             order.goat.name.contains(searchQuery, ignoreCase = true) ||
             order.id.contains(searchQuery, ignoreCase = true)) &&
            when (selectedTabFilter) {
                "Semua" -> true
                "Dikonfirmasi" -> order.status == OrderStatus.PACKING || order.status == OrderStatus.PENDING_PAYMENT
                "Siap diambil" -> order.status == OrderStatus.SHIPPING
                "Selesai" -> order.status == OrderStatus.COMPLETED
                else -> true
            }
        }
    }

    var expandedOrderId by remember(selectedTabFilter) {
        // Expand the first item of the tab by default
        mutableStateOf(filteredOrders.firstOrNull()?.id)
    }

    // Auto-expand first item if selection becomes invalid
    LaunchedEffect(filteredOrders) {
        if (expandedOrderId == null || filteredOrders.none { it.id == expandedOrderId }) {
            expandedOrderId = filteredOrders.firstOrNull()?.id
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Pesanan Saya",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.setTab(AppTab.BERANDA) },
                        modifier = Modifier.testTag("orders_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            "Cari pesanan...",
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
                        .fillMaxWidth()
                        .height(52.dp)
                        .shadow(elevation = 1.dp, shape = CircleShape)
                        .testTag("orders_search_input"),
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
            }

            // Tabs
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val tabOptions = listOf("Semua", "Dikonfirmasi", "Siap diambil", "Selesai")
                items(tabOptions) { tab ->
                    val isSelected = selectedTabFilter == tab
                    val selectedBgColor = when (tab) {
                        "Siap diambil" -> Color(0xFF9C27B0)
                        else -> Color(0xFF1F6E35)
                    }
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSelected) selectedBgColor else MaterialTheme.colorScheme.surface
                            )
                            .clickable { selectedTabFilter = tab }
                            .then(
                                if (!isSelected) Modifier.border(1.dp, Color.LightGray.copy(alpha = 0.5f), CircleShape) else Modifier
                            )
                            .padding(horizontal = 24.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab,
                            color = if (isSelected) Color.White else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Orders list
            if (filteredOrders.isEmpty()) {
                EmptyOrdersView()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredOrders) { order ->
                        val isExpanded = expandedOrderId == order.id
                        OrderItemCard(
                            order = order,
                            isExpanded = isExpanded,
                            onCardClick = {
                                expandedOrderId = if (isExpanded) null else order.id
                            },
                            onActionClick = {
                                if (order.status == OrderStatus.PENDING_PAYMENT) {
                                    viewModel.openPayment(order)
                                } else {
                                    viewModel.trackOrder(order)
                                }
                            },
                            onContactSellerClick = {
                                viewModel.setTab(AppTab.CHAT)
                            }
                        )
                    }

                    // Bottom info card based on tab
                    if (filteredOrders.isNotEmpty()) {
                        item {
                            BottomInfoCard(selectedTabFilter, filteredOrders.size)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyOrdersView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 80.dp),
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
            text = "Belum Ada Pesanan",
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            fontSize = 15.sp
        )
        Text(
            text = "Riwayat transaksi Anda akan terlihat di sini.",
            color = Color.LightGray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 40.dp)
        )
    }
}

@Composable
fun OrderItemCard(
    order: OrderItem,
    isExpanded: Boolean,
    onCardClick: () -> Unit,
    onActionClick: () -> Unit,
    onContactSellerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val picBgColor = when (order.goat.category) {
        GoatCategory.POTONG -> Color(0xFFE8F5E9)
        GoatCategory.ETAWA -> Color(0xFFFFF3E0)
        GoatCategory.PERAH -> Color(0xFFE3F2FD)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clickable { onCardClick() }
            .testTag("order_card_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
            // Header: ID and Date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order ID: ${order.id}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = order.orderDate,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End
                )
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))

            // Body: Content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(picBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    GoatSilhouette(Modifier.size(40.dp), tint = Color.Black.copy(alpha = 0.8f))
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Text Column
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.goat.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    val ageText = if (order.goat.age == 1.0) "1 Th" else "1,5 Th"
                    Text(
                        text = "${order.goat.gender} • ${order.selectedWeight} kg • $ageText",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = formatRupiah(order.totalPrice),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        color = Color(0xFF1F6E35), // Theme Green
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Right Status Badge/Button
                OrderStatusBadge(status = order.status)
            }

            // Expanded content section
            if (isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))

                    // Jadwal Pengambilan (only for SHIPPING status - Siap Diambil)
                    if (order.status == OrderStatus.SHIPPING) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF4F6F4), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Jadwal Pengambilan:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "20 Juni 2024",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "09.00 - 10.00 WIB",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            }
                        }
                    }

                    // Timeline Progress Stepper
                    OrderStepper(status = order.status)

                    // Optional Expanded Button Action Row
                    if (order.status == OrderStatus.SHIPPING || order.status == OrderStatus.PENDING_PAYMENT) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (order.status == OrderStatus.SHIPPING) {
                                OutlinedButton(
                                    onClick = onContactSellerClick,
                                    border = BorderStroke(1.dp, Color(0xFF1F6E35)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1F6E35)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = "Hubungi Penjual",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            } else if (order.status == OrderStatus.PENDING_PAYMENT) {
                                Button(
                                    onClick = onActionClick,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F6E35)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Text(
                                        text = "Bayar Sekarang",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = Color.White
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

@Composable
fun OrderStatusBadge(status: OrderStatus) {
    val isCompleted = status == OrderStatus.COMPLETED
    val isShipping = status == OrderStatus.SHIPPING
    val isPacking = status == OrderStatus.PACKING
    
    val displayName = when (status) {
        OrderStatus.PENDING_PAYMENT -> "Bayar"
        OrderStatus.PACKING -> "Dikonfirmasi"
        OrderStatus.SHIPPING -> "Siap Diambil"
        OrderStatus.COMPLETED -> "Selesai"
    }

    if (isCompleted) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, Color(0xFF1F6E35), RoundedCornerShape(20.dp))
                .background(Color(0xFFE8F5E9))
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayName,
                color = Color(0xFF1F6E35),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    } else {
        val bgColor = when {
            isShipping -> Color(0xFF9C27B0) // Purple
            isPacking -> Color(0xFF1F6E35) // Green
            else -> Color(0xFFEF5350) // Red/Orange for Pay
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(bgColor)
                .padding(horizontal = 16.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = displayName,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OrderStepper(status: OrderStatus) {
    val steps = listOf("Dipesan", "Dikonfirmasi", "Siap Diambil", "Selesai")
    val activeIndex = when (status) {
        OrderStatus.PENDING_PAYMENT -> 0
        OrderStatus.PACKING -> 1
        OrderStatus.SHIPPING -> 2
        OrderStatus.COMPLETED -> 3
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF4F6F4), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        for (i in 0..3) {
            val isPassed = i < activeIndex
            val isActive = i == activeIndex
            val isCompleted = status == OrderStatus.COMPLETED

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Left connecting line
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (i == 0) Color.Transparent
                                else if (isPassed || isCompleted || (isActive && activeIndex > 0)) {
                                    if (isCompleted || i <= activeIndex) Color(0xFF1F6E35) else Color(0xFF9C27B0)
                                } else Color.LightGray.copy(alpha = 0.5f)
                            )
                    )

                    // Circle
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isCompleted || isPassed -> Color(0xFF1F6E35) // Green
                                    isActive -> Color(0xFF9C27B0) // Purple
                                    else -> Color.LightGray.copy(alpha = 0.5f) // Gray
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted || isPassed) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        } else if (isActive) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                        }
                    }

                    // Right connecting line
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .background(
                                if (i == 3) Color.Transparent
                                else if (isPassed || (isCompleted && i < 3)) Color(0xFF1F6E35)
                                else if (isActive) Color.LightGray.copy(alpha = 0.5f) // dashed-like effect
                                else Color.LightGray.copy(alpha = 0.5f)
                            )
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                val labelColor = when {
                    isCompleted || isPassed -> Color(0xFF1F6E35)
                    isActive -> Color(0xFF9C27B0)
                    else -> Color.Gray
                }
                val displayLabel = if (i == 2 && isCompleted) "Diambil" else steps[i]
                Text(
                    text = displayLabel,
                    fontSize = 10.sp,
                    fontWeight = if (isActive || isCompleted || isPassed) FontWeight.Bold else FontWeight.Medium,
                    color = labelColor,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun PackageIcon(modifier: Modifier = Modifier) {
    val leftPath = remember { Path() }
    val rightPath = remember { Path() }
    val topPath = remember { Path() }
    val tapePath = remember { Path() }
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        // Brown colors
        val lightBrown = Color(0xFFD7CCC8)
        val mediumBrown = Color(0xFFA1887F)
        val darkBrown = Color(0xFF8D6E63)
        val tapeColor = Color(0xFF5D4037)

        // Left side
        leftPath.reset()
        leftPath.moveTo(w * 0.15f, h * 0.4f)
        leftPath.lineTo(w * 0.5f, h * 0.6f)
        leftPath.lineTo(w * 0.5f, h * 0.9f)
        leftPath.lineTo(w * 0.15f, h * 0.7f)
        leftPath.close()
        drawPath(leftPath, color = mediumBrown)

        // Right side
        rightPath.reset()
        rightPath.moveTo(w * 0.5f, h * 0.6f)
        rightPath.lineTo(w * 0.85f, h * 0.4f)
        rightPath.lineTo(w * 0.85f, h * 0.7f)
        rightPath.lineTo(w * 0.5f, h * 0.9f)
        rightPath.close()
        drawPath(rightPath, color = darkBrown)

        // Top side
        topPath.reset()
        topPath.moveTo(w * 0.5f, h * 0.2f)
        topPath.lineTo(w * 0.85f, h * 0.4f)
        topPath.lineTo(w * 0.5f, h * 0.6f)
        topPath.lineTo(w * 0.15f, h * 0.4f)
        topPath.close()
        drawPath(topPath, color = lightBrown)

        // Tape on top
        tapePath.reset()
        tapePath.moveTo(w * 0.4f, h * 0.26f)
        tapePath.lineTo(w * 0.75f, h * 0.46f)
        tapePath.lineTo(w * 0.6f, h * 0.54f)
        tapePath.lineTo(w * 0.25f, h * 0.34f)
        tapePath.close()
        drawPath(tapePath, color = tapeColor)
    }
}

@Composable
fun BottomInfoCard(tab: String, count: Int) {
    if (tab == "Semua") return

    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color.LightGray.copy(alpha = 0.6f),
                    style = Stroke(width = 3f, pathEffect = dashEffect),
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (tab == "Siap diambil") Color(0xFFF3E5F5) else Color(0xFFE8F5E9)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (tab == "Siap diambil") {
                    PackageIcon(modifier = Modifier.size(24.dp))
                } else {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF4CAF50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            val countText = if (count == 1) "1 pesanan" else "$count pesanan"

            val titleText = when (tab) {
                "Siap diambil" -> "Hanya $countText siap diambil"
                "Selesai" -> "Hanya $countText selesai"
                else -> "Hanya $countText dikonfirmasi"
            }

            val subtitleText = when (tab) {
                "Siap diambil" -> "Segera ambil pesanan Anda!"
                "Selesai" -> "Terima kasih telah berbelanja!"
                else -> "Peternak sedang menyiapkan pesanan Anda."
            }

            Text(
                text = titleText,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                fontSize = 14.sp
            )

            Text(
                text = subtitleText,
                color = Color.LightGray,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            if (tab == "Selesai") {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Lihat pesanan lain di tab berbeda",
                    color = Color(0xFF1F6E35),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.clickable { /* Tab navigation handled by user clicks */ }
                )
            }
        }
    }
}
