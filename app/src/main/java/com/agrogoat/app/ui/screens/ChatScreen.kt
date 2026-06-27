package com.agrogoat.app.ui.screens
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.outlined.*

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.outlined.SentimentSatisfied
import androidx.compose.material.icons.outlined.AttachFile
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Mic
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
import com.agrogoat.app.data.*
import com.agrogoat.app.ui.components.GoatSilhouette
import com.agrogoat.app.viewmodel.AgroGoatViewModel
import com.agrogoat.app.viewmodel.AppTab
import com.agrogoat.app.viewmodel.ChatScreenState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: AgroGoatViewModel,
    modifier: Modifier = Modifier
) {
    val currentScreenState by viewModel.chatScreenState.collectAsState()
    val selectedChatRoom by viewModel.selectedChatRoom.collectAsState()
    val chatRooms by viewModel.chatInboxRooms.collectAsState()

    if (currentScreenState == ChatScreenState.DETAIL) {
        androidx.activity.compose.BackHandler {
            viewModel.goBackToChatList()
        }
    }

    when (currentScreenState) {
        ChatScreenState.LIST -> {
            ChatListScreen(
                chatRooms = chatRooms,
                onRoomClick = { room ->
                    viewModel.selectChatRoom(room)
                },
                onBackClick = {
                    viewModel.setTab(AppTab.BERANDA)
                },
                modifier = modifier
            )
        }
        ChatScreenState.DETAIL -> {
            val room = selectedChatRoom ?: if (chatRooms.isNotEmpty()) chatRooms.first() else null
            if (room != null) {
                ChatDetailScreen(
                    chatRoom = room,
                    viewModel = viewModel,
                    onBackClick = {
                        viewModel.goBackToChatList()
                    },
                    modifier = modifier
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada percakapan")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    chatRooms: List<ChatRoom>,
    onRoomClick: (ChatRoom) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize().padding(bottom = 90.dp),
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp),
                color = Color.White
            ) {
                Box(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.Black
                        )
                    }
                    
                    Text(
                        text = "Chat",
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        },
        containerColor = Color.White
    ) { innerPadding ->
        if (chatRooms.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("💬", fontSize = 48.sp)
                    Text(
                        text = "Belum ada chat masuk.",
                        color = Color.Gray,
                        fontSize = 15.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chatRooms) { room ->
                    val borderStroke = if (room.hasBorder) BorderStroke(2.dp, Color(0xFF2196F3)) else null
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .let { if (borderStroke != null) it.border(borderStroke, RoundedCornerShape(12.dp)) else it }
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5F5))
                            .clickable { onRoomClick(room) }
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF81C784)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = room.initials,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(14.dp))
                            
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = room.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = room.lastMessage,
                                    color = Color.Gray,
                                    fontSize = 13.sp,
                                    maxLines = 1
                                )
                            }
                            
                            if (room.hasCheckmark) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "✓✓",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.Bottom)
                                )
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
fun ChatDetailScreen(
    chatRoom: ChatRoom,
    viewModel: AgroGoatViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.activeChatMessages.collectAsState()
    var textInput by remember { mutableStateOf("") }
    val isPartnerOnline by viewModel.activePartnerOnline.collectAsState()
    val partnerLastSeen by viewModel.activePartnerLastSeen.collectAsState()
    val currentUserEmail by viewModel.userEmail.collectAsState()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp),
                color = Color(0xFF2E7D32)
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

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
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (isPartnerOnline) Color(0xFF4CAF50) else Color.Gray)
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
                        Text(
                            text = if (isPartnerOnline) "Online" else if (partnerLastSeen.isNotEmpty()) "Terakhir terlihat: $partnerLastSeen" else "Offline",
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
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Memulai panggilan...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Call,
                                contentDescription = "Panggil",
                                tint = Color.White
                            )
                        }
                        IconButton(
                            onClick = {
                                Toast.makeText(context, "Menu lainnya...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        },
        containerColor = Color(0xFFEDE8E3)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    val isMe = msg.senderEmail.equals(currentUserEmail, ignoreCase = true) || msg.senderEmail.isEmpty()
                    val isSystem = msg.sender == MessageSender.SYSTEM

                    if (isSystem) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = msg.content,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Normal,
                                color = Color(0xFF8D8477),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE4DDD4))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
                        ) {
                            Column(
                                modifier = Modifier.widthIn(max = 280.dp),
                                horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = if (isMe) 14.dp else 2.dp,
                                                topEnd = if (isMe) 2.dp else 14.dp,
                                                bottomStart = 14.dp,
                                                bottomEnd = 14.dp
                                            )
                                        )
                                        .background(if (isMe) Color(0xFFD2F4D9) else Color(0xFFFFFFFF))
                                        .padding(if (msg.content.startsWith("[PRODUCT_CARD]")) 6.dp else 12.dp, 8.dp)
                                ) {
                                    when {
                                        msg.content.startsWith("[PRODUCT_CARD]") -> {
                                            ProductAttachment(msg.content.removePrefix("[PRODUCT_CARD]"))
                                        }
                                        msg.content.startsWith("[BUKTI_TRANSFER]") -> {
                                            TransferProofAttachment(msg.content.removePrefix("[BUKTI_TRANSFER]"))
                                        }
                                        msg.content == "..." -> {
                                            TypingIndicator()
                                        }
                                        else -> {
                                            Text(text = msg.content, fontSize = 14.sp, color = Color.Black)
                                        }
                                    }
                                }

                                Row(
                                    modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    if (msg.timestamp.isNotEmpty()) {
                                        Text(text = msg.timestamp, fontSize = 10.sp, color = Color.Gray)
                                    }
                                    if (isMe && msg.content != "...") {
                                        Text(text = "✓✓", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ChatInput(
                textInput = textInput,
                onValueChange = { textInput = it },
                onSend = {
                    viewModel.sendMessage(textInput)
                    textInput = ""
                }
            )
        }
    }
}

