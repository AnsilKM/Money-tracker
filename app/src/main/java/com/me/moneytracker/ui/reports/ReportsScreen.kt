package com.mee.moneytracker.ui.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mee.moneytracker.ui.home.ruledBackground
import com.mee.moneytracker.ui.theme.AmountLarge
import com.mee.moneytracker.ui.theme.AmountMedium
import com.mee.moneytracker.ui.theme.BrassDivider
import com.mee.moneytracker.ui.theme.CardSurface
import com.mee.moneytracker.ui.theme.Fraunces
import com.mee.moneytracker.ui.theme.IBMPlexMono
import com.mee.moneytracker.ui.theme.IBMPlexSans
import com.mee.moneytracker.ui.theme.InkPrimary
import com.mee.moneytracker.ui.theme.LedgerRed
import com.mee.moneytracker.ui.theme.DeepForestIncome
import com.mee.moneytracker.ui.theme.PaperBackground
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import org.koin.androidx.compose.koinViewModel
import java.util.Locale
import com.mee.moneytracker.data.ExpenseWithCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Calendar
import android.app.DatePickerDialog
import android.os.Build
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@Composable
fun ReportsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToCredits: () -> Unit = {},
    viewModel: ReportsViewModel = koinViewModel()
) {
    val tabIndex by viewModel.tabIndex.collectAsState()
    val dateLabel by viewModel.dateLabel.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()
    val totalSpend by viewModel.totalSpend.collectAsState()
    val breakdown by viewModel.categoryBreakdown.collectAsState()
    val expenses by viewModel.expenses.collectAsState()
    val weeklyDailyTotals by viewModel.weeklyDailyTotals.collectAsState()
    val monthlyWeeklyTotals by viewModel.monthlyWeeklyTotals.collectAsState()
    val showIncome by viewModel.showIncome.collectAsState()
    val isNextPeriodEnabled by viewModel.isNextPeriodEnabled.collectAsState()

    ReportsContent(
        tabIndex = tabIndex,
        dateLabel = dateLabel,
        currentDate = currentDate,
        totalSpend = totalSpend,
        breakdown = breakdown,
        expenses = expenses,
        weeklyDailyTotals = weeklyDailyTotals,
        monthlyWeeklyTotals = monthlyWeeklyTotals,
        showIncome = showIncome,
        isNextPeriodEnabled = isNextPeriodEnabled,
        onTabSelected = { viewModel.setTab(it) },
        onNavigatePeriod = { viewModel.navigatePeriod(it) },
        onSelectDate = { year, month, day -> viewModel.selectDate(year, month, day) },
        onNavigateToDaily = { viewModel.selectDayAndNavigate(it) },
        onNavigateToWeekly = { viewModel.selectWeekAndNavigate(it) },
        onToggleIncome = { viewModel.toggleIncomeMode(it) },
        onNavigateBack = onNavigateBack,
        onNavigateToHome = onNavigateToHome,
        onNavigateToCredits = onNavigateToCredits
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsContent(
    tabIndex: Int,
    dateLabel: String,
    currentDate: Calendar,
    totalSpend: Double,
    breakdown: List<CategoryBreakdown>,
    expenses: List<ExpenseWithCategory>,
    weeklyDailyTotals: List<DailyTotal>,
    monthlyWeeklyTotals: List<WeeklyTotal>,
    showIncome: Boolean,
    isNextPeriodEnabled: Boolean = true,
    onTabSelected: (Int) -> Unit,
    onNavigatePeriod: (Int) -> Unit,
    onSelectDate: (Int, Int, Int) -> Unit,
    onNavigateToDaily: (Calendar) -> Unit,
    onNavigateToWeekly: (Calendar) -> Unit,
    onToggleIncome: (Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToCredits: () -> Unit = {}
) {
    val context = LocalContext.current
    val accentColor = if (showIncome) DeepForestIncome else LedgerRed
    val totalLabel = if (showIncome) "TOTAL INCOME" else "TOTAL SPEND"
    var showMonthPicker by remember { mutableStateOf(false) }

    val lazyListState = rememberLazyListState()
    var isNavBarVisible by remember { mutableStateOf(true) }
    var prevIndex by remember { mutableStateOf(0) }
    var prevOffset by remember { mutableStateOf(0) }

    LaunchedEffect(lazyListState.firstVisibleItemIndex, lazyListState.firstVisibleItemScrollOffset) {
        val currentIndex = lazyListState.firstVisibleItemIndex
        val currentOffset = lazyListState.firstVisibleItemScrollOffset
        
        if (currentIndex > prevIndex) {
            isNavBarVisible = false
        } else if (currentIndex < prevIndex) {
            isNavBarVisible = true
        } else {
            if (currentOffset > prevOffset + 5) {
                isNavBarVisible = false
            } else if (currentOffset < prevOffset - 5) {
                isNavBarVisible = true
            }
        }
        
        prevIndex = currentIndex
        prevOffset = currentOffset
    }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            initialDate = currentDate,
            onDismissRequest = { showMonthPicker = false },
            onDateSelected = { year, month ->
                onSelectDate(year, month, 1)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Reports",
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = InkPrimary
                    )
                },
                navigationIcon = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = PaperBackground
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                        end = innerPadding.calculateEndPadding(LayoutDirection.Ltr)
                    )
                    .ruledBackground(lineColor = Color(0xFFEBE3D3), spacing = 28.dp)
            ) {
            // Custom bahi-khata styled tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperBackground)
                    .border(width = 0.5.dp, color = BrassDivider.copy(alpha = 0.3f)),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Daily", "Weekly", "Monthly").forEachIndexed { index, label ->
                    val isSelected = tabIndex == index
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onTabSelected(index) }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label.uppercase(Locale.getDefault()),
                            fontFamily = Fraunces,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isSelected) InkPrimary else InkPrimary.copy(alpha = 0.4f),
                            letterSpacing = 1.sp
                        )
                        if (isSelected) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(2.dp)
                                    .background(BrassDivider)
                            )
                        }
                    }
                }
            }

            // ── Expense / Income Toggle ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 4.dp)
                    .border(1.dp, BrassDivider.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .background(CardSurface, RoundedCornerShape(6.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(false to "− EXPENSES", true to "+ INCOME").forEach { (income, label) ->
                    val selected = showIncome == income
                    val tabColor = if (income) DeepForestIncome else LedgerRed
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (selected) tabColor else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { onToggleIncome(income) }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (selected) CardSurface else InkPrimary.copy(alpha = 0.55f),
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }

            // Period Navigation (Prev / Next Arrows + Date Display)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp, horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigatePeriod(-1) }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Period",
                        tint = InkPrimary
                    )
                }

                Row(
                    modifier = if (tabIndex == 1) {
                        Modifier.weight(1f)
                    } else {
                        Modifier
                            .weight(1f)
                            .clickable {
                                if (tabIndex == 2) {
                                    showMonthPicker = true
                                } else {
                                    val dialog = DatePickerDialog(
                                        context,
                                        { _, year, month, dayOfMonth ->
                                            onSelectDate(year, month, dayOfMonth)
                                        },
                                        currentDate.get(Calendar.YEAR),
                                        currentDate.get(Calendar.MONTH),
                                        currentDate.get(Calendar.DAY_OF_MONTH)
                                    )
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                        dialog.datePicker.maxDate = System.currentTimeMillis()
                                    }
                                    dialog.show()
                                }
                            }
                    },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dateLabel,
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Bold,
                        fontStyle = FontStyle.Italic,
                        fontSize = 18.sp,
                        color = InkPrimary,
                        textAlign = TextAlign.Center
                    )
                    if (tabIndex != 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Select Specific Date",
                            tint = BrassDivider,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                IconButton(
                    onClick = { onNavigatePeriod(1) },
                    enabled = isNextPeriodEnabled
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Period",
                        tint = if (isNextPeriodEnabled) InkPrimary else InkPrimary.copy(alpha = 0.25f)
                    )
                }
            }

            // Total Spend/Income Box
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = totalLabel,
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = InkPrimary.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = String.format(Locale.getDefault(), "₹%,.2f", totalSpend),
                    style = AmountLarge,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (breakdown.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No entries recorded in this moneytracker period.",
                        fontFamily = Fraunces,
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.sp,
                        color = InkPrimary.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    // Item 1: Donut chart + legend
                    item {
                        DonutChart(
                            breakdown = breakdown,
                            totalSpend = totalSpend,
                            accentColor = accentColor,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Item 2: Breakdown Title
                    item {
                        Text(
                            text = "CATEGORY BREAKDOWN",
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = InkPrimary.copy(alpha = 0.6f),
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = BrassDivider, thickness = 1.dp)
                    }

                    // Item List: Category percentage breakdowns
                    items(breakdown) { item ->
                        CategoryBreakdownRow(item = item, accentColor = accentColor)
                        HorizontalDivider(color = Color(0xFFEBE3D3), thickness = 1.dp)
                    }

                    // Item 3: Title for Recorded Entries / Weekly Daily totals / Monthly Weekly totals
                    if (tabIndex == 0) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "RECORDED ENTRIES",
                                fontFamily = IBMPlexSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = InkPrimary.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = BrassDivider, thickness = 1.dp)
                        }

                        // Item List: Actual entries in the report period
                        items(expenses) { item ->
                            ReportExpenseRowItem(item = item, accentColor = accentColor)
                            HorizontalDivider(color = Color(0xFFEBE3D3), thickness = 1.dp)
                        }
                    } else if (tabIndex == 1) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "DAILY BREAKDOWN",
                                fontFamily = IBMPlexSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = InkPrimary.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = BrassDivider, thickness = 1.dp)
                        }

                        items(weeklyDailyTotals) { item ->
                            DailyTotalRowItem(
                                item = item,
                                accentColor = accentColor,
                                onClick = { onNavigateToDaily(item.calendar) }
                            )
                            HorizontalDivider(color = Color(0xFFEBE3D3), thickness = 1.dp)
                        }
                    } else if (tabIndex == 2) {
                        item {
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = "WEEKLY BREAKDOWN",
                                fontFamily = IBMPlexSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = InkPrimary.copy(alpha = 0.6f),
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = BrassDivider, thickness = 1.dp)
                        }

                        items(monthlyWeeklyTotals) { item ->
                            WeeklyTotalRowItem(
                                item = item,
                                accentColor = accentColor,
                                onClick = { onNavigateToWeekly(item.calendar) }
                            )
                            HorizontalDivider(color = Color(0xFFEBE3D3), thickness = 1.dp)
                        }
                    }
                }
            }
        }
        
        AnimatedVisibility(
            visible = isNavBarVisible,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            com.mee.moneytracker.ui.components.FloatingNavBar(
                currentRoute = "reports",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "credit_list" -> onNavigateToCredits()
                    }
                }
            )
        }
    }
}
}

