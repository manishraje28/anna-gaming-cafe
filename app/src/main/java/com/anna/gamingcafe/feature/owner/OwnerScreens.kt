package com.anna.gamingcafe.feature.owner

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.anna.gamingcafe.core.components.*
import com.anna.gamingcafe.core.theme.*
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════════
// OWNER SHELL (Tab Container)
// ═══════════════════════════════════════════════
@Composable
fun OwnerShell(dashVm: OwnerDashboardViewModel, chatVm: OwnerChatViewModel, onSignOut: () -> Unit) {
    var activeTab by remember { mutableStateOf("dashboard") }
    val tabs = listOf("dashboard" to Icons.Default.Dashboard, "bookings" to Icons.Default.EventNote,
        "stations" to Icons.Default.SportsEsports, "customers" to Icons.Default.People,
        "chat" to Icons.Default.Forum, "profile" to Icons.Default.Person)

    Scaffold(
        containerColor = BackBg,
        bottomBar = {
            NavigationBar(containerColor = SurfaceCard, tonalElevation = 0.dp,
                modifier = Modifier.border(BorderStroke(1.dp, EdgeBorder), RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))) {
                tabs.forEach { (id, icon) ->
                    val sel = activeTab == id
                    NavigationBarItem(selected = sel, onClick = { activeTab = id },
                        icon = { Icon(icon, null, modifier = Modifier.size(20.dp)) },
                        label = { Text(id.replaceFirstChar { it.uppercase() }, fontSize = 9.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) },
                        colors = NavigationBarItemDefaults.colors(selectedIconColor = NeonPink, unselectedIconColor = TextMuted,
                            selectedTextColor = TextPrimary, unselectedTextColor = TextMuted, indicatorColor = NeonPink.copy(0.1f)))
                }
            }
        }
    ) { pad ->
        Box(Modifier.padding(pad)) {
            when (activeTab) {
                "dashboard" -> OwnerDashboard(dashVm)
                "bookings" -> OwnerBookings(dashVm)
                "stations" -> OwnerStations(dashVm)
                "customers" -> OwnerCustomers(dashVm)
                "chat" -> OwnerChat(chatVm)
                "profile" -> OwnerProfile(onSignOut)
            }
        }
    }
}

