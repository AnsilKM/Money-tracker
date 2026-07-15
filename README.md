# Ledger & Money Tracker

A premium, offline-first personal finance ledger and money tracking Android application designed with a classic, high-aesthetic vintage paper journal interface. Built using **Jetpack Compose**, **Kotlin**, **Room Database**, and **Koin**.

---

## Key Features

### 1. Unified Ledger & Expense Tracking
* **Modern Expense Logging**: Quick and intuitive logging of income and daily expenses.
* **Smart Categorization**: Group payments into custom categories like Food, Transport, Utilities, etc.

### 2. Credit Cards Management
* **Flexible Billing Cycles**: Select billing dates (e.g., 10th of every month) to automatically group and view statements from the 11th of the current month to the 10th of the next.
* **Cycle Navigation**: Slide back and forth through past billing cycles with intuitive arrow controls.
* **1-31 Day Chip Picker**: Choose statement generation and payment due days via a modern day grid instead of standard calendars.

### 3. Personal Cash Book (Single Payments)
* **Lending & Borrowing**: Track cash lent to friends or borrowed from creditors.
* **Partial Repayments**: Supports customized partial settlement amounts.
* **In-Card Repayment History**: View full statement histories (amounts, custom notes, dates) directly inside the person's card in the main list.

### 4. Smart Loans & EMIs
* **Tenure & Start Months**: Specify interest, installment counts, and the precise calendar month the EMI starts.
* **Visual Status Timeline**: Displays color-coded pills for each installment (🟢 Paid, 🔴 Missed, ⚪ Upcoming) to track your schedule at a glance.
* **Missed EMI Backdating**: Tapping and paying a missed installment automatically backdates the transaction, shifting status pills in real time.

### 5. High-Aesthetic Vintage Design System
* **Ruled Paper Canvas**: Subtle grid lines and textured paper colors (`#F1EADB`) replicating a real leather notebook.
* **Typography**: Fraunces serif typeface combined with IBM Plex Mono for account ledgers.
* **Clean Layouts**: Translucent, borderless, transparent navigation top-bars maximizing canvas readability.

---

## Tech Stack & Architecture

* **UI**: 100% Jetpack Compose with custom material components.
* **Database**: Room Persistence Library for secure, high-speed local SQL storage.
* **Dependency Injection**: Koin for Android dependency management and viewModel scoping.
* **Navigation**: Jetpack Compose Navigation architecture with smooth transitions.

---

## Getting Started

1. Clone the repository:
   ```bash
   git clone https://github.com/AnsilKM/Money-tracker.git
   ```
2. Open the project in **Android Studio (Ladybug or newer)**.
3. Sync Gradle and build the `:app` target on an emulator or physical device.
