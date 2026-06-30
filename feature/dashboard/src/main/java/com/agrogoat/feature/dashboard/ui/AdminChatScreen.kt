package com.agrogoat.feature.dashboard.ui
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrogoat.core.designsystem.R
import com.agrogoat.core.model.ChatRoom
import com.agrogoat.core.model.MessageItem
import com.agrogoat.core.model.MessageSender
import com.agrogoat.core.designsystem.components.GoatImage
import com.agrogoat.core.shared.AgroGoatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChatDashboardScreen(
    viewModel: AgroGoatViewModel,
    onChatClick: (ChatRoom) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val liveChatRooms by viewModel.chatInboxRooms.collectAsState()

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
                        .padding(horizontal = 12.dp, vertical = 18.dp),
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
                        text = "Dasboar pesan",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 8.dp)
                    )
                }
            }
        },
        containerColor = Color(0xFF4CAF50) // Matching screenshot green
    ) { innerPadding ->
        var roomToDelete by remember { mutableStateOf<ChatRoom?>(null) }
        
        if (roomToDelete != null) {
            AlertDialog(
                onDismissRequest = { roomToDelete = null },
                title = { Text(text = "Hapus Obrolan") },
                text = { Text("Apakah Anda yakin ingin menghapus obrolan ini?") },
                confirmButton = {
                    TextButton(onClick = {
                        roomToDelete?.let { viewModel.deleteChatRoom(it.id) }
                        roomToDelete = null
                    }) {
                        Text("Hapus", color = Color.Red, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { roomToDelete = null }) {
                        Text("Batal", color = Color.DarkGray)
                    }
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Pesan Terbaru",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.White
            )

            if (liveChatRooms.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("💬", fontSize = 48.sp, color = Color.White)
                        Text(
                            text = "Belum ada pesan masuk dari pembeli.",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(liveChatRooms) { room ->
                        val colorInit = when (room.initials) {
                            "BS" -> Color(0xFFFFF3E0)
                            "SA" -> Color(0xFFE8F5E9)
                            else -> Color(0xFFE3F2FD)
                        }
                        val tintInit = when (room.initials) {
                            "BS" -> Color(0xFFFF9800)
                            "SA" -> Color(0xFF2E7D32)
                            else -> Color(0xFF1E88E5)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.selectChatRoom(room)
                                    onChatClick(room)
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(colorInit),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = room.initials,
                                        color = tintInit,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = room.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = Color.Black
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = room.lastMessage,
                                        fontSize = 13.sp,
                                        color = Color.Gray,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFFFEBEE))
                                        .clickable { roomToDelete = room },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Hapus",
                                        tint = Color(0xFFC62828),
                                        modifier = Modifier.size(16.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChatDetailScreen(
    chatRoom: ChatRoom,
    viewModel: AgroGoatViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val liveMessages by viewModel.activeChatMessages.collectAsState()
    val isPartnerOnline by viewModel.activePartnerOnline.collectAsState()
    val partnerLastSeen by viewModel.activePartnerLastSeen.collectAsState()
    val currentUserEmail by viewModel.userEmail.collectAsState()
    val usersProfiles by viewModel.usersProfiles.collectAsState()
    val partnerEmail = chatRoom.buyerEmail
    
    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    var messageToDelete by remember { mutableStateOf<String?>(null) }
    
    if (messageToDelete != null) {
        AlertDialog(
            onDismissRequest = { messageToDelete = null },
            title = { Text(text = "Hapus Pesan") },
            text = { Text("Apakah Anda yakin ingin menghapus pesan ini?") },
            confirmButton = {
                TextButton(onClick = {
                    messageToDelete?.let { viewModel.deleteMessage(it) }
                    messageToDelete = null
                }) {
                    Text("Hapus", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { messageToDelete = null }) {
                    Text("Batal")
                }
            }
        )
    }

    val displayMessages = liveMessages

    LaunchedEffect(displayMessages.size) {
        if (displayMessages.isNotEmpty()) {
            listState.animateScrollToItem(displayMessages.size - 1)
        }
    }

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

                    Box(modifier = Modifier.size(38.dp)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF81C784)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = chatRoom.initials,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                        // Online indicator dot
                        val isOnline = isPartnerOnline
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isOnline) Color(0xFF4CAF50) else Color.Gray)
                                .border(1.5.dp, Color.White, CircleShape)
                                .align(Alignment.BottomEnd)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = chatRoom.name,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        val statusStr = if (isPartnerOnline) "Online" else if (partnerLastSeen.isNotEmpty()) "Terakhir terlihat: $partnerLastSeen" else "Offline"
                        Text(
                            text = statusStr,
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        val partnerProfile = usersProfiles[partnerEmail.lowercase()]
                        val partnerPhone = partnerProfile?.get("phone") as? String

                        IconButton(
                            onClick = {
                                if (!partnerPhone.isNullOrBlank()) {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$partnerPhone"))
                                    context.startActivity(intent)
                                } else {
                                    Toast.makeText(context, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Outlined.Call, contentDescription = "Panggil", tint = Color.White)
                        }

                    }
                }
            }
        },
        containerColor = Color(0xFF4CAF50) // Bright-green backdrop matching screenshots
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            // Message bubble list container
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // "Hari Ini" indicator bubble
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.35f))
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "Hari Ini",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                items(displayMessages) { message ->
                    val isMe = message.senderEmail.equals(currentUserEmail, ignoreCase = true)
                    
                    if (message.content.startsWith("[PRODUCT_CARD]")) {
                        // Renders Product Attachment Card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            AdminProductAttachmentBubble(
                                goatId = message.content.removePrefix("[PRODUCT_CARD]"),
                                viewModel = viewModel,
                                timestamp = message.timestamp,
                                isMe = isMe,
                                isRead = message.isRead,
                                modifier = Modifier.pointerInput(Unit) {
                                    detectTapGestures(
                                        onLongPress = {
                                            if (isMe) messageToDelete = message.id
                                        }
                                    )
                                }
                            )
                        }
                    } else {
                        // Standard chat message bubble
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            if (!isMe) {
                                // Avatar for recipient
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F5E9))
                                        .align(Alignment.Top),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = chatRoom.initials,
                                        color = Color(0xFF2E7D32),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            // Text Bubble
                            Card(
                                shape = RoundedCornerShape(
                                    topStart = 16.dp,
                                    topEnd = 16.dp,
                                    bottomStart = if (isMe) 16.dp else 4.dp,
                                    bottomEnd = if (isMe) 4.dp else 16.dp
                                ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isMe) Color(0xFF1B5E20) else Color.White
                                ),
                                modifier = Modifier
                                    .widthIn(max = 260.dp)
                                    .pointerInput(Unit) {
                                        detectTapGestures(
                                            onLongPress = {
                                                if (isMe) messageToDelete = message.id
                                            }
                                        )
                                    }
                            ) {
                                Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                    Text(
                                        text = message.content,
                                        color = if (isMe) Color.White else Color.Black,
                                        fontSize = 14.sp
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        modifier = Modifier.align(Alignment.End),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = message.timestamp,
                                            color = if (isMe) Color.White.copy(alpha = 0.65f) else Color.Gray,
                                            fontSize = 9.sp
                                        )
                                        if (isMe) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            if (message.isRead) {
                                                Text(
                                                    text = "✓✓",
                                                    color = Color.White.copy(alpha = 0.8f),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            } else {
                                                Text(
                                                    text = "✓",
                                                    color = Color.White.copy(alpha = 0.5f),
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Live messages loaded successfully from database
            }

            // Input panel bottom
            val focusRequester = remember { FocusRequester() }
            val keyboardController = LocalSoftwareKeyboardController.current

            Surface(
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp, start = 14.dp, end = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.SentimentSatisfiedAlt,
                        contentDescription = "Emoticon",
                        tint = Color.Gray,
                        modifier = Modifier
                            .size(28.dp)
                            .clickable {
                                focusRequester.requestFocus()
                                keyboardController?.show()
                            }
                            .padding(2.dp)
                    )

                    Card(
                        shape = RoundedCornerShape(26.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5)),
                        modifier = Modifier.weight(1f)
                    ) {
                        BasicTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .focusRequester(focusRequester),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                color = Color.Black,
                                fontSize = 15.sp
                            ),
                            decorationBox = { innerTextField ->
                                if (textInput.isEmpty()) {
                                    Text("Tulis pesan...", color = Color.Gray, fontSize = 14.sp)
                                }
                                innerTextField()
                            }
                        )
                    }

                    val isTyping = textInput.isNotBlank()
                    IconButton(
                        onClick = {
                            if (isTyping) {
                                viewModel.sendMessage(textInput)
                                textInput = ""
                            }
                        },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(if (isTyping) Color(0xFF1B5E20) else Color.LightGray)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Send,
                            contentDescription = "Kirim",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminProductAttachmentBubble(
    goatId: String,
    viewModel: AgroGoatViewModel,
    timestamp: String,
    isMe: Boolean,
    isRead: Boolean = false,
    modifier: Modifier = Modifier
) {
    val goats by viewModel.goats.collectAsState()
    val goat = goats.find { it.id == goatId }

    if (goat != null) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = modifier
                .width(260.dp)
                .shadow(1.dp, RoundedCornerShape(16.dp))
        ) {
            Column {
                // Attachment Image
                GoatImage(
                    imageUri = goat.imageUri,
                    defaultImageRes = com.agrogoat.core.designsystem.R.drawable.etawa,
                    contentDescription = goat.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
    
                // Info Details inside bubble card
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = goat.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "${goat.category} • ${goat.gender} • ${goat.weight} Kg",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val priceStr = "Rp ${java.text.NumberFormat.getNumberInstance(java.util.Locale("id", "ID")).format(goat.price)}"
                        Text(
                            text = priceStr,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF2E7D32)
                        )
    
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFE8F5E9))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Tersedia",
                                color = Color(0xFF2E7D32),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
    
                    Spacer(modifier = Modifier.height(6.dp))
    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = timestamp,
                            color = Color.Gray,
                            fontSize = 9.sp
                        )
                        if (isMe) {
                            Spacer(modifier = Modifier.width(4.dp))
                            if (isRead) {
                                Text(
                                    text = "✓✓",
                                    color = Color(0xFF2E7D32),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Text(
                                    text = "✓",
                                    color = Color.Gray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        Text(text = "[Produk tidak ditemukan]", fontSize = 14.sp, color = Color.Gray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
    }
}
