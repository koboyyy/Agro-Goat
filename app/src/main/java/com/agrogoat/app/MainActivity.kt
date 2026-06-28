package com.agrogoat.app

import android.os.Bundle
import android.widget.GridView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrogoat.app.ui.screens.*
import com.agrogoat.app.ui.theme.MyApplicationTheme
import com.agrogoat.app.viewmodel.AppTab
import com.agrogoat.app.viewmodel.AgroGoatViewModel
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountCircle

import androidx.compose.material.icons.automirrored.rounded.ArrowBack

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.ui.draw.shadow

class MainActivity : ComponentActivity() {
    private val viewModel: AgroGoatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        // Initial check
        val activeNetwork = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
        val isInitialOnline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        viewModel.setOnlineStatus(isInitialOnline)

        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                viewModel.setOnlineStatus(true)
            }

            override fun onLost(network: Network) {
                viewModel.setOnlineStatus(false)
            }
        })

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val isOnline by viewModel.isOnline.collectAsState()

            MyApplicationTheme(darkTheme = isDarkTheme) {
                if (!isOnline) {
                    OfflineScreen(onRetry = {
                        val activeNet = connectivityManager.activeNetwork
                        val caps = connectivityManager.getNetworkCapabilities(activeNet)
                        val online = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
                        viewModel.setOnlineStatus(online)
                    })
                } else {
                    MainAppShell(viewModel)
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.onAppForeground()
    }

    override fun onStop() {
        super.onStop()
        viewModel.onAppBackground()
    }
}

