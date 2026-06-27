package com.agrogoat.app.ui.screens

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
import com.agrogoat.app.ui.components.GoatLogo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.BorderStroke
import com.agrogoat.app.viewmodel.AgroGoatViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

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
    val path = remember { Path() }
    Canvas(modifier = modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        path.reset()
        path.moveTo(w * 0.15f, cy)
        path.quadraticTo(cx, cy - h * 0.32f, w * 0.85f, cy)
        path.quadraticTo(cx, cy + h * 0.32f, w * 0.15f, cy)
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
                val p = remember { Path() }
                Canvas(modifier = Modifier.size(16.dp)) {
                    p.reset()
                    p.moveTo(size.width * 0.15f, size.height * 0.5f)
                    p.lineTo(size.width * 0.45f, size.height * 0.8f)
                    p.lineTo(size.width * 0.85f, size.height * 0.2f)
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
    viewModel: AgroGoatViewModel,
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
    var roleInput by remember { mutableStateOf("Pedagang") }

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

    // Google / Role Selection states
    var showRoleSelectionDialog by remember { mutableStateOf(false) }
    var selectedRoleForDialog by remember { mutableStateOf<String?>(null) }
    var rolesToSelect by remember { mutableStateOf<List<String>>(emptyList()) }
    var pendingProfileData by remember { mutableStateOf<Map<String, Any>?>(null) }

    // Google Sign-In helpers
    suspend fun handleMockGoogleSuccess() {
        isLoading = true
        delay(1500)
        isLoading = false
        
        val email = "google.user@gmail.com"
        val derivedName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
        
        rolesToSelect = listOf("Pedagang", "Penjual")
        selectedRoleForDialog = null
        pendingProfileData = mapOf(
            "name" to derivedName,
            "address" to "Bengkalis, Riau",
            "balance" to 5000000L,
            "phone" to "081299998888",
            "email" to email
        )
        showRoleSelectionDialog = true
    }

    suspend fun handleGoogleUserAuthFlow(email: String, name: String, screen: AuthScreen) {
        val database = com.google.firebase.firestore.FirebaseFirestore.getInstance()
        val getTask = database.collection("users_profiles").document(email).get()
        var attempts = 0
        while (!getTask.isComplete && attempts < 40) {
            delay(50)
            attempts++
        }
        
        val exists = getTask.isSuccessful && getTask.result != null && getTask.result.exists()
        
        if (screen == AuthScreen.REGISTER) {
            if (exists) {
                val data = getTask.result?.data
                val rolesList = data?.get("roles") as? List<String> ?: listOf(data?.get("role") as? String ?: "Pedagang")
                val allRoles = listOf("Pedagang", "Penjual")
                val unregisteredRoles = allRoles.filter { !rolesList.contains(it) }
                
                if (unregisteredRoles.isEmpty()) {
                    isLoading = false
                    Toast.makeText(context, "Semua peran sudah terdaftar untuk email ini. Silakan masuk (Login).", Toast.LENGTH_LONG).show()
                } else {
                    isLoading = false
                    rolesToSelect = unregisteredRoles
                    selectedRoleForDialog = null
                    pendingProfileData = mapOf(
                        "name" to (data?.get("name") as? String ?: name),
                        "address" to (data?.get("address") as? String ?: "Bengkalis, Riau"),
                        "balance" to ((data?.get("balance") as? Long) ?: 0L),
                        "phone" to (data?.get("phone") as? String ?: "081299998888"),
                        "email" to email,
                        "existingRoles" to rolesList
                    )
                    showRoleSelectionDialog = true
                }
            } else {
                // Registering for the first time via Google. Show role selection dialog!
                isLoading = false
                rolesToSelect = listOf("Pedagang", "Penjual")
                selectedRoleForDialog = null
                pendingProfileData = mapOf(
                    "name" to name,
                    "address" to "Bengkalis, Riau",
                    "balance" to 0L,
                    "phone" to "081299998888",
                    "email" to email
                )
                showRoleSelectionDialog = true
            }
        } else {
            // LOGIN SCREEN
            if (!exists) {
                // Check if already registered: if not, show role selection (register them)
                isLoading = false
                rolesToSelect = listOf("Pedagang", "Penjual")
                selectedRoleForDialog = null
                pendingProfileData = mapOf(
                    "name" to name,
                    "address" to "Bengkalis, Riau",
                    "balance" to 0L,
                    "phone" to "081299998888",
                    "email" to email
                )
                showRoleSelectionDialog = true
            } else {
                val data = getTask.result?.data
                var finalName = name
                var finalAddress = "Bengkalis, Riau"
                var finalBalance = 0L
                var finalPhone = "081299998888"
                var finalRoles = listOf("Pedagang")
                
                if (data != null) {
                    finalName = data["name"] as? String ?: name
                    finalAddress = data["address"] as? String ?: "Bengkalis, Riau"
                    finalBalance = (data["balance"] as? Long) ?: 0L
                    finalPhone = data["phone"] as? String ?: "081299998888"
                    
                    val rolesList = data["roles"] as? List<String>
                    val singleRole = data["role"] as? String
                    finalRoles = when {
                        rolesList != null && rolesList.isNotEmpty() -> rolesList
                        singleRole != null -> {
                            if (singleRole.contains(",")) singleRole.split(",").map { it.trim() } else listOf(singleRole)
                        }
                        else -> listOf("Pedagang")
                    }
                }
                
                isLoading = false
                if (finalRoles.size > 1) {
                    rolesToSelect = finalRoles
                    selectedRoleForDialog = null
                    pendingProfileData = mapOf(
                        "name" to finalName,
                        "address" to finalAddress,
                        "balance" to finalBalance,
                        "phone" to finalPhone,
                        "email" to email
                    )
                    showRoleSelectionDialog = true
                } else {
                    val chosenRole = finalRoles.firstOrNull() ?: "Pedagang"
                    viewModel.setUserProfile(
                        name = finalName,
                        address = finalAddress,
                        balance = finalBalance,
                        role = chosenRole,
                        email = email,
                        phone = finalPhone
                    )
                    showSuccessBanner = true
                    delay(1500)
                    onLoginSuccess()
                    showSuccessBanner = false
                }
            }
        }
    }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            val email = account?.email
            
            // Clean formatting of name from email prefix if displayName is blank
            val derivedName = if (!account?.displayName.isNullOrBlank()) {
                account.displayName!!
            } else {
                email?.substringBefore("@")
                    ?.split(".", "_", "-")
                    ?.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                    ?: "Google User"
            }

            if (idToken != null) {
                coroutineScope.launch {
                    isLoading = true
                    try {
                        val auth = FirebaseAuth.getInstance()
                        val credential = GoogleAuthProvider.getCredential(idToken, null)
                        val authTask = auth.signInWithCredential(credential)
                        var authAttempts = 0
                        while (!authTask.isComplete && authAttempts < 100) {
                            delay(50)
                            authAttempts++
                        }
                        if (authTask.isSuccessful) {
                            val firebaseUser = authTask.result?.user
                            val fbEmail = firebaseUser?.email
                            val fbName = if (!firebaseUser?.displayName.isNullOrBlank()) {
                                firebaseUser.displayName!!
                            } else {
                                fbEmail?.substringBefore("@")
                                    ?.split(".", "_", "-")
                                    ?.joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                                    ?: derivedName
                            }
                            if (fbEmail != null) {
                                handleGoogleUserAuthFlow(fbEmail, fbName, currentScreen)
                            } else if (email != null) {
                                handleGoogleUserAuthFlow(email, derivedName, currentScreen)
                            } else {
                                isLoading = false
                                Toast.makeText(context, "Email Google tidak ditemukan", Toast.LENGTH_LONG).show()
                            }
                        } else {
                            if (email != null) {
                                handleGoogleUserAuthFlow(email, derivedName, currentScreen)
                            } else {
                                isLoading = false
                                val errMsg = authTask.exception?.localizedMessage ?: "Firebase Auth Google gagal"
                                Toast.makeText(context, errMsg, Toast.LENGTH_LONG).show()
                            }
                        }
                    } catch (e: Exception) {
                        if (email != null) {
                            handleGoogleUserAuthFlow(email, derivedName, currentScreen)
                        } else {
                            isLoading = false
                            Toast.makeText(context, "Error: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } else {
                // No ID Token (e.g. offline/mock environment/device configuration issue).
                // Let's allow local Firebase/Firestore matching if email exists.
                if (email != null) {
                    coroutineScope.launch {
                        isLoading = true
                        handleGoogleUserAuthFlow(email, derivedName, currentScreen)
                    }
                } else {
                    Toast.makeText(context, "Token ID Google tidak ditemukan", Toast.LENGTH_LONG).show()
                }
            }
        } catch (e: ApiException) {
            val errorStatus = e.statusCode
            val errorMsg = e.localizedMessage ?: ""
            Toast.makeText(context, "Google Sign-In Error (Status: $errorStatus): $errorMsg", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Google Sign-In gagal"
            Toast.makeText(context, "Google Sign-In Error: $errorMsg", Toast.LENGTH_LONG).show()
        }
    }

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
                            enabled = !isLoading,
                            onClick = {
                                if (emailInput.isBlank()) {
                                    showLoginErrorBanner = true
                                    emailErrorMsg = "Email tidak boleh kosong"
                                } else if (passwordInput.isBlank()) {
                                    showLoginErrorBanner = true
                                    passwordErrorMsg = "Kata sandi tidak boleh kosong"
                                } else {
                                    coroutineScope.launch {
                                        isLoading = true
                                        var finalName = ""
                                        var finalAddress = ""
                                        var finalBalance = 0L
                                        var finalPhone = ""
                                        var finalRoles = listOf<String>()
                                        var isSuccess = false
 
                                        try {
                                            val auth = FirebaseAuth.getInstance()
                                            val loginTask = auth.signInWithEmailAndPassword(emailInput, passwordInput)
                                            var attempts = 0
                                            while (!loginTask.isComplete && attempts < 100) {
                                                delay(50)
                                                attempts++
                                            }
                                            if (loginTask.isSuccessful) {
                                                val database = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                val getTask = database.collection("users_profiles").document(emailInput).get()
                                                var getAttempts = 0
                                                while (!getTask.isComplete && getAttempts < 40) {
                                                    delay(50)
                                                    getAttempts++
                                                }
                                                if (getTask.isSuccessful && getTask.result != null && getTask.result.exists()) {
                                                    val data = getTask.result.data
                                                    isSuccess = true
                                                    finalName = data?.get("name") as? String ?: emailInput.substringBefore("@").replaceFirstChar { it.uppercase() }
                                                    finalAddress = data?.get("address") as? String ?: "Bengkalis, Riau"
                                                    finalBalance = (data?.get("balance") as? Long) ?: 0L
                                                    finalPhone = data?.get("phone") as? String ?: "08123456789"
                                                    
                                                    val rolesList = data?.get("roles") as? List<String>
                                                    val singleRole = data?.get("role") as? String
                                                    finalRoles = when {
                                                        rolesList != null && rolesList.isNotEmpty() -> rolesList
                                                        singleRole != null -> {
                                                            if (singleRole.contains(",")) singleRole.split(",").map { it.trim() } else listOf(singleRole)
                                                        }
                                                        else -> listOf("Pedagang")
                                                    }
                                                } else {
                                                    isSuccess = true
                                                    finalName = emailInput.substringBefore("@").replaceFirstChar { it.uppercase() }
                                                    finalAddress = "Bengkalis, Riau"
                                                    finalBalance = 0L
                                                    finalPhone = "08123456789"
                                                    finalRoles = listOf("Pedagang")
                                                }
                                            } else {
                                                showLoginErrorBanner = true
                                                val errorMsg = loginTask.exception?.localizedMessage ?: "Autentikasi gagal"
                                                if (errorMsg.contains("password", ignoreCase = true) || errorMsg.contains("credential", ignoreCase = true)) {
                                                    passwordErrorMsg = "Kata sandi yang Anda masukkan salah"
                                                } else {
                                                    emailErrorMsg = errorMsg
                                                }
                                            }
                                        } catch (e: Exception) {
                                            showLoginErrorBanner = true
                                            emailErrorMsg = e.localizedMessage ?: "Terjadi kesalahan koneksi"
                                        }

                                        if (isSuccess) {
                                            delay(1000)
                                            isLoading = false
                                            if (finalRoles.size > 1) {
                                                rolesToSelect = finalRoles
                                                selectedRoleForDialog = null
                                                pendingProfileData = mapOf(
                                                    "name" to finalName,
                                                    "address" to finalAddress,
                                                    "balance" to finalBalance,
                                                    "phone" to finalPhone,
                                                    "email" to emailInput
                                                )
                                                showRoleSelectionDialog = true
                                            } else {
                                                val chosenRole = finalRoles.firstOrNull() ?: "Pedagang"
                                                viewModel.setUserProfile(
                                                    name = finalName,
                                                    address = finalAddress,
                                                    balance = finalBalance,
                                                    role = chosenRole,
                                                    email = emailInput,
                                                    phone = finalPhone
                                                )
                                                showSuccessBanner = true
                                                delay(1500)
                                                onLoginSuccess()
                                                showSuccessBanner = false
                                            }
                                        } else {
                                            isLoading = false
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("submit_login_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = buttonBgColor,
                                disabledContainerColor = buttonBgColor.copy(alpha = 0.6f)
                            ),
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

                        // Google Sign-In Button
                        OutlinedButton(
                            onClick = {
                                val appCtx = context.applicationContext
                                val webClientId = try {
                                    val resId = appCtx.resources.getIdentifier("default_web_client_id", "string", appCtx.packageName)
                                    if (resId != 0) appCtx.getString(resId) else "1034459376734-fakeclientid.apps.googleusercontent.com"
                                } catch (e: Exception) {
                                    "1034459376734-fakeclientid.apps.googleusercontent.com"
                                }
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(webClientId)
                                    .requestEmail()
                                    .requestProfile()
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("google_login_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Masuk dengan Google",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
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

                        // Role Selection Row
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "Pilih Peran",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Pedagang Card
                                val isPedagang = roleInput == "Pedagang"
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { roleInput = "Pedagang" }
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isPedagang) Color(0xFF1F6E35) else Color(0xFFE2E8F0),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isPedagang) Color(0xFF1F6E35).copy(alpha = 0.08f) else Color(0xFFF5F6F5)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Pembeli",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isPedagang) Color(0xFF1F6E35) else Color.Black
                                        )
                                        Text(
                                            text = "Cari Kambing",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                // Penjual Card
                                val isPenjual = roleInput == "Penjual"
                                Card(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { roleInput = "Penjual" }
                                        .border(
                                            width = 1.5.dp,
                                            color = if (isPenjual) Color(0xFF1F6E35) else Color(0xFFE2E8F0),
                                            shape = RoundedCornerShape(12.dp)
                                        ),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isPenjual) Color(0xFF1F6E35).copy(alpha = 0.08f) else Color(0xFFF5F6F5)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp, horizontal = 8.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = "Penjual",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = if (isPenjual) Color(0xFF1F6E35) else Color.Black
                                        )
                                        Text(
                                            text = "Jual Kambing",
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
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
                                    val p = remember { Path() }
                                    Canvas(modifier = Modifier.size(10.dp)) {
                                        p.reset()
                                        p.moveTo(size.width * 0.15f, size.height * 0.5f)
                                        p.lineTo(size.width * 0.45f, size.height * 0.8f)
                                        p.lineTo(size.width * 0.85f, size.height * 0.2f)
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
                            enabled = !isLoading,
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
                                        try {
                                            val auth = FirebaseAuth.getInstance()
                                            val regTask = auth.createUserWithEmailAndPassword(emailInput, passwordInput)
                                            var attempts = 0
                                            while (!regTask.isComplete && attempts < 120) {
                                                delay(50)
                                                attempts++
                                            }
                                            if (regTask.isSuccessful) {
                                                val database = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                val profile = mapOf(
                                                    "name" to nameInput,
                                                    "address" to "Bengkalis, Riau",
                                                    "balance" to 0L,
                                                    "role" to roleInput,
                                                    "roles" to listOf(roleInput),
                                                    "email" to emailInput,
                                                    "phone" to phoneInput,
                                                    "password" to passwordInput
                                                )
                                                // Save to users_profiles
                                                database.collection("users_profiles").document(emailInput).set(profile)
                                                // Also set as user UID
                                                val uid = regTask.result?.user?.uid ?: ""
                                                if (uid.isNotEmpty()) {
                                                    database.collection("users").document(uid).set(profile + mapOf("uid" to uid))
                                                }
                                                
                                                showSuccessBanner = true
                                                delay(1500)
                                                registeredEmail = emailInput
                                                showSuccessBanner = false
                                            } else {
                                                val exception = regTask.exception
                                                if (exception is com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                                                    // Email already in use! Let's try to verify password to add a new role.
                                                    val loginTask = auth.signInWithEmailAndPassword(emailInput, passwordInput)
                                                    var loginAttempts = 0
                                                    while (!loginTask.isComplete && loginAttempts < 100) {
                                                        delay(50)
                                                        loginAttempts++
                                                    }
                                                    if (loginTask.isSuccessful) {
                                                        val database = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                                        val getTask = database.collection("users_profiles").document(emailInput).get()
                                                        var getAttempts = 0
                                                        while (!getTask.isComplete && getAttempts < 40) {
                                                            delay(50)
                                                            getAttempts++
                                                        }
                                                        if (getTask.isSuccessful && getTask.result != null && getTask.result.exists()) {
                                                            val data = getTask.result.data
                                                            val existingRoles = data?.get("roles") as? List<String> ?: listOf(data?.get("role") as? String ?: "Pedagang")
                                                            if (existingRoles.contains(roleInput)) {
                                                                Toast.makeText(context, "Peran ${roleInput} sudah terdaftar di email ini. Silakan masuk (Login).", Toast.LENGTH_LONG).show()
                                                            } else {
                                                                val updatedRoles = existingRoles + roleInput
                                                                database.collection("users_profiles").document(emailInput).update(
                                                                    mapOf(
                                                                        "role" to roleInput,
                                                                        "roles" to updatedRoles
                                                                    )
                                                                )
                                                                
                                                                viewModel.setUserProfile(
                                                                    name = data?.get("name") as? String ?: nameInput,
                                                                    address = data?.get("address") as? String ?: "Bengkalis, Riau",
                                                                    balance = (data?.get("balance") as? Long) ?: 0L,
                                                                    role = roleInput,
                                                                    email = emailInput,
                                                                    phone = data?.get("phone") as? String ?: phoneInput
                                                                )
                                                                
                                                                Toast.makeText(context, "Berhasil menambahkan peran ${roleInput} ke akun Anda!", Toast.LENGTH_LONG).show()
                                                                showSuccessBanner = true
                                                                delay(1500)
                                                                onLoginSuccess()
                                                                showSuccessBanner = false
                                                            }
                                                        } else {
                                                            Toast.makeText(context, "Data profil tidak ditemukan.", Toast.LENGTH_LONG).show()
                                                        }
                                                    } else {
                                                        Toast.makeText(context, "Email sudah terdaftar. Masukkan kata sandi yang cocok untuk menambahkan peran baru.", Toast.LENGTH_LONG).show()
                                                    }
                                                } else {
                                                    val errorMsg = exception?.localizedMessage ?: "Registrasi gagal"
                                                    Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                                }
                                            }
                                        } catch (e: Exception) {
                                            val errorMsg = e.localizedMessage ?: "Terjadi kesalahan koneksi"
                                            Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                                        }
                                        isLoading = false
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1F6E35),
                                disabledContainerColor = Color(0xFF1F6E35).copy(alpha = 0.6f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Daftar",
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

                        // Google Register Button
                        OutlinedButton(
                            onClick = {
                                val appCtx = context.applicationContext
                                val webClientId = try {
                                    val resId = appCtx.resources.getIdentifier("default_web_client_id", "string", appCtx.packageName)
                                    if (resId != 0) appCtx.getString(resId) else "1034459376734-fakeclientid.apps.googleusercontent.com"
                                } catch (e: Exception) {
                                    "1034459376734-fakeclientid.apps.googleusercontent.com"
                                }
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(webClientId)
                                    .requestEmail()
                                    .requestProfile()
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("google_register_button"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Daftar dengan Google",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color.Black
                                )
                            }
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
                            .background(Color.Black.copy(alpha = 0.4f))
                            .clickable(enabled = false) {},
                        contentAlignment = Alignment.Center
                    ) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                            modifier = Modifier.padding(horizontal = 24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                SegmentedSpinner(modifier = Modifier.size(32.dp), color = Color(0xFF1F6E35))
                                Text(
                                    text = if (currentScreen == AuthScreen.LOGIN) "Memverifikasi akun..." else "Memproses...",
                                    color = Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center
                                )
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

        // Dialog Pemilihan Peran
        if (showRoleSelectionDialog) {
            AlertDialog(
                onDismissRequest = { /* Tidak diizinkan tutup di luar */ },
                title = {
                    Text(
                        text = if (currentScreen == AuthScreen.REGISTER) "Pilih Peran Pendaftaran" else "Pilih Peran Masuk",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFF111612),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                    ) {
                        val messageText = if (currentScreen == AuthScreen.REGISTER) {
                            "Silakan pilih peran utama Anda untuk menyelesaikan pendaftaran Akun Google Anda:"
                        } else {
                            "Akun Anda terdaftar dengan dua peran. Silakan pilih salah satu untuk sesi masuk ini:"
                        }
                        Text(
                            text = messageText,
                            fontSize = 13.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        // Card 1: Pedagang
                        if (rolesToSelect.contains("Pedagang")) {
                            val isPedagangSelected = selectedRoleForDialog == "Pedagang"
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedRoleForDialog = "Pedagang" }
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isPedagangSelected) Color(0xFF1F6E35) else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPedagangSelected) Color(0xFF1F6E35).copy(alpha = 0.08f) else Color.White
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isPedagangSelected) Color(0xFF1F6E35) else Color(0xFFF1F5F9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isPedagangSelected) Color.White else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Pembeli",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isPedagangSelected) Color(0xFF1F6E35) else Color.Black
                                        )
                                        Text(
                                            text = "Cari kambing potong & kambing peranakan terbaik",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }

                        // Card 2: Penjual
                        if (rolesToSelect.contains("Penjual")) {
                            val isPenjualSelected = selectedRoleForDialog == "Penjual"
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedRoleForDialog = "Penjual" }
                                    .border(
                                        width = 1.5.dp,
                                        color = if (isPenjualSelected) Color(0xFF1F6E35) else Color(0xFFE2E8F0),
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isPenjualSelected) Color(0xFF1F6E35).copy(alpha = 0.08f) else Color.White
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(if (isPenjualSelected) Color(0xFF1F6E35) else Color(0xFFF1F5F9)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Person,
                                            contentDescription = null,
                                            tint = if (isPenjualSelected) Color.White else Color.Gray,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Column {
                                        Text(
                                            text = "Penjual (Peternak)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isPenjualSelected) Color(0xFF1F6E35) else Color.Black
                                        )
                                        Text(
                                            text = "Tawarkan ternak Anda ke pasar yang lebih luas",
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val chosenRole = selectedRoleForDialog
                            val profile = pendingProfileData
                            if (chosenRole != null && profile != null) {
                                coroutineScope.launch {
                                    isLoading = true
                                    showRoleSelectionDialog = false
                                    
                                    val email = profile["email"] as? String ?: ""
                                    if (email.isNotEmpty()) {
                                        try {
                                            val database = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                            val userProfileDoc = database.collection("users_profiles").document(email).get()
                                            var attempts = 0
                                            while (!userProfileDoc.isComplete && attempts < 40) {
                                                delay(50)
                                                attempts++
                                            }
                                            if (userProfileDoc.isSuccessful && userProfileDoc.result != null && userProfileDoc.result.exists()) {
                                                val existingData = userProfileDoc.result.data
                                                val existingRoles = existingData?.get("roles") as? List<String> 
                                                    ?: listOf(existingData?.get("role") as? String ?: "Pedagang")
                                                val updatedRoles = if (!existingRoles.contains(chosenRole)) {
                                                    existingRoles + chosenRole
                                                } else {
                                                    existingRoles
                                                }
                                                database.collection("users_profiles").document(email).update(
                                                    mapOf(
                                                        "role" to chosenRole,
                                                        "roles" to updatedRoles
                                                    )
                                                )
                                            } else {
                                                val newProfile = mapOf(
                                                    "name" to (profile["name"] as? String ?: "Google User"),
                                                    "address" to "Bengkalis, Riau",
                                                    "balance" to (profile["balance"] as? Long ?: 0L),
                                                    "role" to chosenRole,
                                                    "roles" to listOf(chosenRole),
                                                    "email" to email,
                                                    "phone" to (profile["phone"] as? String ?: "081299998888")
                                                )
                                                database.collection("users_profiles").document(email).set(newProfile)
                                            }
                                        } catch (e: Exception) {
                                            // Fallback
                                        }
                                    }
                                    
                                    viewModel.setUserProfile(
                                        name = profile["name"] as? String ?: "",
                                        address = profile["address"] as? String ?: "",
                                        balance = profile["balance"] as? Long ?: 0L,
                                        role = chosenRole,
                                        email = email,
                                        phone = profile["phone"] as? String ?: ""
                                    )
                                    showSuccessBanner = true
                                    delay(1500)
                                    onLoginSuccess()
                                    isLoading = false
                                    showSuccessBanner = false
                                }
                            }
                        },
                        enabled = selectedRoleForDialog != null,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1F6E35)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("confirm_role_button")
                    ) {
                        Text("Konfirmasi Peran", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                },
                dismissButton = null,
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
            )
        }
    }
}