// Chart palette — neutral distinct hues separate from LedgerRed / DeepForestIncome
private val chartBarColors = listOf(
    Color(0xFF3A7BD5), // Cobalt Blue
    Color(0xFF9B59B6), // Amethyst Purple
    Color(0xFFE67E22), // Tangerine Orange
    Color(0xFF16A085), // Persian Teal
    Color(0xFFF39C12), // Sunflower Yellow
    Color(0xFF2980B9), // Belize Blue
    Color(0xFF8E44AD), // Wisteria Violet
    Color(0xFFD35400), // Pumpkin
    Color(0xFF1ABC9C), // Turquoise
    Color(0xFF2C3E50), // Midnight Blue
)

@Composable
fun DonutChart(
    breakdown: List<CategoryBreakdown>,
    totalSpend: Double,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    if (breakdown.isEmpty()) return

    val density = LocalDensity.current
    val centerLabelSizePx  = with(density) { 13.sp.toPx() }
    val centerValueSizePx  = with(density) { 17.sp.toPx() }
    val inkArgb            = InkPrimary.toArgb()
    val surfaceArgb        = CardSurface.toArgb()
    val accentArgb         = accentColor.toArgb()
    val totalAmount        = breakdown.sumOf { it.amount }.takeIf { it > 0 } ?: 1.0

    Row(
        modifier = modifier
            .background(CardSurface, RoundedCornerShape(8.dp))
            .border(0.8.dp, BrassDivider.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // ── Donut arc canvas ────────────────────────────────────────────────
        Canvas(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f)
        ) {
            val strokeWidth = size.minDimension * 0.22f
            val radius      = (size.minDimension - strokeWidth) / 2f
            val cx          = size.width  / 2f
            val cy          = size.height / 2f
            val gapDeg      = 3f               // gap between slices in degrees
            val rect        = android.graphics.RectF(
                cx - radius, cy - radius,
                cx + radius, cy + radius
            )

            var startAngle = -90f
            breakdown.forEachIndexed { index, item ->
                val sweep = ((item.amount / totalAmount) * 360f).toFloat() - gapDeg
                drawIntoCanvas { canvas ->
                    val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                        style  = android.graphics.Paint.Style.STROKE
                        this.strokeWidth = strokeWidth
                        strokeCap = android.graphics.Paint.Cap.BUTT
                        color = chartBarColors[index % chartBarColors.size].toArgb()
                    }
                    canvas.nativeCanvas.drawArc(rect, startAngle, sweep, false, paint)
                }
                startAngle += sweep + gapDeg
            }

            // ── Center hole label ────────────────────────────────────────────
            drawIntoCanvas { canvas ->
                val labelPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color    = inkArgb
                    textSize = centerLabelSizePx
                    textAlign= android.graphics.Paint.Align.CENTER
                }
                val valuePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color     = accentArgb
                    textSize  = centerValueSizePx
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface  = android.graphics.Typeface.DEFAULT_BOLD
                }
                canvas.nativeCanvas.drawText(
                    "TOTAL",
                    cx, cy - centerValueSizePx * 0.6f,
                    labelPaint
                )
                canvas.nativeCanvas.drawText(
                    "₹${formatCompact(totalSpend)}",
                    cx, cy + centerValueSizePx * 0.7f,
                    valuePaint
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // ── Legend ────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            breakdown.forEachIndexed { index, item ->
                val color = chartBarColors[index % chartBarColors.size]
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(color, RoundedCornerShape(2.dp))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = item.categoryName,
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = InkPrimary
                        )
                        Text(
                            text = "₹${formatCompact(item.amount)}  (${String.format("%.1f", item.percentage)}%)",
                            fontFamily = IBMPlexMono,
                            fontSize = 10.sp,
                            color = InkPrimary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
    }
}

