package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
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
                "Diproses" -> order.status == OrderStatus.PENDING_PAYMENT || order.status == OrderStatus.PACKING
                "Dikirim" -> order.status == OrderStatus.SHIPPING
                "Selesai" -> order.status == OrderStatus.COMPLETED
                else -> true
            }
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
                val tabOptions = listOf("Semua", "Diproses", "Dikirim", "Selesai")
                items(tabOptions) { tab ->
                    val isSelected = selectedTabFilter == tab
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface
                            )
                            .clickable { selectedTabFilter = tab }
                            .then(
                                if (!isSelected) Modifier.shadow(1.dp, CircleShape) else Modifier
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
                        OrderItemCard(
                            order = order,
                            onActionClick = {
                                if (order.status == OrderStatus.PENDING_PAYMENT) {
                                    viewModel.openPayment(order)
                                } else {
                                    viewModel.trackOrder(order)
                                }
                            }
                        )
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
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = order.status == OrderStatus.COMPLETED

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
            .testTag("order_card_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header: ID and Date with proper space
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ID: ${order.id.take(8)}...",
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = order.orderDate,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.End
                )
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))

            // Body: Content with better spacing to avoid line breaks
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Image - slightly smaller to give text more room
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
                    
                    val ekorText = if (order.id == "AG-2024-0870") "2 ekor" else "1 ekor"
                    Text(
                        text = "${order.goat.gender} • ${order.selectedWeight} kg • $ekorText",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Price and Status on their own lines or combined if space permits
                    // For safety against breaking, we can use a Row but with proper wrapping
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = formatRupiah(order.totalPrice),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            color = Color(0xFF1F6E35), // Theme Green
                            maxLines = 1
                        )
                        
                        // Small status badge
                        OrderStatusBadge(status = order.status)
                    }
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))

            // Bottom Action
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onActionClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val actionText = when {
                        isCompleted -> "Beli Lagi"
                        order.status == OrderStatus.PENDING_PAYMENT -> "Bayar Sekarang"
                        else -> "Lacak Pesanan"
                    }
                    
                    Text(
                        text = actionText,
                        color = Color(0xFF1F6E35), // Theme Green
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, 
                        contentDescription = null, 
                        tint = Color.Gray, 
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun OrderStatusBadge(status: OrderStatus) {
    val (bgColor, textColor, icon) = when (status) {
        OrderStatus.PENDING_PAYMENT -> Triple(Color(0xFFFFEBEE), Color(0xFFD32F2F), "💳")
        OrderStatus.PACKING -> Triple(Color(0xFFFFF3E0), Color(0xFFF57C00), "🕒")
        OrderStatus.SHIPPING -> Triple(Color(0xFFE3F2FD), Color(0xFF1E88E5), "🚚")
        OrderStatus.COMPLETED -> Triple(Color(0xFFE8F5E9), Color(0xFF4CAF50), null)
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (icon != null) {
                Text(text = icon, fontSize = 10.sp)
            } else {
                Icon(Icons.Default.Check, contentDescription = null, tint = textColor, modifier = Modifier.size(10.dp))
            }
            Text(
                text = status.displayName,
                color = textColor,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}
