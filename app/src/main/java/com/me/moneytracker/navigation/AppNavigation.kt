package com.me.moneytracker.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.me.moneytracker.ui.addexpense.AddExpenseScreen
import com.me.moneytracker.ui.home.HomeScreen
import com.me.moneytracker.ui.reports.ReportsScreen
import com.me.moneytracker.ui.splash.SplashScreen
import com.me.moneytracker.ui.credit.CreditListScreen
import com.me.moneytracker.ui.credit.CreditDetailsScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = Modifier.fillMaxSize(),
        enterTransition = { fadeIn(animationSpec = tween(200)) },
        exitTransition = { fadeOut(animationSpec = tween(200)) },
        popEnterTransition = { fadeIn(animationSpec = tween(200)) },
        popExitTransition = { fadeOut(animationSpec = tween(200)) }
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onNavigateToReports = {
                    navController.navigate("reports") {
                        launchSingleTop = true
                    }
                },
                onNavigateToCredits = {
                    navController.navigate("credit_list") {
                        launchSingleTop = true
                    }
                },
                onAddExpenseClick = {
                    navController.navigate("add_expense")
                }
            )
        }
        composable("add_expense") {
            AddExpenseScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("reports") {
            ReportsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToCredits = {
                    navController.navigate("credit_list") {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable("credit_list") {
            CreditListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToDetails = { accountId ->
                    navController.navigate("credit_details/$accountId")
                },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("home") { inclusive = false }
                        launchSingleTop = true
                    }
                },
                onNavigateToReports = {
                    navController.navigate("reports") {
                        launchSingleTop = true
                    }
                }
            )
        }
        composable(
            route = "credit_details/{accountId}",
            arguments = listOf(navArgument("accountId") { type = NavType.LongType })
        ) { backStackEntry ->
            val accountId = backStackEntry.arguments?.getLong("accountId") ?: 0L
            CreditDetailsScreen(
                accountId = accountId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