private fun formatCompact(amount: Double): String {
    return when {
        amount >= 100_000 -> String.format("%.1fL", amount / 100_000)
        amount >= 1_000   -> String.format("%.1fk", amount / 1_000)
        else              -> String.format("%.0f", amount)
    }
}

@Composable
fun CategoryBreakdownRow(item: CategoryBreakdown, accentColor: Color = LedgerRed) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
            .padding(vertical = 10.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Name Stamp — takes available space, ellipsizes if needed
        Box(
            modifier = Modifier
                .weight(1f)
                .graphicsLayer { rotationZ = -1f }
                .border(
                    width = 0.8.dp,
                    color = InkPrimary.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(2.dp)
                )
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(
                text = item.categoryName.uppercase(Locale.getDefault()),
                fontFamily = IBMPlexMono,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = InkPrimary,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }

        // Fixed-width right section: percentage + amount
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 8.dp)
        ) {
            // Percentage — fixed narrow width
            Text(
                text = String.format(Locale.getDefault(), "%.1f%%", item.percentage),
                fontFamily = IBMPlexMono,
                fontSize = 11.sp,
                color = InkPrimary.copy(alpha = 0.6f),
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(10.dp))
            // Amount — compact format to save space
            Text(
                text = "₹${formatCompact(item.amount)}",
                fontFamily = IBMPlexMono,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = accentColor,
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ReportExpenseRowItem(
    item: ExpenseWithCategory,
    accentColor: Color
) {
    val dateTimeFormat = remember(item.expense.dateMillis) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    }
    val formattedDateTime = dateTimeFormat.format(Date(item.expense.dateMillis))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical indicator bar matching type color
        Box(
            modifier = Modifier
                .height(36.dp)
                .width(2.dp)
                .background(accentColor)
        )
        
        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Stamp
                Box(
                    modifier = Modifier
                        .graphicsLayer { rotationZ = -1f }
                        .border(
                            width = 0.8.dp,
                            color = InkPrimary.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(2.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.category.name.uppercase(Locale.getDefault()),
                        fontFamily = IBMPlexMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = InkPrimary,
                        letterSpacing = 0.5.sp
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = formattedDateTime,
                    fontFamily = IBMPlexMono,
                    fontSize = 11.sp,
                    color = InkPrimary.copy(alpha = 0.4f)
                )
            }
            
            if (!item.expense.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = item.expense.note,
                    fontFamily = IBMPlexSans,
                    fontSize = 14.sp,
                    color = InkPrimary.copy(alpha = 0.8f)
                )
            }
        }

        // Amount
        val prefix = if (item.expense.isIncome) "+" else "-"
        Text(
            text = String.format(Locale.getDefault(), "%s₹%,.2f", prefix, item.expense.amount),
            style = AmountMedium,
            color = accentColor,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
fun DailyTotalRowItem(
    item: DailyTotal,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = item.dayName.uppercase(Locale.getDefault()),
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = InkPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.dateLabel,
                fontFamily = IBMPlexMono,
                fontSize = 11.sp,
                color = InkPrimary.copy(alpha = 0.5f)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = String.format(Locale.getDefault(), "₹%,.2f", item.amount),
                style = AmountMedium,
                color = if (item.amount > 0) accentColor else InkPrimary.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate to Daily View",
                tint = InkPrimary.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun WeeklyTotalRowItem(
    item: WeeklyTotal,
    accentColor: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
            .clickable { onClick() }
            .padding(vertical = 12.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = item.weekLabel,
            fontFamily = IBMPlexSans,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = InkPrimary,
            modifier = Modifier.weight(1f)
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = String.format(Locale.getDefault(), "₹%,.2f", item.amount),
                style = AmountMedium,
                color = if (item.amount > 0) accentColor else InkPrimary.copy(alpha = 0.3f),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Navigate to Weekly View",
                tint = InkPrimary.copy(alpha = 0.3f),
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun MonthYearPickerDialog(
    initialDate: Calendar,
    onDismissRequest: () -> Unit,
    onDateSelected: (year: Int, month: Int) -> Unit
) {
    var selectedYear by remember { mutableStateOf(initialDate.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableStateOf(initialDate.get(Calendar.MONTH)) } // 0-indexed
    
    val currentCal = Calendar.getInstance()
    val currentYear = currentCal.get(Calendar.YEAR)
    val currentMonth = currentCal.get(Calendar.MONTH)
    
    val months = listOf(
        "Jan", "Feb", "Mar", "Apr", "May", "Jun", 
        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
    )

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismissRequest,
        containerColor = CardSurface,
        shape = RoundedCornerShape(6.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedYear-- }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Year",
                        tint = InkPrimary
                    )
                }
                
                Text(
                    text = selectedYear.toString(),
                    fontFamily = Fraunces,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = InkPrimary
                )
                
                IconButton(
                    onClick = { selectedYear++ },
                    enabled = selectedYear < currentYear
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Year",
                        tint = if (selectedYear < currentYear) InkPrimary else InkPrimary.copy(alpha = 0.3f)
                    )
                }
            }
        },
        text = {
            Column {
                HorizontalDivider(color = BrassDivider, thickness = 1.dp)
                Spacer(modifier = Modifier.height(16.dp))
                // Grid of 12 months (3 columns x 4 rows)
                val chunkedMonths = months.chunked(3)
                chunkedMonths.forEachIndexed { rowIndex, rowMonths ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowMonths.forEachIndexed { colIndex, monthName ->
                            val monthVal = rowIndex * 3 + colIndex
                            val isSelected = selectedMonth == monthVal
                            val isFuture = selectedYear > currentYear || (selectedYear == currentYear && monthVal > currentMonth)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) BrassDivider else InkPrimary.copy(alpha = if (isFuture) 0.15f else 0.3f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .background(if (isSelected) BrassDivider.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable(enabled = !isFuture) { selectedMonth = monthVal }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = monthName.uppercase(Locale.getDefault()),
                                    fontFamily = IBMPlexMono,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp,
                                    color = if (isFuture) InkPrimary.copy(alpha = 0.25f) else InkPrimary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onDateSelected(selectedYear, selectedMonth)
                    onDismissRequest()
                }
            ) {
                Text(
                    text = "SELECT",
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Bold,
                    color = BrassDivider
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text(
                    text = "CANCEL",
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary.copy(alpha = 0.6f)
                )
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ReportsScreenPreview() {
    val mockBreakdown = listOf(
        CategoryBreakdown("Food", 4500.0, 52.9f),
        CategoryBreakdown("Bills", 2500.0, 29.4f),
        CategoryBreakdown("Transport", 1500.0, 17.6f)
    )
    com.mee.moneytracker.ui.theme.LedgerTheme {
        ReportsContent(
            tabIndex = 0,
            dateLabel = "14 July 2026",
            currentDate = Calendar.getInstance(),
            totalSpend = 8500.0,
            breakdown = mockBreakdown,
            expenses = emptyList(),
            weeklyDailyTotals = emptyList(),
            monthlyWeeklyTotals = emptyList(),
            showIncome = false,
            onTabSelected = {},
            onNavigatePeriod = {},
            onSelectDate = { _, _, _ -> },
            onNavigateToDaily = {},
            onNavigateToWeekly = {},
            onToggleIncome = {},
            onNavigateBack = {}
        )
    }
}
