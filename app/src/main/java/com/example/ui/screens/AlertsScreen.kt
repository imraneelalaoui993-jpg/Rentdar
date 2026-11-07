package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSeverity
import com.example.data.model.AlertType
import com.example.data.model.CarAlert
import com.example.ui.RentDarViewModel
import com.example.ui.components.AlertSeverityChip
import com.example.ui.components.MoroccanPlateBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusOrangeBg
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import com.example.util.StringsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
  viewModel: RentDarViewModel,
  modifier: Modifier = Modifier
) {
  val lang by viewModel.currentLanguage.collectAsState()
  val cars by viewModel.cars.collectAsState()
  val alerts = viewModel.getAlerts()

  var selectedTypeFilter by remember { mutableStateOf<AlertType?>(null) }
  var alertToRenew by remember { mutableStateOf<CarAlert?>(null) }

  val filteredAlerts = alerts.filter { alert ->
    selectedTypeFilter == null || alert.type == selectedTypeFilter
  }

  val criticalCount = alerts.count { it.severity == AlertSeverity.CRITICAL }
  val warningCount = alerts.count { it.severity == AlertSeverity.WARNING }
  val noticeCount = alerts.count { it.severity == AlertSeverity.NOTICE }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = StringsHelper.tr(lang, "Alertes & Maintenance Véhicules", "تنبيهات وصيانة السيارات"),
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        color = Color(0xFF0F172A)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = StringsHelper.tr(
          lang,
          "Suivi des dates d'Assurance, Vignette fiscale, Contrôle technique et Vidange moteur.",
          "متابعة تواريخ التأمين، الضريبة السنوية، الفحص التقني وتغيير الزيت."
        ),
        fontSize = 12.sp,
        color = Color(0xFF64748B)
      )
    }

    // Severity Counters
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = StatusRedBg,
          border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.3f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$criticalCount", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = StatusRed)
            Text(
              text = StringsHelper.tr(lang, "Critiques / Retard", "حرجة / متأخرة"),
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = StatusRed
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = StatusOrangeBg,
          border = BorderStroke(1.dp, StatusOrange.copy(alpha = 0.3f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$warningCount", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = StatusOrange)
            Text(
              text = StringsHelper.tr(lang, "Sous 7 jours", "خلال 7 أيام"),
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = StatusOrange
            )
          }
        }

        Surface(
          shape = RoundedCornerShape(10.dp),
          color = Color(0xFFFEF9C3),
          border = BorderStroke(1.dp, Color(0xFFEAB308).copy(alpha = 0.3f)),
          modifier = Modifier.weight(1f)
        ) {
          Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "$noticeCount", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF854D0E))
            Text(
              text = StringsHelper.tr(lang, "Sous 30 jours", "خلال 30 يوماً"),
              fontSize = 10.sp,
              fontWeight = FontWeight.SemiBold,
              color = Color(0xFF854D0E)
            )
          }
        }
      }
    }

    // Filter Chips Row
    item {
      LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {
          FilterChip(
            selected = selectedTypeFilter == null,
            onClick = { selectedTypeFilter = null },
            label = { Text(StringsHelper.tr(lang, "Tous (${alerts.size})", "الكل")) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = EmeraldGreen,
              selectedLabelColor = Color.White
            )
          )
        }
        AlertType.values().forEach { type ->
          item {
            FilterChip(
              selected = selectedTypeFilter == type,
              onClick = { selectedTypeFilter = type },
              label = {
                Text(StringsHelper.alertTypeLabel(type, lang))
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldGreen,
                selectedLabelColor = Color.White
              )
            )
          }
        }
      }
    }

    if (filteredAlerts.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = StatusGreenBg),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = StringsHelper.tr(lang, "Tout est en règle !", "جميع الوثائق سارية المفعول!"),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = StatusGreen
              )
              Text(
                text = StringsHelper.tr(
                  lang,
                  "Aucune alerte urgente dans cette catégorie.",
                  "لا توجد أي تنبيهات عاجلة في هذه الفئة."
                ),
                fontSize = 12.sp,
                color = Color(0xFF065F46)
              )
            }
          }
        }
      }
    } else {
      items(filteredAlerts) { alert ->
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(
            1.dp,
            when (alert.severity) {
              AlertSeverity.CRITICAL -> StatusRed
              AlertSeverity.WARNING -> StatusOrange
              else -> CardBorder
            }
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = when (alert.type) {
                    AlertType.INSURANCE -> Icons.Default.Security
                    AlertType.VIGNETTE -> Icons.Default.CheckCircle
                    AlertType.CONTROLE_TECHNIQUE -> Icons.Default.Build
                    AlertType.OIL_CHANGE -> Icons.Default.Speed
                  },
                  contentDescription = null,
                  tint = when (alert.severity) {
                    AlertSeverity.CRITICAL -> StatusRed
                    AlertSeverity.WARNING -> StatusOrange
                    else -> EmeraldGreen
                  },
                  modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = StringsHelper.alertTypeLabel(alert.type, lang),
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp
                )
              }

              AlertSeverityChip(
                severity = alert.severity,
                text = if (alert.type == AlertType.OIL_CHANGE) {
                  "${alert.kmLeft} km"
                } else {
                  if (alert.daysLeft < 0) "${-alert.daysLeft}j retard" else "${alert.daysLeft}j restants"
                }
              )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = alert.carName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                  text = alert.description,
                  fontSize = 12.sp,
                  color = when (alert.severity) {
                    AlertSeverity.CRITICAL -> StatusRed
                    AlertSeverity.WARNING -> StatusOrange
                    else -> Color(0xFF64748B)
                  },
                  fontWeight = FontWeight.Medium
                )
              }
              MoroccanPlateBadge(plate = alert.carPlate)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Échéance : ${alert.dueDate}",
                fontSize = 11.sp,
                color = Color(0xFF94A3B8)
              )

              Button(
                onClick = { alertToRenew = alert },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Text(
                  text = StringsHelper.tr(lang, "Renouveler", "تجديد"),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
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

  // Renewal Dialog
  if (alertToRenew != null) {
    val targetCar = cars.find { it.id == alertToRenew?.carId }
    var newDate by remember { mutableStateOf("2027-11-20") }
    var newKm by remember { mutableStateOf("${(targetCar?.currentMileage ?: 50000) + 10000}") }

    AlertDialog(
      onDismissRequest = { alertToRenew = null },
      title = {
        Text(
          text = StringsHelper.tr(
            lang,
            "Mettre à jour : ${alertToRenew?.let { StringsHelper.alertTypeLabel(it.type, lang) }}",
            "تحديث الصلاحية"
          ),
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column {
          Text(
            text = "${targetCar?.name} (${targetCar?.plate})",
            fontSize = 13.sp,
            color = Color(0xFF475569)
          )
          Spacer(modifier = Modifier.height(10.dp))
          if (alertToRenew?.type == AlertType.OIL_CHANGE) {
            OutlinedTextField(
              value = newKm,
              onValueChange = { newKm = it },
              label = { Text(StringsHelper.tr(lang, "Prochaine vidange (km)", "الكيلومتراج المقبل")) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )
          } else {
            OutlinedTextField(
              value = newDate,
              onValueChange = { newDate = it },
              label = { Text(StringsHelper.tr(lang, "Nouvelle date (AAAA-MM-JJ)", "تاريخ الصلاحية الجديد")) },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true
            )
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            targetCar?.let { car ->
              val updated = when (alertToRenew?.type) {
                AlertType.INSURANCE -> car.copy(insuranceExpiry = newDate)
                AlertType.VIGNETTE -> car.copy(vignetteExpiry = newDate)
                AlertType.CONTROLE_TECHNIQUE -> car.copy(controleTechniqueExpiry = newDate)
                AlertType.OIL_CHANGE -> car.copy(nextOilChangeMileage = newKm.toIntOrNull() ?: car.nextOilChangeMileage)
                null -> car
              }
              viewModel.saveCar(updated)
            }
            alertToRenew = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
        ) {
          Text(StringsHelper.tr(lang, "Enregistrer", "حفظ"))
        }
      },
      dismissButton = {
        TextButton(onClick = { alertToRenew = null }) {
          Text(StringsHelper.tr(lang, "Annuler", "إلغاء"))
        }
      }
    )
  }
}