// ═══════════════════════════════════════════════
// DASHBOARD
// ═══════════════════════════════════════════════
@Composable
fun OwnerDashboard(vm: OwnerDashboardViewModel) {
    if (vm.isLoading) { LoadingScreen(); return }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Dashboard", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text("Real-time overview", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(bottom = 16.dp))

        // Stats Row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            data class Stat(val label: String, val value: String, val color: Color)
            listOf(Stat("REVENUE", formatRupees(vm.todayRevenue), NeonCyan),
                Stat("ACTIVE", "${vm.activeBookings}", LiveGreen),
                Stat("AVAILABLE", "${vm.availableStations}", StarGold)).forEach { st ->
                Box(Modifier.weight(1f).background(SurfaceCard, RoundedCornerShape(12.dp)).border(1.dp, EdgeBorder, RoundedCornerShape(12.dp)).padding(10.dp)) {
                    Column {
                        Text(st.label, color = TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(st.value, color = st.color, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Occupancy Chart
        AnnaCard(Modifier.padding(bottom = 14.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Weekly Occupancy", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                StatusBadge("+18% YoY", LiveGreen)
            }
            Spacer(Modifier.height(12.dp))
            Canvas(Modifier.fillMaxWidth().height(80.dp)) {
                val w = size.width; val h = size.height
                val pts = listOf(0.8f, 0.75f, 0.6f, 0.45f, 0.25f, 0.15f, 0.1f).mapIndexed { i, y -> Offset(w * i / 6f, h * y) }
                val path = Path().apply {
                    moveTo(pts[0].x, pts[0].y)
                    for (i in 1 until pts.size) {
                        val cp1 = Offset(pts[i - 1].x + (pts[i].x - pts[i - 1].x) / 2, pts[i - 1].y)
                        val cp2 = Offset(pts[i - 1].x + (pts[i].x - pts[i - 1].x) / 2, pts[i].y)
                        cubicTo(cp1.x, cp1.y, cp2.x, cp2.y, pts[i].x, pts[i].y)
                    }
                }
                val bgPath = Path().apply { addPath(path); lineTo(w, h); lineTo(0f, h); close() }
                drawPath(bgPath, Brush.verticalGradient(listOf(NeonCyan.copy(0.3f), Color.Transparent)))
                drawPath(path, NeonCyan, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
                pts.forEach { drawCircle(Color.White, 3.dp.toPx(), it); drawCircle(NeonCyan, 1.5.dp.toPx(), it) }
            }
            Spacer(Modifier.height(6.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun").forEach {
                    Text(it, color = TextMuted, fontSize = 8.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
        }

        // Recent Bookings
        SectionLabel("RECENT BOOKINGS")
        vm.bookings.take(5).forEach { bk ->
            AnnaCard(Modifier.padding(bottom = 8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text(bk.bookingRef, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("${bk.bookingDate} • ${bk.paymentMethod ?: "—"}", color = TextSecondary, fontSize = 9.sp)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(formatRupees(bk.totalPrice), color = StarGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        StatusBadge(bk.status, when (bk.status) { "COMPLETED" -> LiveGreen; "CANCELLED" -> ErrorRed; "ACTIVE" -> GlowBlue; else -> WarningOrange })
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// BOOKINGS MANAGEMENT
// ═══════════════════════════════════════════════
@Composable
fun OwnerBookings(vm: OwnerDashboardViewModel) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Bookings", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text("Manage all reservations", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))

        if (vm.bookings.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No bookings yet", color = TextMuted) }
        } else {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                vm.bookings.forEach { bk ->
                    AnnaCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(bk.bookingRef, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("${bk.bookingDate} • ${formatRupees(bk.totalPrice)}", color = TextSecondary, fontSize = 9.sp)
                            }
                            StatusBadge(bk.status, when (bk.status) { "COMPLETED" -> LiveGreen; "CANCELLED" -> ErrorRed; "ACTIVE" -> GlowBlue; else -> WarningOrange })
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (bk.status == "PENDING") {
                                if (bk.paymentStatus == "PENDING") {
                                    Button(onClick = { vm.updatePaymentStatus(bk.id, "PAID") }, colors = ButtonDefaults.buttonColors(containerColor = LiveGreen),
                                        shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f), contentPadding = PaddingValues(8.dp)) {
                                        Text("Mark Paid", color = BackBg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Button(onClick = { vm.updateBookingStatus(bk.id, "CANCELLED") }, colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                    shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f), contentPadding = PaddingValues(8.dp)) {
                                    Text("Cancel", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (bk.status == "CONFIRMED") {
                                Button(onClick = { vm.updateBookingStatus(bk.id, "ACTIVE") }, colors = ButtonDefaults.buttonColors(containerColor = GlowBlue),
                                    shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f), contentPadding = PaddingValues(8.dp)) {
                                    Text("Start Session", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            if (bk.status == "ACTIVE") {
                                Button(onClick = { vm.updateBookingStatus(bk.id, "COMPLETED") }, colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f), contentPadding = PaddingValues(8.dp)) {
                                    Text("Complete", color = BackBg, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ═══════════════════════════════════════════════
// STATIONS MANAGEMENT
// ═══════════════════════════════════════════════
@Composable
fun OwnerStations(vm: OwnerDashboardViewModel) {
    var showAdd by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newType by remember { mutableStateOf("PS5") }
    var newPrice by remember { mutableStateOf("250") }
    var newSpecs by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Stations", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                Text("Manage gaming setups", color = TextSecondary, fontSize = 11.sp)
            }
            Box(Modifier.background(NeonCyan.copy(0.15f), RoundedCornerShape(8.dp)).clickable { showAdd = true }.padding(horizontal = 10.dp, vertical = 6.dp)) {
                Text("+ Add", color = NeonCyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(12.dp))

        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            vm.stations.forEach { st ->
                AnnaCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(st.type, NeonCyan)
                            Spacer(Modifier.width(8.dp))
                            Text(st.name, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(formatRupees(st.pricePerHour) + "/hr", color = StarGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("Specs: ${st.specs}", color = TextSecondary, fontSize = 9.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("AVAILABLE" to LiveGreen, "OCCUPIED" to ErrorRed, "MAINTENANCE" to WarningOrange).forEach { (s, c) ->
                            val sel = st.status == s
                            Box(Modifier.clip(RoundedCornerShape(6.dp)).background(if (sel) c else Color.Transparent).border(0.5.dp, if (sel) c else EdgeBorder, RoundedCornerShape(6.dp))
                                .clickable { vm.updateStationStatus(st.id, s) }.padding(horizontal = 8.dp, vertical = 4.dp)) {
                                Text(s.take(4), color = if (sel) BackBg else TextMuted, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Station Dialog
    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false }, containerColor = SurfaceCard, shape = RoundedCornerShape(18.dp),
            title = { Text("Add Station", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(newName, { newName = it }, label = { Text("Name") }, singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Type: ", color = TextSecondary, fontSize = 11.sp)
                        listOf("PS5", "PC").forEach { t ->
                            AnnaChip(t, newType == t, { newType = t })
                            Spacer(Modifier.width(6.dp))
                        }
                    }
                    OutlinedTextField(newPrice, { newPrice = it }, label = { Text("Price/hr (₹)") }, singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                    OutlinedTextField(newSpecs, { newSpecs = it }, label = { Text("Specs") },
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary))
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotBlank()) { vm.addStation(newName, newType, newPrice.toDoubleOrNull() ?: 250.0, newSpecs.ifBlank { "Standard setup" })
                        showAdd = false; newName = ""; newSpecs = "" }
                }, colors = ButtonDefaults.buttonColors(containerColor = LiveGreen)) { Text("Save", color = BackBg, fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel", color = TextMuted) } }
        )
    }
}

// ═══════════════════════════════════════════════
// CUSTOMERS
// ═══════════════════════════════════════════════
@Composable
fun OwnerCustomers(vm: OwnerDashboardViewModel) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Customers", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text("${vm.customers.size} registered users", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))

        if (vm.customers.isEmpty()) { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("No customers yet", color = TextMuted) } }
        else Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            vm.customers.forEach { c ->
                AnnaCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(32.dp).background(NeonPink.copy(0.15f), CircleShape), contentAlignment = Alignment.Center) {
                                Text(c.fullName.take(1).uppercase(), color = NeonPink, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(c.fullName.ifBlank { "Unknown" }, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(c.phone ?: c.email ?: "", color = TextSecondary, fontSize = 9.sp)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(formatRupees(c.totalSpend), color = StarGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("${c.totalVisits} visits", color = TextSecondary, fontSize = 9.sp)
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

// ═══════════════════════════════════════════════
// OWNER CHAT
// ═══════════════════════════════════════════════
@Composable
fun OwnerChat(vm: OwnerChatViewModel) {
    if (vm.isLoading) { LoadingScreen(); return }

    AnimatedContent(targetState = vm.selectedRoom != null, label = "chat_nav") { inChat ->
        if (inChat && vm.selectedRoom != null) {
            OwnerChatDetail(vm)
        } else {
            OwnerChatList(vm)
        }
    }
}

@Composable
fun OwnerChatList(vm: OwnerChatViewModel) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Support Inbox", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text("${vm.rooms.size} active conversations", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(bottom = 12.dp))

        if (vm.rooms.isEmpty()) {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Forum, null, tint = TextMuted, modifier = Modifier.size(40.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("No active chats", color = TextMuted, fontSize = 12.sp)
                }
            }
        } else {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                vm.rooms.forEach { room ->
                    AnnaCard(Modifier.clickable { vm.selectRoom(room) }) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Box(Modifier.size(36.dp).background(NeonPink.copy(0.15f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Person, null, tint = NeonPink, modifier = Modifier.size(18.dp))
                            }
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Customer Chat", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text("Room: ${room.id.take(8)}...", color = TextSecondary, fontSize = 9.sp)
                            }
                            StatusBadge("OPEN", LiveGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OwnerChatDetail(vm: OwnerChatViewModel) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(vm.messages.size) {
        if (vm.messages.isNotEmpty()) listState.animateScrollToItem(vm.messages.size - 1)
    }

    Column(Modifier.fillMaxSize().imePadding()) {
        // Header
        Surface(color = SurfaceCard, shadowElevation = 2.dp) {
            Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { vm.backToList() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = NeonPink, modifier = Modifier.size(20.dp))
                }
                Text("Customer Chat", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Messages
        LazyColumn(Modifier.weight(1f).fillMaxWidth().background(BackBg), state = listState,
            contentPadding = PaddingValues(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            items(vm.messages, key = { it.id }) { msg ->
                val isMe = msg.senderId == vm.currentUserId
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start) {
                    Surface(
                        modifier = Modifier.widthIn(max = 280.dp),
                        color = if (isMe) NeonPink.copy(0.12f) else SurfaceCard,
                        border = BorderStroke(0.5.dp, if (isMe) NeonPink.copy(0.3f) else EdgeBorder),
                        shape = RoundedCornerShape(16.dp, 16.dp, if (isMe) 4.dp else 16.dp, if (isMe) 16.dp else 4.dp)
                    ) {
                        Column(Modifier.padding(12.dp, 8.dp)) {
                            Text(msg.content, color = TextPrimary, fontSize = 13.sp, lineHeight = 18.sp)
                            val time = msg.createdAt?.let { try { it.substringAfter("T").take(5) } catch (_: Exception) { "" } } ?: ""
                            Text(time, color = TextMuted, fontSize = 9.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
                        }
                    }
                }
            }
        }

        // Input
        Surface(color = SurfaceCard) {
            Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.Bottom) {
                OutlinedTextField(vm.inputText, vm::updateInput, Modifier.weight(1f), placeholder = { Text("Reply...", color = TextMuted) }, maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = TextPrimary, unfocusedTextColor = TextPrimary, focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent, focusedContainerColor = SurfaceElevated, unfocusedContainerColor = SurfaceElevated, cursorColor = NeonPink),
                    shape = RoundedCornerShape(22.dp))
                Spacer(Modifier.width(6.dp))
                Box(Modifier.size(44.dp).clip(CircleShape).background(if (vm.inputText.isNotBlank()) NeonPink else NeonPink.copy(0.3f))
                    .clickable(vm.inputText.isNotBlank()) { vm.sendReply(); scope.launch { if (vm.messages.isNotEmpty()) listState.animateScrollToItem(vm.messages.size - 1) } },
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.AutoMirrored.Filled.Send, null, tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════════
// OWNER PROFILE
// ═══════════════════════════════════════════════
@Composable
fun OwnerProfile(onSignOut: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Settings", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text("Cafe configuration", color = TextSecondary, fontSize = 11.sp, modifier = Modifier.padding(bottom = 20.dp))

        AnnaCard(Modifier.padding(bottom = 12.dp)) {
            Text("Cafe Info", color = NeonPink, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            Spacer(Modifier.height(8.dp))
            listOf("Name" to "Anna Gaming Cafe", "Hours" to "09:00 AM – 03:00 AM", "UPI ID" to "annagaming@upi").forEach { (k, v) ->
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(k, color = TextSecondary, fontSize = 11.sp); Text(v, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(onClick = onSignOut, Modifier.fillMaxWidth(), border = BorderStroke(1.dp, ErrorRed.copy(0.5f)), shape = RoundedCornerShape(12.dp)) {
            Text("Sign Out", color = ErrorRed, fontWeight = FontWeight.Bold)
        }
    }
}
