package com.me.moneytracker.ui.addexpense

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.me.moneytracker.data.Category
import com.me.moneytracker.ui.home.ruledBackground
import com.me.moneytracker.ui.theme.AmountLarge
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
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun AddExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddExpenseViewModel = koinViewModel()
) {
    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()

    AddExpenseContent(
        expenseCategories = expenseCategories,
        incomeCategories = incomeCategories,
        onNavigateBack = onNavigateBack,
        onAddExpense = { amount, categoryId, dateMillis, note, isIncome ->
            viewModel.addExpense(amount, categoryId, dateMillis, note, isIncome, onNavigateBack)
        },
        onAddCustomCategory = { name, isIncome, callback ->
            viewModel.addCustomCategory(name, isIncome, callback)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddExpenseContent(
    expenseCategories: List<Category>,
    incomeCategories: List<Category>,
    onNavigateBack: () -> Unit,
    onAddExpense: (Double, Long, Long, String?, Boolean) -> Unit,
    onAddCustomCategory: (String, Boolean, (Category) -> Unit) -> Unit
) {
    val context = LocalContext.current

    var isIncome by remember { mutableStateOf(false) }
    var amountInput by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var noteInput by remember { mutableStateOf("") }

    // Track time separately so we can display & merge it precisely
    val nowCal = remember { Calendar.getInstance() }
    var selectedHour   by remember { mutableStateOf(nowCal.get(Calendar.HOUR_OF_DAY)) }
    var selectedMinute by remember { mutableStateOf(nowCal.get(Calendar.MINUTE)) }

    // Custom Category adding states
    var isAddingCustomCategory by remember { mutableStateOf(false) }
    var customCategoryName by remember { mutableStateOf("") }

    val formattedDate = remember(selectedDate) {
        SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(selectedDate))
    }
    val formattedTime = remember(selectedHour, selectedMinute) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, selectedHour)
            set(Calendar.MINUTE, selectedMinute)
        }
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.time)
    }

    val activeCategories = if (isIncome) incomeCategories else expenseCategories
    val accentColor = if (isIncome) DeepForestIncome else LedgerRed

    // When toggling type, deselect category to avoid cross-type mismatch
    LaunchedEffect(isIncome) { selectedCategory = null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "New Entry",
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = InkPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back",
                            tint = InkPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = PaperBackground
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .ruledBackground(lineColor = Color(0xFFEBE3D3), spacing = 28.dp)
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // ── Expense / Income Toggle ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BrassDivider.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                    .background(CardSurface, RoundedCornerShape(6.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(false to "− EXPENSE", true to "+ INCOME").forEach { (income, label) ->
                    val selected = isIncome == income
                    val tabColor = if (income) DeepForestIncome else LedgerRed
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (selected) tabColor else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .clickable { isIncome = income }
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

            Spacer(modifier = Modifier.height(20.dp))

            // Amount Input Field
            Text(
                text = "AMOUNT",
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = InkPrimary.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = amountInput,
                onValueChange = { input ->
                    if (input.all { it.isDigit() || it == '.' }) {
                        // Allow only one decimal point
                        if (input.count { it == '.' } <= 1) {
                            amountInput = input
                        }
                    }
                },
                textStyle = AmountLarge.copy(color = accentColor, textAlign = TextAlign.Start),
                placeholder = {
                    Text(
                        text = "₹0.00",
                        style = AmountLarge,
                        color = accentColor.copy(alpha = 0.3f)
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                prefix = {
                    Text(
                        text = "₹ ",
                        style = AmountLarge,
                        color = accentColor
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrassDivider,
                    unfocusedBorderColor = BrassDivider.copy(alpha = 0.5f),
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Category Label & Stamp Selector Grid
            Text(
                text = "LEDGER CATEGORY",
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = InkPrimary.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            AnimatedVisibility(
                visible = activeCategories.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(300)) + expandVertically(animationSpec = tween(300)),
                exit = fadeOut(animationSpec = tween(300))
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    activeCategories.forEach { category ->
                        val isSelected = selectedCategory?.id == category.id
                        val rotation = remember(category.id) {
                            (category.name.hashCode() % 5 - 2f) * 1.2f
                        }
                        Box(
                            modifier = Modifier
                                .graphicsLayer { rotationZ = rotation }
                                .border(
                                    width = if (isSelected) 1.8.dp else 0.8.dp,
                                    color = if (isSelected) accentColor else InkPrimary.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(2.dp)
                                )
                                .background(if (isSelected) accentColor.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { selectedCategory = category }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = category.name.uppercase(Locale.getDefault()),
                                fontFamily = IBMPlexMono,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                color = if (isSelected) accentColor else InkPrimary
                            )
                        }
                    }

                    // Inline custom category trigger badge
                    if (!isAddingCustomCategory) {
                        Box(
                            modifier = Modifier
                                .border(
                                    width = 0.8.dp,
                                    color = BrassDivider,
                                    shape = RoundedCornerShape(2.dp)
                                )
                                .clickable { isAddingCustomCategory = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "+ ADD CUSTOM",
                                fontFamily = IBMPlexMono,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = BrassDivider
                            )
                        }
                    }
                }
            }

            // Inline Custom Category Input Form
            if (isAddingCustomCategory) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = customCategoryName,
                        onValueChange = { customCategoryName = it },
                        placeholder = {
                            Text(
                                "Enter Category Name",
                                fontFamily = IBMPlexSans,
                                fontSize = 14.sp
                            )
                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrassDivider,
                            unfocusedBorderColor = BrassDivider.copy(alpha = 0.5f),
                            focusedContainerColor = CardSurface,
                            unfocusedContainerColor = CardSurface
                        ),
                        singleLine = true,
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (customCategoryName.isNotBlank()) {
                                onAddCustomCategory(customCategoryName, isIncome) { newCat ->
                                    selectedCategory = newCat
                                    customCategoryName = ""
                                    isAddingCustomCategory = false
                                }
                            }
                        },
                        modifier = Modifier
                            .border(1.dp, BrassDivider, RoundedCornerShape(4.dp))
                            .background(CardSurface)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Confirm Category",
                            tint = BrassDivider
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Date Picker Field
            Text(
                text = "LEDGER DATE",
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = InkPrimary.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.8.dp, BrassDivider.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .background(CardSurface)
                    .clickable {
                        val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
                        val dialog = DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val newCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                    // Preserve the currently selected hour/minute
                                    set(Calendar.HOUR_OF_DAY, selectedHour)
                                    set(Calendar.MINUTE, selectedMinute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                selectedDate = newCal.timeInMillis
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        dialog.datePicker.maxDate = System.currentTimeMillis()
                        dialog.show()
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date",
                    tint = BrassDivider
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = formattedDate,
                    fontFamily = IBMPlexMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = InkPrimary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Time Picker Field ────────────────────────────────────────────
            Text(
                text = "LEDGER TIME",
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = InkPrimary.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.8.dp, BrassDivider.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                    .background(CardSurface)
                    .clickable {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                selectedHour   = hour
                                selectedMinute = minute
                                // Rebuild selectedDate with the new time
                                val cal = Calendar.getInstance().apply {
                                    timeInMillis = selectedDate
                                    set(Calendar.HOUR_OF_DAY, hour)
                                    set(Calendar.MINUTE, minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                selectedDate = cal.timeInMillis
                            },
                            selectedHour,
                            selectedMinute,
                            false // 12-hour format
                        ).show()
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Time",
                    tint = BrassDivider
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = formattedTime,
                    fontFamily = IBMPlexMono,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = InkPrimary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Optional Note Field
            Text(
                text = "NOTE (OPTIONAL)",
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = InkPrimary.copy(alpha = 0.6f),
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = noteInput,
                onValueChange = { noteInput = it },
                placeholder = {
                    Text(
                        "Add a short handwritten memo...",
                        fontFamily = IBMPlexSans,
                        fontSize = 14.sp,
                        fontStyle = FontStyle.Italic,
                        color = InkPrimary.copy(alpha = 0.4f)
                    )
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = BrassDivider,
                    unfocusedBorderColor = BrassDivider.copy(alpha = 0.5f),
                    focusedContainerColor = CardSurface,
                    unfocusedContainerColor = CardSurface
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(36.dp))

            // Record Entry Button: Styled like a bold ink stamp (thick border)
            Button(
                onClick = {
                    val amount = amountInput.toDoubleOrNull() ?: 0.0
                    val category = selectedCategory
                    if (amount > 0 && category != null) {
                        // Merge selected date + time into final millis
                        val finalMillis = Calendar.getInstance().apply {
                            timeInMillis = selectedDate
                            set(Calendar.HOUR_OF_DAY, selectedHour)
                            set(Calendar.MINUTE, selectedMinute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis
                        onAddExpense(amount, category.id, finalMillis, noteInput, isIncome)
                    }
                },
                enabled = amountInput.toDoubleOrNull() != null && selectedCategory != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = accentColor,
                    disabledContainerColor = accentColor.copy(alpha = 0.3f),
                    contentColor = CardSurface,
                    disabledContentColor = CardSurface.copy(alpha = 0.6f)
                ),
                shape = RoundedCornerShape(4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer { rotationZ = 0.5f }
                    .border(1.5.dp, BrassDivider, RoundedCornerShape(4.dp))
            ) {
                Text(
                    text = if (isIncome) "RECORD INCOME" else "RECORD EXPENSE",
                    fontFamily = Fraunces,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(vertical = 6.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddExpenseScreenPreview() {
    val mockExpenseCategories = listOf(
        Category(id = 1, name = "Food", isDefault = true, isIncome = false),
        Category(id = 2, name = "Transport", isDefault = true, isIncome = false)
    )
    val mockIncomeCategories = listOf(
        Category(id = 3, name = "Salary", isDefault = true, isIncome = true),
        Category(id = 4, name = "Business", isDefault = true, isIncome = true)
    )
    com.me.moneytracker.ui.theme.LedgerTheme {
        AddExpenseContent(
            expenseCategories = mockExpenseCategories,
            incomeCategories = mockIncomeCategories,
            onNavigateBack = {},
            onAddExpense = { _, _, _, _, _ -> },
            onAddCustomCategory = { _, _, _ -> }
        )
    }
}
