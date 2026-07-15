package com.me.moneytracker.ui.credit.components

import androidx.compose.ui.graphics.Color

data class CardColorScheme(
    val name: String,
    val start: Color,
    val end: Color
)

val CardColorSchemes = listOf(
    CardColorScheme("Ocean",    Color(0xFF2C3E50), Color(0xFF3498DB)),
    CardColorScheme("Emerald",  Color(0xFF134E5E), Color(0xFF71B280)),
    CardColorScheme("Violet",   Color(0xFF4A00E0), Color(0xFF8E2DE2)),
    CardColorScheme("Sunset",   Color(0xFFf7971e), Color(0xFFffd200)),
    CardColorScheme("Rose",     Color(0xFF94716B), Color(0xFFb79891)),
    CardColorScheme("Midnight", Color(0xFF232526), Color(0xFF414345)),
    CardColorScheme("Gold",     Color(0xFFf2994a), Color(0xFFf2c94c)),
    CardColorScheme("Forest",   Color(0xFF11998e), Color(0xFF38ef7d)),
    CardColorScheme("Cherry",   Color(0xFFc0392b), Color(0xFF922B21)),
    CardColorScheme("Lavender", Color(0xFF6A3093), Color(0xFFA044FF)),
)
