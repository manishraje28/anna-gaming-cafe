package com.anna.gamingcafe.feature.customer

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anna.gamingcafe.core.components.*
import com.anna.gamingcafe.core.theme.*

// ═══════════════════════════════════════════════
// HOME SCREEN
// ═══════════════════════════════════════════════
@Composable
fun HomeScreen(viewModel: HomeViewModel, onNavigate: (String) -> Unit) {
    val profile = viewModel.profile
    if (viewModel.isLoading) { LoadingScreen(); return }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Welcome
        Text("Welcome back, ${profile?.fullName ?: "Gamer"} ⚡", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text("Your premium gaming companion", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(bottom = 16.dp))

        // Loyalty Card
        AnnaCard(modifier = Modifier.padding(bottom = 16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("VANGUARD ELITE", color = GlowBlue, fontSize = 8.sp, fontWeight = FontWeight.Black, letterSpacing = 1.sp)
                    Text("Earn Free Session", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                StatusBadge("${profile?.loyaltyStars ?: 0}★", StarGold)
            }
            Spacer(Modifier.height(12.dp))
            val progress = (profile?.loyaltyStars ?: 0) / 500f
            LinearProgressIndicator(progress = { progress.coerceIn(0f, 1f) }, color = NeonCyan, trackColor = EdgeBorder,
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape))
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${profile?.loyaltyStars ?: 0} Stars", color = TextSecondary, fontSize = 9.sp)
                Text("500 Goal", color = NeonCyan, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Quick Actions
        Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            data class QA(val label: String, val icon: ImageVector, val tab: String)
            val actions = listOf(QA("Reserve", Icons.Default.CalendarMonth, "book"), QA("Games", Icons.Default.SportsEsports, "games"),
                QA("Chat", Icons.Default.Forum, "chat"), QA("Wallet", Icons.Default.AccountBalanceWallet, "wallet"))
            actions.forEach { qa ->
                Box(
                    modifier = Modifier.weight(1f).background(SurfaceCard, RoundedCornerShape(12.dp))
                        .border(1.dp, EdgeBorder, RoundedCornerShape(12.dp)).clickable { onNavigate(qa.tab) }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(qa.icon, null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(qa.label, color = TextPrimary, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Promotions
        SectionLabel("HIGHLIGHTS & PROMOTIONS")
        viewModel.promotions.forEach { promo ->
            AnnaCard(modifier = Modifier.padding(bottom = 10.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(promo.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    StatusBadge(promo.badgeText, try { Color(android.graphics.Color.parseColor(promo.badgeColor)) } catch (_: Exception) { NeonCyan })
                }
                Spacer(Modifier.height(6.dp))
                Text(promo.description, color = TextSecondary, fontSize = 10.sp, lineHeight = 14.sp)
            }
        }

        // Pricing
        SectionLabel("HOURLY RATES")
        AnnaCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("PS5 Standard Couch", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("₹250/hr", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Esports PC Rig", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("₹350/hr", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Pro Arena PC (RTX 4090)", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text("₹450/hr", color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = EdgeBorder)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccessTime, null, tint = StarGold, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(6.dp))
                Text("Open daily: 09:00 AM – 03:00 AM", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

// ═══════════════════════════════════════════════
// BOOKING SCREEN
// ═══════════════════════════════════════════════
@Composable
fun BookingScreen(viewModel: BookingViewModel) {
    val s = viewModel.uiState
    if (s.isLoading) { LoadingScreen(); return }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Book a Station", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text("Reserve your gaming setup in real-time", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(bottom = 16.dp))

        // Platform
        SectionLabel("1. PLATFORM")
        Row(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            listOf("PS5" to Icons.Default.Tv, "PC" to Icons.Default.Monitor).forEach { (p, icon) ->
                Box(
                    Modifier.weight(1f).background(if (s.selectedPlatform == p) NeonCyan.copy(0.1f) else SurfaceDark, RoundedCornerShape(10.dp))
                        .border(1.dp, if (s.selectedPlatform == p) NeonCyan else EdgeBorder, RoundedCornerShape(10.dp))
                        .clickable { viewModel.selectPlatform(p) }.padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(icon, null, tint = if (s.selectedPlatform == p) NeonCyan else TextSecondary, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(if (p == "PS5") "PlayStation 5" else "Esports PC", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Date
        SectionLabel("2. DATE")
        LazyRow(Modifier.fillMaxWidth().padding(bottom = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(viewModel.dateOptions.size) { i ->
                AnnaChip(viewModel.dateOptions[i], s.selectedDateIndex == i, { viewModel.selectDate(i) })
            }
        }

        // Package
        SectionLabel("3. PACKAGE")
        s.packages.forEachIndexed { i, pkg ->
            val sel = s.selectedPackageIndex == i
            AnnaCard(modifier = Modifier.padding(bottom = 8.dp).clickable { viewModel.selectPackage(i) }.then(
                if (sel) Modifier.border(1.dp, NeonCyan, RoundedCornerShape(14.dp)) else Modifier
            )) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(pkg.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(pkg.description, color = TextSecondary, fontSize = 9.sp)
                    }
                    Text(formatRupees(pkg.basePrice), color = StarGold, fontSize = 13.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        // Station
        SectionLabel("4. STATION")
        val filtered = s.stations.filter { it.type == s.selectedPlatform }
        if (filtered.isEmpty()) {
            Text("No stations available", color = TextMuted, fontSize = 11.sp)
        }
        filtered.forEach { st ->
            val sel = s.selectedStationId == st.id
            val avail = st.status == "AVAILABLE"
            AnnaCard(modifier = Modifier.padding(bottom = 8.dp).clickable(enabled = avail) { viewModel.selectStation(st.id) }.then(
                if (sel) Modifier.border(1.dp, NeonCyan, RoundedCornerShape(14.dp)) else Modifier
            )) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(6.dp).background(if (avail) LiveGreen else ErrorRed, CircleShape))
                        Spacer(Modifier.width(8.dp))
                        Text(st.name, color = if (avail) TextPrimary else TextMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(st.status, color = if (avail) LiveGreen else ErrorRed, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
                Text("Specs: ${st.specs}", color = TextSecondary, fontSize = 9.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }

        // Payment Method
        if (s.selectedStationId.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            SectionLabel("5. PAYMENT")
            Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("UPI", "CASH", "WALLET").forEach { m ->
                    AnnaChip(
                        label = when(m) { "WALLET" -> "Wallet (${formatRupees(s.walletBalance)})"; else -> m },
                        selected = s.selectedPaymentMethod == m,
                        onClick = { viewModel.selectPaymentMethod(m) }
                    )
                }
            }
        }

        // Error
        s.error?.let {
            Text(it, color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(bottom = 8.dp))
        }

        // Confirm
        Spacer(Modifier.height(8.dp))
        val pkg = s.packages.getOrNull(s.selectedPackageIndex)
        Button(
            onClick = { viewModel.confirmBooking() },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = s.selectedStationId.isNotEmpty() && !s.isBooking,
            colors = ButtonDefaults.buttonColors(containerColor = NeonCyan, disabledContainerColor = NeonCyan.copy(0.3f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            if (s.isBooking) CircularProgressIndicator(Modifier.size(20.dp), color = BackBg, strokeWidth = 2.dp)
            else Text("CONFIRM BOOKING (${formatRupees(pkg?.basePrice ?: 0.0)})", color = BackBg, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(Modifier.height(16.dp))

        // Confirmation Dialog
        s.confirmedBooking?.let { bk ->
            AlertDialog(
                onDismissRequest = { viewModel.dismissConfirmation() },
                containerColor = SurfaceCard,
                shape = RoundedCornerShape(18.dp),
                title = {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(40.dp).background(LiveGreen.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Check, null, tint = LiveGreen, modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("Booking Confirmed!", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                    }
                },
                text = {
                    Column(Modifier.fillMaxWidth().background(BackBg, RoundedCornerShape(12.dp)).border(1.dp, EdgeBorder, RoundedCornerShape(12.dp)).padding(12.dp)) {
                        listOf("REF" to bk.bookingRef, "AMOUNT" to formatRupees(bk.totalPrice), "PAYMENT" to (bk.paymentMethod ?: "—"), "STATUS" to bk.status).forEach { (k, v) ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(k, color = TextMuted, fontSize = 9.sp)
                                Text(v, color = if (k == "AMOUNT") StarGold else NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { viewModel.dismissConfirmation() }) { Text("Done", color = NeonCyan, fontWeight = FontWeight.Bold) } }
            )
        }
    }
}

// ═══════════════════════════════════════════════
// GAMES CATALOG SCREEN
// ═══════════════════════════════════════════════
@Composable
fun GamesCatalogScreen(viewModel: GamesViewModel) {
    if (viewModel.isLoading) { LoadingScreen(); return }
    val focusManager = LocalFocusManager.current

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Text("Game Library", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text("Browse available titles", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))

        // Search
        OutlinedTextField(
            value = viewModel.searchQuery, onValueChange = viewModel::updateSearch,
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            placeholder = { Text("Search games...", color = TextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary, modifier = Modifier.size(18.dp)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary,
                focusedBorderColor = NeonCyan, unfocusedBorderColor = EdgeBorder, focusedContainerColor = SurfaceDark, unfocusedContainerColor = SurfaceDark, cursorColor = NeonCyan),
            shape = RoundedCornerShape(12.dp)
        )

        // Filters
        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("ALL", "PS5", "PC").forEach { p ->
                AnnaChip(if (p == "ALL") "All" else p, viewModel.platformFilter == p, { viewModel.updatePlatformFilter(p) })
            }
        }

        // Games list
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (viewModel.filteredGames.isEmpty()) {
                Text("No games found", color = TextMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 16.dp))
            }
            viewModel.filteredGames.forEach { game ->
                AnnaCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(game.title, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.width(6.dp))
                                StatusBadge(game.platform, NeonCyan)
                            }
                            Text(game.category, color = TextSecondary, fontSize = 9.sp, modifier = Modifier.padding(top = 2.dp))
                        }
                        Icon(Icons.Default.SportsEsports, null, tint = TextMuted, modifier = Modifier.size(20.dp))
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ═══════════════════════════════════════════════
// WALLET & PROFILE SCREEN
// ═══════════════════════════════════════════════
@Composable
fun WalletScreen(viewModel: WalletViewModel, onSignOut: () -> Unit) {
    val profile = viewModel.profile
    if (viewModel.isLoading) { LoadingScreen(); return }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("My Profile", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
        Text("Wallet, history & settings", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(bottom = 16.dp))

        // Wallet Card
        Box(
            Modifier.fillMaxWidth().height(160.dp)
                .background(Brush.linearGradient(PremiumGradient), RoundedCornerShape(16.dp)).padding(16.dp)
        ) {
            Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("ANNA DIGITAL CARD", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Icon(Icons.Default.CreditCard, null, tint = Color.White, modifier = Modifier.size(16.dp))
                }
                Column {
                    Text("Balance", color = Color.White.copy(0.7f), fontSize = 9.sp)
                    Text(formatRupees(profile?.walletBalance ?: 0.0), color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Black)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ref: ${profile?.referralCode ?: "—"}", color = Color.White.copy(0.8f), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    Text(profile?.fullName?.uppercase() ?: "", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Top Up
        SectionLabel("QUICK TOP-UP")
        Row(Modifier.fillMaxWidth().padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(250, 500, 1000, 2000).forEach { amt ->
                AnnaChip("₹$amt", viewModel.selectedTopUp == amt, { viewModel.selectTopUp(amt) }, accentColor = LiveGreen)
            }
        }

        Button(
            onClick = { viewModel.toggleTopUpDialog() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LiveGreen),
            shape = RoundedCornerShape(12.dp)
        ) { Text("Top Up ₹${viewModel.selectedTopUp}", color = BackBg, fontWeight = FontWeight.Bold, fontSize = 12.sp) }

        // Stats
        SectionLabel("STATS")
        AnnaCard(Modifier.padding(bottom = 12.dp)) {
            listOf("Loyalty Stars" to "${profile?.loyaltyStars ?: 0}★", "Total Visits" to "${profile?.totalVisits ?: 0}",
                "Total Spent" to formatRupees(profile?.totalSpend ?: 0.0)).forEach { (k, v) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(k, color = TextSecondary, fontSize = 11.sp)
                    Text(v, color = NeonCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Booking History
        if (viewModel.bookings.isNotEmpty()) {
            SectionLabel("RECENT BOOKINGS")
            viewModel.bookings.take(5).forEach { bk ->
                AnnaCard(Modifier.padding(bottom = 8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text(bk.bookingRef, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(bk.bookingDate, color = TextSecondary, fontSize = 9.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatRupees(bk.totalPrice), color = StarGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            StatusBadge(bk.status, when (bk.status) { "COMPLETED" -> LiveGreen; "CANCELLED" -> ErrorRed; else -> NeonCyan })
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        // Sign Out
        OutlinedButton(
            onClick = onSignOut, modifier = Modifier.fillMaxWidth(),
            border = BorderStroke(1.dp, ErrorRed.copy(0.5f)), shape = RoundedCornerShape(12.dp)
        ) { Text("Sign Out", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
        Spacer(Modifier.height(24.dp))
    }

    // Top-up Dialog
    if (viewModel.showTopUpDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.toggleTopUpDialog() },
            containerColor = SurfaceCard,
            title = { Text("Confirm Top-Up", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = { Text("Add ₹${viewModel.selectedTopUp} to your wallet via UPI?", color = TextSecondary, fontSize = 12.sp) },
            confirmButton = { TextButton(onClick = { viewModel.confirmTopUp() }) { Text("Confirm", color = LiveGreen, fontWeight = FontWeight.Bold) } },
            dismissButton = { TextButton(onClick = { viewModel.toggleTopUpDialog() }) { Text("Cancel", color = TextMuted) } }
        )
    }
}
