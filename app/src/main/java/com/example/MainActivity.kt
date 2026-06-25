package com.example

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.AppTab
import com.example.viewmodel.AgroGoatViewModel
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.ChatBubble
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.AccountCircle

import androidx.compose.material.icons.automirrored.rounded.ArrowBack

class MainActivity : ComponentActivity() {
    private val viewModel: AgroGoatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            MyApplicationTheme(darkTheme = isDarkTheme) {
                MainAppShell(viewModel)
            }
        }
    }
}

@Composable
fun MainAppShell(
    viewModel: AgroGoatViewModel,
    modifier: Modifier = Modifier
) {
    val currentTab by viewModel.currentTab.collectAsState()
    val hideBottomBarFromVM by viewModel.hideBottomBar.collectAsState()
    var isLoggedIn by remember { mutableStateOf(false) }

    if (!isLoggedIn) {
        LoginScreen(onLoginSuccess = { isLoggedIn = true })
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
                    AppTab.PROFIL -> ProfileScreen(viewModel)
                    AppTab.NOTIFIKASI -> NotificationScreen(viewModel)
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
                }
            }

            // CUSTOM FLOATING BOTTOM NAVIGATION BAR
            // Hide if on specific screens to focus on the content
            val hideNavBar = currentTab == AppTab.LACAK_PESANAN ||
                    currentTab == AppTab.NOTIFIKASI ||
                    currentTab == AppTab.PEMBAYARAN ||
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