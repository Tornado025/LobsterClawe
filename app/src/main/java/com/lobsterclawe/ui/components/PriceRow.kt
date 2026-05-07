package com.lobsterclawe.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lobsterclawe.network.PriceResult
import com.lobsterclawe.ui.theme.*

@Composable
fun PriceRow(
    priceResult: PriceResult,
    onStoreClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = priceResult.item,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Gray900
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val minPrice = priceResult.prices.minOfOrNull { it.price_inr }
            priceResult.prices.forEach { storePrice ->
                val isBest = storePrice.price_inr == minPrice
                FilterChip(
                    selected = isBest,
                    onClick = { onStoreClick(storePrice.url) },
                    label = {
                        Text("${storePrice.store}: ₹${storePrice.price_inr}")
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = TealLight,
                        selectedLabelColor = TealText,
                        containerColor = White,
                        labelColor = Gray500
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isBest,
                        borderColor = Gray200,
                        selectedBorderColor = Teal
                    )
                )
            }
        }
    }
}
