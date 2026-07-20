package com.mee.moneytracker.ui.credit.components

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mee.moneytracker.data.CreditAccount
import com.mee.moneytracker.ui.theme.BrassDivider
import com.mee.moneytracker.ui.theme.CardSurface
import com.mee.moneytracker.ui.theme.DeepForestIncome
import com.mee.moneytracker.ui.theme.Fraunces
import com.mee.moneytracker.ui.theme.IBMPlexMono
import com.mee.moneytracker.ui.theme.IBMPlexSans
import com.mee.moneytracker.ui.theme.InkPrimary
import com.mee.moneytracker.ui.theme.LedgerRed
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun AddCreditCardDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, limit: Double?, billDay: Int?, dueDay: Int?, colorScheme: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var limitStr by remember { mutableStateOf("") }
    var billDayStr by remember { mutableStateOf("") }
    var dueDayStr by remember { mutableStateOf("") }
    var selectedColorScheme by remember { mutableIntStateOf(0) }

    val onDayChange: (String, (String) -> Unit) -> Unit = { input, setter ->
        val clean = input.filter { it.isDigit() }
        if (clean.isEmpty()) {
            setter("")
        } else {
            val num = clean.toIntOrNull()
            if (num != null && num in 1..31) {
                setter(clean)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        shape = RoundedCornerShape(6.dp),
        title = {
            Text(
                text = "New Credit Card",
                fontFamily = Fraunces,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = InkPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive Card Mockup Face
                val scheme = CardColorSchemes[selectedColorScheme]
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.586f)
                        .border(1.dp, BrassDivider.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(scheme.start, scheme.end)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name.ifBlank { "CARD NAME" }.uppercase(Locale.getDefault()),
                                fontFamily = Fraunces,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Box(
                                modifier = Modifier
                                    .size(28.dp, 20.dp)
                                    .background(Color(0xFFF39C12), RoundedCornerShape(4.dp))
                            )
                        }
                        
                        val displayLimit = if (limitStr.isNotBlank()) "LIMIT: ₹$limitStr" else "LIMIT: ₹XXXXXX"
                        Text(
                            text = displayLimit,
                            fontFamily = IBMPlexMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val displayBill = if (billDayStr.isNotBlank()) "BILL: Day $billDayStr" else "BILL: --"
                            val displayDue = if (dueDayStr.isNotBlank()) "DUE: Day $dueDayStr" else "DUE: --"
                            Text(
                                text = displayBill,
                                fontFamily = IBMPlexMono,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                            Text(
                                text = displayDue,
                                fontFamily = IBMPlexMono,
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // Text fields
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Card Name (e.g. HDFC Millennia)") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrassDivider,
                        unfocusedBorderColor = InkPrimary.copy(alpha = 0.3f),
                        focusedLabelColor = BrassDivider
                    )
                )

                OutlinedTextField(
                    value = limitStr,
                    onValueChange = { limitStr = it },
                    label = { Text("Credit Limit (Optional)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrassDivider,
                        unfocusedBorderColor = InkPrimary.copy(alpha = 0.3f),
                        focusedLabelColor = BrassDivider
                    )
                )

                // Day inputs for statement and due day
                OutlinedTextField(
                    value = billDayStr,
                    onValueChange = { onDayChange(it) { billDayStr = it } },
                    label = { Text("Statement/Bill Day of Month (1-31)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrassDivider,
                        unfocusedBorderColor = InkPrimary.copy(alpha = 0.3f),
                        focusedLabelColor = BrassDivider
                    )
                )

                OutlinedTextField(
                    value = dueDayStr,
                    onValueChange = { onDayChange(it) { dueDayStr = it } },
                    label = { Text("Payment Due Day of Month (1-31)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrassDivider,
                        unfocusedBorderColor = InkPrimary.copy(alpha = 0.3f),
                        focusedLabelColor = BrassDivider
                    )
                )

                // Color scheme picker
                Text(
                    text = "CARD COLOR",
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = InkPrimary.copy(alpha = 0.5f),
                    letterSpacing = 0.5.sp
                )
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(CardColorSchemes.size) { i ->
                        val s = CardColorSchemes[i]
                        val isSelected = selectedColorScheme == i
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) BrassDivider else Color.Transparent,
                                    shape = RoundedCornerShape(50)
                                )
                                .padding(if (isSelected) 3.dp else 0.dp)
                                .background(
                                    brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                        colors = listOf(s.start, s.end)
                                    ),
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { selectedColorScheme = i }
                        )
                    }
                }
            }
        },
        confirmButton = {
            val isEnabled = name.isNotBlank() && billDayStr.isNotBlank() && dueDayStr.isNotBlank()
            TextButton(
                onClick = {
                    if (isEnabled) {
                        onConfirm(
                            name,
                            limitStr.toDoubleOrNull(),
                            billDayStr.toIntOrNull(),
                            dueDayStr.toIntOrNull(),
                            selectedColorScheme
                        )
                    }
                },
                enabled = isEnabled
            ) {
                Text(
                    text = "CREATE",
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) BrassDivider else InkPrimary.copy(alpha = 0.3f)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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

@Composable
fun AddSinglePaymentDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, amount: Double, dueDate: Long?, type: String, note: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var noteStr by remember { mutableStateOf("") }
    var selectedDueDate by remember { mutableStateOf<Long?>(null) }
    var isLent by remember { mutableStateOf(true) } // true: Lent, false: Borrowed
    
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        shape = RoundedCornerShape(6.dp),
        title = {
            Text(
                text = "New Single Payment",
                fontFamily = Fraunces,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = InkPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive Receipt Mockup
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, InkPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                        .background(CardSurface)
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SINGLE PAYMENT RECEIPT",
                            fontFamily = IBMPlexMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = InkPrimary.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (isLent) "LENT" else "BORROWED",
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = if (isLent) DeepForestIncome else LedgerRed,
                            modifier = Modifier
                                .border(1.dp, if (isLent) DeepForestIncome else LedgerRed, RoundedCornerShape(2.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = BrassDivider, thickness = 1.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title.ifBlank { "PAYMENT TITLE" },
                            fontFamily = Fraunces,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = InkPrimary
                        )
                        Text(
                            text = if (amountStr.isNotBlank()) "₹$amountStr" else "₹0.00",
                            fontFamily = IBMPlexMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = InkPrimary
                        )
                    }
                    if (selectedDueDate != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Due: " + SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDueDate!!)),
                            fontFamily = IBMPlexMono,
                            fontSize = 11.sp,
                            color = LedgerRed.copy(alpha = 0.8f)
                        )
                    }
                }

                // Text fields
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Person Name") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrassDivider,
                        unfocusedBorderColor = InkPrimary.copy(alpha = 0.3f),
                        focusedLabelColor = BrassDivider
                    )
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("Initial Amount (₹)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrassDivider,
                        unfocusedBorderColor = InkPrimary.copy(alpha = 0.3f),
                        focusedLabelColor = BrassDivider
                    )
                )

                OutlinedTextField(
                    value = noteStr,
                    onValueChange = { noteStr = it },
                    label = { Text("Optional Note") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BrassDivider,
                        unfocusedBorderColor = InkPrimary.copy(alpha = 0.3f),
                        focusedLabelColor = BrassDivider
                    )
                )

                // Lent vs Borrowed Toggle
                Column {
                    Text(
                        text = "TRANSACTION DIRECTION",
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = InkPrimary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(true to "Lent (they owe you)", false to "Borrowed (you owe them)").forEach { (valState, label) ->
                            val isSelected = isLent == valState
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) BrassDivider else InkPrimary.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .background(if (isSelected) BrassDivider.copy(alpha = 0.15f) else Color.Transparent)
                                    .clickable { isLent = valState }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontFamily = IBMPlexSans,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 11.sp,
                                    color = InkPrimary
                                )
                            }
                        }
                    }
                }

                // Date picker for due date
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(0.5.dp, InkPrimary.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                        .clickable {
                            val now = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    val cal = Calendar.getInstance().apply {
                                        set(Calendar.YEAR, y)
                                        set(Calendar.MONTH, m)
                                        set(Calendar.DAY_OF_MONTH, d)
                                    }
                                    selectedDueDate = cal.timeInMillis
                                },
                                now.get(Calendar.YEAR),
                                now.get(Calendar.MONTH),
                                now.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Date",
                        tint = BrassDivider,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = selectedDueDate?.let {
                            "Target Due Date: " + SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(it))
                        } ?: "Select Due Date (Optional)",
                        fontFamily = IBMPlexSans,
                        fontSize = 14.sp,
                        color = InkPrimary
                    )
                }
            }
        },
        confirmButton = {
            val amount = amountStr.toDoubleOrNull()
            val isValid = title.isNotBlank() && amount != null && amount > 0
            TextButton(
                onClick = {
                    if (isValid) {
                        onConfirm(title,
                            amount, selectedDueDate, if (isLent) "LENT" else "BORROWED", noteStr.ifBlank { null })
                    }
                },
                enabled = isValid
            ) {
                Text(
                    text = "CREATE",
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Bold,
                    color = if (isValid) BrassDivider else InkPrimary.copy(alpha = 0.3f)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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

@Composable
fun AddLoanDialog(
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        principal: Double,
        dueDay: Int?,
        emi: Double,
        tenureMonths: Int,
        emiStartMonth: Int,
        emiStartYear: Int
    ) -> Unit
) {
    var source by remember { mutableStateOf("") }
    var principalStr by remember { mutableStateOf("") }
    var emiStr by remember { mutableStateOf("") }
    var tenureStr by remember { mutableStateOf("") }
    var dueDayStr by remember { mutableStateOf("") }

    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    val currentCal = Calendar.getInstance()
    var selectedMonth by remember { mutableIntStateOf(currentCal.get(Calendar.MONTH) + 1) }
    var selectedYear by remember { mutableIntStateOf(currentCal.get(Calendar.YEAR)) }

    val onDayChange: (String, (String) -> Unit) -> Unit = { input, setter ->
        val clean = input.filter { it.isDigit() }
        if (clean.isEmpty()) {
            setter("")
        } else {
            val num = clean.toIntOrNull()
            if (num != null && num in 1..31) {
                setter(clean)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        shape = RoundedCornerShape(6.dp),
        title = {
            Text(
                text = "New Borrowing / Loan",
                fontFamily = Fraunces,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = InkPrimary
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive passbook certificate mockup
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, BrassDivider.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                            .background(CardSurface)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "LOAN STATEMENT PREVIEW",
                            fontFamily = IBMPlexMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            color = InkPrimary.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        HorizontalDivider(color = BrassDivider, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = source.ifBlank { "LOAN SOURCE" },
                                    fontFamily = Fraunces,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = InkPrimary
                                )
                                if (tenureStr.isNotBlank()) {
                                    Text(
                                        text = "Tenure: $tenureStr months",
                                        fontFamily = IBMPlexSans,
                                        fontSize = 11.sp,
                                        color = InkPrimary.copy(alpha = 0.6f)
                                    )
                                }
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = if (principalStr.isNotBlank()) "₹$principalStr" else "₹0.00",
                                    fontFamily = IBMPlexMono,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = LedgerRed
                                )
                                if (emiStr.isNotBlank()) {
                                    Text(
                                        text = "EMI: ₹$emiStr/mo",
                                        fontFamily = IBMPlexSans,
                                        fontSize = 11.sp,
                                        color = InkPrimary.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }

                // Text fields
                item {
                    OutlinedTextField(
                        value = source,
                        onValueChange = { source = it },
                        label = { Text("Loan Source (e.g. SBI Bank, Car Loan)") },
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrassDivider,
                            unfocusedBorderColor = InkPrimary.copy(alpha = 0.3f),
                            focusedLabelColor = BrassDivider
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = principalStr,
                        onValueChange = { principalStr = it },
                        label = { Text("Borrowed Principal Amount (₹)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrassDivider,
                            unfocusedBorderColor = InkPrimary.copy(alpha = 0.3f),
                            focusedLabelColor = BrassDivider
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = emiStr,
                        onValueChange = { emiStr = it },
                        label = { Text("Monthly EMI Amount") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrassDivider,
                            unfocusedBorderColor = InkPrimary.copy(alpha = 0.3f),
                            focusedLabelColor = BrassDivider
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = tenureStr,
                        onValueChange = { tenureStr = it },
                        label = { Text("Loan Tenure in Months") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrassDivider,
                            unfocusedBorderColor = InkPrimary.copy(alpha = 0.3f),
                            focusedLabelColor = BrassDivider
                        )
                    )
                }

                item {
                    OutlinedTextField(
                        value = dueDayStr,
                        onValueChange = { onDayChange(it) { dueDayStr = it } },
                        label = { Text("Payment Due Day of Month (1-31)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrassDivider,
                            unfocusedBorderColor = InkPrimary.copy(alpha = 0.3f),
                            focusedLabelColor = BrassDivider
                        )
                    )
                }

                // Month selector
                item {
                    Text(
                        text = "EMI START MONTH",
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = InkPrimary.copy(alpha = 0.5f),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.lazy.LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(months.size) { index ->
                            val mName = months[index]
                            val mNum = index + 1
                            val isSelected = selectedMonth == mNum
                            Box(
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) BrassDivider else InkPrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .background(if (isSelected) BrassDivider.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable { selectedMonth = mNum }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = mName,
                                    fontFamily = IBMPlexSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) BrassDivider else InkPrimary
                                )
                            }
                        }
                    }
                }

                // Year selector
                item {
                    val years = listOf(currentCal.get(Calendar.YEAR) - 1, currentCal.get(Calendar.YEAR), currentCal.get(Calendar.YEAR) + 1, currentCal.get(Calendar.YEAR) + 2)
                    Text(
                        text = "EMI START YEAR",
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = InkPrimary.copy(alpha = 0.5f),
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        years.forEach { year ->
                            val isSelected = selectedYear == year
                            Box(
                                modifier = Modifier
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) BrassDivider else InkPrimary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .background(if (isSelected) BrassDivider.copy(alpha = 0.1f) else Color.Transparent)
                                    .clickable { selectedYear = year }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = year.toString(),
                                    fontFamily = IBMPlexSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) BrassDivider else InkPrimary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            val principal = principalStr.toDoubleOrNull()
            val emi = emiStr.toDoubleOrNull()
            val tenure = tenureStr.toIntOrNull()
            val dueDay = dueDayStr.toIntOrNull()
            val isEnabled = source.isNotBlank() && 
                            principal != null && principal > 0 && 
                            emi != null && emi > 0 && 
                            tenure != null && tenure > 0 && 
                            dueDay != null
            TextButton(
                onClick = {
                    if (isEnabled) {
                        onConfirm(
                            source,
                            principal,
                            dueDay,
                            emi,
                            tenure,
                            selectedMonth,
                            selectedYear
                        )
                    }
                },
                enabled = isEnabled
            ) {
                Text(
                    text = "CREATE",
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) BrassDivider else InkPrimary.copy(alpha = 0.3f)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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

@Composable
fun SettleSinglePaymentDialog(
    account: CreditAccount,
    balance: Double,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double) -> Unit
) {
    val maxAmount = abs(balance)
    var isPartial by remember { mutableStateOf(false) }
    var partialAmountStr by remember { mutableStateOf("") }
    
    val selectedAmount = if (isPartial) {
        partialAmountStr.toDoubleOrNull() ?: 0.0
    } else {
        maxAmount
    }
    
    val isEnabled = selectedAmount > 0.0 && selectedAmount <= maxAmount
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        shape = RoundedCornerShape(6.dp),
        title = {
            Text(
                text = "Record Settle/Payment",
                fontFamily = Fraunces,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = InkPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Outstanding: ₹%,.2f".format(maxAmount),
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = InkPrimary
                )
                
                // Select Option
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.dp,
                                color = if (!isPartial) BrassDivider else InkPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(if (!isPartial) BrassDivider.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { isPartial = false }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Fully Paid",
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (!isPartial) BrassDivider else InkPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .border(
                                width = 1.dp,
                                color = if (isPartial) BrassDivider else InkPrimary.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(if (isPartial) BrassDivider.copy(alpha = 0.1f) else Color.Transparent)
                            .clickable { isPartial = true }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Partial Payment",
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (isPartial) BrassDivider else InkPrimary
                        )
                    }
                }
                
                if (isPartial) {
                    OutlinedTextField(
                        value = partialAmountStr,
                        onValueChange = { input ->
                            // Allow digits and at most one decimal point
                            if (input.count { it == '.' } <= 1 && input.all { it.isDigit() || it == '.' }) {
                                partialAmountStr = input
                            }
                        },
                        label = { Text("Payment Amount (₹)") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = BrassDivider,
                            unfocusedBorderColor = InkPrimary.copy(alpha = 0.3f),
                            focusedLabelColor = BrassDivider
                        )
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (isEnabled) onConfirm(selectedAmount) },
                enabled = isEnabled
            ) {
                Text(
                    text = "CONFIRM",
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Bold,
                    color = if (isEnabled) BrassDivider else InkPrimary.copy(alpha = 0.3f)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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

@Composable
fun ConfirmDeleteAccountDialog(
    account: CreditAccount,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        shape = RoundedCornerShape(6.dp),
        title = {
            Text(
                text = "Delete Account",
                fontFamily = Fraunces,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = InkPrimary
            )
        },
        text = {
            Text(
                text = "Are you sure you want to delete ${account.name} and all its statements? This cannot be undone.",
                fontFamily = IBMPlexSans,
                fontSize = 15.sp,
                color = InkPrimary
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = "DELETE",
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Bold,
                    color = LedgerRed
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
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
