package com.me.moneytracker.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.me.moneytracker.data.Category
import com.me.moneytracker.data.CategoryDao
import com.me.moneytracker.ui.theme.BrassDivider
import com.me.moneytracker.ui.theme.Fraunces
import com.me.moneytracker.ui.theme.InkPrimary
import com.me.moneytracker.ui.theme.PaperBackground
import kotlinx.coroutines.delay
import org.koin.compose.koinInject
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    categoryDao: CategoryDao? = null // Nullable for previews
) {
    val lineProgress = remember { Animatable(0f) }
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current
    val injectedDao = if (isPreview) null else koinInject<CategoryDao>()
    val dao = categoryDao ?: injectedDao

    LaunchedEffect(Unit) {
        if (!isPreview && dao != null) {
            // Seeding database categories on first launch
            val defaultExpenseCategories = listOf("Food", "Transport", "Bills", "Shopping", "Entertainment", "Health", "Others")
            defaultExpenseCategories.forEach { name ->
                dao.insertCategory(Category(name = name, isDefault = true, isIncome = false))
            }
            val defaultIncomeCategories = listOf("Salary", "Business", "Allowance", "Investments", "Others")
            defaultIncomeCategories.forEach { name ->
                dao.insertCategory(Category(name = name, isDefault = true, isIncome = true))
            }
        }

        // Animate the underline drawing itself over 900ms
        lineProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = LinearEasing)
        )
        
        // Wait a small buffer (total splash duration ~ 1.1s) and navigate
        if (!isPreview) {
            delay(200.milliseconds)
            onNavigateToHome()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PaperBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App icon: foreground vector on circular background (matches adaptive icon)
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .graphicsLayer { rotationZ = -2f }
                    .background(
                        color = androidx.compose.ui.graphics.Color(0xFFF7F2E7),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .border(
                        width = 1.5.dp,
                        color = BrassDivider.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(28.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = com.me.moneytracker.R.drawable.ic_launcher_foreground),
                    contentDescription = "App Icon",
                    modifier = Modifier.size(97.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Money Tracker",
                fontFamily = Fraunces,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                color = InkPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            // Underline Canvas drawing ink stroke
            Canvas(
                modifier = Modifier
                    .width(160.dp)
                    .height(16.dp)
            ) {
                val w = size.width
                val h = size.height
                val midY = h / 2f
                
                // Path forming a slightly curved handwritten ink stroke
                val path = Path().apply {
                    moveTo(0f, midY)
                    // Slight S-curve or downward quadratic stroke
                    quadraticTo(w / 2f, midY + 6.dp.toPx(), w, midY - 2.dp.toPx())
                }

                // Render the path incrementally from left to right using clipping
                clipRect(right = w * lineProgress.value) {
                    drawPath(
                        path = path,
                        color = BrassDivider,
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    com.me.moneytracker.ui.theme.LedgerTheme {
        SplashScreen(onNavigateToHome = {})
    }
}
