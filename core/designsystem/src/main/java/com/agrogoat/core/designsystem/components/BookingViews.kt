package com.agrogoat.core.designsystem.components
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.agrogoat.core.designsystem.R
import com.agrogoat.core.model.*
import com.agrogoat.core.designsystem.components.*
import com.agrogoat.core.designsystem.components.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.agrogoat.core.designsystem.components.detail.GoatDetailView

enum class CatalogSubScreen {
    LIST,
    DETAIL,
    BOOKING_STEP1,
    BOOKING_STEP2,
    BOOKING_STEP3,
    BOOKING_SUCCESS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingStep1View(
    goat: GoatItem,
    name: String,
    onNameChange: (String) -> Unit,
    phone: String,
    onPhoneChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Pesan Kambing",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        containerColor = Color(0xFFF9FBF9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            // Steps indicator
            BookingProgressHeader(currentStep = 1)

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                "Data Pembeli",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 1. Nama Lengkap Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Nama Lengkap", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E7D32),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Nomor HP Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Nomor HP", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                OutlinedTextField(
                    value = phone,
                    onValueChange = onPhoneChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E7D32),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Email Field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Alamat Email (Opsional)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                OutlinedTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E7D32),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Catatan Field (max 200 characters)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Catatan (Opsional)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { if (it.length <= 200) onNotesChange(it) },
                    placeholder = {
                        Text(
                            "Contoh: Kondisi kandang sudah siap, mohon...",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2E7D32),
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Counter
                Text(
                    text = "${notes.length}/200",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Next CTA Button
            Button(
                onClick = onNext,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Selanjutnya", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -------------------------------------------------------------
// SUB-SCREEN 3: STEP 2 (JADWAL & LOKASI)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingStep2View(
    selectedDate: String,
    onDateClick: () -> Unit,
    selectedTimeSlot: String,
    onTimeSlotSelect: (String) -> Unit,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Pesan Kambing",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        containerColor = Color(0xFFF9FBF9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            BookingProgressHeader(currentStep = 2)

            Spacer(modifier = Modifier.height(18.dp))

            // 1. Pilih Tanggal Pengambilan
            Text("Pilih Tanggal Pengambilan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .clickable { onDateClick() }
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedDate,
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )

                    CustomCalendarIcon(
                        modifier = Modifier.size(20.dp),
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. Pilih Jam Pengambilan
            Text("Pilih Jam Pengambilan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(12.dp))

            val timeSlots = listOf(
                "08.00 - 09.00", "09.00 - 10.00", "10.00 - 11.00",
                "13.00 - 14.00", "14.00 - 15.00", "15.00 - 16.00"
            )

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                timeSlots.chunked(3).forEach { rowSlots ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowSlots.forEach { slot ->
                            val isSelected = selectedTimeSlot == slot
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) Color(0xFF2E7D32) else Color.White)
                                    .border(1.dp, if (isSelected) Color(0xFF2E7D32) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                                    .clickable { onTimeSlotSelect(slot) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = slot,
                                    color = if (isSelected) Color.White else Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Lokasi Pengambilan Card
            Text("Lokasi Pengambilan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(10.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        CustomLocationPinIcon(modifier = Modifier.size(22.dp).padding(top = 2.dp), tint = Color.Black)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                "AGRO GOAT BENGKALIS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.Black
                            )
                            Text(
                                "Jl. Sudirman, Senggoro, Kec. Bengkalis\nKabupaten Bengkalis, Riau",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // "Lihat di Maps" Pill Button
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFFE8F5E9))
                            .clickable {
                                Toast.makeText(context, "Membuka Google Maps...", Toast.LENGTH_SHORT).show()
                            }
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CustomLocationPinIcon(modifier = Modifier.size(14.dp), tint = Color(0xFF2E7D32))
                        Text(
                            "Lihat di Maps",
                            color = Color(0xFF2E7D32),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            // Navigation Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Outlined Kembali
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF2E7D32)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Kembali", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Filled Selanjutnya
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Selanjutnya", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -------------------------------------------------------------
// SUB-SCREEN 4: STEP 3 (KONFIRMASI PESANAN)
// -------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingStep3View(
    goat: GoatItem,
    name: String,
    phone: String,
    email: String,
    date: String,
    timeSlot: String,
    notes: String,
    onBack: () -> Unit,
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Pesan Kambing",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        },
        containerColor = Color(0xFFF9FBF9)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp)
        ) {
            BookingProgressHeader(currentStep = 3)

            Spacer(modifier = Modifier.height(18.dp))

            Text("Konfirmasi Pesanan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(10.dp))

            // Main Card Summary Info Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    
                    // 1. Goat Info (Header row)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(70.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFFFF3E0)),
                            contentAlignment = Alignment.Center
                        ) {
                            GoatSilhouette(modifier = Modifier.size(46.dp), tint = Color.Black.copy(alpha = 0.8f))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(goat.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color.Black)
                            Text("Jantan • ${goat.weight} kg • ${goat.age} Th", fontSize = 11.sp, color = Color.Gray)
                            Text(formatRupiah(goat.price), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp, color = Color(0xFF2E7D32))
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // 2. Data Pembeli
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Data Pembeli", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                        Text(name, fontSize = 12.sp, color = Color.Gray)
                        Text(phone, fontSize = 12.sp, color = Color.Gray)
                        if (email.isNotEmpty()) {
                            Text(email, fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // 3. Jadwal Pengambilan
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Jadwal Pengambilan", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CustomCalendarIcon(modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Text(date, fontSize = 12.sp, color = Color.Gray)
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CustomClockIcon(modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Text(timeSlot, fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // 4. Lokasi Pengambilan
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("Lokasi Pengambilan", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            CustomLocationPinIcon(modifier = Modifier.size(14.dp).padding(top = 2.dp), tint = Color.Black)
                            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("AGRO GOAT BENGKALIS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                                Text("Jl. Sudirman, Senggoro, Kec. Bengkalis Kabupaten Bengkalis, Riau", fontSize = 11.sp, color = Color.Gray, lineHeight = 15.sp)
                            }
                        }
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9), thickness = 1.dp)

                    // 5. Catatan
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("Catatan", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
                        Text(notes.ifBlank { "-" }, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            // Navigation Actions Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Outlined Kembali
                Button(
                    onClick = onBack,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF2E7D32)
                    ),
                    border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Kembali", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                // Filled Selanjutnya
                Button(
                    onClick = onNext,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text("Selanjutnya", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

// -------------------------------------------------------------
// SUB-SCREEN 5: STEP 4 (SUCCESS SCREEN)
// -------------------------------------------------------------
@Composable
fun BookingSuccessView(
    bookingCode: String,
    onCopyCode: () -> Unit,
    onViewOrders: () -> Unit,
    onBackToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Concentric Circle Green checkmark
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4CAF50)),
                contentAlignment = Alignment.Center
            ) {
                // Checkmark
                val p = remember { Path() }
                Canvas(modifier = Modifier.size(34.dp)) {
                    p.reset()
                    p.moveTo(size.width * 0.15f, size.height * 0.52f)
                    p.lineTo(size.width * 0.44f, size.height * 0.8f)
                    p.lineTo(size.width * 0.88f, size.height * 0.24f)
                    drawPath(p, color = Color.White, style = Stroke(width = 5.dp.toPx()))
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            "Pesanan Berhasil!",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            "Kambing Anda telah dipesan.\nSilahkan ambil sesuai jadwal yang telah dipilih.",
            fontSize = 13.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Booking Code Card Box
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F6F4)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "Kode Booking",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = bookingCode,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = Color(0xFF1B5E20)
                    )

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = onCopyCode,
                        modifier = Modifier.size(24.dp)
                    ) {
                        CustomCopyIcon(modifier = Modifier.fillMaxSize(), tint = Color(0xFF2E7D32))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))

        // Action Buttons Row/Column
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // "Lihat Pesanan Saya" button
            Button(
                onClick = onViewOrders,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Lihat Pesanan Saya", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            // "Kembali ke Beranda" button
            Button(
                onClick = onBackToHome,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF2E7D32)
                ),
                border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Kembali ke Beranda", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}

// -------------------------------------------------------------
// PROGRESS INDICATOR HEADER COMPOSABLE
// -------------------------------------------------------------
@Composable
fun BookingProgressHeader(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        StepCircleItem(step = 1, label = "Data Diri", isActive = currentStep >= 1, isCompleted = currentStep > 1)
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(1.5.dp)
                .background(if (currentStep >= 2) Color(0xFF66BB6A) else Color(0xFFE2E8F0))
        )
        StepCircleItem(step = 2, label = "Jadwal", isActive = currentStep >= 2, isCompleted = currentStep > 2)
        Box(
            modifier = Modifier
                .width(60.dp)
                .height(1.5.dp)
                .background(if (currentStep >= 3) Color(0xFF66BB6A) else Color(0xFFE2E8F0))
        )
        StepCircleItem(step = 3, label = "Konfirmasi", isActive = currentStep >= 3, isCompleted = false)
    }
}

@Composable
fun StepCircleItem(
    step: Int,
    label: String,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(
                    if (isCompleted || isActive) Color(0xFF66BB6A) else Color(0xFFE2E8F0)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            } else {
                Text(
                    text = step.toString(),
                    color = if (isActive) Color.White else Color(0xFF888888),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) Color.Black else Color.LightGray
        )
    }
}

// -------------------------------------------------------------
// CUSTOM COMPOSABLE VECTOR ICONS DRAWING
// -------------------------------------------------------------
@Composable
fun CustomCalendarIcon(modifier: Modifier = Modifier, tint: Color = Color.Gray) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.08f

        // Outline rectangle
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.1f, h * 0.2f),
            size = Size(w * 0.8f, h * 0.7f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = stroke)
        )

        // Top horizontal dividing line
        drawLine(
            color = tint,
            start = Offset(w * 0.1f, h * 0.42f),
            end = Offset(w * 0.9f, h * 0.42f),
            strokeWidth = stroke
        )

        // Two little binder loops at top
        drawLine(
            color = tint,
            start = Offset(w * 0.3f, h * 0.08f),
            end = Offset(w * 0.3f, h * 0.22f),
            strokeWidth = stroke
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.7f, h * 0.08f),
            end = Offset(w * 0.7f, h * 0.22f),
            strokeWidth = stroke
        )
    }
}

