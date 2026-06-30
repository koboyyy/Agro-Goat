package com.agrogoat.core.designsystem.components.detail

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrogoat.core.designsystem.R
import com.agrogoat.core.model.GoatCategory
import com.agrogoat.core.model.GoatItem
import com.agrogoat.core.designsystem.components.GoatImage
import com.agrogoat.core.designsystem.components.formatRupiah

@Composable
fun GoatDetailView(
    goat: GoatItem,
    onBack: () -> Unit,
    onToggleFav: () -> Unit,
    onChat: () -> Unit,
    onOrder: () -> Unit,
    sellerName: String = "",
    sellerPhotoUrl: String? = null,
    sellerLocationStr: String? = null,
    sellerMapsUrl: String? = null,
    sellerLat: Double? = null,
    sellerLng: Double? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val imageRes = when (goat.category) {
        GoatCategory.POTONG -> com.agrogoat.core.designsystem.R.drawable.burawa
        GoatCategory.ETAWA -> com.agrogoat.core.designsystem.R.drawable.etawa
        GoatCategory.PERAH -> com.agrogoat.core.designsystem.R.drawable.kacang
    }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { isVisible = true }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // ─── Hero Image ───
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
            ) {
                GoatImage(
                    imageUri = goat.imageUri,
                    defaultImageRes = imageRes,
                    contentDescription = goat.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Gradient overlay bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.55f))
                            )
                        )
                )

                // Back button
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.95f))
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Kembali",
                        tint = Color(0xFF1B5E20),
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Favorite button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.95f))
                        .clickable(onClick = onToggleFav),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (goat.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorit",
                        tint = if (goat.isFavorite) Color(0xFFE53935) else Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }

                // Status badges on image bottom
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Available badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF2E7D32)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF69F0AE))
                            )
                            Text(
                                "Tersedia",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Gender badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (goat.gender.equals("Jantan", ignoreCase = true))
                            Color(0xFF1565C0).copy(alpha = 0.9f)
                        else Color(0xFFAD1457).copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = if (goat.gender.equals("Jantan", ignoreCase = true)) "♂ ${goat.gender}" else "♀ ${goat.gender}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    // New badge
                    if (goat.isNew) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color(0xFFF57C00)
                        ) {
                            Text(
                                "✨ Baru",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // ─── Main Content Card ───
            AnimatedVisibility(
                visible = isVisible,
                enter = slideInVertically(
                    initialOffsetY = { it / 3 },
                    animationSpec = tween(400)
                ) + fadeIn(animationSpec = tween(400))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 16.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {

                    // ── Name & Price ──
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = goat.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1A1A1A),
                                lineHeight = 30.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    Icons.Filled.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    goat.location,
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        "Harga",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        formatRupiah(goat.price),
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                // Share button
                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT,
                                                "Lihat kambing ${goat.name} seharga ${formatRupiah(goat.price)} di AgroGoat! 🐐")
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Bagikan via"))
                                    },
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F5E9))
                                        .size(42.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Share,
                                        contentDescription = "Bagikan",
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ── Info Grid ──
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Informasi Kambing",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1A1A1A)
                            )
                            Spacer(Modifier.height(16.dp))

                            val categoryLabel = when (goat.category) {
                                GoatCategory.POTONG -> "Kambing Potong"
                                GoatCategory.ETAWA -> "Kambing Etawa"
                                GoatCategory.PERAH -> "Kambing Perah"
                            }

                            val infos = listOf(
                                Triple(Icons.Outlined.Category, "Jenis", categoryLabel),
                                Triple(Icons.Outlined.Scale, "Bobot", "${goat.weight} kg"),
                                Triple(Icons.Outlined.Cake, "Umur", if (goat.age < 1) "${(goat.age * 12).toInt()} bulan" else "${goat.age} tahun"),
                                Triple(Icons.Outlined.Person, "Jenis Kelamin", goat.gender),
                            )

                            infos.chunked(2).forEach { rowItems ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    rowItems.forEach { (icon, label, value) ->
                                        InfoChip(
                                            icon = icon,
                                            label = label,
                                            value = value,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    if (rowItems.size == 1) {
                                        Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }

                    // ── Seller Card ──
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Seller avatar
                            if (!sellerPhotoUrl.isNullOrEmpty()) {
                                GoatImage(
                                    imageUri = sellerPhotoUrl,
                                    defaultImageRes = com.agrogoat.core.designsystem.R.drawable.etawa,
                                    contentDescription = "Foto Penjual",
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F5E9)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = (sellerName.ifEmpty { goat.sellerEmail ?: "P" })
                                            .take(1).uppercase(),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Penjual",
                                    fontSize = 11.sp,
                                    color = Color.Gray,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = sellerName.ifEmpty {
                                        goat.sellerEmail?.substringBefore("@")?.replaceFirstChar { it.uppercase() }
                                            ?: "Penjual AgroGoat"
                                    },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color(0xFF1A1A1A),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    repeat(5) {
                                        Icon(
                                            Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFFFB300),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    Text(
                                        "4.9",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        modifier = Modifier.padding(start = 2.dp)
                                    )
                                }
                                if (goat.location.isNotEmpty()) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            Icons.Filled.LocationOn,
                                            contentDescription = null,
                                            tint = Color.Gray,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            goat.location,
                                            fontSize = 11.sp,
                                            color = Color.Gray,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            // Chat button
                            OutlinedButton(
                                onClick = onChat,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2E7D32)),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    Icons.Filled.Chat,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Chat", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                            }
                        }
                    }

                    // ── Lokasi Peternakan Card ──
                    val displayLocation = sellerLocationStr?.takeIf { it.isNotBlank() } ?: goat.location
                    if (displayLocation.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        "Lokasi Peternakan",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "📍 $displayLocation",
                                    fontSize = 13.sp,
                                    color = Color.DarkGray
                                )
                                Spacer(Modifier.height(12.dp))

                                // ── Embedded OpenStreetMap WebView ──
                                FarmMiniMapView(
                                    locationQuery = displayLocation,
                                    lat = sellerLat,
                                    lng = sellerLng,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                )

                                Spacer(Modifier.height(12.dp))
                                // Open in Google Maps button
                                OutlinedButton(
                                    onClick = {
                                        val mapsIntent = if (!sellerMapsUrl.isNullOrBlank()) {
                                            Intent(Intent.ACTION_VIEW, Uri.parse(sellerMapsUrl))
                                        } else {
                                            val query = Uri.encode(displayLocation)
                                            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/$query"))
                                        }
                                        try {
                                            context.startActivity(mapsIntent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Tidak bisa membuka Maps", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, Color(0xFF4285F4)),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF4285F4))
                                ) {
                                    Icon(
                                        Icons.Filled.Map,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(
                                        "Buka di Google Maps",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // ── Description ──
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                "Deskripsi",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF1A1A1A)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = goat.description.ifEmpty {
                                    "Kambing ini dalam kondisi sehat dan terawat. " +
                                    "Dibesarkan dengan pakan berkualitas dan telah mendapatkan vaksinasi rutin. " +
                                    "Siap untuk dipotong atau dikembangbiakkan."
                                },
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = Color(0xFF555555)
                            )
                        }
                    }

                    // ── Health & Cert Info ──
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                "🛡️ Garansi & Kesehatan",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1B5E20)
                            )
                            HealthBadgeRow()
                        }
                    }
                }
            }
        }

        // ─── Sticky Bottom Bar ───
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color.White,
                shadowElevation = 16.dp,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Harga",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                        Text(
                            formatRupiah(goat.price),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Color(0xFF2E7D32)
                        )
                    }
                    Button(
                        onClick = onOrder,
                        modifier = Modifier
                            .weight(1.2f)
                            .height(50.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            Icons.Filled.ShoppingCart,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "Pesan Sekarang",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F7F5))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF2E7D32),
                modifier = Modifier.size(18.dp)
            )
        }
        Column {
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1A1A1A),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HealthBadgeRow() {
    val badges = listOf(
        "✅ Sehat & Terawat",
        "💉 Sudah Vaksin",
        "🌿 Pakan Organik",
        "📋 Bersertifikat"
    )
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        badges.chunked(2).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { badge ->
                    Text(
                        text = badge,
                        fontSize = 12.sp,
                        color = Color(0xFF1B5E20),
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }
        }
    }
}

