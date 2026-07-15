package com.me.moneytracker.ui.credit.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.me.moneytracker.data.CreditAccount
import com.me.moneytracker.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun SinglePaymentTemplateItem(
    account: CreditAccount,
    balance: Double,
    transactions: List<com.me.moneytracker.data.CreditTransaction> = emptyList(),
    onSettle: () -> Unit,
    onDelete: () -> Unit
) {
    val repayments = androidx.compose.runtime.remember(transactions) {
        val sorted = transactions.sortedBy { it.dateMillis }
        if (sorted.size > 1) sorted.drop(1) else emptyList()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .border(1.dp, InkPrimary.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .background(CardSurface)
            .padding(16.dp)
    ) {
        Column {
            // Receipt header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECEIPT FOR SINGLE PAYMENT",
                    fontFamily = IBMPlexMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = InkPrimary.copy(alpha = 0.5f)
                )
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val statusText = when {
                        balance > 0 -> "OWES YOU"
                        balance < 0 -> "YOU OWE"
                        else -> "SETTLED"
                    }
                    val statusColor = when {
                        balance > 0 -> DeepForestIncome
                        balance < 0 -> LedgerRed
                        else -> InkPrimary.copy(alpha = 0.4f)
                    }
                    Text(
                        text = statusText,
                        fontFamily = IBMPlexSans,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = statusColor,
                        modifier = Modifier
                            .border(1.dp, statusColor, RoundedCornerShape(2.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = LedgerRed.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BrassDivider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Body
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        fontFamily = Fraunces,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = InkPrimary
                    )
                    if (account.dueDateMillis != null && balance != 0.0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(account.dueDateMillis))
                        Text(
                            text = "Due: $dateStr",
                            fontFamily = IBMPlexMono,
                            fontSize = 11.sp,
                            color = LedgerRed.copy(alpha = 0.8f)
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val originalAmount = account.limitAmount ?: 0.0
                    val outstandingAmount = abs(balance)

                    if (originalAmount > 0.0 && outstandingAmount < originalAmount && outstandingAmount > 0.0) {
                        val paidAmount = originalAmount - outstandingAmount
                        Text(
                            text = String.format(Locale.getDefault(), "Paid: ₹%,.2f", paidAmount),
                            fontFamily = IBMPlexSans,
                            fontSize = 11.sp,
                            color = DeepForestIncome
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "Due: ₹%,.2f", outstandingAmount),
                            fontFamily = IBMPlexMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (balance < 0) LedgerRed else DeepForestIncome
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = String.format(Locale.getDefault(), "Original: ₹%,.2f", originalAmount),
                            fontFamily = IBMPlexMono,
                            fontSize = 11.sp,
                            color = InkPrimary.copy(alpha = 0.5f)
                        )
                    } else {
                        val displayAmount = if (balance != 0.0) outstandingAmount else originalAmount
                        Text(
                            text = String.format(Locale.getDefault(), "₹%,.2f", displayAmount),
                            fontFamily = IBMPlexMono,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = if (balance < 0) LedgerRed else if (balance > 0) DeepForestIncome else InkPrimary
                        )
                    }
                    if (balance != 0.0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onSettle,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (balance < 0) LedgerRed else DeepForestIncome
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                            modifier = Modifier.height(28.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "SETTLE",
                                fontFamily = IBMPlexSans,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            if (repayments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = BrassDivider.copy(alpha = 0.2f), thickness = 0.5.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "REPAYMENT HISTORY",
                    fontFamily = IBMPlexMono,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp,
                    color = InkPrimary.copy(alpha = 0.5f),
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repayments.forEach { tx ->
                        val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(tx.dateMillis))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Paid: ₹%,.2f (%s)".format(tx.amount, tx.note ?: "Partial Payment"),
                                fontFamily = IBMPlexSans,
                                fontSize = 12.sp,
                                color = InkPrimary.copy(alpha = 0.7f)
                            )
                            Text(
                                text = dateStr,
                                fontFamily = IBMPlexMono,
                                fontSize = 10.sp,
                                color = InkPrimary.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "---------------------------------------------",
                fontFamily = IBMPlexMono,
                fontSize = 10.sp,
                color = InkPrimary.copy(alpha = 0.2f),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