@Composable
fun CustomClockIcon(modifier: Modifier = Modifier, tint: Color = Color.Gray) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = w * 0.42f
        val stroke = w * 0.08f

        // Dial Circle
        drawCircle(
            color = tint,
            radius = r,
            center = Offset(cx, cy),
            style = Stroke(width = stroke)
        )

        // Center dot
        drawCircle(
            color = tint,
            radius = w * 0.06f,
            center = Offset(cx, cy)
        )

        // Hour hand
        drawLine(
            color = tint,
            start = Offset(cx, cy),
            end = Offset(cx, cy - r * 0.5f),
            strokeWidth = stroke
        )

        // Minute hand
        drawLine(
            color = tint,
            start = Offset(cx, cy),
            end = Offset(cx + r * 0.45f, cy),
            strokeWidth = stroke
        )
    }
}

@Composable
fun CustomCopyIcon(modifier: Modifier = Modifier, tint: Color = Color.Gray) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = w * 0.08f

        // Back page outline
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.24f, h * 0.1f),
            size = Size(w * 0.62f, h * 0.65f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = stroke)
        )

        // Front page white mask
        drawRoundRect(
            color = Color.White,
            topLeft = Offset(w * 0.1f, h * 0.26f),
            size = Size(w * 0.62f, h * 0.65f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )

        // Front page outline
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.1f, h * 0.26f),
            size = Size(w * 0.62f, h * 0.65f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
            style = Stroke(width = stroke)
        )
    }
}

