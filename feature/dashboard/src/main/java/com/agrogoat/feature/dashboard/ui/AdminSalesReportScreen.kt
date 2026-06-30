package com.agrogoat.feature.dashboard.ui

import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrogoat.core.designsystem.components.formatRupiah
import com.agrogoat.core.shared.AgroGoatViewModel
import com.agrogoat.core.model.OrderStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSalesReportScreen(
    viewModel: AgroGoatViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isYearlyMode by remember { mutableStateOf(false) }

    val allOrders by viewModel.orders.collectAsState()
    val validOrders = remember(allOrders) {
        allOrders.filter { it.status != OrderStatus.PENDING_PAYMENT }
    }

    val currentCalendar = Calendar.getInstance()
    val currentMonth = currentCalendar.get(Calendar.MONTH) // 0-indexed
    val currentYear = currentCalendar.get(Calendar.YEAR)

    val monthName = SimpleDateFormat("MMMM yyyy", Locale("id", "ID")).format(currentCalendar.time)

    // Parse order dates: "dd MMM yyyy, HH:mm"
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    val currentMonthOrders = remember(validOrders) {
        validOrders.filter {
            try {
                val date = sdf.parse(it.orderDate)
                if (date != null) {
                    val cal = Calendar.getInstance().apply { time = date }
                    cal.get(Calendar.MONTH) == currentMonth && cal.get(Calendar.YEAR) == currentYear
                } else false
            } catch (e: Exception) { false }
        }
    }

    val currentYearOrders = remember(validOrders) {
        validOrders.filter {
            try {
                val date = sdf.parse(it.orderDate)
                if (date != null) {
                    val cal = Calendar.getInstance().apply { time = date }
                    cal.get(Calendar.YEAR) == currentYear
                } else false
            } catch (e: Exception) { false }
        }
    }

    val prevYearOrders = remember(validOrders) {
        validOrders.filter {
            try {
                val date = sdf.parse(it.orderDate)
                if (date != null) {
                    val cal = Calendar.getInstance().apply { time = date }
                    cal.get(Calendar.YEAR) == currentYear - 1
                } else false
            } catch (e: Exception) { false }
        }
    }

    // --- Monthly Stats ---
    val monthlyRevenue = currentMonthOrders.sumOf { it.totalPrice }
    val monthlyGoatsSold = currentMonthOrders.size
    val monthlyAvgPrice = if (monthlyGoatsSold > 0) monthlyRevenue / monthlyGoatsSold else 0L

    val topSellingCategories = remember(currentMonthOrders) {
        currentMonthOrders.groupBy { it.goat.category.name }
            .map { (cat, orders) -> cat to orders.size }
            .sortedByDescending { it.second }
            .take(3)
    }

    val maxDays = currentCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    val dailyChartPoints = remember(currentMonthOrders) {
        val points = mutableListOf<Float>()
        val salesByDay = currentMonthOrders.groupBy { 
            try {
                val date = sdf.parse(it.orderDate)
                if (date != null) {
                    val cal = Calendar.getInstance().apply { time = date }
                    cal.get(Calendar.DAY_OF_MONTH)
                } else 1
            } catch (e: Exception) { 1 }
        }.mapValues { it.value.sumOf { o -> o.totalPrice } }
        
        val maxSale = salesByDay.values.maxOrNull()?.toFloat() ?: 1f
        val safeMax = if (maxSale == 0f) 1f else maxSale

        for (i in 1..maxDays) {
            val dailySale = salesByDay[i]?.toFloat() ?: 0f
            // Normalizing to 0.15f - 0.85f height range (1.0 is bottom)
            val normalized = 0.85f - ((dailySale / safeMax) * 0.7f)
            points.add(normalized)
        }
        points
    }

    // --- Yearly Stats ---
    val yearlyRevenue = currentYearOrders.sumOf { it.totalPrice }
    val yearlyTransactions = currentYearOrders.size
    val yearlyAvgPrice = if (yearlyTransactions > 0) yearlyRevenue / yearlyTransactions else 0L

    val prevYearRevenue = prevYearOrders.sumOf { it.totalPrice }
    val yearGrowth = if (prevYearRevenue > 0) {
        ((yearlyRevenue - prevYearRevenue).toFloat() / prevYearRevenue.toFloat() * 100).toInt()
    } else if (yearlyRevenue > 0) 100 else 0

    val salesByMonth = remember(currentYearOrders) {
        val map = currentYearOrders.groupBy {
            try {
                val date = sdf.parse(it.orderDate)
                if (date != null) {
                    val cal = Calendar.getInstance().apply { time = date }
                    cal.get(Calendar.MONTH)
                } else 0
            } catch (e: Exception) { 0 }
        }.mapValues { it.value.sumOf { o -> o.totalPrice } }
        
        (0..11).map { map[it] ?: 0L }
    }
    
    val bestMonthIdx = salesByMonth.withIndex().maxByOrNull { it.value }?.index ?: currentMonth
    val monthNamesShort = listOf("Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des")
    val bestMonthName = monthNamesShort[bestMonthIdx]
    
    val maxMonthSale = salesByMonth.maxOrNull()?.toFloat() ?: 1f
    val safeMaxMonth = if (maxMonthSale == 0f) 1f else maxMonthSale

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
                        .padding(horizontal = 12.dp, vertical = 16.dp),
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
                        text = if (isYearlyMode) "Laporan Tahun $currentYear" else "Laporan Penjualan",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )

                    if (isYearlyMode) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp)),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("$currentYear", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("▼", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    }
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
            // Tab Selector Pill capsule
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isYearlyMode = false },
                        shape = RoundedCornerShape(20.dp),
                        color = if (!isYearlyMode) Color(0xFF2E7D32) else Color.Transparent
                    ) {
                        Text(
                            text = "Bulan Ini",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (!isYearlyMode) Color.White else Color.Gray
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { isYearlyMode = true },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isYearlyMode) Color(0xFF2E7D32) else Color.Transparent
                    ) {
                        Text(
                            text = "Tahun ini",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = if (isYearlyMode) Color.White else Color.Gray
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!isYearlyMode) {
                    // MONTHLY VIEW
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.05f),
                                    radius = 120.dp.toPx(),
                                    center = Offset(size.width, size.height / 2f)
                                )
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.03f),
                                    radius = 160.dp.toPx(),
                                    center = Offset(size.width, size.height / 2f)
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Total Pendapatan ($monthName)",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = formatRupiah(monthlyRevenue),
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Transaksi Bulan Ini: $monthlyGoatsSold",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Stat Cards Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Total Terjual", fontSize = 12.sp, color = Color.Gray)
                                Text("$monthlyGoatsSold", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                Text("Ekor", fontSize = 10.sp, color = Color.Gray)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Rata-rata/Ekor", fontSize = 12.sp, color = Color.Gray)
                                Text(
                                    if (monthlyAvgPrice >= 1_000_000) "${monthlyAvgPrice / 1_000_000}jt" else formatRupiah(monthlyAvgPrice),
                                    fontSize = 22.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = Color(0xFF1E88E5)
                                )
                                Text("Per ekor", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    // Daily Sales Trend Chart Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Tren Penjualan Harian",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Lihat Detail →",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.clickable {}
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .padding(vertical = 8.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val width = size.width
                                    val height = size.height

                                    val points = dailyChartPoints.mapIndexed { index, normalizedY ->
                                        val xPos = if (maxDays > 1) {
                                            (index.toFloat() / (maxDays - 1).toFloat()) * width
                                        } else {
                                            width / 2f
                                        }
                                        Offset(xPos, normalizedY * height)
                                    }

                                    if (points.isNotEmpty()) {
                                        val fillPath = Path().apply {
                                            moveTo(points.first().x, height)
                                            for (point in points) {
                                                lineTo(point.x, point.y)
                                            }
                                            lineTo(points.last().x, height)
                                            close()
                                        }
                                        drawPath(
                                            path = fillPath,
                                            brush = Brush.verticalGradient(
                                                colors = listOf(Color(0xFFE8F5E9), Color.White)
                                            )
                                        )

                                        val strokePath = Path().apply {
                                            moveTo(points.first().x, points.first().y)
                                            for (i in 1 until points.size) {
                                                lineTo(points[i].x, points[i].y)
                                            }
                                        }
                                        drawPath(
                                            path = strokePath,
                                            color = Color(0xFF2E7D32),
                                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                                        )

                                        // Draw point dots for a few selected days (e.g. 1st, 5th, 10th...)
                                        val displayIndices = listOf(0, 4, 9, 14, 19, 24, maxDays - 1)
                                        for (i in points.indices) {
                                            if (i in displayIndices) {
                                                drawCircle(
                                                    color = Color(0xFF2E7D32),
                                                    radius = 4.dp.toPx(),
                                                    center = points[i]
                                                )
                                                drawCircle(
                                                    color = Color.White,
                                                    radius = 2.dp.toPx(),
                                                    center = points[i]
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf(1, 5, 10, 15, 20, 25, maxDays).forEach { day ->
                                    Text("$day", fontSize = 10.sp, color = Color.Gray)
                                }
                            }
                        }
                    }

                    // Best Selling Products Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Kategori Produk Terlaris",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                            }

                            if (topSellingCategories.isEmpty()) {
                                Text("Belum ada penjualan bulan ini.", color = Color.Gray, fontSize = 13.sp)
                            } else {
                                topSellingCategories.forEachIndexed { index, (name, count) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(Color(0xFFF8FAFC))
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${index + 1}. $name",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color.DarkGray
                                        )
                                        Text(
                                            text = "$count Ekor",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF2E7D32)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // YEARLY VIEW
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                drawCircle(
                                    color = Color.White.copy(alpha = 0.05f),
                                    radius = 130.dp.toPx(),
                                    center = Offset(size.width, size.height / 2f)
                                )
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(20.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "TOTAL PENDAPATAN $currentYear",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = formatRupiah(yearlyRevenue),
                                        color = Color.White,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (yearGrowth >= 0) "▲ $yearGrowth% dari tahun ${currentYear - 1} • $yearlyTransactions Transaksi" else "▼ ${-yearGrowth}% dari tahun ${currentYear - 1} • $yearlyTransactions Transaksi",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Stats Cards Grid (3 cards)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Total", fontSize = 11.sp, color = Color.Gray)
                                Text("$yearlyTransactions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Transaksi", fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Rata-rata", fontSize = 11.sp, color = Color.Gray)
                                Text(
                                    if (yearlyAvgPrice >= 1_000_000) "${yearlyAvgPrice / 1_000_000}jt" else formatRupiah(yearlyAvgPrice),
                                    fontSize = 18.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = Color(0xFF1E88E5)
                                )
                                Text("Per ekor", fontSize = 9.sp, color = Color.Gray)
                            }
                        }

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(80.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text("Bulan", fontSize = 11.sp, color = Color.Gray)
                                Text(bestMonthName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF6D00))
                                Text("Terbaik", fontSize = 9.sp, color = Color.Gray)
                            }
                        }
                    }

                    // Monthly Sales Bar Chart
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Penjualan Per Bulan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.Black
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                salesByMonth.forEachIndexed { index, valRaw ->
                                    val valK = valRaw / 1_000_000f // in millions
                                    val isCurrentMonth = index == currentMonth
                                    val isFuture = index > currentMonth
                                    
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    ) {
                                        if (isCurrentMonth && valK > 0) {
                                            Text(
                                                text = "${String.format(Locale.US, "%.1f", valK)}jt",
                                                fontSize = 7.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1B5E20),
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        }

                                        val heightRatio = if (safeMaxMonth > 0) (valRaw.toFloat() / safeMaxMonth) * 0.9f else 0.05f
                                        val actualRatio = max(0.05f, heightRatio)

                                        if (isFuture) {
                                            Box(
                                                modifier = Modifier
                                                    .width(10.dp)
                                                    .fillMaxHeight(0.1f) // Dummy small height for future
                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                    .border(
                                                        width = 1.dp,
                                                        color = Color.Gray.copy(alpha = 0.5f),
                                                        shape = RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                                    )
                                                    .background(Color(0xFFECEFF1))
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .width(10.dp)
                                                    .fillMaxHeight(actualRatio)
                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                    .background(if (isCurrentMonth) Color(0xFF1B5E20) else Color(0xFF81C784))
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(monthNamesShort[index], fontSize = 8.sp, color = Color.Gray)
                                    }
                                }
                            }
                        }
                    }

                    // Yearly Comparison
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Perbandingan Tahunan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.Black
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Tahun ${currentYear - 1}", fontSize = 11.sp, color = Color.Gray)
                                        val pYText = if (prevYearRevenue >= 1_000_000) "${prevYearRevenue / 1_000_000}jt" else formatRupiah(prevYearRevenue)
                                        Text(pYText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text("→", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                    if (yearGrowth != 0) {
                                        val growthColor = if (yearGrowth > 0) Color(0xFF2E7D32) else Color.Red
                                        val growthBg = if (yearGrowth > 0) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                        val prefix = if (yearGrowth > 0) "+" else ""
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(growthBg)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("$prefix$yearGrowth%", color = growthColor, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(4.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text("Tahun $currentYear", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                        val cYText = if (yearlyRevenue >= 1_000_000) "${yearlyRevenue / 1_000_000}jt" else formatRupiah(yearlyRevenue)
                                        Text(cYText, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