@Composable
fun MainAppShell(
    viewModel: AgroGoatViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val hideBottomBarFromVM by viewModel.hideBottomBar.collectAsState()
    
    var isCheckingSession by remember { mutableStateOf(true) }
    var isLoggedIn by remember { mutableStateOf(false) }
    var lastTab by remember { mutableStateOf(AppTab.BERANDA) }

    LaunchedEffect(currentTab) {
        if (currentTab != AppTab.LACAK_PESANAN && 
            currentTab != AppTab.PEMBAYARAN && 
            currentTab != AppTab.NOTIFIKASI && 
            currentTab != AppTab.INFORMASI_AKUN && 
            currentTab != AppTab.PENGATURAN && 
            currentTab != AppTab.UBAH_KATA_SANDI) {
            lastTab = currentTab
        }
    }

    if (isLoggedIn && currentTab != AppTab.BERANDA) {
        androidx.activity.compose.BackHandler {
            when (currentTab) {
                AppTab.LACAK_PESANAN -> viewModel.setTab(AppTab.PESANAN)
                AppTab.PEMBAYARAN -> viewModel.setTab(AppTab.KATALOG)
                AppTab.NOTIFIKASI -> viewModel.setTab(lastTab)
                AppTab.INFORMASI_AKUN -> viewModel.setTab(AppTab.PROFIL)
                AppTab.PENGATURAN -> viewModel.setTab(AppTab.PROFIL)
                AppTab.UBAH_KATA_SANDI -> viewModel.setTab(AppTab.PENGATURAN)
                else -> viewModel.setTab(AppTab.BERANDA)
            }
        }
    }

    LaunchedEffect(Unit) {
        val isFirebaseInitialized = try {
            com.google.firebase.FirebaseApp.getInstance()
            true
        } catch (e: Exception) {
            false
        }
        if (isFirebaseInitialized) {
            try {
                val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                val currentUser = auth.currentUser
                if (currentUser != null && currentUser.email != null) {
                    val email = currentUser.email!!.trim().lowercase(java.util.Locale.ROOT)
                    val database = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    val getTask = database.collection("users_profiles").document(email).get()
                    var attempts = 0
                    while (!getTask.isComplete && attempts < 100) {
                        kotlinx.coroutines.delay(50)
                        attempts++
                    }
                    if (getTask.isSuccessful && getTask.result != null && getTask.result.exists()) {
                        val data = getTask.result.data
                        val finalName = data?.get("name") as? String ?: (currentUser.displayName ?: email.substringBefore("@"))
                        val finalAddress = data?.get("address") as? String ?: "Bengkalis, Riau"
                        val finalBalance = (data?.get("balance") as? Long) ?: 0L
                        val finalPhone = data?.get("phone") as? String ?: "081299998888"
                        
                        val lastChosenRole = data?.get("role") as? String
                        val rolesList = data?.get("roles") as? List<String>
                        val finalRoles = when {
                            rolesList != null && rolesList.isNotEmpty() -> rolesList
                            lastChosenRole != null -> {
                                if (lastChosenRole.contains(",")) lastChosenRole.split(",").map { it.trim() } else listOf(lastChosenRole)
                            }
                            else -> listOf("Pedagang")
                        }
                        val chosenRole = if (lastChosenRole != null && finalRoles.contains(lastChosenRole)) {
                            lastChosenRole
                        } else {
                            finalRoles.firstOrNull() ?: "Pedagang"
                        }
                        
                        viewModel.setUserProfile(
                            name = finalName,
                            address = finalAddress,
                            balance = finalBalance,
                            role = chosenRole,
                            email = email,
                            phone = finalPhone
                        )
                        isLoggedIn = true
                    } else {
                        auth.signOut()
                    }
                }
            } catch (e: Exception) {
                // Ignore session check errors to fall back to login screen
            }
        }
        isCheckingSession = false
    }

    if (isCheckingSession) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF1F6E35)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(color = Color.White)
                Text(
                    text = "Menghubungkan Sesi...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    } else if (!isLoggedIn) {
        LoginScreen(
            viewModel = viewModel,
            onLoginSuccess = { isLoggedIn = true }
        )
    } else {
        val userRole by viewModel.userRole.collectAsState()
        if (userRole == "Penjual") {
            AdminDashboardScreen(
                viewModel = viewModel,
                onLogout = {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    isLoggedIn = false
                    viewModel.setTab(AppTab.BERANDA)
                }
            )
        } else {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // CONTENT AREA with smooth animated switcher transitions
                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentTab) {
                        AppTab.BERANDA -> HomeScreen(viewModel)
                        AppTab.KATALOG -> CatalogScreen(viewModel)
                        AppTab.CHAT -> ChatScreen(viewModel)
                        AppTab.PESANAN -> OrdersScreen(viewModel)
                        AppTab.PROFIL -> ProfileScreen(
                            viewModel = viewModel,
                            onLogout = {
                                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                                isLoggedIn = false
                                viewModel.setTab(AppTab.BERANDA)
                            }
                        )
                        AppTab.NOTIFIKASI -> NotificationScreen(viewModel, onBack = { viewModel.setTab(lastTab) })
                        AppTab.PEMBAYARAN -> PaymentScreen(viewModel)
                        AppTab.LACAK_PESANAN -> {
                            val orderToTrack by viewModel.selectedOrderForTracking.collectAsState()
                            orderToTrack?.let {
                                TrackingScreen(
                                    order = it,
                                    onBack = { viewModel.setTab(AppTab.PESANAN) }
                                )
                            }
                        }
                        AppTab.INFORMASI_AKUN -> AccountInfoScreen(viewModel, onBack = { viewModel.setTab(AppTab.PROFIL) })
                        AppTab.PENGATURAN -> SettingsScreen(viewModel, onBack = { viewModel.setTab(AppTab.PROFIL) })
                        AppTab.UBAH_KATA_SANDI -> ChangePasswordScreen(viewModel, onBack = { viewModel.setTab(AppTab.PENGATURAN) })
                    }
                }

                // CUSTOM FLOATING BOTTOM NAVIGATION BAR
                // Hide if on specific screens to focus on the content
                val hideNavBar = currentTab == AppTab.LACAK_PESANAN ||
                        currentTab == AppTab.NOTIFIKASI ||
                        currentTab == AppTab.PEMBAYARAN ||
                        currentTab == AppTab.INFORMASI_AKUN ||
                        currentTab == AppTab.PENGATURAN ||
                        currentTab == AppTab.UBAH_KATA_SANDI ||
                        hideBottomBarFromVM

                if (!hideNavBar) {
                    val activeColor = MaterialTheme.colorScheme.primary
                    val inactiveColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.44f)

                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .padding(horizontal = 0.dp),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(76.dp)
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // TAB 1: Beranda
                            val isHomeSelected = currentTab == AppTab.BERANDA
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = if (isHomeSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                else
                                    Color.Transparent,
                                onClick = {
                                    viewModel.setTab(AppTab.BERANDA)
                                }
                            ) {

                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Home,
                                        contentDescription = "Beranda",
                                        tint = if (isHomeSelected) activeColor else inactiveColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Beranda",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isHomeSelected) activeColor else inactiveColor
                                    )
                                }
                            }

                            // TAB 2: Katalog
                            val isKatalogSelected = currentTab == AppTab.KATALOG
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = if (isKatalogSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                else
                                    Color.Transparent,
                                onClick = {
                                    viewModel.setTab(AppTab.KATALOG)
                                }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.GridView,
                                        contentDescription = "Katalog",
                                        tint = if (isKatalogSelected) activeColor else inactiveColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Katalog",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isKatalogSelected) activeColor else inactiveColor
                                    )
                                }
                            }


                            // TAB 3: Chat
                            val isChatSelected = currentTab == AppTab.CHAT
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = if (isChatSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                else
                                    Color.Transparent,
                                onClick = {
                                    viewModel.setTab(AppTab.CHAT)
                                }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ChatBubble,
                                        contentDescription = "Chat",
                                        tint = if (isChatSelected) activeColor else inactiveColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Chat",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isChatSelected) activeColor else inactiveColor
                                    )
                                }
                            }


                            // TAB 4: Pesanan
                            val isPesananSelected = currentTab == AppTab.PESANAN
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = if (isPesananSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                else
                                    Color.Transparent,
                                onClick = {
                                    viewModel.setTab(AppTab.PESANAN)
                                }
                            ) {

                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.ReceiptLong,
                                        contentDescription = "Pesanan",
                                        tint = if (isPesananSelected) activeColor else inactiveColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Pesanan",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isPesananSelected) activeColor else inactiveColor
                                    )
                                }
                            }

                            // TAB 5: Profil
                            val isProfilSelected = currentTab == AppTab.PROFIL
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 6.dp, vertical = 6.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = if (isProfilSelected)
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)
                                else
                                    Color.Transparent,
                                onClick = {
                                    viewModel.setTab(AppTab.PROFIL)
                                }
                            ) {

                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.AccountCircle,
                                        contentDescription = "Profil",
                                        tint = if (isProfilSelected) activeColor else inactiveColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Profil",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isProfilSelected) activeColor else inactiveColor
                                    )
                                }
                            }


                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OfflineScreen(
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F4)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .shadow(12.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFEBEE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⚠️", fontSize = 32.sp)
                }

                Text(
                    text = "Koneksi Terputus",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Aplikasi ini memerlukan koneksi internet untuk dapat diakses. Harap periksa jaringan internet Anda dan coba lagi.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F6E35)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "Coba Lagi",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}