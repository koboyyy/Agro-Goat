package com.agrogoat.app.ui.components.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.agrogoat.app.R
import com.agrogoat.app.data.GoatCategory
import com.agrogoat.app.data.GoatItem

@Composable
fun GoatDetailView(
    goat: GoatItem,
    onBack:()->Unit,
    onToggleFav:()->Unit,
    onChat:()->Unit,
    onOrder:()->Unit,
    modifier: Modifier = Modifier
){
    val scrollState = rememberScrollState()

    val imageRes = when(goat.category){
        GoatCategory.POTONG -> R.drawable.burawa
        GoatCategory.ETAWA -> R.drawable.etawa
        GoatCategory.PERAH -> R.drawable.kacang
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8F8F8))
    ){

        Column {

            GoatHeroImage(
                imageRes = imageRes,
                contentDescription = goat.name,
                isFavorite = goat.isFavorite,
                onBack = onBack,
                onFavorite = onToggleFav
            )

            GoatDetailCard {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(scrollState)
                        .padding(bottom = 140.dp)
                ) {

                    GoatDetailHeader(goat)

                    Spacer(Modifier.height(24.dp))

                    GoatInfoGrid(goat)

                    Spacer(Modifier.height(24.dp))

                    GoatDescription(goat)

                }

            }

        }

        Box(
            modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
        ){
            GoatBottomBar(
                onChat = onChat,
                onOrder = onOrder
            )
        }
    }
}
