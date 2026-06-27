package com.agrogoat.app.ui.screens
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*

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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.window.Dialog
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.agrogoat.app.ui.components.GoatImage
import com.agrogoat.app.viewmodel.AgroGoatViewModel
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfilScreen(
    viewModel: AgroGoatViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userName by viewModel.userName.collectAsState()
    val userFarmName by viewModel.userFarmName.collectAsState()
    val userAddress by viewModel.userAddress.collectAsState()
    val userBio by viewModel.userBio.collectAsState()
    val userPhone by viewModel.userPhone.collectAsState()
    val userPhotoUrl by viewModel.userPhotoUrl.collectAsState()

    val context = LocalContext.current

    // Screen states
    var isEditing by remember { mutableStateOf(false) }

    // Form inputs state
    var editNameInput by remember { mutableStateOf(userName) }
    var editFarmNameInput by remember { mutableStateOf(userFarmName) }
    var editAddressInput by remember { mutableStateOf(userAddress) }
    var editBioInput by remember { mutableStateOf(userBio) }
    var editPhoneInput by remember { mutableStateOf(userPhone) }
    var editPhotoUriStr by remember { mutableStateOf<String?>(userPhotoUrl) }
    var bitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // Dialog Modal states
    var showSuccessModal by remember { mutableStateOf(false) }
    var showFailureModal by remember { mutableStateOf(false) }
    var failureReason by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    // Dropdown list state
    var locationExpanded by remember { mutableStateOf(false) }
    val locationList = listOf("Bengkalis, Riau", "Pekanbaru, Riau", "Dumai, Riau", "Siak, Riau", "Medan, Sumut")

    // Launcher for changing photo
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        if (uri != null) {
            editPhotoUriStr = uri.toString()
            try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    bitmap = android.graphics.ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    bitmap = android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Sync form inputs when viewmodel state changes or when entering edit mode
    LaunchedEffect(isEditing, userName, userFarmName, userAddress, userBio, userPhone, userPhotoUrl) {
        if (isEditing) {
            editNameInput = userName
            editFarmNameInput = userFarmName
            editAddressInput = userAddress
            editBioInput = userBio
            editPhoneInput = userPhone
            editPhotoUriStr = userPhotoUrl
            bitmap = null
        }
    }

    if (isEditing) {
        // Edit Profil Screen (Mockup 3)
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
                        IconButton(onClick = { isEditing = false }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Kembali",
                                tint = Color.White
                            )
                        }

                        Text(
                            text = "Edit Profil",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(48.dp))
                    }
                }
            },
            containerColor = Color(0xFF4CAF50)
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular profile photo inside white ring (floating camera badge)
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .clip(CircleShape)
                        .border(4.dp, Color.White, CircleShape)
                        .background(Color(0xFF81C784))
                        .clickable { launcher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "Foto Profil",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else if (!editPhotoUriStr.isNullOrEmpty()) {
                        GoatImage(
                            imageUri = editPhotoUriStr,
                            defaultImageRes = com.agrogoat.app.R.drawable.burawa,
                            contentDescription = "Foto Profil",
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Text(
                            text = editNameInput.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase().ifEmpty { "AF" },
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 44.sp
                        )
                    }

                    // Floating camera badge
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, Color.Gray, CircleShape)
                            .align(Alignment.BottomEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PhotoCamera,
                            contentDescription = null,
                            tint = Color.DarkGray,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Text(
                    text = "Ketuk untuk ganti foto",
                    fontSize = 12.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )

                // White Card for input fields
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
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Nama Lengkap / Usaha
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Nama Lengkap / Usaha", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            OutlinedTextField(
                                value = editNameInput,
                                onValueChange = { editNameInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }

                        // Nama Peternakan
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Nama Peternakan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            OutlinedTextField(
                                value = editFarmNameInput,
                                onValueChange = { editFarmNameInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                placeholder = { Text("Masukkan nama peternakan") },
                                singleLine = true
                            )
                        }

                        // Lokasi Peternakan (Dropdown Chevron)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Lokasi Peternakan", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = editAddressInput,
                                    onValueChange = {},
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    trailingIcon = {
                                        IconButton(onClick = { locationExpanded = true }) {
                                            Icon(imageVector = Icons.Outlined.ArrowDropDown, contentDescription = null, tint = Color.DarkGray)
                                        }
                                    }
                                )
                                DropdownMenu(
                                    expanded = locationExpanded,
                                    onDismissRequest = { locationExpanded = false }
                                ) {
                                    locationList.forEach { loc ->
                                        DropdownMenuItem(
                                            text = { Text(loc) },
                                            onClick = {
                                                editAddressInput = loc
                                                locationExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        // Tentang Peternak (Bio)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Tentang Peternak (Bio)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            OutlinedTextField(
                                value = editBioInput,
                                onValueChange = { if (it.length <= 300) editBioInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                minLines = 3,
                                placeholder = { Text("Tulis deskripsi peternakan...") }
                            )
                            Text(
                                text = "${editBioInput.length}/300",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.End)
                            )
                        }

                        // No. WhatsApp Aktif
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("No. WhatsApp Aktif", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            OutlinedTextField(
                                value = editPhoneInput,
                                onValueChange = { editPhoneInput = it },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }
                    }
                }

                // Bottom Buttons row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Batal Button
                    OutlinedButton(
                        onClick = { isEditing = false },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray),
                        border = BorderStroke(1.dp, Color.LightGray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Batal", fontWeight = FontWeight.SemiBold)
                    }

                    // Simpan Perubahan Button
                    Button(
                        onClick = {
                            if (editNameInput.isBlank() || editAddressInput.isBlank()) {
                                Toast.makeText(context, "Harap isi Nama dan Lokasi!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            isSaving = true
                            
                            // Image upload block
                            val finalAction = { photoUrlStr: String? ->
                                viewModel.updateFullProfile(
                                    name = editNameInput,
                                    farmName = editFarmNameInput,
                                    address = editAddressInput,
                                    bio = editBioInput,
                                    phone = editPhoneInput,
                                    photoUrl = photoUrlStr,
                                    onSuccess = {
                                        isSaving = false
                                        showSuccessModal = true
                                    },
                                    onFailure = { reason ->
                                        isSaving = false
                                        failureReason = reason
                                        showFailureModal = true
                                    }
                                )
                            }

                            if (bitmap != null && editPhotoUriStr != null) {
                                Toast.makeText(context, "Mengunggah foto profil...", Toast.LENGTH_SHORT).show()
                                kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch(kotlinx.coroutines.SupervisorJob()) {
                                    val uploadedUrl = com.agrogoat.app.utils.CloudinaryUploader.uploadImage(
                                        context,
                                        android.net.Uri.parse(editPhotoUriStr!!)
                                    )
                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                        finalAction(uploadedUrl)
                                    }
                                }
                            } else {
                                finalAction(editPhotoUriStr)
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B5E20)),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isSaving
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("Simpan Perubahan 💾", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else {
        // Profile detail screen (Standard view)
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
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
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
                                imageVector = Icons.Outlined.OpenInNew,
                                contentDescription = "Bagikan",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
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
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile initials or picture matching Screenshot 3
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.matchParentSize()) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.05f),
                            radius = 160.dp.toPx(),
                            center = center
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(130.dp)
                            .clip(CircleShape)
                            .border(4.dp, Color.White, CircleShape)
                            .background(Color(0xFF81C784)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!userPhotoUrl.isNullOrEmpty()) {
                            GoatImage(
                                imageUri = userPhotoUrl,
                                defaultImageRes = com.agrogoat.app.R.drawable.burawa,
                                contentDescription = "Avatar",
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Text(
                                text = userName.split(" ").mapNotNull { it.firstOrNull() }.joinToString("").take(2).uppercase().ifEmpty { "AF" },
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 44.sp
                            )
                        }

                        // Green verification badge
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
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Personal info card
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
                            val peternakanLabel = if (userFarmName.isNotEmpty()) userFarmName else "Makmur Farm"
                            Text(
                                text = "🏆 Pengelola $peternakanLabel • ${userAddress.ifEmpty { "Bengkalis, Riau" }}",
                                fontSize = 13.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFF8E1))
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text("⭐ Pro Seller", color = Color(0xFFFFB300), fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }

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

                    // Stats card Grid
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Terjual", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("156", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                Text("Ekor", fontSize = 10.sp, color = Color.Gray)
                            }

                            Box(modifier = Modifier.width(1.dp).height(50.dp).background(Color(0xFFECEFF1)))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Rating", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("4.9", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFB300))
                                Row(horizontalArrangement = Arrangement.spacedBy(1.dp)) {
                                    repeat(5) {
                                        Icon(
                                            imageVector = Icons.Outlined.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                }
                            }

                            Box(modifier = Modifier.width(1.dp).height(50.dp).background(Color(0xFFECEFF1)))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Sejak", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("2023", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                                Text("Tahun", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }

                    // About peternak card
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
                                text = userBio.ifEmpty { "Kami fokus pada pembibitan kambing Etawa dan Boer berkualitas tinggi. Semua ternak dirawat dengan standar kesehatan terbaik dan terjamin kesehatannya. Melayani pengiriman se-Sumatera." },
                                fontSize = 13.sp,
                                color = Color.DarkGray,
                                lineHeight = 20.sp
                            )
                            if (userPhone.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "WhatsApp: $userPhone",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }

                    // Action buttons
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
                            onClick = { isEditing = true },
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
    }

    // SUCCESS DIALOG (Screenshot 1)
    if (showSuccessModal) {
        Dialog(onDismissRequest = { showSuccessModal = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Check mark circle
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE8F5E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Text(
                        text = "Profil Berhasil Diperbarui! ✅",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    Text(
                        text = "Informasi profil penjual Anda telah berhasil disimpan dan ditampilkan.",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    Button(
                        onClick = {
                            showSuccessModal = false
                            isEditing = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Lihat Profil Saya 👤", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }

    // FAILURE DIALOG (Screenshot 2)
    if (showFailureModal) {
        Dialog(onDismissRequest = { showFailureModal = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Close mark circle
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFEBEE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFE53935)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Close,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    Text(
                        text = "Gagal Memperbarui Profil",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )

                    val farmLabel = if (editFarmNameInput.isNotEmpty()) editFarmNameInput else "Makmur Farm"
                    Text(
                        text = "Perubahan pada profil\n\"$farmLabel\"\ntidak dapat disimpan.",
                        fontSize = 13.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )

                    // Error details box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFFEBEE))
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = failureReason,
                            color = Color(0xFFC62828),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showFailureModal = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            border = BorderStroke(1.dp, Color.LightGray),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Kembali", color = Color.Gray, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showFailureModal = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Coba Lagi", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
