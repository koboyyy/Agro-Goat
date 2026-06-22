package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.GoatSilhouette
import com.example.viewmodel.AgroGoatViewModel
import com.example.viewmodel.AppTab
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: AgroGoatViewModel,
    modifier: Modifier = Modifier
) {
    val messages by viewModel.messages.collectAsState()
    var textInput by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Scroll to bottom when list grows
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickPrompts = listOf(
        "Kambing ready mas?",
        "Ada diskon khusus?",
        "Bisa kirim ke Bengkalis?",
        "Bagaimana pakan etawanya?"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                color = Color(0xFF1F6E35)
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.setTab(AppTab.BERANDA) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
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
                            Text("PB", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                                .align(Alignment.BottomEnd)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Pak Budi Farm", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(text = "Online", color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Medium, fontSize = 11.sp)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text("📞", fontSize = 18.sp, modifier = Modifier.clickable { })
                        Text("📹", fontSize = 18.sp, modifier = Modifier.clickable { })
                        Text("⁝", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { })
                    }
                }
            }
        },
        containerColor = Color(0xFFEFE8DE)
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.sender == MessageSender.USER
                    val isSystem = msg.sender == MessageSender.SYSTEM

                    if (isSystem) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = msg.content,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE0D8D0))
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Column(
                                modifier = Modifier.widthIn(max = 300.dp),
                                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(
                                            RoundedCornerShape(
                                                topStart = 14.dp,
                                                topEnd = 14.dp,
                                                bottomStart = if (isUser) 14.dp else 2.dp,
                                                bottomEnd = if (isUser) 2.dp else 14.dp
                                            )
                                        )
                                        .background(if (isUser) Color(0xFFD9FDD3) else Color(0xFFFFFFFF))
                                        .padding(if (msg.content.startsWith("[BUKTI_TRANSFER]")) 4.dp else 12.dp, 8.dp)
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
                                    Text(text = msg.timestamp, fontSize = 10.sp, color = Color.Gray)
                                    if (isUser && msg.content != "...") {
                                        Text(text = "✓✓", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Input section remains the same...
            ChatInput(
                textInput = textInput,
                onValueChange = { textInput = it },
                onSend = {
                    viewModel.sendMessage(textInput)
                    textInput = ""
                },
                quickPrompts = quickPrompts,
                onPromptClick = { viewModel.sendMessage(it) }
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
                .background(Color(0xFFF4F6F4))
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
                Text(text = "Rp 5.500.000", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF1F6E35))
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
        // Mock Screenshot Image Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("BUKTI TRANSFER", fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Gray)
                Text("BERHASIL", fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF4CAF50))
            }
        }
        
        // Caption
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
    onSend: () -> Unit,
    quickPrompts: List<String>,
    onPromptClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable { onPromptClick(prompt) }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(text = prompt, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1F6E35))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("😊", fontSize = 20.sp, modifier = Modifier.padding(4.dp))

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
                Text("📎", fontSize = 18.sp, modifier = Modifier.padding(horizontal = 6.dp))
                Text("📷", fontSize = 18.sp, modifier = Modifier.padding(horizontal = 6.dp))
            }

            val isTyping = textInput.isNotBlank()
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F6E35))
                    .clickable { if (isTyping) onSend() },
                contentAlignment = Alignment.Center
            ) {
                if (isTyping) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = "Kirim", tint = Color.White, modifier = Modifier.size(18.dp))
                } else {
                    Text("🎤", fontSize = 18.sp)
                }
            }
        }
    }
}
