package com.me.moneytracker.ui.credit

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.me.moneytracker.data.Category
import com.me.moneytracker.data.CreditAccount
import com.me.moneytracker.data.CreditTransaction
import com.me.moneytracker.ui.credit.components.ConfirmDeleteAccountDialog
import com.me.moneytracker.ui.home.ruledBackground
import com.me.moneytracker.ui.theme.AmountMedium
import com.me.moneytracker.ui.theme.BrassDivider
import com.me.moneytracker.ui.theme.CardSurface
import com.me.moneytracker.ui.theme.DeepForestIncome
import com.me.moneytracker.ui.theme.Fraunces
import com.me.moneytracker.ui.theme.IBMPlexMono
import com.me.moneytracker.ui.theme.IBMPlexSans
import com.me.moneytracker.ui.theme.InkPrimary
import com.me.moneytracker.ui.theme.LedgerRed
import com.me.moneytracker.ui.theme.PaperBackground
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditDetailsScreen(
    accountId: Long,
    onNavigateBack: () -> Unit,
    viewModel: CreditViewModel? = if (androidx.compose.ui.platform.LocalInspectionMode.current) null else koinViewModel()
) {
    LaunchedEffect(accountId) {
        viewModel?.selectAccount(accountId)
    }

    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current

    val account = if (isPreview || viewModel == null) {
        remember {
            CreditAccount(
                id = 1,
                name = "HDFC Card",
                type = "CREDIT_CARD",
                limitAmount = 100000.0,
                billDayOfMonth = 10,
                dueDayOfMonth = 25,
                colorScheme = 0
            )
        }
    } else {
        viewModel.selectedAccount.collectAsState().value
    }

    val transactions = if (isPreview || viewModel == null) {
        remember {
            val now = System.currentTimeMillis()
            listOf(
                CreditTransaction(id = 1, accountId = 1, amount = 15000.0, type = "RECEIVED", dateMillis = now - 2 * 24 * 3600 * 1000, note = "Online Shopping"),
                CreditTransaction(id = 2, accountId = 1, amount = 10000.0, type = "RECEIVED", dateMillis = now - 24 * 3600 * 1000, note = "Grocery Store"),
                CreditTransaction(id = 3, accountId = 1, amount = 5000.0, type = "GIVEN", dateMillis = now, note = "Part Payment")
            )
        }
    } else {
        viewModel.transactions.collectAsState().value
    }

    val categories = if (isPreview || viewModel == null) {
        remember {
            listOf(
                Category(id = 1, name = "Food", isDefault = true),
                Category(id = 2, name = "Transport", isDefault = true)
            )
        }
    } else {
        viewModel.categories.collectAsState().value
    }

    var showAddTxDialog by remember { mutableStateOf(false) }
    var dialogTxType by remember { mutableStateOf("RECEIVED") } // "RECEIVED" (charge/borrow) or "GIVEN" (pay/settle)
    var dialogPrefilledAmount by remember { mutableStateOf<Double?>(null) }
    var dialogPrefilledNote by remember { mutableStateOf("") }
    var cycleOffset by remember { mutableIntStateOf(0) }
    var dialogPrefilledDate by remember { mutableStateOf<Long?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDeleteTxConfirm by remember { mutableStateOf<CreditTransaction?>(null) }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = account?.name ?: "Account Details",
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
                            contentDescription = "Back",
                            tint = InkPrimary
                        )
                    }
                },
                actions = {
                    account?.let { acc ->
                        IconButton(onClick = {
                            showDeleteConfirm = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete Account",
                                tint = LedgerRed.copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        containerColor = PaperBackground
    ) { innerPadding ->
        account?.let { acc ->
            // Calculations
            val totalGiven = transactions.filter { it.type == "GIVEN" }.sumOf { it.amount }
            val totalReceived = transactions.filter { it.type == "RECEIVED" }.sumOf { it.amount }
            val netBalance = totalGiven - totalReceived

            // Calculate missed months for Loan
            val missedMonths = remember(transactions, acc) {
                val list = mutableListOf<Pair<Int, Long>>()
                val emi = acc.installmentAmount ?: 0.0
                val tenure = acc.tenureMonths ?: 0
                val startM = acc.emiStartMonth
                val startY = acc.emiStartYear
                val dueDay = acc.dueDayOfMonth ?: 1
                
                if (acc.type == "LOAN" && emi > 0.0 && tenure > 0 && startM != null && startY != null) {
                    val now = Calendar.getInstance()
                    for (i in 0 until tenure) {
                        var m = startM + i
                        var y = startY
                        while (m > 12) {
                            m -= 12
                            y += 1
                        }
                        val expectedCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, y)
                            set(Calendar.MONTH, m - 1)
                            set(Calendar.DAY_OF_MONTH, dueDay)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        // Check if expected date is in the past
                        if (expectedCal.before(now)) {
                            val hasPaid = transactions.any { tx ->
                                if (tx.type == "GIVEN") {
                                    val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
                                    txCal.get(Calendar.YEAR) == y && (txCal.get(Calendar.MONTH) + 1) == m
                                } else false
                            }
                            if (!hasPaid) {
                                list.add(Pair(i, expectedCal.timeInMillis))
                            }
                        }
                    }
                }
                list
            }

            val historyItems = remember(transactions, missedMonths) {
                val items = mutableListOf<HistoryItem>()
                transactions.forEach { items.add(HistoryItem.TransactionItem(it)) }
                missedMonths.forEach { items.add(HistoryItem.MissedPaymentItem(it.first, it.second)) }
                items.sortByDescending {
                    when (it) {
                        is HistoryItem.TransactionItem -> it.tx.dateMillis
                        is HistoryItem.MissedPaymentItem -> it.expectedDueDate
                    }
                }
                items
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .ruledBackground(Color(0xFFF1EADB))
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Top Card summary of balances and limits
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .border(1.dp, BrassDivider.copy(alpha = 0.5f), RoundedCornerShape(4.dp)),
                        colors = CardDefaults.cardColors(containerColor = CardSurface),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Net Balance
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "NET BALANCE",
                                    fontFamily = IBMPlexSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = InkPrimary.copy(alpha = 0.5f),
                                    letterSpacing = 1.sp
                                )
                                val dateStr = when {
                                    acc.type == "CREDIT_CARD" -> {
                                        val billPart = if (acc.billDayOfMonth != null) "Bill: Day ${acc.billDayOfMonth}" else ""
                                        val duePart = if (acc.dueDayOfMonth != null) "Due: Day ${acc.dueDayOfMonth}" else ""
                                        if (billPart.isNotEmpty() && duePart.isNotEmpty()) "$billPart | $duePart" else billPart + duePart
                                    }
                                    acc.dueDayOfMonth != null -> "Due Day: Day ${acc.dueDayOfMonth}"
                                    acc.dueDateMillis != null -> "Due: " + SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(acc.dueDateMillis))
                                    else -> "No Deadline"
                                }
                                Text(
                                    text = dateStr,
                                    fontFamily = IBMPlexMono,
                                    fontSize = 11.sp,
                                    color = if ((acc.dueDateMillis != null || acc.dueDayOfMonth != null) && netBalance != 0.0) LedgerRed.copy(alpha = 0.8f) else InkPrimary.copy(alpha = 0.4f)
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            val formattedBalance = String.format(Locale.getDefault(), "₹%,.2f",
                                abs(netBalance)
                            )
                            val balanceColor = when {
                                netBalance > 0 -> DeepForestIncome
                                netBalance < 0 -> LedgerRed
                                else -> InkPrimary
                            }
                            val balanceLabel = when {
                                netBalance > 0 -> if (acc.type == "PERSON") "Owes you $formattedBalance" else "Surplus of $formattedBalance"
                                netBalance < 0 -> if (acc.type == "PERSON") "You owe $formattedBalance" else "Outstanding: $formattedBalance"
                                else -> "All Settled"
                            }
                            Text(
                                text = balanceLabel,
                                fontFamily = Fraunces,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = balanceColor
                            )

                            // Progress Indicators (Credit Card limit, or Loan repayments)
                            if (acc.type == "CREDIT_CARD" && acc.limitAmount != null) {
                                val limit = acc.limitAmount
                                val used = abs(minOf(0.0, netBalance))
                                val usedPct = (used / limit).toFloat().coerceIn(0f, 1f)
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Card Utilization (${(usedPct * 100).toInt()}% Used)",
                                        fontFamily = IBMPlexSans,
                                        fontSize = 12.sp,
                                        color = InkPrimary.copy(alpha = 0.6f)
                                    )
                                    Text(
                                        text = "Limit: ₹%,.2f".format(limit),
                                        fontFamily = IBMPlexMono,
                                        fontSize = 12.sp,
                                        color = InkPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { usedPct },
                                    modifier = Modifier.fillMaxWidth().height(6.dp),
                                    color = if (usedPct > 0.85f) LedgerRed else BrassDivider,
                                    trackColor = BrassDivider.copy(alpha = 0.15f)
                                )
                            } else if (acc.type == "LOAN") {
                                val principal = acc.limitAmount ?: 0.0
                                val outstanding = abs(netBalance)
                                val emi = acc.installmentAmount ?: 1.0
                                val tenure = acc.tenureMonths ?: 1
                                val paidEMIs = if (emi > 0.0) {
                                    val paidAmt = max(0.0, principal - outstanding)
                                    (paidAmt / emi).roundToInt().coerceAtMost(tenure)
                                } else 0
                                val repaidPct = if (tenure > 0) (paidEMIs.toFloat() / tenure.toFloat()).coerceIn(0f, 1f) else 0f
                                
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Paid: $paidEMIs / $tenure EMIs",
                                        fontFamily = IBMPlexSans,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = DeepForestIncome
                                    )
                                    Text(
                                        text = "Principal: ₹%,.2f".format(principal),
                                        fontFamily = IBMPlexMono,
                                        fontSize = 12.sp,
                                        color = InkPrimary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { repaidPct },
                                    modifier = Modifier.fillMaxWidth().height(6.dp),
                                    color = DeepForestIncome,
                                    trackColor = BrassDivider.copy(alpha = 0.15f)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "EMI Amount: ₹%,.2f/mo".format(emi),
                                        fontFamily = IBMPlexSans,
                                        fontSize = 12.sp,
                                        color = InkPrimary.copy(alpha = 0.6f)
                                    )
                                    if (acc.dueDayOfMonth != null) {
                                        Text(
                                            text = "Due Day: Day ${acc.dueDayOfMonth}",
                                            fontFamily = IBMPlexSans,
                                            fontSize = 12.sp,
                                            color = LedgerRed.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Floating quick action buttons for adding transactions
                    if (acc.type == "LOAN") {
                        // Loan action buttons: Pay EMI & Close EMI (prefills EMI/outstanding in dialog)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val emi = acc.installmentAmount ?: 0.0
                            val outstanding = abs(netBalance)
                            val principal = acc.limitAmount ?: 0.0
                            val tenure = acc.tenureMonths ?: 1
                            val paidEMIs = if (emi > 0.0) {
                                val paidAmt = max(0.0, principal - outstanding)
                                (paidAmt / emi).roundToInt().coerceAtMost(tenure)
                            } else 0
                            val hasPaidCurrentMonth = remember(transactions) {
                                val currentCal = Calendar.getInstance()
                                val currentYear = currentCal.get(Calendar.YEAR)
                                val currentMonth = currentCal.get(Calendar.MONTH)
                                
                                transactions.any { tx ->
                                    if (tx.type == "GIVEN") {
                                        val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
                                        txCal.get(Calendar.YEAR) == currentYear && 
                                        txCal.get(Calendar.MONTH) == currentMonth
                                    } else false
                                }
                            }

                            val isEmiPayable = emi > 0.0 && outstanding > 0.0 && paidEMIs < tenure && !hasPaidCurrentMonth
                            val emiButtonText = when {
                                paidEMIs >= tenure -> "FULLY PAID"
                                hasPaidCurrentMonth -> "PAID THIS MONTH"
                                else -> "PAY EMI (₹%,.0f)".format(emi)
                            }

                            Button(
                                onClick = {
                                    if (isEmiPayable) {
                                        val currentCal = Calendar.getInstance()
                                        val startM = acc.emiStartMonth
                                        val startY = acc.emiStartYear
                                        val currentInstallmentIndex = if (startM != null && startY != null) {
                                            val startCal = Calendar.getInstance().apply {
                                                set(Calendar.YEAR, startY)
                                                set(Calendar.MONTH, startM - 1)
                                                set(Calendar.DAY_OF_MONTH, 1)
                                            }
                                            val diffYears = currentCal.get(Calendar.YEAR) - startCal.get(Calendar.YEAR)
                                            val diffMonths = currentCal.get(Calendar.MONTH) - startCal.get(Calendar.MONTH)
                                            val monthsElapsed = diffYears * 12 + diffMonths
                                            (monthsElapsed + 1).coerceAtLeast(1)
                                        } else {
                                            paidEMIs + 1
                                        }
                                        val monthLabel = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(currentCal.time)

                                        dialogTxType = "GIVEN"
                                        dialogPrefilledAmount = min(outstanding, emi)
                                        dialogPrefilledNote = "EMI Payment - Month $currentInstallmentIndex ($monthLabel)"
                                        dialogPrefilledDate = System.currentTimeMillis()
                                        showAddTxDialog = true
                                    }
                                },
                                enabled = isEmiPayable,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = DeepForestIncome,
                                    disabledContainerColor = DeepForestIncome.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = emiButtonText,
                                    fontFamily = IBMPlexSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isEmiPayable) PaperBackground else InkPrimary.copy(alpha = 0.4f)
                                )
                            }

                            val isCloseable = outstanding > 0.0
                            Button(
                                onClick = {
                                    if (isCloseable) {
                                        dialogTxType = "GIVEN"
                                        dialogPrefilledAmount = outstanding
                                        dialogPrefilledNote = "Loan Preclosure / Settlement"
                                        showAddTxDialog = true
                                    }
                                },
                                enabled = isCloseable,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = LedgerRed,
                                    disabledContainerColor = LedgerRed.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "CLOSE EMI",
                                    fontFamily = IBMPlexSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isCloseable) PaperBackground else InkPrimary.copy(alpha = 0.4f)
                                )
                            }
                        }
                    } else {
                        // Standard action buttons for Cards and Person accounts (custom amounts allowed)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val chargeLabel = when (acc.type) {
                                "CREDIT_CARD" -> "Record Purchase"
                                else -> "Borrow / Receive"
                            }
                            Button(
                                onClick = {
                                    dialogTxType = "RECEIVED"
                                    dialogPrefilledAmount = null
                                    showAddTxDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = LedgerRed),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = chargeLabel.uppercase(Locale.getDefault()),
                                    fontFamily = IBMPlexSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PaperBackground
                                )
                            }

                            val payLabel = when (acc.type) {
                                "CREDIT_CARD" -> "Pay Card Bill"
                                else -> "Lend / Pay Back"
                            }
                            Button(
                                onClick = {
                                    dialogTxType = "GIVEN"
                                    dialogPrefilledAmount = null
                                    showAddTxDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = DeepForestIncome),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = payLabel.uppercase(Locale.getDefault()),
                                    fontFamily = IBMPlexSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = PaperBackground
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Statements / Transaction List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 24.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "TRANSACTION HISTORY",
                                    fontFamily = IBMPlexSans,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = InkPrimary.copy(alpha = 0.6f),
                                    letterSpacing = 1.sp
                                )

                                if (acc.type == "CREDIT_CARD" && acc.billDayOfMonth != null) {
                                    val (startCal, endCal) = getBillingCycleRange(acc.billDayOfMonth, cycleOffset)
                                    val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
                                    val rangeText = "${sdf.format(startCal.time)} - ${sdf.format(endCal.time)}"

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { cycleOffset-- },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                                contentDescription = "Previous Cycle",
                                                tint = BrassDivider,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Text(
                                            text = rangeText,
                                            fontFamily = IBMPlexMono,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = InkPrimary
                                        )
                                        IconButton(
                                            onClick = { cycleOffset++ },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                                contentDescription = "Next Cycle",
                                                tint = BrassDivider,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = BrassDivider, thickness = 1.dp)
                        }

                        val displayItems = when (acc.type) {
                            "LOAN" -> historyItems
                            "CREDIT_CARD" -> {
                                val billDay = acc.billDayOfMonth
                                if (billDay != null) {
                                    val (startCal, endCal) = getBillingCycleRange(billDay, cycleOffset)
                                    val startM = startCal.timeInMillis
                                    val endM = endCal.timeInMillis
                                    transactions
                                        .filter { it.dateMillis in startM..endM }
                                        .map { HistoryItem.TransactionItem(it) }
                                } else {
                                    transactions.map { HistoryItem.TransactionItem(it) }
                                }
                            }
                            else -> transactions.map { HistoryItem.TransactionItem(it) }
                        }

                        if (displayItems.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No statements recorded under this account.",
                                        fontFamily = Fraunces,
                                        fontStyle = FontStyle.Italic,
                                        fontSize = 14.sp,
                                        color = InkPrimary.copy(alpha = 0.4f)
                                    )
                                }
                            }
                        } else {
                            items(displayItems) { item ->
                                when (item) {
                                    is HistoryItem.TransactionItem -> {
                                        CreditTransactionRow(tx = item.tx, accountType = acc.type, onDelete = { showDeleteTxConfirm = item.tx })
                                        HorizontalDivider(color = Color(0xFFEBE3D3), thickness = 1.dp)
                                    }
                                    is HistoryItem.MissedPaymentItem -> {
                                        val emi = acc.installmentAmount ?: 0.0
                                        MissedPaymentRow(
                                            monthIndex = item.monthIndex,
                                            dueDate = item.expectedDueDate,
                                            emiAmount = emi,
                                            onPayClick = {
                                                val cal = Calendar.getInstance().apply { timeInMillis = item.expectedDueDate }
                                                val monthLabel = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(cal.time)
                                                dialogTxType = "GIVEN"
                                                dialogPrefilledAmount = emi
                                                dialogPrefilledNote = "EMI Payment - Month ${item.monthIndex + 1} ($monthLabel)"
                                                dialogPrefilledDate = item.expectedDueDate
                                                showAddTxDialog = true
                                            }
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                    }
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                }
            }
        }

        if (showAddTxDialog) {
            AddCreditTransactionDialog(
                type = dialogTxType,
                accountType = account?.type ?: "PERSON",
                prefilledAmount = dialogPrefilledAmount,
                prefilledNote = dialogPrefilledNote,
                prefilledDateMillis = dialogPrefilledDate,
                onDismiss = { 
                    showAddTxDialog = false
                    dialogPrefilledAmount = null
                    dialogPrefilledNote = ""
                    dialogPrefilledDate = null
                },
                onConfirm = { amount, note, date ->
                    account?.let { acc ->
                        viewModel?.addCreditTransaction(acc.id, amount, dialogTxType, note, date, false, null)
                    }
                    showAddTxDialog = false
                    dialogPrefilledAmount = null
                    dialogPrefilledNote = ""
                    dialogPrefilledDate = null
                }
            )
        }

        if (showDeleteConfirm) {
            account?.let { acc ->
                ConfirmDeleteAccountDialog(
                    account = acc,
                    onDismiss = { showDeleteConfirm = false },
                    onConfirm = {
                        viewModel?.deleteAccount(acc)
                        showDeleteConfirm = false
                        onNavigateBack()
                    }
                )
            }
        }

        showDeleteTxConfirm?.let { tx ->
            AlertDialog(
                onDismissRequest = { showDeleteTxConfirm = null },
                containerColor = CardSurface,
                shape = RoundedCornerShape(6.dp),
                title = {
                    Text(
                        text = "Delete Transaction",
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = InkPrimary
                    )
                },
                text = {
                    Text(
                        text = "Are you sure you want to delete this payment of ₹%,.2f?".format(tx.amount),
                        fontFamily = IBMPlexSans,
                        fontSize = 14.sp,
                        color = InkPrimary.copy(alpha = 0.8f)
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel?.deleteTransaction(tx)
                            showDeleteTxConfirm = null
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
                    TextButton(onClick = { showDeleteTxConfirm = null }) {
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
    }
}

@Composable
fun CreditTransactionRow(
    tx: CreditTransaction,
    accountType: String,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
            .padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            val typeLabel = when (tx.type) {
                "GIVEN" -> {
                    when (accountType) {
                        "CREDIT_CARD" -> "Repayment"
                        "LOAN" -> "Loan Repayment"
                        else -> "Lent / Returned"
                    }
                }
                else -> {
                    when (accountType) {
                        "CREDIT_CARD" -> "Card Charge"
                        "LOAN" -> "Borrowed Cash"
                        else -> "Received / Borrowed"
                    }
                }
            }
            Text(
                text = typeLabel,
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = InkPrimary
            )
            if (!tx.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tx.note,
                    fontFamily = IBMPlexSans,
                    fontSize = 13.sp,
                    color = InkPrimary.copy(alpha = 0.8f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(tx.dateMillis))
            Text(
                text = dateStr,
                fontFamily = IBMPlexMono,
                fontSize = 11.sp,
                color = InkPrimary.copy(alpha = 0.4f)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            val prefix = if (tx.type == "GIVEN") "+" else "-"
            val amountColor = if (tx.type == "GIVEN") DeepForestIncome else LedgerRed
            Text(
                text = String.format(Locale.getDefault(), "%s₹%,.2f", prefix, tx.amount),
                style = AmountMedium,
                color = amountColor,
                textAlign = TextAlign.End,
                modifier = Modifier.padding(start = 8.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete entry",
                    tint = InkPrimary.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCreditTransactionDialog(
    type: String, // "GIVEN" or "RECEIVED"
    accountType: String,
    prefilledAmount: Double? = null,
    prefilledNote: String = "",
    prefilledDateMillis: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (
        amount: Double,
        note: String?,
        dateMillis: Long
    ) -> Unit
) {
    var amountStr by remember { mutableStateOf(prefilledAmount?.let { String.format(Locale.US, "%.2f", it).replace(Regex("\\.00$"), "") } ?: "") }
    var note by remember { mutableStateOf(prefilledNote) }
    var selectedDate by remember { mutableLongStateOf(prefilledDateMillis ?: System.currentTimeMillis()) }

    val context = LocalContext.current

    val dialogTitle = when (type) {
        "GIVEN" -> {
            when (accountType) {
                "CREDIT_CARD" -> "Card Repayment"
                "LOAN" -> "Repay Loan Amount"
                else -> "Lend / Return Cash"
            }
        }
        else -> {
            when (accountType) {
                "CREDIT_CARD" -> "Record Card Expense"
                "LOAN" -> "Borrow Cash Inflow"
                else -> "Receive / Borrow Cash"
            }
        }
    }



    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardSurface,
        shape = RoundedCornerShape(6.dp),
        title = {
            Text(
                text = dialogTitle,
                fontFamily = Fraunces,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = InkPrimary
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Amount (₹)") },
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
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("Note / Description") },
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
                }

                // Date Picker field
                item {
                    Text(
                        text = "TRANSACTION DATE",
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = InkPrimary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(0.5.dp, InkPrimary.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .clickable {
                                val calendar = Calendar.getInstance().apply { timeInMillis = selectedDate }
                                val dialog = DatePickerDialog(
                                    context,
                                    { _, year, month, dayOfMonth ->
                                        val newCal = Calendar.getInstance().apply {
                                            set(Calendar.YEAR, year)
                                            set(Calendar.MONTH, month)
                                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
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
                            text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDate)),
                            fontFamily = IBMPlexSans,
                            fontSize = 14.sp,
                            color = InkPrimary
                        )
                    }
                }


            }
        },
        confirmButton = {
            val amountVal = amountStr.toDoubleOrNull()
            val isEnabled = amountVal != null && amountVal > 0
            val buttonText = if (accountType == "LOAN" && type == "GIVEN") {
                if (amountStr.isNotBlank()) "PAY EMI (₹$amountStr)" else "PAY EMI"
            } else "RECORD"
            TextButton(
                onClick = {
                    if (isEnabled) {
                        onConfirm(amountVal, note.ifBlank { null }, selectedDate)
                    }
                },
                enabled = isEnabled
            ) {
                Text(
                    text = buttonText,
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

sealed class HistoryItem {
    data class TransactionItem(val tx: CreditTransaction) : HistoryItem()
    data class MissedPaymentItem(val monthIndex: Int, val expectedDueDate: Long) : HistoryItem()
}

@Composable
fun MissedPaymentRow(
    monthIndex: Int,
    dueDate: Long,
    emiAmount: Double,
    onPayClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardSurface)
            .border(1.dp, LedgerRed.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "MISSED PAYMENT - MONTH ${monthIndex + 1}",
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = LedgerRed
            )
            Spacer(modifier = Modifier.height(2.dp))
            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(dueDate))
            Text(
                text = "Was due on: $dateStr",
                fontFamily = IBMPlexMono,
                fontSize = 11.sp,
                color = InkPrimary.copy(alpha = 0.5f)
            )
        }

        Button(
            onClick = onPayClick,
            colors = ButtonDefaults.buttonColors(containerColor = LedgerRed),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
            modifier = Modifier.height(28.dp),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = "PAY (₹%,.0f)".format(emiAmount),
                fontFamily = IBMPlexSans,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = Color.White
            )
        }
    }
}

fun getBillingCycleRange(billDay: Int, offset: Int): Pair<Calendar, Calendar> {
    val targetEndCal = Calendar.getInstance().apply {
        val todayDay = get(Calendar.DAY_OF_MONTH)
        if (todayDay <= billDay) {
            // Ends this month
        } else {
            // Ends next month
            add(Calendar.MONTH, 1)
        }
        set(Calendar.DAY_OF_MONTH, billDay)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }
    
    targetEndCal.add(Calendar.MONTH, offset)
    
    val targetStartCal = (targetEndCal.clone() as Calendar).apply {
        add(Calendar.MONTH, -1)
        add(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    
    return Pair(targetStartCal, targetEndCal)
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CreditDetailsScreenPreview() {
    com.me.moneytracker.ui.theme.LedgerTheme {
        CreditDetailsScreen(
            accountId = 1L,
            onNavigateBack = {}
        )
    }
}
