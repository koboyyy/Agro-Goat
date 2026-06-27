package com.agrogoat.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.ImageDecoder
import androidx.compose.ui.platform.LocalContext
import androidx.annotation.DrawableRes
import com.agrogoat.app.R
import com.agrogoat.app.data.*
import com.agrogoat.app.utils.ImageCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.NumberFormat
import java.util.Locale

/**
 * Format Long numbers to Indonesian Rupiah currency format
 */
fun formatRupiah(value: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return formatter.format(value).replace(",00", "").replace("Rp", "Rp ")
}

@Composable
fun GoatLogo(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.logo),
        contentDescription = "Goat Logo",
        modifier = modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun AppHeader(
    userName: String,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Circular GoatLogo
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFF2E7D32), CircleShape)
                    .testTag("app_logo_container"),
                contentAlignment = Alignment.Center
            ) {
                GoatLogo(modifier = Modifier.fillMaxSize())
            }

            Column {
                Text(
                    text = buildAnnotatedString {
                        withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, color = Color.Black)) {
                            append("Agro ")
                        }
                        withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, color = Color(0xFFFF9800))) {
                            append("Goat")
                        }
                    },
                    fontSize = 18.sp,
                    modifier = Modifier.testTag("app_title")
                )
                Text(
                    text = "Bengkalis",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Notification badge button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .shadow(elevation = 1.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .clickable { onNotificationClick() }
                    .testTag("notification_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notification",
                    tint = Color.Black,
                    modifier = Modifier.size(24.dp)
                )
                // Red badge
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 6.dp, end = 6.dp)
                        .size(16.dp)
                        .background(Color.Red, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "3",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Profile circle button
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2E7D32)) // Forest Green PM
                    .clickable { onProfileClick() }
                    .testTag("profile_avatar"),
                contentAlignment = Alignment.Center
            ) {
                val initials = if (userName.length >= 2) {
                    "${userName[0]}${userName.substringAfter(" ", "").firstOrNull() ?: userName.getOrNull(1) ?: ""}"
                } else "PM"
                
                Text(
                    text = initials.uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun SearchAndFilterBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Input Box
        Box(
            modifier = Modifier
                .weight(1f)
                .height(54.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(28.dp))
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Cari Kambing",
                    tint = Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
                
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = "Cari Kambing...",
                            color = Color.Gray.copy(alpha = 0.7f),
                            fontSize = 15.sp
                        )
                    }
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = Color.Black,
                            fontSize = 15.sp
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_input")
                    )
                }
            }
        }

        // Green Custom Filter Button
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF2E7D32)) // Forest green matching spec
                .clickable { onFilterClick() }
                .testTag("filter_button"),
            contentAlignment = Alignment.Center
        ) {
            CustomFilterIcon(
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
fun PromoSpecialBanner(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {

            // Background Image
            Image(
                painter = painterResource(id = R.drawable.banner_agro_goat),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Isi Banner
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Column kiri
                Column(
                    modifier = Modifier
                        .weight(0.68f)
                        .fillMaxHeight()
                        .padding(start = 14.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Isi text dan badge
                }
            }
        }
    }
}

@Composable
fun PromoBadgeItem(
    icon: @Composable () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.width(56.dp)
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFF2E7D32), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
        Text(
            text = text,
            fontSize = 5.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32),
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 6.sp
        )
    }
}

