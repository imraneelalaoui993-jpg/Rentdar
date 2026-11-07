package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.ui.RentDarViewModel
import com.example.ui.components.BookingStatusChip
import com.example.ui.components.MoroccanPlateBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed
import com.example.util.StringsHelper

@Composable
fun CalendarScreen(
  viewModel: RentDarViewModel,
  modifier: Modifier = Modifier
) {
  val cars by viewModel.cars.collectAsState()
  val bookings by viewModel.bookings.collectAsState()
  val lang by viewModel.currentLanguage.collectAsState()

  var selectedViewTab by remember { mutableIntStateOf(0) } // 0 = Monthly Grid, 1 = Per-Car Timeline
  var selectedDay by remember { mutableIntStateOf(7) } // Default to 7th (today: 2026-11-07)

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
  ) {
    Spacer(modifier = Modifier.height(8.dp))

    // View Selector Tabs
    TabRow(
      selectedTabIndex = selectedViewTab,
      containerColor = Color(0xFFF1F5F9),
      indicator = { tabPositions ->
        TabRowDefaults.SecondaryIndicator(
          modifier = Modifier.tabIndicatorOffset(tabPositions[selectedViewTab]),
          color = EmeraldGreen
        )
      },
      modifier = Modifier
        .clip(RoundedCornerShape(10.dp))
        .fillMaxWidth()
    ) {
      Tab(
        selected = selectedViewTab == 0,
        onClick = { selectedViewTab = 0 },
        text = {
          Text(
            StringsHelper.tr(lang, "Vue Mensuelle", "التقويم الشهري"),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (selectedViewTab == 0) EmeraldGreen else Color(0xFF64748B)
          )
        }
      )
      Tab(
        selected = selectedViewTab == 1,
        onClick = { selectedViewTab = 1 },
        text = {
          Text(
            StringsHelper.tr(lang, "Planning par Véhicule", "جدول السيارات"),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            color = if (selectedViewTab == 1) EmeraldGreen else Color(0xFF64748B)
          )
        }
      )
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Month Navigation Header
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Text(
        text = StringsHelper.tr(lang, "Novembre 2026", "نونبر 2026"),
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = Color(0xFF0F172A)
      )

      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = {}) {
          Icon(Icons.Default.ChevronLeft, contentDescription = "Mois précédent")
        }
        IconButton(onClick = {}) {
          Icon(Icons.Default.ChevronRight, contentDescription = "Mois suivant")
        }
      }
    }

    if (selectedViewTab == 0) {
      // Monthly Grid View
      MonthlyGridView(
        bookings = bookings,
        selectedDay = selectedDay,
        onDaySelected = { selectedDay = it },
        onBookingClick = { viewModel.selectBooking(it) },
        lang = lang,
        onNewBooking = { viewModel.openNewBooking() }
      )
    } else {
      // Per-Car Timeline View
      PerCarTimelineView(
        cars = cars,
        bookings = bookings,
        lang = lang,
        onBookingClick = { viewModel.selectBooking(it) }
      )
    }
  }
}

