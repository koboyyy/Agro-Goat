package com.agrogoat.app.ui.screens
import androidx.compose.material.icons.outlined.*

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrogoat.app.viewmodel.AgroGoatViewModel
import com.agrogoat.app.viewmodel.AppTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: AgroGoatViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    // VM states
    val userName by viewModel.userName.collectAsState()
    val userAddress by viewModel.userAddress.collectAsState()

    // Edit profile dialog states
    var showEditDialog by remember { mutableStateOf(false) }
    var inputName by remember { mutableStateOf(userName) }
    var inputAddress by remember { mutableStateOf(userAddress) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF7F8F7) // Clean off-white background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .padding(top = 40.dp, bottom = 100.dp), // Clear space for bottom bar
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. User Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Custom drawn Avatar silhouette matching the mockup
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF212224)),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val r = size.width / 2f
                        // Head circle
                        drawCircle(
                            color = Color.White,
                            radius = r * 0.28f,
                            center = Offset(r, r * 0.8f)
                        )
                        // Shoulders
                        drawCircle(
                            color = Color.White,
                            radius = r * 0.48f,
                            center = Offset(r, r * 1.7f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = userName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.testTag("profile_username_label")
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "+62 822-6883-0122",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Menu items Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Item 1: Informasi Akun
                ProfileMenuCard(
                    title = "Informasi Akun",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = null,
                            tint = Color(0xFF212224),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onClick = {
                        inputName = userName
                        inputAddress = userAddress
                        showEditDialog = true
                    }
                )

                // Item 2: Pesanan Saya
                ProfileMenuCard(
                    title = "Pesanan Saya",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            tint = Color(0xFF212224),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onClick = {
                        viewModel.setTab(AppTab.PESANAN)
                    }
                )

                // Item 3: Notifikasi
                ProfileMenuCard(
                    title = "Notifikasi",
                    icon = {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            tint = Color(0xFF212224),
                            modifier = Modifier.size(24.dp)
                        )
                    },
                    onClick = {
                        viewModel.setTab(AppTab.NOTIFIKASI)
                    }
                )

                // Item 4: Pengaturan (No icon, aligned text)
                ProfileMenuCard(
                    title = "Pengaturan",
                    icon = {
                        Spacer(modifier = Modifier.width(24.dp))
                    },
                    onClick = {
                        Toast.makeText(context, "Fitur Pengaturan akan segera hadir!", Toast.LENGTH_SHORT).show()
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // 3. Logout Button (Keluar)
            OutlinedButton(
                onClick = {
                    Toast.makeText(context, "Berhasil keluar dari akun.", Toast.LENGTH_SHORT).show()
                    onLogout()
                },
                border = BorderStroke(1.dp, Color(0xFFEF5350)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF5350)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Keluar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // EDIT PROFILE DIALOG
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F6E35)),
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
fun ProfileMenuCard(
    title: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(elevation = 0.5.dp, shape = RoundedCornerShape(8.dp)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            icon()
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color.Black
            )
        }
    }
}