/**
 * Custom Tab component matching style
 */
@Composable
fun FilterTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color(0xFF2E7D32) else Color.White
    val textColor = if (isSelected) Color.White else Color(0xFF757575)
    val borderStroke = if (isSelected) null else BorderStroke(1.dp, Color(0xFFE2E8F0))
    
    Box(
        modifier = modifier
            .let { if (borderStroke != null) it.border(borderStroke, CircleShape) else it }
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 22.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            color = textColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp
        )
    }
}

/**
 * Custom Dropdown button matching style
 */
@Composable
fun FilterDropdown(
    label: String,
    selectedValue: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFiltering = selectedValue != "Semua"
    val borderColor = if (isFiltering) Color(0xFF2E7D32) else Color(0xFFE2E8F0)
    val textColor = if (isFiltering) Color(0xFF2E7D32) else Color(0xFF757575)
    
    Box(
        modifier = modifier
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isFiltering) selectedValue else label,
            color = textColor,
            fontWeight = if (isFiltering) FontWeight.Bold else FontWeight.Normal,
            fontSize = 14.sp,
            maxLines = 1
        )
    }
}

@Composable
fun SortDropdownItem(
    label: String,
    isActive: Boolean,
    isTerbaru: Boolean,
    onClick: () -> Unit
) {
    val textToShow = if (isActive) "✓ $label" else label
    
    val (bgColor, borderStroke, textColor) = when {
        isActive && isTerbaru -> Triple(
            Color(0xFFE8F5E9), // light green
            null,
            Color(0xFF2E7D32)  // brand green
        )
        isActive && !isTerbaru -> Triple(
            Color(0xFFFFF3E0), // light orange
            BorderStroke(1.dp, Color(0xFFF57C00)), // orange border
            Color(0xFFF57C00)  // orange text
        )
        else -> Triple(
            Color.Transparent,
            null,
            Color.Black
        )
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .let { modifier ->
                if (borderStroke != null) {
                    modifier.border(borderStroke, RoundedCornerShape(8.dp))
                } else {
                    modifier
                }
            }
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = textToShow,
            color = textColor,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp
        )
    }
}
