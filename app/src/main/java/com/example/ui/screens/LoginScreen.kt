package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GoatLogo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class AuthScreen {
    LOGIN,
    REGISTER
}


@Composable
fun SegmentedSpinner(modifier: Modifier = Modifier, color: Color = Color.White) {
    val transition = rememberInfiniteTransition(label = "spinner")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )
    Canvas(modifier = modifier.size(24.dp).rotate(angle)) {
        val strokeWidth = 3.dp.toPx()
        // Draw 4 segments of arcs
        for (i in 0..3) {
            drawArc(
                color = color,
                startAngle = (i * 90f) + 15f,
                sweepAngle = 60f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun CustomEyeIcon(visible: Boolean, tint: Color, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val path = Path().apply {
            moveTo(w * 0.15f, cy)
            quadraticTo(cx, cy - h * 0.32f, w * 0.85f, cy)
            quadraticTo(cx, cy + h * 0.32f, w * 0.15f, cy)
        }
        drawPath(path, color = tint, style = Stroke(width = 1.8.dp.toPx()))
        drawCircle(color = tint, radius = w * 0.14f, center = Offset(cx, cy))
        if (!visible) {
            drawLine(
                color = tint,
                start = Offset(w * 0.25f, h * 0.25f),
                end = Offset(w * 0.75f, h * 0.75f),
                strokeWidth = 1.8.dp.toPx()
            )
        }
    }
}

@Composable
fun SuccessPopup(
    title: String,
    message: String,
    actionText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Green check icon
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1F6E35)),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(16.dp)) {
                    val p = Path().apply {
                        moveTo(size.width * 0.15f, size.height * 0.5f)
                        lineTo(size.width * 0.45f, size.height * 0.8f)
                        lineTo(size.width * 0.85f, size.height * 0.2f)
                    }
                    drawPath(p, color = Color.White, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = message,
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Text(
                    text = actionText,
                    color = Color(0xFF1F6E35),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf(AuthScreen.LOGIN) }

    // Forms Inputs
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var phoneInput by remember { mutableStateOf("") }
    var confirmPasswordInput by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var isConfirmPasswordVisible by remember { mutableStateOf(false) }
    var isTermsAccepted by remember { mutableStateOf(true) }

    // Loading & Success Flows
    var isLoading by remember { mutableStateOf(false) }
    var showSuccessBanner by remember { mutableStateOf(false) }
    var registeredEmail by remember { mutableStateOf("") }

    // Login Validation States
    var showLoginErrorBanner by remember { mutableStateOf(false) }
    var emailErrorMsg by remember { mutableStateOf("") }
    var passwordErrorMsg by remember { mutableStateOf("") }

    // Pre-fill on redirect
    LaunchedEffect(registeredEmail) {
        if (registeredEmail.isNotEmpty()) {
            emailInput = registeredEmail
            passwordInput = ""
            currentScreen = AuthScreen.LOGIN
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        // Split Background: Green at top, light gray at bottom
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.40f)
                    .background(Color(0xFF1F6E35))
            ) {
                // Decorative bubbles (Lighter green overlapping circles)
                // Bubble 1: Top-Right
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 60.dp, y = (-40).dp)
                        .size(240.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E8546).copy(alpha = 0.28f))
                )
                // Bubble 2: Mid-Right
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .offset(x = 100.dp, y = (-120).dp)
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E8546).copy(alpha = 0.22f))
                )
                // Bubble 3: Top-Left
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(x = (-80).dp, y = 60.dp)
                        .size(190.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E8546).copy(alpha = 0.22f))
                )
                // Bubble 4: Mid-Left/Bottom-Left
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .offset(x = (-50).dp, y = 40.dp)
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E8546).copy(alpha = 0.25f))
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.60f)
                    .background(Color(0xFFF4F6F4))
            )
        }

        // Main content column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Upper Header Region
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.30f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // White round logo container
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    GoatLogo(modifier = Modifier.fillMaxSize())
                }

                Spacer(modifier = Modifier.height(8.dp))

                // "Agro Goat" Title
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
                            append("Agro ")
                        }
                        withStyle(style = SpanStyle(color = Color(0xFFFFB300), fontWeight = FontWeight.Bold)) { // Golden Orange Goat
                            append("Goat")
                        }
                    },
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Bengkalis",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    lineHeight = 32.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Masuk untuk melanjutkan",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Lower Form Region - Floating Card Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.70f)
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp)
                    .shadow(16.dp, RoundedCornerShape(32.dp))
                    .clip(RoundedCornerShape(32.dp))
                    .background(Color.White)
            ) {
                val scrollState = rememberScrollState()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                        .verticalScroll(scrollState),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentScreen == AuthScreen.LOGIN) {
                        // Title and Subtitle
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Selamat Datang",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111612)
                            )
                            Text(
                                text = "Masuk untuk melanjutkan ke dashboard",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }

                        // Error Banner
                        if (showLoginErrorBanner) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFFFEE2E2))
                                    .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFEF5350)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Column {
                                    Text(
                                        text = "Login Gagal!",
                                        color = Color(0xFFC62828),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "Email atau kata sandi yang Anda masukkan salah.",
                                        color = Color(0xFFEF5350),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }

                        // Email Field
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Alamat Email",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (emailErrorMsg.isNotEmpty()) Color(0xFFEF5350) else Color.Gray
                            )

                            val hasEmailError = emailErrorMsg.isNotEmpty()
                            TextField(
                                value = emailInput,
                                onValueChange = {
                                    emailInput = it
                                    if (hasEmailError) {
                                        emailErrorMsg = ""
                                        if (passwordErrorMsg.isEmpty()) showLoginErrorBanner = false
                                    }
                                },
                                placeholder = { Text("contoh@agrogoat.id", color = Color.Gray.copy(alpha = 0.5f), fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = if (hasEmailError) Color(0xFFEF5350) else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (hasEmailError) {
                                        Box(
                                            modifier = Modifier
                                                .padding(end = 8.dp)
                                                .size(20.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFFEF5350)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("!", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("login_email_input")
                                    .border(
                                        width = 1.dp,
                                        color = if (hasEmailError) Color(0xFFEF5350) else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = if (hasEmailError) Color(0xFFFFF5F5) else Color(0xFFF5F6F5),
                                    unfocusedContainerColor = if (hasEmailError) Color(0xFFFFF5F5) else Color(0xFFF5F6F5),
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
                            if (hasEmailError) {
                                Text(
                                    text = "⚠️ $emailErrorMsg",
                                    color = Color(0xFFEF5350),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Password Field
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Kata Sandi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (passwordErrorMsg.isNotEmpty()) Color(0xFFEF5350) else Color.Gray
                            )

                            val hasPasswordError = passwordErrorMsg.isNotEmpty()
                            TextField(
                                value = passwordInput,
                                onValueChange = {
                                    passwordInput = it
                                    if (hasPasswordError) {
                                        passwordErrorMsg = ""
                                        if (emailErrorMsg.isEmpty()) showLoginErrorBanner = false
                                    }
                                },
                                placeholder = { Text("••••••••", color = Color.Gray.copy(alpha = 0.4f), fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = if (hasPasswordError) Color(0xFFEF5350) else Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .clickable { isPasswordVisible = !isPasswordVisible }
                                            .padding(8.dp)
                                    ) {
                                        CustomEyeIcon(
                                            visible = isPasswordVisible,
                                            tint = if (hasPasswordError) Color(0xFFEF5350) else Color.Gray
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("login_password_input")
                                    .border(
                                        width = 1.dp,
                                        color = if (hasPasswordError) Color(0xFFEF5350) else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(12.dp)
                                    ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = if (hasPasswordError) Color(0xFFFFF5F5) else Color(0xFFF5F6F5),
                                    unfocusedContainerColor = if (hasPasswordError) Color(0xFFFFF5F5) else Color(0xFFF5F6F5),
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
                            if (hasPasswordError) {
                                Text(
                                    text = "⚠️ $passwordErrorMsg",
                                    color = Color(0xFFEF5350),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
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
                                    Toast
                                        .makeText(context, "Fitur reset kata sandi dikirim ke email!", Toast.LENGTH_SHORT)
                                        .show()
                                }
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Submit Button
                        val buttonBgColor = if (showLoginErrorBanner) Color(0xFFEF5350) else Color(0xFF1F6E35)
                        Button(
                            onClick = {
                                if (emailInput == "user@salah.com") {
                                    showLoginErrorBanner = true
                                    emailErrorMsg = "Email tidak terdaftar di sistem kami"
                                    passwordErrorMsg = "Kata sandi yang Anda masukkan salah"
                                } else if (emailInput.isBlank()) {
                                    showLoginErrorBanner = true
                                    emailErrorMsg = "Email tidak boleh kosong"
                                } else if (passwordInput.isBlank()) {
                                    showLoginErrorBanner = true
                                    passwordErrorMsg = "Kata sandi tidak boleh kosong"
                                } else {
                                    // Success sequence
                                    coroutineScope.launch {
                                        isLoading = true
                                        delay(1500)
                                        showSuccessBanner = true
                                        delay(1500)
                                        onLoginSuccess()
                                        isLoading = false
                                        showSuccessBanner = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_login_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonBgColor),
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
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE2E8F0)))
                            Text(
                                text = "atau",
                                color = Color.Gray,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp)
                            )
                            Box(modifier = Modifier.weight(1f).height(1.dp).background(Color(0xFFE2E8F0)))
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
                                    currentScreen = AuthScreen.REGISTER
                                    showLoginErrorBanner = false
                                    emailErrorMsg = ""
                                    passwordErrorMsg = ""
                                }
                            )
                        }
                    } else {
                        // REGISTER SCREEN
                        // Title and Subtitle
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Daftar Akun",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF111612)
                            )
                            Text(
                                text = "Buat akun baru untuk memulai",
                                fontSize = 13.sp,
                                color = Color.Gray
                            )
                        }

                        // Nama Lengkap
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Nama Lengkap",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            TextField(
                                value = nameInput,
                                onValueChange = { nameInput = it },
                                placeholder = { Text("Masukkan nama lengkap", color = Color.Gray.copy(alpha = 0.5f), fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF5F6F5),
                                    unfocusedContainerColor = Color(0xFFF5F6F5),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        // Alamat Email
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
                                placeholder = { Text("contoh@agrogoat.id", color = Color.Gray.copy(alpha = 0.5f), fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Email,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF5F6F5),
                                    unfocusedContainerColor = Color(0xFFF5F6F5),
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

                        // Nomor Telepon
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Nomor Telepon",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            TextField(
                                value = phoneInput,
                                onValueChange = { phoneInput = it },
                                placeholder = { Text("+62 xxx-xxxx-xxxx", color = Color.Gray.copy(alpha = 0.5f), fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF5F6F5),
                                    unfocusedContainerColor = Color(0xFFF5F6F5),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                        }

                        // Kata Sandi
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
                                placeholder = { Text("••••••••", color = Color.Gray.copy(alpha = 0.4f), fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .clickable { isPasswordVisible = !isPasswordVisible }
                                            .padding(8.dp)
                                    ) {
                                        CustomEyeIcon(visible = isPasswordVisible, tint = Color.Gray)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF5F6F5),
                                    unfocusedContainerColor = Color(0xFFF5F6F5),
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

                        // Konfirmasi Kata Sandi
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Konfirmasi Kata Sandi",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            TextField(
                                value = confirmPasswordInput,
                                onValueChange = { confirmPasswordInput = it },
                                placeholder = { Text("••••••••", color = Color.Gray.copy(alpha = 0.4f), fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = Color.Gray,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .clickable { isConfirmPasswordVisible = !isConfirmPasswordVisible }
                                            .padding(8.dp)
                                    ) {
                                        CustomEyeIcon(visible = isConfirmPasswordVisible, tint = Color.Gray)
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF5F6F5),
                                    unfocusedContainerColor = Color(0xFFF5F6F5),
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                visualTransformation = if (isConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation()
                            )
                        }

                        // Terms & Conditions Checkbox
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isTermsAccepted = !isTermsAccepted }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isTermsAccepted) Color(0xFF1F6E35) else Color.Transparent)
                                    .border(1.5.dp, Color(0xFF1F6E35), RoundedCornerShape(4.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isTermsAccepted) {
                                    Canvas(modifier = Modifier.size(10.dp)) {
                                        val p = Path().apply {
                                            moveTo(size.width * 0.15f, size.height * 0.5f)
                                            lineTo(size.width * 0.45f, size.height * 0.8f)
                                            lineTo(size.width * 0.85f, size.height * 0.2f)
                                        }
                                        drawPath(p, color = Color.White, style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round))
                                    }
                                }
                            }
                            Text(
                                text = buildAnnotatedString {
                                    append("Saya setuju dengan ")
                                    withStyle(style = SpanStyle(color = Color(0xFF1F6E35), fontWeight = FontWeight.Bold)) {
                                        append("Syarat & Ketentuan")
                                    }
                                },
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Daftar Button
                        Button(
                            onClick = {
                                if (nameInput.isBlank() || emailInput.isBlank() || phoneInput.isBlank() || passwordInput.isBlank()) {
                                    Toast.makeText(context, "Harap lengkapi semua kolom!", Toast.LENGTH_SHORT).show()
                                } else if (passwordInput != confirmPasswordInput) {
                                    Toast.makeText(context, "Konfirmasi sandi tidak cocok!", Toast.LENGTH_SHORT).show()
                                } else if (!isTermsAccepted) {
                                    Toast.makeText(context, "Anda harus menyetujui Syarat & Ketentuan!", Toast.LENGTH_SHORT).show()
                                } else {
                                    coroutineScope.launch {
                                        isLoading = true
                                        delay(1500)
                                        showSuccessBanner = true
                                        delay(1500)
                                        registeredEmail = emailInput
                                        isLoading = false
                                        showSuccessBanner = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F6E35)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Daftar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }

                        // Divider link back to login
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Sudah punya akun? ",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Masuk Sekarang",
                                color = Color(0xFF1F6E35),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.clickable {
                                    currentScreen = AuthScreen.LOGIN
                                }
                            )
                        }
                    }
                }

                // Loading Overlay (Dimmable Scrim and Active button/text loader)
                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.45f))
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp, vertical = 18.dp)
                        ) {
                            Spacer(modifier = Modifier.weight(1f))

                            // Active spinner container aligned exactly to the button position
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1F6E35).copy(alpha = 0.85f)),
                                contentAlignment = Alignment.Center
                            ) {
                                SegmentedSpinner(modifier = Modifier.size(24.dp))
                            }

                            if (currentScreen == AuthScreen.LOGIN) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = "Memverifikasi akun...",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                            } else {
                                Spacer(modifier = Modifier.height(20.dp))
                            }
                        }
                    }
                }
            }
        }

        // Floating Success Notification Card (overlaps green area at the top)
        if (showSuccessBanner) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(top = 16.dp)
            ) {
                if (currentScreen == AuthScreen.LOGIN) {
                    SuccessPopup(
                        title = "Login Berhasil! 🎉",
                        message = "Selamat datang kembali, Peternak Maju.",
                        actionText = "Mengalihkan ke dashboard..."
                    )
                } else {
                    SuccessPopup(
                        title = "Pendaftaran Berhasil! 🎉",
                        message = "Akun Anda telah dibuat.",
                        actionText = "Mengalihkan ke halaman login..."
                    )
                }
            }
        }
    }
}
