package com.agrogoat.feature.dashboard.ui
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrogoat.core.model.OrderItem
import com.agrogoat.core.model.OrderStatus
import com.agrogoat.core.designsystem.components.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(
    order: OrderItem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Lacak Pesanan",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            // Order Summary Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = order.goat.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Order ID: ${order.id}",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                        Text(
                            text = formatRupiah(order.totalPrice),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Tracking Info Section
            item {
                Text(
                    text = "Status Pengiriman",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // Timeline / Stepper
            val trackingSteps = getTrackingSteps(order)
            itemsIndexed(trackingSteps) { index, step ->
                TrackingStepItem(
                    step = step,
                    isFirst = index == 0,
                    isLast = index == trackingSteps.size - 1,
                    isActive = index == 0 // Assuming the first one is the most recent/active
                )
            }
            
            // Shipping Address
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Alamat Pengiriman",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Eko Prasetyo",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Jl. Sudirman No. 123, Bengkalis, Riau, 28711",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

data class TrackingStep(
    val title: String,
    val description: String,
    val time: String,
    val status: OrderStatus
)

fun getTrackingSteps(order: OrderItem): List<TrackingStep> {
    val steps = mutableListOf<TrackingStep>()
    
    when (order.status) {
        OrderStatus.COMPLETED -> {
            steps.add(TrackingStep("Pesanan Selesai", "Kambing telah diterima dengan baik oleh pembeli.", "15 Jun, 14:20", OrderStatus.COMPLETED))
            steps.add(TrackingStep("Pesanan Sampai", "Kurir telah sampai di lokasi tujuan.", "15 Jun, 13:45", OrderStatus.COMPLETED))
            steps.add(TrackingStep("Dalam Perjalanan", "Pesanan sedang dikirim oleh kurir Agro Goat.", "15 Jun, 09:00", OrderStatus.SHIPPING))
            steps.add(TrackingStep("Sedang Dikemas", "Pihak peternak sedang menyiapkan kambing.", "14 Jun, 16:00", OrderStatus.PACKING))
            steps.add(TrackingStep("Pesanan Dibuat", "Pesanan berhasil dibuat.", "14 Jun, 15:30", OrderStatus.PENDING_PAYMENT))
        }
        OrderStatus.SHIPPING -> {
            steps.add(TrackingStep("Dalam Perjalanan", "Pesanan dalam perjalanan ke alamat tujuan.", "20 Jun, 10:30", OrderStatus.SHIPPING))
            steps.add(TrackingStep("Sedang Dikemas", "Kambing telah diperiksa kesehatannya dan siap kirim.", "20 Jun, 08:00", OrderStatus.PACKING))
            steps.add(TrackingStep("Pesanan Dibuat", "Pesanan berhasil dibuat.", "19 Jun, 21:00", OrderStatus.PENDING_PAYMENT))
        }
        OrderStatus.PACKING -> {
            steps.add(TrackingStep("Sedang Dikemas", "Peternak sedang melakukan verifikasi akhir kondisi kambing.", "20 Jun, 09:15", OrderStatus.PACKING))
            steps.add(TrackingStep("Pesanan Dibuat", "Pesanan berhasil dibuat.", "20 Jun, 09:00", OrderStatus.PENDING_PAYMENT))
        }
        OrderStatus.PENDING_PAYMENT -> {
            steps.add(TrackingStep("Menunggu Konfirmasi", "Silakan tunggu peternak mengonfirmasi pesanan Anda.", "20 Jun, 08:45", OrderStatus.PENDING_PAYMENT))
        }
    }
    return steps
}

@Composable
fun TrackingStepItem(
    step: TrackingStep,
    isFirst: Boolean,
    isLast: Boolean,
    isActive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(30.dp)
        ) {
            // Top line
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(if (isFirst) Color.Transparent else Color.LightGray)
            )
            
            // Dot
            Box(
                modifier = Modifier
                    .size(if (isActive) 14.dp else 10.dp)
                    .clip(CircleShape)
                    .background(if (isActive) MaterialTheme.colorScheme.primary else Color.LightGray)
            )
            
            // Bottom line
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .weight(1f)
                    .background(if (isLast) Color.Transparent else Color.LightGray)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(
            modifier = Modifier
                .padding(bottom = 24.dp)
                .weight(1f)
        ) {
            Text(
                text = step.title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = if (isActive) MaterialTheme.colorScheme.primary else Color.Black
            )
            Text(
                text = step.description,
                fontSize = 12.sp,
                color = Color.Gray,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = step.time,
                fontSize = 11.sp,
                color = Color.LightGray
            )
        }
    }
}
