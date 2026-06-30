package com.agrogoat.app.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.launch

data class PickedLocation(
    val address: String,
    val lat: Double,
    val lng: Double,
    val mapsUrl: String
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LocationPickerDialog(
    initialAddress: String = "",
    initialLat: Double? = null,
    initialLng: Double? = null,
    onLocationPicked: (PickedLocation) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var pickedAddress by remember { mutableStateOf(initialAddress) }
    var pickedLat by remember { mutableStateOf(initialLat ?: -1.5174) }
    var pickedLng by remember { mutableStateOf(initialLng ?: 102.1142) }
    var isLoading by remember { mutableStateOf(true) }
    var isLocating by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val hasLocationPermission = remember {
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            coroutineScope.launch {
                isLocating = true
                getDeviceLocation(context) { lat, lng, address ->
                    pickedLat = lat
                    pickedLng = lng
                    pickedAddress = address
                    isLocating = false
                    webViewRef?.evaluateJavascript(
                        "if (typeof moveMarker === 'function') moveMarker($lat, $lng, '$address');",
                        null
                    )
                }
            }
        }
    }

    fun requestDeviceLocation() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            isLocating = true
            getDeviceLocation(context) { lat, lng, address ->
                pickedLat = lat
                pickedLng = lng
                pickedAddress = address
                isLocating = false
                webViewRef?.evaluateJavascript(
                    "if (typeof moveMarker === 'function') moveMarker($lat, $lng, '$address');",
                    null
                )
            }
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Generate OpenStreetMap HTML with interactive marker
    val mapHtml = remember(pickedLat, pickedLng) {
        buildMapHtml(pickedLat, pickedLng, initialAddress)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2E7D32))
                            .padding(horizontal = 8.dp, vertical = 14.dp),
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "Pilih Lokasi Peternakan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Instruction banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "📍 Ketuk peta untuk menentukan lokasi peternakan Anda",
                            fontSize = 12.sp,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Map WebView
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    settings.builtInZoomControls = false
                                    settings.displayZoomControls = false
                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            isLoading = false
                                        }
                                    }
                                    // JS Interface to receive location from map
                                    addJavascriptInterface(object : Any() {
                                        @JavascriptInterface
                                        fun onLocationSelected(lat: Double, lng: Double, address: String) {
                                            pickedLat = lat
                                            pickedLng = lng
                                            pickedAddress = address
                                        }
                                    }, "AndroidBridge")
                                    loadDataWithBaseURL(
                                        "https://unpkg.com",
                                        mapHtml,
                                        "text/html",
                                        "UTF-8",
                                        null
                                    )
                                    webViewRef = this
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Loading overlay
                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF2E7D32))
                                    Text(
                                        text = "Memuat Peta...",
                                        fontSize = 13.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }
                        }

                        // My Location FAB
                        FloatingActionButton(
                            onClick = { requestDeviceLocation() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .size(48.dp),
                            containerColor = Color.White,
                            contentColor = Color(0xFF2E7D32),
                            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                        ) {
                            if (isLocating) {
                                CircularProgressIndicator(
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.size(22.dp),
                                    strokeWidth = 2.5.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Filled.MyLocation,
                                    contentDescription = "Lokasi Saya",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }

                    // Bottom Address Panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Picked location display
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
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
                                    Text("📍", fontSize = 16.sp)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Lokasi Dipilih",
                                        fontSize = 11.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = if (pickedAddress.isNotEmpty()) pickedAddress else "Ketuk peta untuk memilih lokasi",
                                        fontSize = 13.sp,
                                        color = if (pickedAddress.isNotEmpty()) Color.Black else Color.Gray,
                                        fontWeight = if (pickedAddress.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal,
                                        maxLines = 2
                                    )
                                    if (pickedLat != -1.5174 || pickedAddress.isNotEmpty()) {
                                        Text(
                                            text = "%.5f, %.5f".format(pickedLat, pickedLng),
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        // Confirm Button
                        Button(
                            onClick = {
                                val mapsUrl = "https://www.google.com/maps?q=$pickedLat,$pickedLng"
                                val finalAddress = if (pickedAddress.isNotEmpty()) pickedAddress
                                else "%.4f, %.4f".format(pickedLat, pickedLng)
                                onLocationPicked(
                                    PickedLocation(
                                        address = finalAddress,
                                        lat = pickedLat,
                                        lng = pickedLng,
                                        mapsUrl = mapsUrl
                                    )
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp),
                            enabled = pickedAddress.isNotEmpty() || (pickedLat != -1.5174 && pickedLng != 102.1142)
                        ) {
                            Text(
                                text = "✅ Konfirmasi Lokasi Ini",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getDeviceLocation(
    context: android.content.Context,
    onResult: (lat: Double, lng: Double, address: String) -> Unit
) {
    try {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        val cancellationToken = CancellationTokenSource()
        fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellationToken.token)
            .addOnSuccessListener { location ->
                if (location != null) {
                    val lat = location.latitude
                    val lng = location.longitude
                    // Reverse geocode
                    try {
                        val geocoder = android.location.Geocoder(context, java.util.Locale("id", "ID"))
                        val addresses = geocoder.getFromLocation(lat, lng, 1)
                        val address = if (!addresses.isNullOrEmpty()) {
                            val addr = addresses[0]
                            listOfNotNull(
                                addr.subLocality ?: addr.locality,
                                addr.subAdminArea ?: addr.adminArea,
                                addr.countryName
                            ).take(3).joinToString(", ")
                        } else {
                            "%.4f, %.4f".format(lat, lng)
                        }
                        onResult(lat, lng, address)
                    } catch (e: Exception) {
                        onResult(lat, lng, "%.4f, %.4f".format(lat, lng))
                    }
                }
            }
            .addOnFailureListener {
                // Fallback silently
            }
    } catch (e: SecurityException) {
        // Permission not granted
    }
}

fun buildMapHtml(initLat: Double, initLng: Double, initAddress: String): String {
    return """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" crossorigin=""/>
<style>
  * { margin: 0; padding: 0; box-sizing: border-box; }
  body { width: 100%; height: 100vh; overflow: hidden; }
  #map { width: 100%; height: 100vh; }
  .leaflet-control-attribution { display: none !important; }
  .search-box {
    position: absolute;
    top: 10px;
    left: 10px;
    right: 60px;
    z-index: 1000;
    display: flex;
    gap: 6px;
  }
  .search-box input {
    flex: 1;
    padding: 10px 14px;
    border-radius: 10px;
    border: 1px solid #ccc;
    font-size: 14px;
    outline: none;
    box-shadow: 0 2px 8px rgba(0,0,0,0.15);
  }
  .search-box button {
    padding: 10px 14px;
    background: #2E7D32;
    color: white;
    border: none;
    border-radius: 10px;
    font-size: 13px;
    font-weight: bold;
    cursor: pointer;
    box-shadow: 0 2px 8px rgba(0,0,0,0.2);
    white-space: nowrap;
  }
</style>
</head>
<body>
<div class="search-box">
  <input type="text" id="searchInput" placeholder="Cari lokasi..." />
  <button onclick="searchLocation()">Cari</button>
</div>
<div id="map"></div>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js" crossorigin=""></script>
<script>
  var map = L.map('map', { zoomControl: true }).setView([$initLat, $initLng], 13);
  
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    maxZoom: 19,
    attribution: '© OpenStreetMap'
  }).addTo(map);

  var markerIcon = L.icon({
    iconUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-icon.png',
    iconSize: [25, 41],
    iconAnchor: [12, 41],
    popupAnchor: [1, -34],
    shadowUrl: 'https://unpkg.com/leaflet@1.9.4/dist/images/marker-shadow.png',
    shadowSize: [41, 41]
  });

  var marker = L.marker([$initLat, $initLng], { icon: markerIcon, draggable: true }).addTo(map);
  ${if (initAddress.isNotEmpty()) "marker.bindPopup('$initAddress').openPopup();" else ""}

  function reverseGeocode(lat, lng) {
    fetch('https://nominatim.openstreetmap.org/reverse?format=json&lat=' + lat + '&lon=' + lng + '&accept-language=id')
      .then(r => r.json())
      .then(data => {
        var addr = data.display_name || (lat.toFixed(4) + ', ' + lng.toFixed(4));
        var short = '';
        if (data.address) {
          var a = data.address;
          var parts = [a.village || a.suburb || a.town || a.city_district, a.county || a.city, a.state, a.country].filter(Boolean);
          short = parts.slice(0, 3).join(', ');
        }
        var finalAddr = short || addr;
        marker.setLatLng([lat, lng]);
        marker.bindPopup(finalAddr).openPopup();
        AndroidBridge.onLocationSelected(lat, lng, finalAddr);
      })
      .catch(() => {
        var fallback = lat.toFixed(4) + ', ' + lng.toFixed(4);
        marker.setLatLng([lat, lng]);
        marker.bindPopup(fallback).openPopup();
        AndroidBridge.onLocationSelected(lat, lng, fallback);
      });
  }

  map.on('click', function(e) {
    reverseGeocode(e.latlng.lat, e.latlng.lng);
  });

  marker.on('dragend', function(e) {
    var pos = marker.getLatLng();
    reverseGeocode(pos.lat, pos.lng);
  });

  function moveMarker(lat, lng, address) {
    map.setView([lat, lng], 15);
    marker.setLatLng([lat, lng]);
    if (address) marker.bindPopup(address).openPopup();
    AndroidBridge.onLocationSelected(lat, lng, address || (lat.toFixed(4) + ', ' + lng.toFixed(4)));
  }

  function searchLocation() {
    var query = document.getElementById('searchInput').value.trim();
    if (!query) return;
    fetch('https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent(query) + '&limit=1&accept-language=id')
      .then(r => r.json())
      .then(data => {
        if (data && data.length > 0) {
          var lat = parseFloat(data[0].lat);
          var lng = parseFloat(data[0].lon);
          reverseGeocode(lat, lng);
          map.setView([lat, lng], 15);
        }
      });
  }

  document.getElementById('searchInput').addEventListener('keypress', function(e) {
    if (e.key === 'Enter') searchLocation();
  });
</script>
</body>
</html>
""".trimIndent()
}