@Composable
fun CategoryCardItem(
    label: String,
    iconRes: Int,
    iconBgColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderStroke = if (isSelected) {
        Modifier.border(1.5.dp, Color(0xFF2E7D32), RoundedCornerShape(20.dp))
    } else {
        Modifier.border(1.dp, Color(0xFFE0E0E0), RoundedCornerShape(20.dp))
    }

    val containerBgColor = if (isSelected) Color(0xFFE8F5E9) else Color.White
    val textLabelColor = if (isSelected) Color(0xFF2E7D32) else Color.Black

    Card(
        modifier = modifier
            .width(108.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .then(borderStroke)
            .testTag("category_card_${label.lowercase()}"),
        colors = CardDefaults.cardColors(containerColor = containerBgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Flat styling like design
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Circle with Goat Silhouette inside
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = label,
                    modifier = Modifier.size(34.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = textLabelColor
            )
        }
    }
}

@Composable
fun GoatVerticalRowItem(
    goat: GoatItem,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    val picRes = when (goat.category) {
        GoatCategory.POTONG -> R.drawable.burawa
        GoatCategory.ETAWA -> R.drawable.etawa
        GoatCategory.PERAH -> R.drawable.kacang
    }

    val picBgColor = when (goat.category) {
        GoatCategory.POTONG -> Color(0xFFE8F5E9)
        GoatCategory.ETAWA -> Color(0xFFFFF3E0)
        GoatCategory.PERAH -> Color(0xFFE3F2FD)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .testTag("goat_card_${goat.id}"),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image box on left with optional BARU tag
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(picBgColor),
                contentAlignment = Alignment.Center
            ) {
                GoatImage(
                    imageUri = goat.imageUri,
                    defaultImageRes = picRes,
                    contentDescription = goat.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                if (goat.isNew) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF2E7D32))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = "BARU",
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Info column in the middle
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = goat.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.3).sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = Color.Black
                )

                // Attributes Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "• ${goat.gender}",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• ${goat.weight} kg",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "• ${goat.age} Th",
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Price
                Text(
                    text = formatRupiah(goat.price),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF2E7D32), // Green color matching design
                        fontWeight = FontWeight.ExtraBold
                    )
                )

                // Location row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    CustomLocationPinIcon(
                        modifier = Modifier.size(12.dp),
                        tint = Color(0xFFE53935) // Red Pin
                    )
                    Text(
                        text = goat.location,
                        fontSize = 11.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Vertically Centered Favorite Button Box
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .shadow(elevation = 1.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .background(Color(0xFFF5F5F5))
                    .clickable { onFavoriteToggle() }
                    .testTag("fav_btn_${goat.id}"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (goat.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (goat.isFavorite) Color.Red else Color.LightGray,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun ShieldCheckIcon(modifier: Modifier = Modifier, tint: Color = Color(0xFF2E7D32)) {
    val path = remember { Path() }
    val check = remember { Path() }
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Draw shield path
        path.reset()
        path.moveTo(w * 0.5f, h * 0.15f)
        path.lineTo(w * 0.8f, h * 0.25f)
        path.quadraticTo(w * 0.8f, h * 0.6f, w * 0.5f, h * 0.85f)
        path.quadraticTo(w * 0.2f, h * 0.6f, w * 0.2f, h * 0.25f)
        path.close()
        drawPath(path, color = tint, style = Stroke(width = w * 0.08f))
        
        // Draw checkmark
        check.reset()
        check.moveTo(w * 0.38f, h * 0.48f)
        check.lineTo(w * 0.48f, h * 0.58f)
        check.lineTo(w * 0.68f, h * 0.38f)
        drawPath(check, color = tint, style = Stroke(width = w * 0.08f))
    }
}

@Composable
fun HandDeliveryIcon(modifier: Modifier = Modifier, tint: Color = Color(0xFF2E7D32)) {
    val path = remember { Path() }
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        val strokeWidth = w * 0.08f
        path.reset()
        path.moveTo(w * 0.2f, h * 0.4f)
        path.lineTo(w * 0.5f, h * 0.4f)
        path.lineTo(w * 0.5f, h * 0.7f)
        path.lineTo(w * 0.2f, h * 0.7f)
        path.close()
        
        path.moveTo(w * 0.5f, h * 0.5f)
        path.lineTo(w * 0.75f, h * 0.5f)
        path.lineTo(w * 0.85f, h * 0.62f)
        path.lineTo(w * 0.85f, h * 0.7f)
        path.lineTo(w * 0.5f, h * 0.7f)
        drawPath(path, color = tint, style = Stroke(width = strokeWidth))
        
        // wheels
        drawCircle(color = tint, radius = w * 0.08f, center = Offset(w * 0.35f, h * 0.78f))
        drawCircle(color = tint, radius = w * 0.08f, center = Offset(w * 0.7f, h * 0.78f))
    }
}

@Composable
fun GoatImage(
    imageUri: String?,
    @DrawableRes defaultImageRes: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    var bitmap by remember(imageUri) { mutableStateOf<android.graphics.Bitmap?>(null) }

    LaunchedEffect(imageUri) {
        if (!imageUri.isNullOrEmpty()) {
            try {
                if (imageUri.startsWith("http://") || imageUri.startsWith("https://")) {
                    val cachedBitmap = ImageCache.fetchAndCacheImage(context, imageUri)
                    if (cachedBitmap != null) {
                        bitmap = cachedBitmap
                    }
                } else {
                    val uri = android.net.Uri.parse(imageUri)
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                        bitmap = android.graphics.ImageDecoder.decodeBitmap(source)
                    } else {
                        @Suppress("DEPRECATION")
                        bitmap = android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap!!.asImageBitmap(),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Image(
            painter = painterResource(id = defaultImageRes),
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale
        )
    }
}
