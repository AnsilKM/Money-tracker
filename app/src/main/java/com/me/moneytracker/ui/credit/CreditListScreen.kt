package com.me.moneytracker.ui.credit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.me.moneytracker.data.CreditAccount
import com.me.moneytracker.ui.credit.components.*
import com.me.moneytracker.ui.home.ruledBackground
import com.me.moneytracker.ui.theme.*
import org.koin.androidx.compose.koinViewModel
import java.util.Locale
import java.util.Calendar
import java.util.Date
import java.text.SimpleDateFormat
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreditListScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetails: (Long) -> Unit,
    onNavigateToHome: () -> Unit = {},
    onNavigateToReports: () -> Unit = {},
    viewModel: CreditViewModel? = if (androidx.compose.ui.platform.LocalInspectionMode.current) null else koinViewModel()
) {
    val isPreview = androidx.compose.ui.platform.LocalInspectionMode.current

    val accounts = if (isPreview || viewModel == null) {
        remember {
            listOf(
                CreditAccount(id = 1, name = "HDFC Card", type = "CREDIT_CARD", limitAmount = 100000.0, billDayOfMonth = 10, dueDayOfMonth = 25, colorScheme = 0),
                CreditAccount(id = 2, name = "John Lending", type = "PERSON", limitAmount = 5000.0),
                CreditAccount(id = 3, name = "SBI Home Loan", type = "LOAN", limitAmount = 500000.0, installmentAmount = 15000.0, tenureMonths = 12, dueDayOfMonth = 5, emiStartMonth = 6, emiStartYear = 2026)
            )
        }
    } else {
        viewModel.activeAccounts.collectAsState().value
    }

    val balances = if (isPreview || viewModel == null) {
        remember { mapOf(1L to -25000.0, 2L to 1500.0, 3L to -485000.0) }
    } else {
        viewModel.accountBalances.collectAsState().value
    }

    val accountTransactions = if (isPreview || viewModel == null) {
        remember { emptyMap() }
    } else {
        viewModel.accountTransactions.collectAsState().value
    }

    var activeTab by remember { mutableIntStateOf(0) } // 0: Cards, 1: Single Payments, 2: Loans

    // FAB Speed Dial state
    var isMenuExpanded by remember { mutableStateOf(false) }

    // Dialog state
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedCreateType by remember { mutableStateOf("CREDIT_CARD") } // "CREDIT_CARD", "PERSON", "LOAN"

    var showSettleDialogForAccount by remember { mutableStateOf<Pair<CreditAccount, Double>?>(null) }
    var showDeleteConfirmForAccount by remember { mutableStateOf<CreditAccount?>(null) }
    var showPayBillDialogForAccount by remember { mutableStateOf<Pair<CreditAccount, Double>?>(null) }

    // Hoist animation state so both FAB and overlay can use it
    val menuProgress by animateFloatAsState(
        targetValue = if (isMenuExpanded) 1f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "MenuProgress"
    )
    val rotationAngle by animateFloatAsState(
        targetValue = if (isMenuExpanded) 45f else 0f,
        animationSpec = tween(durationMillis = 250),
        label = "MainFabRotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Credit Books",
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
        floatingActionButton = {
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                if (menuProgress > 0f) {
                    // Sub-FAB 1: Credit Card (Left, label to the left of FAB)
                    Row(
                        modifier = Modifier
                            .offset(x = (-95 * menuProgress).dp, y = (0 * menuProgress).dp)
                            .graphicsLayer(
                                scaleX = menuProgress,
                                scaleY = menuProgress,
                                alpha = menuProgress
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Card",
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = InkPrimary,
                            modifier = Modifier
                                .background(CardSurface, RoundedCornerShape(4.dp))
                                .border(
                                    0.5.dp,
                                    BrassDivider.copy(alpha = 0.5f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        FloatingActionButton(
                            onClick = {
                                isMenuExpanded = false
                                selectedCreateType = "CREDIT_CARD"
                                showCreateDialog = true
                            },
                            containerColor = Color(0xFFE67E22),
                            contentColor = Color.White,
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Icon(
                                imageVector = CreditCardIcon,
                                contentDescription = "Add Credit Card",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Sub-FAB 2: Single Payment (Diagonal, label diagonally upper-left of icon)
                    Box(
                        modifier = Modifier
                            .offset(x = (-68 * menuProgress).dp, y = (-68 * menuProgress).dp)
                            .graphicsLayer(
                                scaleX = menuProgress,
                                scaleY = menuProgress,
                                alpha = menuProgress
                            )
                            .size(width = 110.dp, height = 80.dp)
                    ) {
                        Text(
                            text = "Payment",
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = InkPrimary,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .background(CardSurface, RoundedCornerShape(4.dp))
                                .border(
                                    0.5.dp,
                                    BrassDivider.copy(alpha = 0.5f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        FloatingActionButton(
                            onClick = {
                                isMenuExpanded = false
                                selectedCreateType = "PERSON"
                                showCreateDialog = true
                            },
                            containerColor = Color(0xFF16A085),
                            contentColor = Color.White,
                            modifier = Modifier
                                .size(44.dp)
                                .align(Alignment.BottomEnd),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Add Single Payment",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    // Sub-FAB 3: Loan (Top, label above FAB)
                    Column(
                        modifier = Modifier
                            .offset(x = (0 * menuProgress).dp, y = (-95 * menuProgress).dp)
                            .graphicsLayer(
                                scaleX = menuProgress,
                                scaleY = menuProgress,
                                alpha = menuProgress
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Loan",
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = InkPrimary,
                            modifier = Modifier
                                .background(CardSurface, RoundedCornerShape(4.dp))
                                .border(
                                    0.5.dp,
                                    BrassDivider.copy(alpha = 0.5f),
                                    RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        FloatingActionButton(
                            onClick = {
                                isMenuExpanded = false
                                selectedCreateType = "LOAN"
                                showCreateDialog = true
                            },
                            containerColor = Color(0xFF3A7BD5),
                            contentColor = Color.White,
                            modifier = Modifier.size(44.dp),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Add Loan",
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                // Main FAB
                FloatingActionButton(
                    onClick = { isMenuExpanded = !isMenuExpanded },
                    containerColor = BrassDivider,
                    contentColor = PaperBackground,
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Toggle Add Menu",
                        modifier = Modifier.graphicsLayer(rotationZ = rotationAngle)
                    )
                }
            }
        },
        bottomBar = {
            com.me.moneytracker.ui.components.FloatingNavBar(
                currentRoute = "credit_list",
                onNavigate = { route ->
                    when (route) {
                        "home" -> onNavigateToHome()
                        "reports" -> onNavigateToReports()
                    }
                }
            )
        },
        containerColor = PaperBackground
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .ruledBackground(Color(0xFFF1EADB))
            ) {
                // Custom vintage-style tab layout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                        .border(1.dp, BrassDivider, RoundedCornerShape(4.dp))
                        .background(CardSurface, RoundedCornerShape(4.dp)),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val tabLabels = listOf("Cards", "Payments", "Loans")
                    tabLabels.forEachIndexed { index, label ->
                        val isSelected = activeTab == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .background(
                                    if (isSelected) BrassDivider else Color.Transparent,
                                    RoundedCornerShape(3.dp)
                                )
                                .clickable { activeTab = index }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label.uppercase(Locale.getDefault()),
                                fontFamily = IBMPlexSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = if (isSelected) PaperBackground else InkPrimary,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                // Tab Content
                val filteredAccounts = when (activeTab) {
                    0 -> accounts.filter { it.type == "CREDIT_CARD" }
                    1 -> accounts.filter { it.type == "PERSON" }
                    else -> accounts.filter { it.type == "LOAN" }
                }

                if (filteredAccounts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        val emptyMsg = when (activeTab) {
                            0 -> "No Credit Cards registered."
                            1 -> "No Single Payments logged."
                            else -> "No Active Borrowings/Loans."
                        }
                        Text(
                            text = emptyMsg,
                            fontFamily = Fraunces,
                            fontStyle = FontStyle.Italic,
                            fontSize = 15.sp,
                            color = InkPrimary.copy(alpha = 0.5f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(filteredAccounts) { account ->
                            val balance = balances[account.id] ?: 0.0
                            when (activeTab) {
                                0 -> {
                                    val txs = accountTransactions[account.id] ?: emptyList()
                                    val unpaidBill = if (account.billDayOfMonth != null) {
                                        com.me.moneytracker.ui.credit.components.calculateUnpaidBill(account.billDayOfMonth, txs)
                                    } else 0.0

                                    CreditCardTemplateItem(
                                        account = account,
                                        balance = balance,
                                        transactions = txs,
                                        onClick = { onNavigateToDetails(account.id) },
                                        onPayBill = if (unpaidBill > 0.0) {
                                            { showPayBillDialogForAccount = Pair(account, unpaidBill) }
                                        } else null
                                    )
                                }

                                1 -> SinglePaymentTemplateItem(
                                    account = account,
                                    balance = balance,
                                    transactions = accountTransactions[account.id] ?: emptyList(),
                                    onSettle = {
                                        showSettleDialogForAccount = Pair(account, balance)
                                    },
                                    onDelete = { showDeleteConfirmForAccount = account }
                                )

                                else -> LoanTemplateItem(
                                    account = account,
                                    balance = balance,
                                    transactions = accountTransactions[account.id] ?: emptyList(),
                                    onClick = { onNavigateToDetails(account.id) })
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }


            }

            if (menuProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f * menuProgress))
                        .clickable(
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null
                        ) {
                            isMenuExpanded = false
                        }
                )
            }

            if (showCreateDialog) {
                when (selectedCreateType) {
                    "CREDIT_CARD" -> {
                        AddCreditCardDialog(
                            onDismiss = { showCreateDialog = false },
                            onConfirm = { name, limit, billDay, dueDay, colorScheme ->
                                viewModel?.addCreditAccount(
                                    name = name,
                                    type = "CREDIT_CARD",
                                    limitAmount = limit,
                                    dueDateMillis = null,
                                    repaymentType = null,
                                    installmentAmount = null,
                                    interestRate = null,
                                    initialAmount = null,
                                    initialTxType = null,
                                    tenureMonths = null,
                                    dueDayOfMonth = dueDay,
                                    billDayOfMonth = billDay,
                                    colorScheme = colorScheme
                                )
                                showCreateDialog = false
                            }
                        )
                    }

                    "PERSON" -> {
                        AddSinglePaymentDialog(
                            onDismiss = { showCreateDialog = false },
                            onConfirm = { name, amount, dueDate, lentType, note ->
                                viewModel?.addCreditAccount(
                                    name = name,
                                    type = "PERSON",
                                    limitAmount = amount,
                                    dueDateMillis = dueDate,
                                    repaymentType = null,
                                    installmentAmount = null,
                                    interestRate = null,
                                    initialAmount = amount,
                                    initialTxType = if (lentType == "LENT") "GIVEN" else "RECEIVED",
                                    tenureMonths = null,
                                    dueDayOfMonth = null,
                                    initialNote = note
                                )
                                showCreateDialog = false
                            }
                        )
                    }

                    "LOAN" -> {
                        AddLoanDialog(
                            onDismiss = { showCreateDialog = false },
                            onConfirm = { name, principal, dueDay, emi, tenure, startMonth, startYear ->
                                viewModel?.addCreditAccount(
                                    name = name,
                                    type = "LOAN",
                                    limitAmount = principal,
                                    dueDateMillis = null,
                                    repaymentType = "INSTALLMENTS",
                                    installmentAmount = emi,
                                    interestRate = null,
                                    initialAmount = principal,
                                    initialTxType = "RECEIVED",
                                    tenureMonths = tenure,
                                    dueDayOfMonth = dueDay,
                                    colorScheme = 0,
                                    emiStartMonth = startMonth,
                                    emiStartYear = startYear
                                )
                                showCreateDialog = false
                            }
                        )
                    }
                }
            }

            showSettleDialogForAccount?.let { (account, balance) ->
                SettleSinglePaymentDialog(
                    account = account,
                    balance = balance,
                    onDismiss = { showSettleDialogForAccount = null },
                    onConfirm = { amount ->
                        val type = if (balance < 0) "GIVEN" else "RECEIVED"
                        viewModel?.addCreditTransaction(
                            accountId = account.id,
                            amount = amount,
                            type = type,
                            note = if (amount == abs(balance)) "Settled" else "Partial Payment",
                            dateMillis = System.currentTimeMillis(),
                            syncToLedger = false,
                            categoryId = null
                        )
                        showSettleDialogForAccount = null
                    }
                )
            }

            showDeleteConfirmForAccount?.let { account ->
                ConfirmDeleteAccountDialog(
                    account = account,
                    onDismiss = { showDeleteConfirmForAccount = null },
                    onConfirm = {
                        viewModel?.deleteAccount(account)
                        showDeleteConfirmForAccount = null
                    }
                )
            }

            showPayBillDialogForAccount?.let { (account, amount) ->
                val currentCal = Calendar.getInstance()
                val monthLabel = SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(currentCal.time)
                AddCreditTransactionDialog(
                    type = "GIVEN",
                    accountType = "CREDIT_CARD",
                    prefilledAmount = amount,
                    prefilledNote = "Card Bill Payment - $monthLabel",
                    prefilledDateMillis = System.currentTimeMillis(),
                    onDismiss = { showPayBillDialogForAccount = null },
                    onConfirm = { payAmount, note, date ->
                        viewModel?.addCreditTransaction(
                            accountId = account.id,
                            amount = payAmount,
                            type = "GIVEN",
                            note = note ?: "Card Bill Payment - $monthLabel",
                            dateMillis = date,
                            syncToLedger = false,
                            categoryId = null
                        )
                        showPayBillDialogForAccount = null
                    }
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun CreditListScreenPreview() {
    com.me.moneytracker.ui.theme.LedgerTheme {
        CreditListScreen(
            onNavigateBack = {},
            onNavigateToDetails = {}
        )
    }
}
