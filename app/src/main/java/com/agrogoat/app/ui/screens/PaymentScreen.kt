package com.agrogoat.app.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrogoat.app.ui.components.formatRupiah
import com.agrogoat.app.viewmodel.AgroGoatViewModel
import com.agrogoat.app.viewmodel.AppTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    viewModel: AgroGoatViewModel,
    modifier: Modifier = Modifier
) {
    val order by viewModel.selectedOrderForPayment.collectAsState()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    
    if (order == null) return

    val accountNumber = "0123-4567-8901-532"
    val accountName = "Admin Agro Goat (PT. AGRO JAYA)"
    val bankName = "BANK BRI"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Instruksi Pembayaran", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = { viewModel.setTab(AppTab.PESANAN) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Total Tagihan", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                    Text(
                        formatRupiah(order!!.totalPrice + 2500),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 28.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("ID Pesanan: ${order!!.id.take(12)}...", fontSize = 12.sp, color = Color.Gray)
                }
            }

            // Bank Account Info
            Text("Transfer ke Rekening Berikut", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🏦", fontSize = 22.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(bankName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }

                    Column {
                        Text("Nomor Rekening", fontSize = 12.sp, color = Color.Gray)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(accountNumber, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, letterSpacing = 1.sp)
                            TextButton(onClick = {
                                clipboardManager.setText(AnnotatedString(accountNumber))
                                Toast.makeText(context, "Nomor rekening berhasil disalin!", Toast.LENGTH_SHORT).show()
                            }) {
                                Text("SALIN", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    Column {
                        Text("Nama Penerima", fontSize = 12.sp, color = Color.Gray)
                        Text(accountName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                }
            }

            // Warning/Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF9C4).copy(alpha = 0.4f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF5D4037), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Mohon lampirkan bukti transfer (screenshot) pada tombol di bawah agar admin dapat memproses pesanan Anda lebih cepat.",
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        color = Color(0xFF5D4037)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    // Navigate to Chat and simulated sending proof
                    viewModel.sendMessage("[BUKTI_TRANSFER] Halo Admin, saya sudah transfer sebesar ${formatRupiah(order!!.totalPrice + 2500)} untuk pesanan ${order!!.id.take(8)}")
                    viewModel.processOrderPayment(order!!.id)
                    viewModel.setTab(AppTab.CHAT)
                    Toast.makeText(context, "Bukti pembayaran terkirim! Admin sedang memverifikasi.", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Kirim Bukti Screenshot Pembayaran", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
