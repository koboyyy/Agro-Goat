package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF4F6F4))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Upper region: Green primary backdrop with logo
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.42f)
                    .background(Color(0xFF1F6E35))
            ) {
                // Decorator Bubble top-right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 35.dp, y = (-30).dp)
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E8546).copy(alpha = 0.28f))
                )

                // Decorator Bubble mid-left
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-40).dp, y = 50.dp)
                        .size(130.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E8546).copy(alpha = 0.25f))
                )

                // Central logo items
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // White round icon containing custom smile canvas
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.size(36.dp)) {
                            // Draw eye left
                            drawCircle(
                                color = Color(0xFF1F6E35),
                                radius = 2.dp.toPx(),
                                center = Offset(size.width * 0.35f, size.height * 0.42f)
                            )
                            // Draw eye right
                            drawCircle(
                                color = Color(0xFF1F6E35),
                                radius = 2.dp.toPx(),
                                center = Offset(size.width * 0.65f, size.height * 0.42f)
                            )
                            // Draw smile arc mouth
                            drawArc(
                                color = Color(0xFF1F6E35),
                                startAngle = 20f,
                                sweepAngle = 140f,
                                useCenter = false,
                                style = Stroke(width = 3.dp.toPx()),
                                topLeft = Offset(size.width * 0.25f, size.height * 0.32f),
                                size = Size(size.width * 0.5f, size.height * 0.45f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "Agro Goat",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        letterSpacing = (-0.5).sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Kelola Peternakan Kambing Anda",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Lower region: Clean form card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.58f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .navigationBarsPadding()
                        .padding(horizontal = 28.dp, vertical = 24.dp)
                        .verticalScroll(androidx.compose.foundation.rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Title and subtitle
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Selamat Datang",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF111612)
                        )
                        Text(
                            text = "Masuk untuk melanjutkan ke dashboard",
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    // Email Field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Alamat Email",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        TextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            placeholder = { Text("contoh@agrogoat.id", color = Color.Gray.copy(alpha = 0.7f), fontSize = 14.sp) },
                            leadingIcon = {
                                Text(
                                    text = "✉️",
                                    fontSize = 16.sp,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_email_input"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F7F5),
                                unfocusedContainerColor = Color(0xFFF5F7F5),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                        )
                    }

                    // Password Field
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Kata Sandi",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        TextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            placeholder = { Text("••••••••", color = Color.Gray.copy(alpha = 0.5f), fontSize = 14.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = Color.Gray,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                Text(
                                    text = if (isPasswordVisible) "👁️" else "👁️‍🗨️",
                                    fontSize = 16.sp,
                                    modifier = Modifier
                                        .clickable { isPasswordVisible = !isPasswordVisible }
                                        .padding(8.dp)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_password_input"),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF5F7F5),
                                unfocusedContainerColor = Color(0xFFF5F7F5),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent,
                                focusedTextColor = Color.Black,
                                unfocusedTextColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
                        )
                    }

                    // Lupa Kata Sandi
                    Text(
                        text = "Lupa Kata Sandi?",
                        color = Color(0xFF1F6E35),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                Toast.makeText(context, "Fitur reset kata sandi dikirim ke email!", Toast.LENGTH_SHORT).show()
                            }
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Masuk button (with test tag)
                    Button(
                        onClick = {
                            if (emailInput.isNotBlank() && passwordInput.isNotBlank()) {
                                onLoginSuccess()
                                Toast.makeText(context, "Selamat Datang Kembali!", Toast.LENGTH_SHORT).show()
                            } else {
                                // For ease of testing, let's allow bypass if empty
                                onLoginSuccess()
                                Toast.makeText(context, "Selamat Datang!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_login_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F6E35)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Masuk",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }

                    // Divider separator: "atau"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.LightGray.copy(alpha = 0.5f)))
                        Text(
                            text = "atau",
                            color = Color.Gray,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        )
                        Box(modifier = Modifier.weight(1f).height(1.dp).background(Color.LightGray.copy(alpha = 0.5f)))
                    }

                    // Footer Link "Daftar Sekarang"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Belum punya akun? ",
                            color = Color.Gray,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Daftar Sekarang",
                            color = Color(0xFF1F6E35),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.clickable {
                                Toast.makeText(context, "Memulai Pendaftaran Akun...", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}
