package com.me.moneytracker.ui.credit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.me.moneytracker.data.CreditAccount
import com.me.moneytracker.data.CreditTransaction
import com.me.moneytracker.ui.theme.BrassDivider
import com.me.moneytracker.ui.theme.Fraunces
import com.me.moneytracker.ui.theme.IBMPlexMono
import com.me.moneytracker.ui.theme.IBMPlexSans
import com.me.moneytracker.ui.theme.LedgerTheme
import androidx.compose.ui.tooling.preview.Preview
import java.util.Calendar
import java.util.Locale
import kotlin.math.abs

@Composable
fun CreditCardTemplateItem(
    account: CreditAccount,
    balance: Double,
    transactions: List<CreditTransaction> = emptyList(),
    onClick: () -> Unit,
    onPayBill: (() -> Unit)? = null
) {
    val scheme = CardColorSchemes.getOrElse(account.colorScheme) { CardColorSchemes[0] }
    
    val unpaidBill = androidx.compose.runtime.remember(account, transactions) {
        val billDay = account.billDayOfMonth
        if (billDay != null) {
            calculateUnpaidBill(billDay, transactions)
        } else {
            0.0
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .border(1.5.dp, BrassDivider, RoundedCornerShape(15.dp))
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(scheme.start, scheme.end)
                ),
                shape = RoundedCornerShape(15.dp)
            )
            .clickable { onClick() }
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Card Name & Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = account.name.uppercase(Locale.getDefault()),
                    fontFamily = Fraunces,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )
                // Gold card chip representation
                Box(
                    modifier = Modifier
                        .size(32.dp, 24.dp)
                        .background(Color(0xFFF39C12), RoundedCornerShape(4.dp))
                        .border(1.dp, Color(0xFFD35400), RoundedCornerShape(4.dp))
                )
            }

            // Middle: Used Balance styled like a Card Number!
            val balanceLabel = if (balance > 0.0) "SURPLUS BALANCE" else "OUTSTANDING BALANCE"
            val displayAmt = when {
                balance == 0.0 -> "SETTLED"
                balance > 0.0 -> String.format(Locale.getDefault(), "-₹%,.2f", balance)
                else -> String.format(Locale.getDefault(), "₹%,.2f", abs(balance))
            }
            Column {
                Text(
                    text = balanceLabel,
                    fontFamily = IBMPlexSans,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = displayAmt,
                    fontFamily = IBMPlexMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = if (balance < 0) Color(0xFFFF7675) else if (balance > 0) Color(0xFF2ECC71) else Color.White
                )
            }

            // Footer: Due Date & Limit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "LIMIT",
                        fontFamily = IBMPlexSans,
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = if (account.limitAmount != null) "₹%,.2f".format(account.limitAmount) else "N/A",
                        fontFamily = IBMPlexMono,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "DUE DATE",
                        fontFamily = IBMPlexSans,
                        fontSize = 8.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    val dateLabel = if (account.dueDayOfMonth != null) {
                        "Day ${account.dueDayOfMonth}"
                    } else "Not Set"
                    Text(
                        text = dateLabel,
                        fontFamily = IBMPlexMono,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }

            if (unpaidBill > 0.0 && onPayBill != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "UNPAID STATEMENT BILL",
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = String.format(Locale.getDefault(), "₹%,.2f", unpaidBill),
                            fontFamily = IBMPlexMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFFFF7675)
                        )
                    }

                    androidx.compose.material3.Button(
                        onClick = { onPayBill() },
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2ECC71)
                        ),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        modifier = Modifier.height(26.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "PAY BILL",
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CreditCardTemplateItemPreview() {
    LedgerTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            CreditCardTemplateItem(
                account = CreditAccount(
                    name = "HDFC Regalia",
                    type = "CREDIT_CARD",
                    limitAmount = 500000.0,
                    dueDayOfMonth = 20,
                    colorScheme = 0
                ),
                balance = -45250.0,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            CreditCardTemplateItem(
                account = CreditAccount(
                    name = "ICICI Amazon Pay",
                    type = "CREDIT_CARD",
                    limitAmount = 250000.0,
                    dueDayOfMonth = 10,
                    colorScheme = 1
                ),
                balance = 1200.0,
                onClick = {}
            )

            Spacer(modifier = Modifier.height(16.dp))

            CreditCardTemplateItem(
                account = CreditAccount(
                    name = "Amex Platinum",
                    type = "CREDIT_CARD",
                    limitAmount = 1000000.0,
                    dueDayOfMonth = 5,
                    colorScheme = 2
                ),
                balance = 0.0,
                onClick = {}
            )
        }
    }
}

fun calculateUnpaidBill(
    billDay: Int,
    transactions: List<CreditTransaction>
): Double {
    val currentCal = Calendar.getInstance()
    val todayDay = currentCal.get(Calendar.DAY_OF_MONTH)
    
    val latestBillCal = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, billDay)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
        if (todayDay < billDay) {
            add(Calendar.MONTH, -1)
        }
    }
    
    val cycleStartCal = (latestBillCal.clone() as Calendar).apply {
        add(Calendar.MONTH, -1)
        add(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    
    val startMillis = cycleStartCal.timeInMillis
    val endMillis = latestBillCal.timeInMillis
    
    val cycleCharges = transactions
        .filter { it.dateMillis in startMillis..endMillis }
        .filter { it.type == "RECEIVED" }
        .sumOf { it.amount }
        
    val cycleRepayments = transactions
        .filter { it.dateMillis in startMillis..endMillis }
        .filter { it.type == "GIVEN" }
        .sumOf { it.amount }
        
    val statementBalance = maxOf(0.0, cycleCharges - cycleRepayments)
    if (statementBalance <= 0.0) return 0.0
    
    val repaymentsAfterBill = transactions
        .filter { it.dateMillis > endMillis }
        .filter { it.type == "GIVEN" }
        .sumOf { it.amount }
        
    return maxOf(0.0, statementBalance - repaymentsAfterBill)
}
