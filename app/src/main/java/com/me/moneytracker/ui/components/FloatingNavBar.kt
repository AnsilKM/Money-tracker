package com.me.moneytracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.me.moneytracker.ui.theme.BrassDivider
import com.me.moneytracker.ui.theme.CardSurface
import com.me.moneytracker.ui.theme.IBMPlexSans
import com.me.moneytracker.ui.theme.InkPrimary

@Composable
fun FloatingNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp, start = 32.dp, end = 32.dp)
            .height(64.dp)
            .background(CardSurface, RoundedCornerShape(32.dp))
            .border(1.dp, BrassDivider.copy(alpha = 0.4f), RoundedCornerShape(32.dp))
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        NavBarItem(
            label = "Home",
            icon = HomeIcon,
            isSelected = currentRoute == "home",
            onClick = { onNavigate("home") },
            modifier = Modifier.weight(1f)
        )
        NavBarItem(
            label = "Credit",
            icon = CreditIcon,
            isSelected = currentRoute == "credit_list",
            onClick = { onNavigate("credit_list") },
            modifier = Modifier.weight(1f)
        )
        NavBarItem(
            label = "Reports",
            icon = ReportsIcon,
            isSelected = currentRoute == "reports",
            onClick = { onNavigate("reports") },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NavBarItem(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tintColor = if (isSelected) BrassDivider else InkPrimary.copy(alpha = 0.4f)
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tintColor,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontFamily = IBMPlexSans,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp,
            color = tintColor
        )
    }
}

private val HomeIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Home",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f
        ) {
            moveTo(12f, 3f)
            lineTo(20f, 10f)
            lineTo(20f, 21f)
            lineTo(14f, 21f)
            lineTo(14f, 14f)
            lineTo(10f, 14f)
            lineTo(10f, 21f)
            lineTo(4f, 21f)
            lineTo(4f, 10f)
            close()
        }
    }.build()

private val CreditIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Credit",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f
        ) {
            moveTo(2f, 5f)
            lineTo(22f, 5f)
            lineTo(22f, 19f)
            lineTo(2f, 19f)
            close()
        }
        path(
            fill = SolidColor(Color.White)
        ) {
            moveTo(2f, 8f)
            lineTo(22f, 8f)
            lineTo(22f, 10.5f)
            lineTo(2f, 10.5f)
            close()
        }
    }.build()

private val ReportsIcon: ImageVector
    get() = ImageVector.Builder(
        name = "Reports",
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply {
        path(
            fill = null,
            stroke = SolidColor(Color.White),
            strokeLineWidth = 2f
        ) {
            // Bar 1
            moveTo(4f, 20f)
            lineTo(4f, 12f)
            lineTo(8f, 12f)
            lineTo(8f, 20f)
            close()
            // Bar 2
            moveTo(10f, 20f)
            lineTo(10f, 6f)
            lineTo(14f, 6f)
            lineTo(14f, 20f)
            close()
            // Bar 3
            moveTo(16f, 20f)
            lineTo(16f, 14f)
            lineTo(20f, 14f)
            lineTo(20f, 20f)
            close()
        }
    }.build()