@Composable
fun ProductAttachment(text: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp))
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                GoatSilhouette(Modifier.size(24.dp), Color.Black)
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "Kambing Etawa Jantan", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color.Black)
                Text(text = "Rp 5.500.000", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF2E7D32))
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = text, fontSize = 14.sp, color = Color.Black)
    }
}

@Composable
fun TransferProofAttachment(text: String) {
    Column(
        modifier = Modifier
            .width(240.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Outlined.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("BUKTI TRANSFER", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray)
                Text("BERHASIL", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF4CAF50))
            }
        }
        
        if (text.isNotBlank()) {
            Text(
                text = text,
                fontSize = 12.sp,
                color = Color.Black,
                modifier = Modifier.padding(10.dp)
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(modifier = Modifier.size(6.dp).background(Color.Gray, CircleShape))
        Box(modifier = Modifier.size(6.dp).background(Color.Gray.copy(alpha = 0.6f), CircleShape))
        Box(modifier = Modifier.size(6.dp).background(Color.Gray.copy(alpha = 0.3f), CircleShape))
    }
}

@Composable
fun ChatInput(
    textInput: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5))
            .padding(bottom = 12.dp, top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.SentimentSatisfied,
                contentDescription = "Emoji",
                tint = Color.Gray,
                modifier = Modifier
                    .size(28.dp)
                    .clickable { }
                    .padding(2.dp)
            )

            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    if (textInput.isEmpty()) {
                        Text("Ketik pesan...", color = Color.LightGray, fontSize = 14.sp)
                    }
                    BasicTextField(
                        value = textInput,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = Color.Black, fontSize = 14.sp),
                        singleLine = true
                    )
                }
                
                Icon(
                    imageVector = Icons.Outlined.AttachFile,
                    contentDescription = "Lampiran",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { }
                        .padding(horizontal = 2.dp)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Icon(
                    imageVector = Icons.Outlined.PhotoCamera,
                    contentDescription = "Kamera",
                    tint = Color.Gray,
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { }
                        .padding(horizontal = 2.dp)
                )
            }

            val isTyping = textInput.isNotBlank()
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32))
                    .clickable { if (isTyping) onSend() },
                contentAlignment = Alignment.Center
            ) {
                if (isTyping) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = "Kirim",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Outlined.Mic,
                        contentDescription = "Suara",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
