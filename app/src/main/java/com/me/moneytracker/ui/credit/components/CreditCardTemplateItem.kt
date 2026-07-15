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
import com.me.moneytracker.ui.theme.BrassDivider
import com.me.moneytracker.ui.theme.Fraunces
import com.me.moneytracker.ui.theme.IBMPlexMono
import com.me.moneytracker.ui.theme.IBMPlexSans
import com.me.moneytracker.ui.theme.LedgerTheme
import androidx.compose.ui.tooling.preview.Preview
import java.util.Locale
import kotlin.math.abs

@Composable
fun CreditCardTemplateItem(
    account: CreditAccount,
    balance: Double,
    onClick: () -> Unit
) {
    val scheme = CardColorSchemes.getOrElse(account.colorScheme) { CardColorSchemes[0] }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .aspectRatio(1.886f) // Card ratio
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
