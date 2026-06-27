package com.agrogoat.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrogoat.app.viewmodel.AgroGoatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfilScreen(
    viewModel: AgroGoatViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsState()
    val userAddress by viewModel.userAddress.collectAsState()
    val context = LocalContext.current
    
    var showEditDialog by remember { mutableStateOf(false) }
    var editNameInput by remember { mutableStateOf(userName) }
    var editAddressInput by remember { mutableStateOf(userAddress) }

    // Synchronize edit inputs when userName/userAddress updates
    LaunchedEffect(userName, userAddress) {
        editNameInput = userName
        editAddressInput = userAddress
    }

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
                        .padding(horizontal = 8.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Profil Penjual",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = { Toast.makeText(context, "Membagikan profil...", Toast.LENGTH_SHORT).show() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Bagikan",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFF4CAF50) // Premium green background color matching Image 4
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            // Profile Initials box matching Image 4 circles
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                // Background circle overlays
                Canvas(modifier = Modifier.matchParentSize()) {
                    drawCircle(
                        color = Color.White.copy(alpha = 0.05f),
                        radius = 160.dp.toPx(),
                        center = center
                    )
                }

                // Avatar initials circle with thick outline
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .background(Color(0xFF81C784)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase().ifEmpty { "AF" },
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 44.sp
                    )

                    // Green checkmark verification badge
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1B5E20))
                            .border(2.dp, Color.White, CircleShape)
                            .align(Alignment.BottomEnd),
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

            // Cards container
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Personal Info Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = userName.ifEmpty { "Ahmad Fadillah" },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🏆 Pengelola Makmur Farm • ${userAddress.ifEmpty { "Bengkalis, Riau" }}",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Badges row
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Pro Seller Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFFF8E1))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("⭐ Pro Seller", color = Color(0xFFFFB300), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }

                            // Terverifikasi Badge
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFE8F5E9))
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("✓ Terverifikasi", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Stats Card Grid
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
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        // Terjual Box
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Terjual", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("156", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                            Text("Ekor", fontSize = 10.sp, color = Color.Gray)
                        }

                        Box(modifier = Modifier.width(1.dp).height(50.dp).background(Color(0xFFECEFF1)))

                        // Rating Box
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Rating", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("4.9", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFB300))
                            Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                repeat(5) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB300),
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.width(1.dp).height(50.dp).background(Color(0xFFECEFF1)))

                        // Sejak Box
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Sejak", fontSize = 12.sp, color = Color.Gray)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("2023", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                            Text("Tahun", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }

                // About section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Tentang Peternak",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Kami fokus pada pembibitan kambing Etawa dan Boer berkualitas tinggi. Semua ternak dirawat dengan standar kesehatan terbaik dan terjamin kesehatannya. Melayani pengiriman se-Sumatera.",
                            fontSize = 13.sp,
                            color = Color.DarkGray,
                            lineHeight = 20.sp
                        )
                    }
                }

                // Edit Profile Button & Logout Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onLogout,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.dp, Color.White),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Log Out 🚪", fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = {
                                viewModel.clearOldData {
                                    Toast.makeText(context, "Semua data lama berhasil dihapus!", Toast.LENGTH_LONG).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Reset Data", fontWeight = FontWeight.SemiBold, color = Color.White)
                        }
                    }

                    Button(
                        onClick = { showEditDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                        shape = RoundedCornerShape(20.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                    ) {
                        Text("Edit Profil", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Edit Profile Modal
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Profil Peternak", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editNameInput,
                        onValueChange = { editNameInput = it },
                        label = { Text("Nama Peternak") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = editAddressInput,
                        onValueChange = { editAddressInput = it },
                        label = { Text("Alamat / Lokasi") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editNameInput.isNotBlank() && editAddressInput.isNotBlank()) {
                            viewModel.updateProfile(editNameInput, editAddressInput)
                            showEditDialog = false
                            Toast.makeText(context, "Profil diperbarui!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Harap lengkapi semua kolom!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            }
        )
    }
}
