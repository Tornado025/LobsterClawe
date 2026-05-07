package com.lobsterclawe.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lobsterclawe.network.Recipe
import com.lobsterclawe.ui.theme.*

@Composable
fun RecipeCard(recipe: Recipe, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, Gray200)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Teal, TealLight)
                        )
                    )
            ) {
                AsyncImage(
                    model = null, // In a real app, you'd have an image URL
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = recipe.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Gray900
            )
            Text(
                text = recipe.summary,
                fontSize = 13.sp,
                color = Gray500,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = TealLight,
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = "${recipe.cookTimeMinutes} min",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    fontSize = 11.sp,
                    color = TealText,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
