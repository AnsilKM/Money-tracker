package com.me.moneytracker.ui.credit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.me.moneytracker.data.CreditAccount
import com.me.moneytracker.ui.theme.*
import java.util.Locale

import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max

data class EmiPeriod(
    val monthName: String,
    val year: Int,
    val status: EmiStatus
)

enum class EmiStatus {
    PAID, MISSED, UPCOMING
}

@Composable
fun LoanTemplateItem(
    account: CreditAccount,
    balance: Double,
    transactions: List<com.me.moneytracker.data.CreditTransaction>,
    onClick: () -> Unit
) {
    val emiStartM = account.emiStartMonth
    val emiStartY = account.emiStartYear
    val tenure = account.tenureMonths ?: 0
    
    val emiPeriods = remember(transactions, emiStartM, emiStartY, tenure) {
        if (emiStartM == null || emiStartY == null || tenure <= 0) {
            emptyList()
        } else {
            val list = mutableListOf<EmiPeriod>()
            val monthsShort = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
            val now = Calendar.getInstance()
            val currentYear = now.get(Calendar.YEAR)
            val currentMonth = now.get(Calendar.MONTH) + 1 // 1-based
            
            for (i in 0 until tenure) {
                var m = emiStartM + i
                var y = emiStartY
                while (m > 12) {
                    m -= 12
                    y += 1
                }
                
                val isFuture = if (y > currentYear) {
                    true
                } else if (y == currentYear) {
                    m > currentMonth
                } else {
                    false
                }
                
                val isPaid = transactions.any { tx ->
                    if (tx.type == "GIVEN") {
                        val txCal = Calendar.getInstance().apply { timeInMillis = tx.dateMillis }
                        txCal.get(Calendar.YEAR) == y && (txCal.get(Calendar.MONTH) + 1) == m
                    } else false
                }
                
                val status = when {
                    isPaid -> EmiStatus.PAID
                    isFuture -> EmiStatus.UPCOMING
                    else -> EmiStatus.MISSED
                }
                
                list.add(EmiPeriod(monthsShort[m - 1], y, status))
            }
            list
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .border(1.dp, BrassDivider.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .background(CardSurface)
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Column {
            // Certificate Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "LOAN LEDGER STATEMENT",
                    fontFamily = IBMPlexMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = InkPrimary.copy(alpha = 0.5f)
                )
                Text(
                    text = "ACTIVE",
                    fontFamily = IBMPlexSans,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = LedgerRed,
                    modifier = Modifier
                        .border(1.dp, LedgerRed, RoundedCornerShape(2.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BrassDivider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // Loan content grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = account.name,
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = InkPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (account.installmentAmount != null) {
                        Text(
                            text = "EMI: ₹%,.2f/mo".format(account.installmentAmount),
                            fontFamily = IBMPlexSans,
                            fontSize = 12.sp,
                            color = InkPrimary.copy(alpha = 0.6f)
                        )
                    }
                    if (account.tenureMonths != null) {
                        val principal = account.limitAmount ?: 0.0
                        val outstanding = abs(balance)
                        val emi = account.installmentAmount ?: 1.0
                        val paidEMIs = if (emi > 0.0) {
                            val paidAmt = max(0.0, principal - outstanding)
                            Math.round(paidAmt / emi).toInt().coerceAtMost(account.tenureMonths)
                        } else 0
                        Text(
                            text = "Paid: $paidEMIs / ${account.tenureMonths} EMIs",
                            fontFamily = IBMPlexSans,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = DeepForestIncome
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "OUTSTANDING",
                        fontFamily = IBMPlexSans,
                        fontSize = 10.sp,
                        color = InkPrimary.copy(alpha = 0.5f)
                    )
                    Text(
                        text = String.format(Locale.getDefault(), "₹%,.2f", abs(balance)),
                        fontFamily = IBMPlexMono,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = LedgerRed
                    )
                    
                    if (account.dueDayOfMonth != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Due Day: Day ${account.dueDayOfMonth}",
                            fontFamily = IBMPlexMono,
                            fontSize = 11.sp,
                            color = LedgerRed.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (emiPeriods.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BrassDivider.copy(alpha = 0.2f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "EMI TIMELINE",
                    fontFamily = IBMPlexMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = InkPrimary.copy(alpha = 0.4f),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(emiPeriods.size) { index ->
                        val period = emiPeriods[index]
                        val bgColor = when (period.status) {
                            EmiStatus.PAID -> Color(0xFFE8F5E9)
                            EmiStatus.MISSED -> Color(0xFFFFEBEE)
                            EmiStatus.UPCOMING -> Color(0xFFF5F5F5)
                        }
                        val textColor = when (period.status) {
                            EmiStatus.PAID -> Color(0xFF2E7D32)
                            EmiStatus.MISSED -> Color(0xFFC62828)
                            EmiStatus.UPCOMING -> Color(0xFF757575)
                        }
                        val label = when (period.status) {
                            EmiStatus.PAID -> "🟢"
                            EmiStatus.MISSED -> "🔴"
                            EmiStatus.UPCOMING -> "⚪"
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(bgColor, RoundedCornerShape(4.dp))
                                .border(0.5.dp, textColor.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "$label ${period.monthName} '${period.year.toString().takeLast(2)}",
                                fontFamily = IBMPlexSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = textColor
                            )
                        }
                    }
                }
            }
        }
    }
}
