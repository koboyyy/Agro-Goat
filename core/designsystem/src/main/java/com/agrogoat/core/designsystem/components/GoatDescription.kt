package com.agrogoat.core.designsystem.components.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agrogoat.core.model.GoatItem

@Composable
fun GoatDescription(
    goat: GoatItem,
    modifier: Modifier = Modifier
){
    Column(modifier) {
        Text(
            text = "Deskripsi",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color.Black
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = goat.description,
            fontSize = 14.sp,
            lineHeight = 24.sp,
            color = Color.Gray
        )
    }
}
