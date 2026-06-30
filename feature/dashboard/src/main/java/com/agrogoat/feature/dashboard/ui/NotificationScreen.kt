package com.agrogoat.feature.dashboard.ui

import android.widget.Toast
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.ListAlt
import androidx.compose.material.icons.outlined.Notifications
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrogoat.core.model.NotificationItem
import com.agrogoat.core.model.NotificationType
import com.agrogoat.core.shared.AgroGoatViewModel
import com.agrogoat.core.shared.AppTab

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: AgroGoatViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = { viewModel.setTab(AppTab.BERANDA) }
) {
    val context = LocalContext.current
    val dbNotifications by viewModel.notifications.collectAsState()

    // 0 = Semua, 1 = Belum Dibaca, 2 = Promo
    var selectedTabState by remember { mutableStateOf(0) }

    // Filter list according to tab selected
    val filteredNotifications = remember(dbNotifications, selectedTabState) {
        when (selectedTabState) {
            1 -> dbNotifications.filter { !it.isRead }
            2 -> dbNotifications.filter { it.type == NotificationType.PROMO }
            else -> dbNotifications
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Notifikasi",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    TextButton(onClick = { viewModel.markAllNotificationsAsRead() }) {
                        Text(
                            text = "Baca Semua",
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color(0xFFF7F8F7)
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // FILTER TABS BAR (Semua, Belum Dibaca, Promo)
            TabRow(
                selectedTabIndex = selectedTabState,
                containerColor = Color.White,
                contentColor = Color(0xFF2E7D32),
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabState]),
                        color = Color(0xFF2E7D32),
                        height = 3.dp
                    )
                },
                divider = {
                    HorizontalDivider(color = Color(0xFFE2E8F0))
                }
            ) {
                Tab(
                    selected = selectedTabState == 0,
                    onClick = { selectedTabState = 0 },
                    text = {
                        Text(
                            text = "Semua",
                            fontWeight = if (selectedTabState == 0) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (selectedTabState == 0) Color(0xFF2E7D32) else Color.Gray
                        )
                    }
                )
                Tab(
                    selected = selectedTabState == 1,
                    onClick = { selectedTabState = 1 },
                    text = {
                        Text(
                            text = "Belum Dibaca",
                            fontWeight = if (selectedTabState == 1) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (selectedTabState == 1) Color(0xFF2E7D32) else Color.Gray
                        )
                    }
                )
                Tab(
                    selected = selectedTabState == 2,
                    onClick = { selectedTabState = 2 },
                    text = {
                        Text(
                            text = "Promo",
                            fontWeight = if (selectedTabState == 2) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (selectedTabState == 2) Color(0xFF2E7D32) else Color.Gray
                        )
                    }
                )
            }

            // LIST CONTENT
            if (filteredNotifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Notifications,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum ada notifikasi",
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredNotifications) { notification ->
                        NotificationCardItem(
                            notification = notification,
                            onClick = {
                                viewModel.markNotificationAsRead(notification.id)
                                if (notification.type == NotificationType.ORDER_STATUS) {
                                    viewModel.setTab(AppTab.PESANAN)
                                } else {
                                    viewModel.setTab(AppTab.BERANDA)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationCardItem(
    notification: NotificationItem,
    onClick: () -> Unit
) {
    val icon = when (notification.type) {
        NotificationType.ORDER_STATUS -> Icons.Outlined.ListAlt
        NotificationType.PROMO -> Icons.Outlined.CardGiftcard
        NotificationType.SYSTEM -> Icons.Outlined.Info
    }

    val iconBgColor = when (notification.type) {
        NotificationType.ORDER_STATUS -> Color(0xFFE8F5E9) // mint green
        NotificationType.PROMO -> Color(0xFFFFF3E0) // light orange
        NotificationType.SYSTEM -> Color(0xFFE3F2FD) // light blue
    }

    val iconTint = when (notification.type) {
        NotificationType.ORDER_STATUS -> Color(0xFF2E7D32)
        NotificationType.PROMO -> Color(0xFFEF6C00)
        NotificationType.SYSTEM -> Color(0xFF1976D2)
    }

    // Unread cards get a soft mint green background as shown in the mockup
    val containerBg = if (!notification.isRead) {
        Color(0xFFE8F5E9).copy(alpha = 0.44f)
    } else {
        Color.White
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .shadow(elevation = 0.5.dp, shape = RoundedCornerShape(16.dp)),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        colors = CardDefaults.cardColors(containerColor = containerBg),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Circle Icon Container
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Text Content Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notification.isRead) {
                        // Small unread status dot (green)
                        Box(
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2E7D32))
                        )
                    }
                }
                
                Text(
                    text = notification.message,
                    fontSize = 13.sp,
                    color = Color.DarkGray,
                    lineHeight = 18.sp,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = notification.timestamp,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
