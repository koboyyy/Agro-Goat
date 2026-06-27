package com.agrogoat.app.ui.components.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.agrogoat.app.ui.components.GoatImage

@Composable
fun GoatHeroImage(
    imageRes:Int,
    contentDescription:String,
    isFavorite:Boolean,
    onBack:()->Unit,
    onFavorite:()->Unit,
    modifier: Modifier = Modifier,
    imageUri: String? = null
){
    Box(
        modifier = modifier
            .fillMaxWidth()
            .fillMaxHeight(.42f)
    ){
        GoatImage(
            imageUri = imageUri,
            defaultImageRes = imageRes,
            contentDescription = contentDescription,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(20.dp)
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center
        ){
            Icon(Icons.Default.ArrowBack,null,tint=Color.Black)
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(20.dp)
                .size(46.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable(onClick = onFavorite),
            contentAlignment = Alignment.Center
        ){
            Icon(
                if(isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                null,
                tint = if(isFavorite) Color.Red else Color.Gray
            )
        }
    }
}
