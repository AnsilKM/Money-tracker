package com.me.moneytracker.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.me.moneytracker.data.Category
import com.me.moneytracker.data.Expense
import com.me.moneytracker.data.ExpenseWithCategory
import com.me.moneytracker.ui.theme.AmountLarge
import com.me.moneytracker.ui.theme.AmountMedium
import com.me.moneytracker.ui.theme.BrassDivider
import com.me.moneytracker.ui.theme.CardSurface
import com.me.moneytracker.ui.theme.Fraunces
import com.me.moneytracker.ui.theme.IBMPlexMono
import com.me.moneytracker.ui.theme.IBMPlexSans
import com.me.moneytracker.ui.theme.InkPrimary
import com.me.moneytracker.ui.theme.LedgerRed
import com.me.moneytracker.ui.theme.PaperBackground
import com.me.moneytracker.ui.theme.DeepForestIncome
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Custom modifier to draw subtle horizontal ruled lines spanning the screen background
fun Modifier.ruledBackground(lineColor: Color, spacing: Dp = 26.dp): Modifier = drawBehind {
    val spacingPx = spacing.toPx()
    var y = 0f
    while (y < size.height) {
        drawLine(
            color = lineColor,
            start = Offset(0f, y),
            end = Offset(size.width, y),
            strokeWidth = 1f
        )
        y += spacingPx
    }
}