@android.annotation.SuppressLint("SetJavaScriptEnabled")
@Composable
fun FarmMiniMapView(
    locationQuery: String,
    lat: Double? = null,
    lng: Double? = null,
    modifier: Modifier = Modifier
) {
    val encodedLocation = java.net.URLEncoder.encode(locationQuery, "UTF-8")

    val mapHtml = """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" crossorigin=""/>
<style>
  * { margin:0; padding:0; box-sizing:border-box; }
  body { width:100%; height:100vh; overflow:hidden; }
  #map { width:100%; height:100vh; }
  .leaflet-control-zoom { display:none; }
  .leaflet-control-attribution { display:none !important; }
</style>
</head>
<body>
<div id="map"></div>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" crossorigin=""></script>
<script>
  var map = L.map('map', { zoomControl: false, dragging: false, scrollWheelZoom: false, doubleClickZoom: false }).setView([0.5, 104.0], 10);
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', { maxZoom: 19 }).addTo(map);

  var markerIcon = L.icon({
    iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
    iconSize: [25, 41], iconAnchor: [12, 41], popupAnchor: [1, -34],
    shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    shadowSize: [41, 41]
  });

  ${if (lat != null && lng != null) """
    map.setView([$lat, $lng], 14);
    L.marker([$lat, $lng], { icon: markerIcon })
      .addTo(map)
      .bindPopup('$locationQuery')
      .openPopup();
  """ else """
  fetch('https://nominatim.openstreetmap.org/search?format=json&q=$encodedLocation&limit=1&accept-language=id')
    .then(r => r.json())
    .then(data => {
      if (data && data.length > 0) {
        var fetchLat = parseFloat(data[0].lat);
        var fetchLng = parseFloat(data[0].lon);
        map.setView([fetchLat, fetchLng], 13);
        L.marker([fetchLat, fetchLng], { icon: markerIcon })
          .addTo(map)
          .bindPopup('$locationQuery')
          .openPopup();
      } else {
        document.getElementById('map').innerHTML =
          '<div style="display:flex;align-items:center;justify-content:center;height:100%;background:#f0f4f0;flex-direction:column;gap:8px;">' +
          '<span style="font-size:32px;">📍</span>' +
          '<span style="color:#2E7D32;font-size:14px;font-weight:bold;">$locationQuery</span>' +
          '<span style="color:#888;font-size:11px;">Lokasi tidak ditemukan di peta</span>' +
          '</div>';
      }
    })
    .catch(() => {
      document.getElementById('map').innerHTML =
        '<div style="display:flex;align-items:center;justify-content:center;height:100%;background:#f0f4f0;flex-direction:column;gap:8px;">' +
        '<span style="font-size:32px;">🌐</span>' +
        '<span style="color:#888;font-size:12px;">Butuh koneksi internet untuk memuat peta</span>' +
        '</div>';
    });
  """}
</script>
</body>
</html>
""".trimIndent()

    var isLoading by remember { mutableStateOf(true) }

    Box(modifier = modifier) {
        androidx.compose.ui.viewinterop.AndroidView(
            factory = { ctx ->
                android.webkit.WebView(ctx).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    settings.builtInZoomControls = false
                    settings.displayZoomControls = false
                    webViewClient = object : android.webkit.WebViewClient() {
                        override fun onPageFinished(view: android.webkit.WebView?, url: String?) {
                            isLoading = false
                        }
                    }
                    loadDataWithBaseURL(
                        "https://unpkg.com",
                        mapHtml,
                        "text/html",
                        "UTF-8",
                        null
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF0F4F0)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF2E7D32),
                        modifier = Modifier.size(28.dp),
                        strokeWidth = 3.dp
                    )
                    Text(
                        "Memuat peta...",
                        fontSize = 12.sp,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}

