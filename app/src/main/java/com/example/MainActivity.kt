package com.example

import android.os.Bundle
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
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setTab(AppTab.BERANDA) }
                            .testTag("nav_btn_beranda"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Beranda",
                            tint = if (isHomeSelected) activeColor else inactiveColor,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Beranda",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isHomeSelected) activeColor else inactiveColor
                        )
                    }

                    // TAB 2: Katalog
                    val isKatalogSelected = currentTab == AppTab.KATALOG
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setTab(AppTab.KATALOG) }
                            .testTag("nav_btn_katalog"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CustomNavGridIcon(
                            tint = if (isKatalogSelected) activeColor else inactiveColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Katalog",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isKatalogSelected) activeColor else inactiveColor
                        )
                    }

                    // TAB 3: Chat
                    val isChatSelected = currentTab == AppTab.CHAT
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setTab(AppTab.CHAT) }
                            .testTag("nav_btn_chat"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            CustomNavChatIcon(
                                tint = if (isChatSelected) activeColor else inactiveColor,
                                modifier = Modifier.size(22.dp)
                            )
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = 8.dp, y = (-6).dp)
                                    .size(15.dp)
                                    .background(Color(0xFFE53935), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "5",
                                    color = Color.White,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Chat",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isChatSelected) activeColor else inactiveColor
                        )
                    }

                    // TAB 4: Pesanan
                    val isPesananSelected = currentTab == AppTab.PESANAN
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setTab(AppTab.PESANAN) }
                            .testTag("nav_btn_pesanan"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CustomNavDocIcon(
                            tint = if (isPesananSelected) activeColor else inactiveColor,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Pesanan",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isPesananSelected) activeColor else inactiveColor
                        )
                    }

                    // TAB 5: Profil
                    val isProfilSelected = currentTab == AppTab.PROFIL
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setTab(AppTab.PROFIL) }
                            .testTag("nav_btn_profil"),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
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

/**
 * Custom hand-drawn 2x2 catalog grid icon matching bottom navigation designs
 */
@Composable
fun CustomNavGridIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val sizeVal = size.width * 0.42f
        val gap = size.width * 0.16f

        // Top left box
        drawRoundRect(
            color = tint,
            topLeft = Offset(0f, 0f),
            size = Size(sizeVal, sizeVal),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        // Top right box
        drawRoundRect(
            color = tint,
            topLeft = Offset(sizeVal + gap, 0f),
            size = Size(sizeVal, sizeVal),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        // Bottom left box
        drawRoundRect(
            color = tint,
            topLeft = Offset(0f, sizeVal + gap),
            size = Size(sizeVal, sizeVal),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        // Bottom right box
        drawRoundRect(
            color = tint,
            topLeft = Offset(sizeVal + gap, sizeVal + gap),
            size = Size(sizeVal, sizeVal),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
    }
}

/**
 * Custom hand-drawn bubble dialogue chat icon matching bottom navigation designs
 */
@Composable
fun CustomNavChatIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val rectW = w * 0.9f
        val rectH = h * 0.72f

        // Outer border bubble
        drawRoundRect(
            color = tint,
            topLeft = Offset(w * 0.05f, 0f),
            size = Size(rectW, rectH),
            cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
        )

        // Triangle speech tail
        val path = Path().apply {
            moveTo(w * 0.35f, rectH)
            lineTo(w * 0.3f, h * 0.95f)
            lineTo(w * 0.55f, rectH)
            close()
        }
        drawPath(path, color = tint)

        // Three interior dots decoration (white/transparent)
        val dotR = w * 0.045f
        val dotY = rectH * 0.5f
        drawCircle(color = Color.White.copy(alpha = 0.5f), radius = dotR, center = Offset(w * 0.3f, dotY))
        drawCircle(color = Color.White.copy(alpha = 0.5f), radius = dotR, center = Offset(w * 0.5f, dotY))
        drawCircle(color = Color.White.copy(alpha = 0.5f), radius = dotR, center = Offset(w * 0.7f, dotY))
    }
}

/**
 * Custom hand-drawn paper document transaction icon matching bottom navigation designs
 */
@Composable
fun CustomNavDocIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        val strokeWidth = w * 0.08f

        // Draw folder paper outline with custom geometric strokes
        val path = Path().apply {
            moveTo(w * 0.22f, h * 0.08f)
            lineTo(w * 0.62f, h * 0.08f)
            lineTo(w * 0.82f, h * 0.28f)
            lineTo(w * 0.82f, h * 0.92f)
            lineTo(w * 0.22f, h * 0.92f)
            close()
        }
        drawPath(path, color = tint, style = Stroke(width = strokeWidth))

        // Interstitial folder lines
        drawLine(
            color = tint,
            start = Offset(w * 0.38f, h * 0.42f),
            end = Offset(w * 0.66f, h * 0.42f),
            strokeWidth = strokeWidth
        )
        drawLine(
            color = tint,
            start = Offset(w * 0.38f, h * 0.62f),
            end = Offset(w * 0.66f, h * 0.62f),
            strokeWidth = strokeWidth
        )
    }
}
