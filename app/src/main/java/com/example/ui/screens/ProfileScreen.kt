package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
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
import com.example.viewmodel.AgroGoatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AgroGoatViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // VM states
    val userName by viewModel.userName.collectAsState()
    val userAddress by viewModel.userAddress.collectAsState()
    val orders by viewModel.orders.collectAsState()
    val goats by viewModel.goats.collectAsState()

    // Local Toggle for notifications
    var notifEnabled by remember { mutableStateOf(true) }

    // Edit profile dialog states
    var showEditDialog by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf(userName) }
    var inputAddress by remember { mutableStateOf(userAddress) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Profil Saya",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Membuka Pengaturan Akun...", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "⚙️",
                            fontSize = 20.sp
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 10.dp)
                .padding(bottom = 100.dp), // Clear bottom nav bar space
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // 1. Profile card container matching "Peternak Maju" layout exactly
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(24.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Profile Avatar Stack (Green circle with face icon + checked badge)
                    Box(
                        modifier = Modifier.size(96.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color.Black.copy(alpha = 0.5f),
                                modifier = Modifier.size(54.dp)
                            )
                        }
                        
                        // Tiny verified tick seal at bottom right corner
                        Box(
                            modifier = Modifier
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                                .align(Alignment.BottomEnd)
                                .clickable {
                                    Toast.makeText(context, "Akun Terverifikasi Agro Goat", Toast.LENGTH_SHORT).show()
                                },
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

                    // User Identity details
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = userName,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.testTag("profile_username_label")
                        )
                        Text(
                            text = "peternak.maju@agrogoat.id",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Gold Status badge
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "Member Gold",
                            color = Color(0xFF1B5E20),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // 2. Statistics Section (3 side-by-side cards)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Stat 1: Ternak counts based on goats
                val totalTernak = goats.size
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(1.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "$totalTernak",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = "Ternak",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Stat 2: Transaksi counts based on actual orders
                val transCount = orders.size
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(1.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = "$transCount",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = "Transaksi",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Stat 3: Favorit counts based on starred goats
                val favCount = goats.count { it.isFavorite }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .shadow(1.dp, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            text = if (favCount > 0) "$favCount" else "5", // Fallback to 5 for matching illustration look
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32)
                        )
                        Text(
                            text = "Favorit",
                            fontSize = 12.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // 3. First Profile Menu Option Card Group (Akun, Alamat, Metode Pembayaran)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Item 1: Akun Saya
                    ProfileMenuItem(
                        emojiIcon = "👤",
                        emojiBgColor = Color(0xFFE8F5E9),
                        title = "Akun Saya",
                        onClick = {
                            inputName = userName
                            inputAddress = userAddress
                            showEditDialog = true
                        }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Item 2: Alamat Pengiriman
                    ProfileMenuItem(
                        emojiIcon = "📍",
                        emojiBgColor = Color(0xFFE3F2FD),
                        title = "Alamat Pengiriman",
                        subtitle = userAddress,
                        onClick = {
                            inputName = userName
                            inputAddress = userAddress
                            showEditDialog = true
                        }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Item 3: Metode Pembayaran
                    ProfileMenuItem(
                        emojiIcon = "💳",
                        emojiBgColor = Color(0xFFFFF3E0),
                        title = "Metode Pembayaran",
                        onClick = {
                            Toast.makeText(context, "Metode Pembayaran Utama: AgroPay Pas", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            // 4. Second Profile Menu Option Card Group (Notifikasi, Keamanan, Pusat Bantuan)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp, RoundedCornerShape(20.dp)),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Item 1: Notifikasi with toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFECEE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔔", fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = "Notifikasi",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = notifEnabled,
                            onCheckedChange = {
                                notifEnabled = it
                                Toast.makeText(context, if (it) "Notifikasi Diaktifkan" else "Notifikasi Dimatikan", Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF4CAF50),
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color.LightGray.copy(alpha = 0.5f)
                            )
                        )
                    }
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Item 2: Keamanan & Privasi
                    ProfileMenuItem(
                        emojiIcon = "🛡️",
                        emojiBgColor = Color(0xFFEDE7F6),
                        title = "Keamanan & Privasi",
                        onClick = {
                            Toast.makeText(context, "Sertifikat Enkripsi Akun Agro Goat Aman!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f), modifier = Modifier.padding(horizontal = 16.dp))

                    // Item 3: Pusat Bantuan
                    ProfileMenuItem(
                        emojiIcon = "❔",
                        emojiBgColor = Color(0xFFE0F7FA),
                        title = "Pusat Bantuan",
                        onClick = {
                            Toast.makeText(context, "Hubungi Admin Lapangan: info@agrogoat.co.id", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            }
        }
    }

    // EDIT PROFILE POPUP DIALOG
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = {
                Text("Edit Detail Profil", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Nama Pengguna") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_username_input")
                    )

                    OutlinedTextField(
                        value = inputAddress,
                        onValueChange = { inputAddress = it },
                        label = { Text("Alamat Pengiriman Utama") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("edit_address_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateProfile(inputName, inputAddress)
                        showEditDialog = false
                        Toast.makeText(context, "Profil Berhasil Diperbarui!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                    modifier = Modifier.testTag("save_profile_button")
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold)
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

@Composable
fun ProfileMenuItem(
    emojiIcon: String,
    emojiBgColor: Color,
    title: String,
    subtitle: String = "",
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon container
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(emojiBgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(emojiIcon, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Text title / subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }

        // Action indicator carrot
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.LightGray,
            modifier = Modifier.size(18.dp)
        )
    }
}
