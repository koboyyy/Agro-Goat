package com.agrogoat.app.ui.components.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DetailItem(
    title: String,
    value: String?
){
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ){
        Box(
            Modifier
                .padding(top=6.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(Color(0xFF2E7D32))
        )

        Column {
            Text(
                text=title,
                fontWeight=FontWeight.Bold,
                fontSize=13.sp
            )

            Spacer(Modifier.height(3.dp))

            Text(
                text=value ?: "-",
                color=Color.Gray,
                fontSize=13.sp
            )
        }
    }
}
