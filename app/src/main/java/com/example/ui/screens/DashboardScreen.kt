package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.Booking
import com.example.data.model.UserRole
import com.example.ui.RentDarViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.BookingStatusChip
import com.example.ui.components.MoroccanPlateBadge
import com.example.ui.components.StatCard
import com.example.ui.components.WhatsAppActionButton
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import com.example.util.StringsHelper

@Composable
fun DashboardScreen(
  viewModel: RentDarViewModel,
  modifier: Modifier = Modifier
) {
  val stats by viewModel.dashboardStats.collectAsState()
  val cars by viewModel.cars.collectAsState()
  val agency by viewModel.agency.collectAsState()
  val role by viewModel.currentRole.collectAsState()
  val lang by viewModel.currentLanguage.collectAsState()
  val context = LocalContext.current

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      // Agency Header & Date
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = EmeraldGreenDark,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(18.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = agency.name,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
              )
              Text(
                text = "${agency.city} • ${agency.plan} (14j essai gratuit)",
                color = EmeraldGreenLight,
                fontSize = 12.sp
              )
            }
            // Role indicator pill
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (role == UserRole.OWNER) AmberGold else Color(0xFF475569)
            ) {
              Text(
                text = StringsHelper.userRoleLabel(role, lang),
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = StringsHelper.tr(
              lang,
              "Aujourd'hui : Samedi 07 Novembre 2026",
              "اليوم: السبت 07 نونبر 2026"
            ),
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 13.sp
          )
        }
      }
    }

    // Overdue Returns Alert Card (if any!)
    if (stats.overdueReturns.isNotEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = StatusRedBg),
          border = BorderStroke(1.5.dp, StatusRed),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = StatusRed,
                modifier = Modifier.size(22.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = StringsHelper.tr(
                  lang,
                  "⚠️ ALERTE : RETOUR EN RETARD (${stats.overdueReturns.size})",
                  "⚠️ تنبيه: تأخير في الإرجاع (${stats.overdueReturns.size})"
                ),
                color = StatusRed,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
            }
            Spacer(modifier = Modifier.height(8.dp))
            stats.overdueReturns.forEach { overdueBk ->
              Column(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color.White)
                  .clickable { viewModel.selectBooking(overdueBk) }
                  .padding(10.dp)
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = overdueBk.clientName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                  )
                  MoroccanPlateBadge(plate = overdueBk.carPlate)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = StringsHelper.tr(
                    lang,
                    "${overdueBk.carName} • Prévu le ${StringsHelper.formatDate(overdueBk.returnDate)} à ${overdueBk.returnTime}",
                    "${overdueBk.carName} • موعد الإرجاع كان في ${StringsHelper.formatDate(overdueBk.returnDate)}"
                  ),
                  fontSize = 12.sp,
                  color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.height(8.dp))
                WhatsAppActionButton(
                  label = StringsHelper.tr(lang, "Relancer sur WhatsApp", "مراسلة عبر واتساب"),
                  phone = overdueBk.clientPhone,
                  message = StringsHelper.createReturnReminderMsg(overdueBk, agency.name, lang),
                  modifier = Modifier.fillMaxWidth()
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
            }
          }
        }
      }
    }

    // 4 Key Stats in 2x2 grid
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatCard(
            title = StringsHelper.tr(lang, "Départs du jour", "استلامات اليوم"),
            value = "${stats.todayPickups.size}",
            subtitle = StringsHelper.tr(lang, "Prises en charge", "حجوزات تبدأ اليوم"),
            icon = Icons.Default.DirectionsCar,
            iconTint = EmeraldGreen,
            modifier = Modifier.weight(1f)
          )
          StatCard(
            title = StringsHelper.tr(lang, "Retours du jour", "إرجاعات اليوم"),
            value = "${stats.todayReturns.size}",
            subtitle = StringsHelper.tr(lang, "Restitutions prévues", "سيارات تعود اليوم"),
            icon = Icons.Default.Event,
            iconTint = AmberGold,
            modifier = Modifier.weight(1f)
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          StatCard(
            title = StringsHelper.tr(lang, "Disponibles", "السيارات المتاحة"),
            value = "${stats.availableCarsCount} / ${stats.totalCarsCount}",
            subtitle = StringsHelper.tr(lang, "Prêtes à louer", "جاهزة للكراء"),
            icon = Icons.Default.CheckCircle,
            iconTint = StatusGreen,
            modifier = Modifier.weight(1f),
            onClick = { viewModel.setTab(ScreenTab.CARS) }
          )
          StatCard(
            title = StringsHelper.tr(lang, "En retard", "متأخرة"),
            value = "${stats.overdueReturns.size}",
            subtitle = StringsHelper.tr(lang, "À contacter d'urgence", "تتطلب تدخلاً"),
            icon = Icons.Default.Warning,
            iconTint = StatusRed,
            modifier = Modifier.weight(1f)
          )
        }
      }
    }

    // Financial Overview (Owner Only vs Employee Restricted)
    item {
      if (role == UserRole.OWNER) {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, CardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.TrendingUp,
                  contentDescription = null,
                  tint = EmeraldGreen,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = StringsHelper.tr(lang, "Revenus & Trésorerie", "المداخيل والمالية"),
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                )
              }
              Text(
                text = StringsHelper.tr(lang, "Novembre 2026", "نونبر 2026"),
                fontSize = 12.sp,
                color = Color(0xFF64748B)
              )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(
                  text = StringsHelper.tr(lang, "Revenus encaissés", "المداخيل المحصلة"),
                  fontSize = 12.sp,
                  color = Color(0xFF64748B)
                )
                Text(
                  text = StringsHelper.formatCurrency(stats.monthlyIncome, lang),
                  fontSize = 20.sp,
                  fontWeight = FontWeight.Bold,
                  color = EmeraldGreen
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(
                  text = StringsHelper.tr(lang, "Soldes impayés", "الديون غير المستخلصة"),
                  fontSize = 12.sp,
                  color = Color(0xFF64748B)
                )
                Text(
                  text = StringsHelper.formatCurrency(stats.totalUnpaidBalance, lang),
                  fontSize = 18.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (stats.totalUnpaidBalance > 0) StatusOrange else StatusGreen
                )
              }
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
              text = StringsHelper.tr(lang, "Revenus par véhicule :", "المداخيل حسب السيارة:"),
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF334155)
            )
            Spacer(modifier = Modifier.height(8.dp))
            val maxIncome = (stats.carIncomeMap.values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
            cars.forEach { car ->
              val inc = stats.carIncomeMap.getOrDefault(car.id, 0.0)
              val progress = (inc / maxIncome).toFloat().coerceIn(0f, 1f)
              Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(text = car.name, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                  Text(
                    text = StringsHelper.formatCurrency(inc, lang),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                  progress = { progress },
                  modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                  color = EmeraldGreen,
                  trackColor = Color(0xFFE2E8F0)
                )
              }
            }
          }
        }
      } else {
        // Employee role notice
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
          border = BorderStroke(1.dp, CardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.Lock,
              contentDescription = null,
              tint = Color(0xFF64748B),
              modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = StringsHelper.tr(lang, "Vue financière masquée", "المداخيل محجوبة"),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color(0xFF334155)
              )
              Text(
                text = StringsHelper.tr(
                  lang,
                  "En tant qu'employé, les totaux de revenus et montants financiers sont restreints.",
                  "بصفتك مستخدماً، لا يمكنك الاطلاع على المداخيل الإجمالية."
                ),
                fontSize = 11.sp,
                color = Color(0xFF64748B)
              )
            }
          }
        }
      }
    }

    // Quick Action Buttons
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = { viewModel.openNewBooking() },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("new_booking_quick_button")
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = StringsHelper.tr(lang, "Réservation", "حجز جديد"),
            fontWeight = FontWeight.Bold
          )
        }

        OutlinedButton(
          onClick = { viewModel.openAddCar() },
          shape = RoundedCornerShape(10.dp),
          border = BorderStroke(1.5.dp, EmeraldGreen),
          modifier = Modifier
            .weight(1f)
            .height(48.dp)
            .testTag("add_car_quick_button")
        ) {
          Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = StringsHelper.tr(lang, "Véhicule", "إضافة سيارة"),
            color = EmeraldGreen,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // Alerts Bar Shortcut
    item {
      val alerts = viewModel.getAlerts()
      val urgent = alerts.filter { it.severity == com.example.data.model.AlertSeverity.CRITICAL || it.severity == com.example.data.model.AlertSeverity.WARNING }
      if (urgent.isNotEmpty()) {
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = AmberGold.copy(alpha = 0.1f)),
          border = BorderStroke(1.dp, AmberGold.copy(alpha = 0.4f)),
          modifier = Modifier
            .fillMaxWidth()
            .clickable { viewModel.setTab(ScreenTab.ALERTS) }
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = AmberGold)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = StringsHelper.tr(
                  lang,
                  "${urgent.size} alertes d'échéance (Assurance, Vignette...)",
                  "${urgent.size} تنبيهات قريبة (تأمين، ضريبة، زيت...)"
                ),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF78350F)
              )
            }
            Text(
              text = StringsHelper.tr(lang, "Voir >", "عرض >"),
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = AmberGold
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
