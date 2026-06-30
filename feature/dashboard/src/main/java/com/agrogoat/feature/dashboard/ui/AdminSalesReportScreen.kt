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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.TrendingUp
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSalesReportScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isYearlyMode by remember { mutableStateOf(false) }

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
                        text = if (isYearlyMode) "Laporan Tahun 2026" else "Laporan Penjualan",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )

                    if (isYearlyMode) {
                        // "2026 ▼" capsule selector
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
                                Text("2026", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("▼", color = Color.White, fontSize = 10.sp)
                            }
                        }
                    } else {
                        // Calendar and Chart Buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = {},
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White),
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CalendarMonth,
                                    contentDescription = "Kalender",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = {},
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.InsertChart,
                                    contentDescription = "Grafik",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFF4CAF50) // Premium bright-green backdrop matching screenshots
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
                    // Bulan Ini Tab
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

                    // Tahun Ini Tab
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

            // Scrollable Content
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
                    // Main Income Banner Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B5E20))
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Circular design overlay checks
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
                                    text = "Total Pendapatan (Juni 2026)",
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Rp 12.500.000",
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "▲ 15% dari bulan lalu • 8 Transaksi",
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
                                Text("8", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
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
                                Text("3.8jt", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E88E5))
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

                            // Custom Line Graph using Canvas
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp)
                                    .padding(vertical = 8.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val width = size.width
                                    val height = size.height

                                    // Line points
                                    val points = listOf(
                                        Offset(0.08f * width, 0.85f * height),
                                        Offset(0.25f * width, 0.70f * height),
                                        Offset(0.42f * width, 0.78f * height),
                                        Offset(0.58f * width, 0.55f * height),
                                        Offset(0.75f * width, 0.62f * height),
                                        Offset(0.92f * width, 0.40f * height),
                                        Offset(1.08f * width, 0.48f * height),
                                        Offset(1.25f * width, 0.25f * height)
                                    )

                                    // Draw background gradient under path
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

                                    // Draw path line
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

                                    // Draw point dots
                                    for (point in points) {
                                        if (point.x <= width) {
                                            drawCircle(
                                                color = Color(0xFF2E7D32),
                                                radius = 5.dp.toPx(),
                                                center = point
                                            )
                                            drawCircle(
                                                color = Color.White,
                                                radius = 2.5.dp.toPx(),
                                                center = point
                                            )
                                        }
                                    }
                                }
                            }

                            // X Axis Labels
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                listOf("1 Jun", "5 Jun", "10 Jun", "15 Jun", "20 Jun").forEach { label ->
                                    Text(label, fontSize = 10.sp, color = Color.Gray)
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
                                    text = "Produk Terlaris",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                                Text(
                                    text = "Lihat Semua →",
                                    color = Color(0xFF2E7D32),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.clickable {}
                                )
                            }

                            // Product Items list
                            listOf(
                                Pair("1. Kambing Etawa Jantan", "5 Ekor"),
                                Pair("2. Kambing Boer Betina", "2 Ekor"),
                                Pair("3. Kambing Kacang Muda", "1 Ekor")
                            ).forEach { (name, count) ->
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
                                        text = name,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.DarkGray
                                    )
                                    Text(
                                        text = count,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // YEARLY VIEW
                    // Main Income Banner Card (Yearly)
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
                                    text = "TOTAL PENDAPATAN 2026",
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
                                        text = "Rp 142.5jt",
                                        color = Color.White,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )

                                    // Target capsule badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFFFFB300))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "🏆 Target Tercapai 118%",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "▲ 45% dari tahun 2025 • 96 Transaksi",
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
                                Text("96", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
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
                                Text("3.6jt", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E88E5))
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
                                Text("Jun", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF6D00))
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

                            // 12 columns bar representation
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(130.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                val monthlyData = listOf(
                                    Triple("Jan", 8f, false),
                                    Triple("Feb", 10f, false),
                                    Triple("Mar", 12f, false),
                                    Triple("Apr", 9f, false),
                                    Triple("Mei", 14f, false),
                                    Triple("Jun", 12.5f, true), // Highlighted!
                                    Triple("Jul", 5f, null),     // Projected (dotted outline)
                                    Triple("Agu", 6f, null),
                                    Triple("Sep", 7f, null),
                                    Triple("Okt", 9f, null),
                                    Triple("Nov", 11f, null),
                                    Triple("Des", 12f, null)
                                )

                                monthlyData.forEach { (month, valK, highlightState) ->
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Bottom,
                                        modifier = Modifier
                                            .weight(1f)
                                            .fillMaxHeight()
                                    ) {
                                        if (highlightState == true) {
                                            Text(
                                                text = "${valK}jt",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1B5E20),
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        } else if (highlightState == false) {
                                            Text(
                                                text = "${valK.toInt()}jt",
                                                fontSize = 7.sp,
                                                color = Color.Gray,
                                                modifier = Modifier.padding(bottom = 2.dp)
                                            )
                                        }

                                        // Bar view
                                        val heightRatio = valK / 18f
                                        val barColor = if (highlightState == true) Color(0xFF1B5E20) else Color(0xFF81C784)

                                        if (highlightState == null) {
                                            // Future Projection Dotted Bar
                                            Box(
                                                modifier = Modifier
                                                    .width(10.dp)
                                                    .fillMaxHeight(heightRatio)
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
                                                    .fillMaxHeight(heightRatio)
                                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                                    .background(barColor)
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(month, fontSize = 9.sp, color = Color.Gray)
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
                                        Text("Tahun 2025", fontSize = 11.sp, color = Color.Gray)
                                        Text("Rp 98jt", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                                    }
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Text("→", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFE8F5E9))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("+45%", color = Color(0xFF2E7D32), fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                                        Text("Tahun 2026", fontSize = 11.sp, color = Color(0xFF2E7D32))
                                        Text("Rp 142.5jt", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                                    }
                                }
                            }
                        }
                    }

                    // Highlight card at bottom
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF5)),
                        border = BorderStroke(1.dp, Color(0xFFFFECB3))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Produk Terlaris: Kambing Etawa (48 ekor • Rp 85jt)",
                                color = Color(0xFFD84315),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
