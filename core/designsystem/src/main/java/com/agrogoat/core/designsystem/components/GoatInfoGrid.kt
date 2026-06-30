package com.agrogoat.core.designsystem.components.detail

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.agrogoat.core.model.GoatItem

@Composable
fun GoatInfoGrid(
    goat: GoatItem,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            DetailItem(
                title = "Jenis",
                value = goat.category.name
            )

            DetailItem(
                title = "Berat",
                value = "${goat.weight} kg"
            )

            DetailItem(
                title = "Umur",
                value = "${goat.age} Tahun"
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            DetailItem(
                title = "Peternak",
                value = goat.sellerEmail
            )

            DetailItem(
                title = "Lokasi",
                value = goat.location
            )

            MiniMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            )
        }
    }
}