@Composable
fun MonthlyGridView(
  bookings: List<Booking>,
  selectedDay: Int,
  onDaySelected: (Int) -> Unit,
  onBookingClick: (Booking) -> Unit,
  lang: com.example.data.model.AppLanguage,
  onNewBooking: () -> Unit
) {
  val dayNames = if (lang == com.example.data.model.AppLanguage.AR) {
    listOf("إثن", "ثلا", "أرب", "خمي", "جمع", "سبت", "أحد")
  } else {
    listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")
  }

  // November 2026 starts on Sunday (offset 6 if Monday is 0)
  // 1 Nov 2026 is Sunday (index 6)
  val leadingEmptyDays = 6
  val daysInMonth = 30

  // Filter bookings for the selected day
  val selectedDateStr = "2026-11-%02d".format(selectedDay)
  val dayBookings = bookings.filter {
    it.pickupDate <= selectedDateStr && it.returnDate >= selectedDateStr
  }

  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder)
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          // Days of week header
          Row(modifier = Modifier.fillMaxWidth()) {
            dayNames.forEach { name ->
              Text(
                text = name,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF64748B)
              )
            }
          }
          Spacer(modifier = Modifier.height(8.dp))

          // Calendar days grid
          val totalCells = leadingEmptyDays + daysInMonth
          val rows = (totalCells + 6) / 7

          for (r in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
              for (c in 0 until 7) {
                val cellIndex = r * 7 + c
                val dayNumber = cellIndex - leadingEmptyDays + 1

                if (dayNumber in 1..daysInMonth) {
                  val dateStr = "2026-11-%02d".format(dayNumber)
                  val hasBooking = bookings.any {
                    it.pickupDate <= dateStr && it.returnDate >= dateStr
                  }
                  val isSelected = dayNumber == selectedDay
                  val isToday = dayNumber == 7

                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .aspectRatio(1f)
                      .padding(2.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(
                        when {
                          isSelected -> EmeraldGreen
                          isToday -> EmeraldGreenLight
                          else -> Color.Transparent
                        }
                      )
                      .clickable { onDaySelected(dayNumber) },
                    contentAlignment = Alignment.Center
                  ) {
                    Column(
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center
                    ) {
                      Text(
                        text = "$dayNumber",
                        fontSize = 12.sp,
                        fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                        color = when {
                          isSelected -> Color.White
                          isToday -> EmeraldGreen
                          else -> Color(0xFF1E293B)
                        }
                      )
                      if (hasBooking) {
                        Box(
                          modifier = Modifier
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) Color.White else AmberGold)
                        )
                      }
                    }
                  }
                } else {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }
          }
        }
      }
    }

    // Selected Day Header & Bookings List
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = StringsHelper.tr(
            lang,
            "Réservations du $selectedDay Novembre (${dayBookings.size})",
            "حجوزات يوم $selectedDay نونبر (${dayBookings.size})"
          ),
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          color = Color(0xFF0F172A)
        )

        Button(
          onClick = onNewBooking,
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.height(34.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = StringsHelper.tr(lang, "Réserver", "حجز جديد"),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    if (dayBookings.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
          border = BorderStroke(1.dp, CardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier.padding(24.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = StringsHelper.tr(
                lang,
                "Toutes les voitures sont disponibles ce jour-là.",
                "جميع السيارات متاحة في هذا اليوم."
              ),
              color = Color(0xFF64748B),
              fontSize = 13.sp
            )
          }
        }
      }
    } else {
      items(dayBookings) { bk ->
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, CardBorder),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { onBookingClick(bk) }
        ) {
          Column(modifier = Modifier.padding(12.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = bk.clientName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              BookingStatusChip(status = bk.status, lang = lang)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = bk.carName,
                fontSize = 13.sp,
                color = Color(0xFF475569)
              )
              MoroccanPlateBadge(plate = bk.carPlate)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "${StringsHelper.formatDate(bk.pickupDate)} (${bk.pickupTime}) ➔ ${StringsHelper.formatDate(bk.returnDate)} (${bk.returnTime})",
              fontSize = 12.sp,
              color = EmeraldGreen,
              fontWeight = FontWeight.Medium
            )
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}

@Composable
fun PerCarTimelineView(
  cars: List<com.example.data.model.Car>,
  bookings: List<Booking>,
  lang: com.example.data.model.AppLanguage,
  onBookingClick: (Booking) -> Unit
) {
  LazyColumn(
    modifier = Modifier.fillMaxSize(),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    items(cars) { car ->
      val carBookings = bookings.filter {
        it.carId == car.id && (it.status == BookingStatus.ACTIVE || it.status == BookingStatus.RESERVED)
      }

      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, CardBorder),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(text = car.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "${car.fuelType} • ${car.dailyPrice.toInt()} MAD/j",
                fontSize = 12.sp,
                color = Color(0xFF64748B)
              )
            }
            MoroccanPlateBadge(plate = car.plate)
          }

          Spacer(modifier = Modifier.height(12.dp))

          // 30 Days Mini Visual Timeline Bar for November 2026
          Text(
            text = StringsHelper.tr(lang, "Disponibilité Novembre 2026 :", "جاهزية السيارة لشهر نونبر 2026:"),
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            fontWeight = FontWeight.Medium
          )
          Spacer(modifier = Modifier.height(6.dp))

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(18.dp)
              .clip(RoundedCornerShape(4.dp))
              .background(Color(0xFFE2E8F0))
          ) {
            for (day in 1..30) {
              val dateStr = "2026-11-%02d".format(day)
              val bk = carBookings.find { it.pickupDate <= dateStr && it.returnDate >= dateStr }
              val color = when {
                bk?.status == BookingStatus.ACTIVE -> StatusGreen
                bk?.status == BookingStatus.RESERVED -> AmberGold
                else -> Color(0xFFD1FAE5) // Available
              }
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(18.dp)
                  .background(color)
                  .border(0.2.dp, Color.White)
              )
            }
          }

          // Legend for timeline
          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "01 Nov", fontSize = 10.sp, color = Color(0xFF64748B))
            Text(text = "15 Nov", fontSize = 10.sp, color = Color(0xFF64748B))
            Text(text = "30 Nov", fontSize = 10.sp, color = Color(0xFF64748B))
          }

          // Active bookings on this car
          if (carBookings.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            carBookings.forEach { bk ->
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(vertical = 4.dp)
                  .clickable { onBookingClick(bk) }
              ) {
                Row(
                  modifier = Modifier.padding(8.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp), tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = bk.clientName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                  }
                  Text(
                    text = "${StringsHelper.formatDate(bk.pickupDate)} ➔ ${StringsHelper.formatDate(bk.returnDate)}",
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                  )
                }
              }
            }
          }
        }
      }
    }
    item {
      Spacer(modifier = Modifier.height(40.dp))
    }
  }
}
