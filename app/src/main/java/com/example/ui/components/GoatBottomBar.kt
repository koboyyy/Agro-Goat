package com.example.ui.components.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun GoatBottomBar(
    onChat:()->Unit,
    onOrder:()->Unit
){
    Surface(
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ){

            OutlinedButton(
                onClick = onChat,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp,Color(0xFF2E7D32))
            ){
                Text("Chat Penjual", fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onOrder,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2E7D32)
                )
            ){
                Text("Pesan Sekarang", fontWeight = FontWeight.Bold)
            }
        }
    }
}
