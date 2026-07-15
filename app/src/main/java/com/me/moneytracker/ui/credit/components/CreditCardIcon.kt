package com.me.moneytracker.ui.credit.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

val CreditCardIcon: ImageVector
    get() = ImageVector.Builder(
        name = "CreditCard",
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
            // Main card boundary
            moveTo(2f, 5f)
            lineTo(22f, 5f)
            lineTo(22f, 19f)
            lineTo(2f, 19f)
            close()
        }
        path(
            fill = SolidColor(Color.White)
        ) {
            // Magnetic stripe
            moveTo(2f, 8f)
            lineTo(22f, 8f)
            lineTo(22f, 11f)
            lineTo(2f, 11f)
            close()
        }
    }.build()