@Composable
fun HomeScreen(
    onNavigateToReports: () -> Unit,
    onNavigateToCredits: () -> Unit,
    onAddExpenseClick: () -> Unit,
    viewModel: HomeViewModel = koinViewModel()
) {
    val expenses by viewModel.todayExpenses.collectAsState()
    val todayTotalIncome by viewModel.todayTotalIncome.collectAsState()
    val todayTotalExpense by viewModel.todayTotalExpense.collectAsState()
    val todayBalance by viewModel.todayBalance.collectAsState()

    HomeContent(
        expenses = expenses,
        todayTotalIncome = todayTotalIncome,
        todayTotalExpense = todayTotalExpense,
        todayBalance = todayBalance,
        onNavigateToReports = onNavigateToReports,
        onNavigateToCredits = onNavigateToCredits,
        onAddExpenseClick = onAddExpenseClick,
        onDeleteExpense = { viewModel.deleteExpense(it.expense) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    expenses: List<ExpenseWithCategory>,
    todayTotalIncome: Double,
    todayTotalExpense: Double,
    todayBalance: Double,
    onNavigateToReports: () -> Unit,
    onNavigateToCredits: () -> Unit,
    onAddExpenseClick: () -> Unit,
    onDeleteExpense: (ExpenseWithCategory) -> Unit
) {
    // 0 = All, 1 = Expense, 2 = Income
    var selectedTab by remember { mutableStateOf(0) }

    val filteredExpenses = remember(expenses, selectedTab) {
        when (selectedTab) {
            1 -> expenses.filter { !it.expense.isIncome }
            2 -> expenses.filter { it.expense.isIncome }
            else -> expenses
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Money Tracker",
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = InkPrimary
                    )
                },
        actions = {},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpenseClick,
                containerColor = BrassDivider,
                contentColor = PaperBackground,
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Expense Entry"
                )
            }
        },
        bottomBar = {
            com.me.moneytracker.ui.components.FloatingNavBar(
                currentRoute = "home",
                onNavigate = { route ->
                    when (route) {
                        "credit_list" -> onNavigateToCredits()
                        "reports" -> onNavigateToReports()
                    }
                }
            )
        },
        containerColor = PaperBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .ruledBackground(lineColor = Color(0xFFEBE3D3), spacing = 28.dp)
        ) {
            // Prominent Net Book Balance area with Income / Expenses columns
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "NET BOOK BALANCE",
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = InkPrimary.copy(alpha = 0.6f),
                    letterSpacing = 1.5.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                val balanceColor = if (todayBalance >= 0) DeepForestIncome else LedgerRed
                Text(
                    text = String.format(Locale.getDefault(), "₹%,.2f", todayBalance),
                    style = AmountLarge,
                    color = balanceColor
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Income and Expense columns side by side
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "INCOME (+)",
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = DeepForestIncome.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "₹%,.2f", todayTotalIncome),
                            style = AmountMedium,
                            color = DeepForestIncome
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "EXPENSES (-)",
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = LedgerRed.copy(alpha = 0.8f),
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "₹%,.2f", todayTotalExpense),
                            style = AmountMedium,
                            color = LedgerRed
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(10.dp))
                val currentDateStr = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
                Text(
                    text = currentDateStr,
                    fontFamily = Fraunces,
                    fontWeight = FontWeight.Medium,
                    fontStyle = FontStyle.Italic,
                    fontSize = 13.sp,
                    color = BrassDivider
                )
            }

            // Divider separating Summary from Rule list
            HorizontalDivider(
                color = BrassDivider,
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // ── All / Expense / Income tabs ──────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(PaperBackground),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                val tabs = listOf(
                    Triple(0, "ALL", BrassDivider),
                    Triple(1, "EXPENSE", LedgerRed),
                    Triple(2, "INCOME", DeepForestIncome)
                )
                tabs.forEach { (idx, label, color) ->
                    val selected = selectedTab == idx
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedTab = idx }
                            .padding(vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            fontFamily = IBMPlexSans,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (selected) color else InkPrimary.copy(alpha = 0.4f),
                            letterSpacing = 1.sp
                        )
                        if (selected) {
                            Spacer(modifier = Modifier.height(3.dp))
                            Box(
                                modifier = Modifier
                                    .width(32.dp)
                                    .height(2.dp)
                                    .background(color, RoundedCornerShape(1.dp))
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = Color(0xFFEBE3D3),
                thickness = 1.dp
            )

            // Today's Entry List (filtered by tab)
            if (filteredExpenses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val emptyMsg = when (selectedTab) {
                        1 -> "No expense entries for today."
                        2 -> "No income entries for today."
                        else -> "No entries recorded in today's moneytracker."
                    }
                    Text(
                        text = emptyMsg,
                        fontFamily = Fraunces,
                        fontStyle = FontStyle.Italic,
                        fontSize = 16.sp,
                        color = InkPrimary.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp)
                ) {
                    items(filteredExpenses, key = { it.expense.id }) { item ->
                        ExpenseRowItem(
                            item = item,
                            onDelete = { onDeleteExpense(item) }
                        )
                        // Ruled horizontal line separating elements
                        HorizontalDivider(
                            color = Color(0xFFEBE3D3),
                            thickness = 1.dp,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseRowItem(
    item: ExpenseWithCategory,
    onDelete: () -> Unit
) {
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(item.expense.dateMillis))

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Confirmation dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = CardSurface,
            shape = RoundedCornerShape(6.dp),
            title = {
                Text(
                    text = "Delete Entry?",
                    fontFamily = Fraunces,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = InkPrimary
                )
            },
            text = {
                Text(
                    text = "This will permanently remove this moneytracker entry. Are you sure?",
                    fontFamily = IBMPlexSans,
                    fontSize = 14.sp,
                    color = InkPrimary.copy(alpha = 0.75f)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text(
                        text = "DELETE",
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Bold,
                        color = LedgerRed
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(
                        text = "CANCEL",
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Bold,
                        color = BrassDivider
                    )
                }
            }
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min) // Ensures left border fits the full height
            .background(CardSurface)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Vertical rule as a left margin marker (Green for Income, Red for Expense)
        val markerColor = if (item.expense.isIncome) DeepForestIncome else LedgerRed
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(2.dp)
                .background(markerColor)
        )
        
        Spacer(modifier = Modifier.width(12.dp))

        // Content: stamp category & note/time
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Ink stamp category badge: slight rotation, thin outline, bold typography
                Box(
                    modifier = Modifier
                        .graphicsLayer { rotationZ = -1.5f } // Subtle slant for stamp effect
                        .border(
                            width = 0.8.dp,
                            color = InkPrimary.copy(alpha = 0.8f),
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
                    text = formattedTime,
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

        // Amount right-aligned in Tabular IBM Plex Mono (+/- prefix)
        val prefix = if (item.expense.isIncome) "+" else "-"
        val amountColor = if (item.expense.isIncome) DeepForestIncome else LedgerRed
        Text(
            text = String.format(Locale.getDefault(), "%s₹%,.2f", prefix, item.expense.amount),
            style = AmountMedium,
            color = amountColor,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        // Delete button
        IconButton(
            onClick = { showDeleteDialog = true },
            modifier = Modifier.padding(start = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete entry",
                tint = InkPrimary.copy(alpha = 0.3f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val mockCategories = listOf(
        Category(id = 1, name = "Food", isDefault = true),
        Category(id = 2, name = "Transport", isDefault = true),
        Category(id = 3, name = "Shopping", isDefault = true)
    )
    val mockExpenses = listOf(
        ExpenseWithCategory(
            expense = Expense(id = 1, amount = 150.0, categoryId = 1, dateMillis = System.currentTimeMillis(), note = "Dinner with friends"),
            category = mockCategories[0]
        ),
        ExpenseWithCategory(
            expense = Expense(id = 2, amount = 500.0, categoryId = 2, dateMillis = System.currentTimeMillis(), note = "Petrol refill"),
            category = mockCategories[1]
        ),
        ExpenseWithCategory(
            expense = Expense(id = 3, amount = 1200.0, categoryId = 3, dateMillis = System.currentTimeMillis(), note = "Shoes bought"),
            category = mockCategories[2]
        )
    )
    com.me.moneytracker.ui.theme.LedgerTheme {
        HomeContent(
            expenses = mockExpenses,
            todayTotalIncome = 5000.0,
            todayTotalExpense = 1850.0,
            todayBalance = 3150.0,
            onNavigateToReports = {},
            onNavigateToCredits = {},
            onAddExpenseClick = {},
            onDeleteExpense = {}
        )
    }
}
