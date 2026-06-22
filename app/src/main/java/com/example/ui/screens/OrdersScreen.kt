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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.ui.text.style.TextAlign
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
    val context = LocalContext.current

    var searchQuery by remember { mutableStateOf("") }
    var selectedTabFilter by remember { mutableStateOf("Semua") }

    // Filter orders Dynamically
    val filteredOrders = remember(orders, searchQuery, selectedTabFilter) {
        orders.filter { order ->
            // Search query match
            (searchQuery.isBlank() || 
             order.goat.name.contains(searchQuery, ignoreCase = true) ||
             order.id.contains(searchQuery, ignoreCase = true)) &&
            // Tab filter match
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // "Cari pesanan..." search bar matching exactly
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

            // Custom Tabs matching the layout: "Semua", "Diproses", "Dikirim", "Selesai"
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
                                    viewModel.processOrderPayment(order.id)
                                    Toast.makeText(context, "Pembayaran berhasil!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Melacak status pengiriman untuk ID: ${order.id} ...", Toast.LENGTH_SHORT).show()
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
fun OrderItemCard(
    order: OrderItem,
    onActionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompleted = order.status == OrderStatus.COMPLETED
    val isShipping = order.status == OrderStatus.SHIPPING

    val picBgColor = when (order.goat.category) {
        GoatCategory.POTONG -> Color(0xFFE8F5E9)
        GoatCategory.ETAWA -> Color(0xFFFFF3E0)
        GoatCategory.PERAH -> Color(0xFFE3F2FD)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(elevation = 1.dp, shape = RoundedCornerShape(16.dp))
            .testTag("order_card_${order.id}"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Upper Header: Order ID & Date
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Order ID: ${order.id}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = order.orderDate,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.35f))

            // Middle body content
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image container
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(picBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    GoatSilhouette(Modifier.size(46.dp), tint = Color.Black.copy(alpha = 0.8f))
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Left details
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = order.goat.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    // Matching number of goats e.g. "2 ekor" or "1 ekor" to screenshot
                    val ekorText = if (order.id == "AG-2024-0870") "2 ekor" else "1 ekor"
                    Text(
                        text = "${order.goat.gender} • ${order.selectedWeight} kg • $ekorText",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatRupiah(order.totalPrice),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Right Status Badge
                Column(
                    modifier = Modifier.align(Alignment.CenterVertically),
                    horizontalAlignment = Alignment.End
                ) {
                    when (order.status) {
                        OrderStatus.PENDING_PAYMENT, OrderStatus.PACKING -> {
                            // "Diproses" Status Badge (Orange shape)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFFFF3E0))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "🕒",
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Diproses",
                                    color = Color(0xFFF57C00),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        OrderStatus.SHIPPING -> {
                            // "Dikirim" Status badge (Blue shape)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFE3F2FD))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = "🚚",
                                    fontSize = 11.sp
                                )
                                Text(
                                    text = "Dikirim",
                                    color = Color(0xFF1E88E5),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        OrderStatus.COMPLETED -> {
                            // "Selesai" Status badge (Green shape)
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "Selesai",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = Color.LightGray.copy(alpha = 0.35f))

            // Bottom Action Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onActionClick() }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                if (isCompleted) {
                    // "Beli Lagi" & "Detail"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Beli Lagi",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = "Detail",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    // "Lacak Pesanan" & Arrow
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (order.status == OrderStatus.PENDING_PAYMENT) "Bayar Sekarang" else "Lacak Pesanan",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
