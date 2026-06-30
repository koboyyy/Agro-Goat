package com.agrogoat.core.designsystem.components.detail

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp

@Composable
fun MiniMap(
    modifier: Modifier = Modifier
){
    Canvas(
        modifier=modifier
            .border(
                1.dp,
                Color(0xFFE5E7EB),
                RoundedCornerShape(14.dp)
            )
    ){
        drawRect(Color(0xFFF5F5F5))
        drawLine(Color.White, Offset(0f,size.height*.5f), Offset(size.width,size.height*.5f),8f)
        drawCircle(Color.Red,10f, Offset(size.width*.65f,size.height*.45f))
    }
}
