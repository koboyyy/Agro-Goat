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
import androidx.compose.material.icons.filled.ArrowBack
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

    // List of quick helper responses
    val quickPrompts = listOf(
        "Kambing ready mas?",
        "Ada diskon khusus?",
        "Bisa kirim ke Bengkalis?",
        "Bagaimana pakan etawanya?"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            // Elegant green custom toolbar matching screenshot
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                color = Color(0xFF1F6E35) // Rich Green Primary
            ) {
                Row(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Button
                    IconButton(
                        onClick = { viewModel.setTab(AppTab.BERANDA) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("chat_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Avatar PB stacked with Online Indicator
                    Box(
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF81C784)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "PB",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                        // Tiny Online Dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF4CAF50))
                                .align(Alignment.BottomEnd)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // Title and Active state subtitle
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "Pak Budi Farm",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Online",
                            color = Color.White.copy(alpha = 0.85f),
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                    }

                    // Action Icons: Phone, Video, More
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = "📞",
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clickable {
                                    Toast
                                        .makeText(context, "Memulai panggilan ke Pak Budi...", Toast.LENGTH_SHORT)
                                        .show()
                                }
                                .padding(4.dp)
                        )
                        Text(
                            text = "📹",
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clickable {
                                    Toast
                                        .makeText(context, "Memulai panggilan video...", Toast.LENGTH_SHORT)
                                        .show()
                                }
                                .padding(4.dp)
                        )
                        Text(
                            text = "⁝",
                            fontSize = 20.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable {
                                    Toast
                                        .makeText(context, "Opsi Lainnya", Toast.LENGTH_SHORT)
                                        .show()
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFEFE8DE) // Cozy sand-gray/beige background matching screenshot
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Messages list viewport
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    val isUser = msg.sender == MessageSender.USER
                    val isSystem = msg.sender == MessageSender.SYSTEM

                    if (isSystem) {
                        // Centered date subtitle capsule e.g. "Hari ini"
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
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
                        // Message Bubble Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                        ) {
                            Column(
                                modifier = Modifier.widthIn(max = 280.dp),
                                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                            ) {
                                // Customized Bubble
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
                                        .background(
                                            if (isUser) Color(0xFFD9FDD3) else Color(0xFFFFFFFF)
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    if (msg.content.startsWith("[PRODUCT_CARD]")) {
                                        // Product attachment card inside bubble
                                        val regularText = msg.content.removePrefix("[PRODUCT_CARD]")
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
                                                    Text(
                                                        text = "Kambing Etawa Jantan",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 13.sp,
                                                        color = Color.Black
                                                    )
                                                    Text(
                                                        text = "Rp 5.500.000",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 12.sp,
                                                        color = Color(0xFF1F6E35)
                                                    )
                                                }
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = regularText,
                                                fontSize = 14.sp,
                                                color = Color.Black
                                            )
                                        }
                                    } else if (msg.content == "...") {
                                        // Typing indicator animation mockup
                                        Row(
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Box(modifier = Modifier.size(6.dp).background(Color.Gray, CircleShape))
                                            Box(modifier = Modifier.size(6.dp).background(Color.Gray.copy(alpha = 0.6f), CircleShape))
                                            Box(modifier = Modifier.size(6.dp).background(Color.Gray.copy(alpha = 0.3f), CircleShape))
                                        }
                                    } else {
                                        Text(
                                            text = msg.content,
                                            fontSize = 14.sp,
                                            color = Color.Black
                                        )
                                    }
                                }

                                // Bubble Footer (Timestamp & Status Ticks)
                                Row(
                                    modifier = Modifier.padding(top = 2.dp, start = 4.dp, end = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = msg.timestamp,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                    if (isUser && msg.content != "...") {
                                        // WhatsApp double tick green/blue color
                                        Text(
                                            text = "✓✓",
                                            color = Color(0xFF4CAF50),
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Quick helpers list row
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickPrompts) { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White)
                            .clickable {
                                viewModel.sendMessage(prompt)
                            }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                            .testTag("quick_prompt_${prompt.hashCode()}")
                    ) {
                        Text(
                            text = prompt,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1F6E35) // Matching forest green
                        )
                    }
                }
            }

            // High Fidelity Input Section resembling WhatsApp structure
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 90.dp) // Leave clean padding for navigation bar
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emoji selection trigger button
                    IconButton(
                        onClick = {
                            Toast.makeText(context, "Keyboard Emoji Terbuka", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Text("😊", fontSize = 20.sp)
                    }

                    // Rounded message box capsule
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(horizontal = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Main core TextField using stable and transparent BasicTextField
                        Box(modifier = Modifier.weight(1f)) {
                            if (textInput.isEmpty()) {
                                Text(
                                    "Ketik pesan...",
                                    color = Color.LightGray,
                                    fontSize = 14.sp
                                )
                            }
                            BasicTextField(
                                value = textInput,
                                onValueChange = { newVal -> textInput = newVal },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("chat_input_text"),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    color = Color.Black,
                                    fontSize = 14.sp
                                ),
                                singleLine = true
                            )
                        }

                        // Attachment paperclip
                        Text(
                            text = "📎",
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clickable {
                                    Toast.makeText(context, "Pilih berkas lampiran", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 6.dp)
                        )

                        // Camera trigger icon
                        Text(
                            text = "📷",
                            fontSize = 18.sp,
                            modifier = Modifier
                                .clickable {
                                    Toast.makeText(context, "Membuka Kamera", Toast.LENGTH_SHORT).show()
                                }
                                .padding(horizontal = 6.dp)
                        )
                    }

                    // Green Microphone Action Button (switches to Send if input is typed)
                    val isTyping = textInput.isNotBlank()
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1F6E35))
                            .clickable {
                                if (isTyping) {
                                    viewModel.sendMessage(textInput)
                                    textInput = ""
                                } else {
                                    Toast.makeText(context, "Tahan untuk merekam suara...", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .testTag("send_msg_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isTyping) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Kirim",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = "🎤",
                                fontSize = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
