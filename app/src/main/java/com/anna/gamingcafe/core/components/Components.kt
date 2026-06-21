package com.anna.gamingcafe.core.components

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anna.gamingcafe.core.theme.*

@Composable
fun AnnaCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        border = BorderStroke(1.dp, EdgeBorder),
        shape = RoundedCornerShape(14.dp),
        content = { Column(modifier = Modifier.padding(14.dp), content = content) }
    )
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(6.dp))
            .border(0.5.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text, color = color, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.5.sp)
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        color = TextSecondary,
        fontSize = 9.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun AnnaChip(label: String, selected: Boolean, onClick: () -> Unit, accentColor: Color = NeonCyan) {
    Box(
        modifier = Modifier
            .background(if (selected) accentColor.copy(alpha = 0.12f) else SurfaceDark, RoundedCornerShape(10.dp))
            .border(1.dp, if (selected) accentColor else EdgeBorder, RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = if (selected) accentColor else TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun LoadingScreen() {
    Box(Modifier.fillMaxSize().background(BackBg), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = NeonCyan, strokeWidth = 2.dp, modifier = Modifier.size(32.dp))
    }
}

fun formatRupees(amount: Double): String = "₹${String.format("%.0f", amount)}"
