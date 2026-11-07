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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Car
import com.example.data.model.CarStatus
import com.example.data.model.UserRole
import com.example.ui.RentDarViewModel
import com.example.ui.components.CarStatusChip
import com.example.ui.components.MoroccanPlateBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed
import com.example.util.StringsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarsScreen(
  viewModel: RentDarViewModel,
  modifier: Modifier = Modifier
) {
  val cars by viewModel.cars.collectAsState()
  val agency by viewModel.agency.collectAsState()
  val role by viewModel.currentRole.collectAsState()
  val lang by viewModel.currentLanguage.collectAsState()

  var searchQuery by remember { mutableStateOf("") }
  var selectedStatusFilter by remember { mutableStateOf<CarStatus?>(null) }
  var carToDelete by remember { mutableStateOf<Car?>(null) }

  val planLimit = viewModel.getPlanLimit()
  val currentCarCount = cars.size
  val canAdd = viewModel.canAddCar()

  val filteredCars = cars.filter { car ->
    val matchesSearch = car.name.contains(searchQuery, ignoreCase = true) ||
      car.plate.contains(searchQuery, ignoreCase = true)
    val matchesStatus = selectedStatusFilter == null || car.status == selectedStatusFilter
    matchesSearch && matchesStatus
  }

  Box(modifier = modifier.fillMaxSize()) {
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        Spacer(modifier = Modifier.height(8.dp))
        // Subscription Plan usage banner
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFF1F5F9),
          border = BorderStroke(1.dp, CardBorder),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = StringsHelper.tr(
                  lang,
                  "Parc automobile (${agency.plan})",
                  "أسطول السيارات (${agency.plan})"
                ),
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
              )
              Text(
                text = "$currentCarCount / $planLimit " + StringsHelper.tr(lang, "voitures", "سيارات"),
                fontWeight = FontWeight.Bold,
                color = if (canAdd) EmeraldGreen else StatusRed,
                fontSize = 13.sp
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            val ratio = (currentCarCount.toFloat() / planLimit.toFloat()).coerceIn(0f, 1f)
            LinearProgressIndicator(
              progress = { ratio },
              modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
              color = if (canAdd) EmeraldGreen else StatusRed,
              trackColor = Color(0xFFCBD5E1)
            )
            if (!canAdd) {
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = StringsHelper.tr(
                  lang,
                  "⚠️ Limite atteinte pour votre forfait Starter (5 max). Mettez à niveau dans Paramètres.",
                  "⚠️ وصلت إلى الحد الأقصى لباقة Starter (5 كحد أقصى). قم بالترقية في الإعدادات."
                ),
                color = StatusRed,
                fontSize = 11.sp
              )
            }
          }
        }
      }

      // Search & Filters
      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          placeholder = {
            Text(
              StringsHelper.tr(
                lang,
                "Rechercher un modèle ou matricule...",
                "بحث عن طراز أو رقم اللوحة..."
              )
            )
          },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("search_cars_field"),
          shape = RoundedCornerShape(12.dp),
          singleLine = true
        )
      }

      // Filter Chips Row
      item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          item {
            FilterChip(
              selected = selectedStatusFilter == null,
              onClick = { selectedStatusFilter = null },
              label = {
                Text(
                  StringsHelper.tr(lang, "Tous (${cars.size})", "الكل (${cars.size})")
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = EmeraldGreen,
                selectedLabelColor = Color.White
              )
            )
          }
          item {
            FilterChip(
              selected = selectedStatusFilter == CarStatus.AVAILABLE,
              onClick = { selectedStatusFilter = CarStatus.AVAILABLE },
              label = {
                Text(
                  StringsHelper.tr(
                    lang,
                    "Disponibles (${cars.count { it.status == CarStatus.AVAILABLE }})",
                    "متاحة (${cars.count { it.status == CarStatus.AVAILABLE }})"
                  )
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = StatusGreen,
                selectedLabelColor = Color.White
              )
            )
          }
          item {
            FilterChip(
              selected = selectedStatusFilter == CarStatus.RENTED,
              onClick = { selectedStatusFilter = CarStatus.RENTED },
              label = {
                Text(
                  StringsHelper.tr(
                    lang,
                    "En location (${cars.count { it.status == CarStatus.RENTED }})",
                    "مؤجرة (${cars.count { it.status == CarStatus.RENTED }})"
                  )
                )
              },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = StatusOrange,
                selectedLabelColor = Color.White
              )
            )
          }
          item {
            FilterChip(
              selected = selectedStatusFilter == CarStatus.MAINTENANCE,
              onClick = { selectedStatusFilter = CarStatus.MAINTENANCE },
              label = {
                Text(
                  StringsHelper.tr(
                    lang,
                    "Maintenance (${cars.count { it.status == CarStatus.MAINTENANCE }})",
                    "صيانة (${cars.count { it.status == CarStatus.MAINTENANCE }})"
                  )
                )
              }
            )
          }
        }
      }

      // Cars List
      if (filteredCars.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(40.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = StringsHelper.tr(
                lang,
                "Aucun véhicule ne correspond à votre recherche.",
                "لا توجد سيارة تطابق معايير البحث."
              ),
              color = Color(0xFF64748B)
            )
          }
        }
      } else {
        items(filteredCars) { car ->
          CarItemCard(
            car = car,
            role = role,
            lang = lang,
            onEdit = { viewModel.openAddCar(car) },
            onDelete = { carToDelete = car },
            onNewBookingForCar = {
              viewModel.openNewBooking()
            }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(72.dp))
      }
    }

    // Floating Action Button to Add Car
    FloatingActionButton(
      onClick = {
        if (canAdd) {
          viewModel.openAddCar(null)
        } else {
          viewModel.saveCar(Car("", "", "", 2024, 0.0, 0, CarStatus.AVAILABLE, "", "", "", "", "", 0))
        }
      },
      containerColor = EmeraldGreen,
      contentColor = Color.White,
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(20.dp)
        .testTag("add_car_fab")
    ) {
      Icon(Icons.Default.Add, contentDescription = "Ajouter un véhicule")
    }
  }

  // Confirm delete dialog
  if (carToDelete != null) {
    AlertDialog(
      onDismissRequest = { carToDelete = null },
      title = {
        Text(
          StringsHelper.tr(lang, "Supprimer le véhicule ?", "حذف السيارة؟"),
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Text(
          StringsHelper.tr(
            lang,
            "Êtes-vous sûr de vouloir supprimer ${carToDelete?.name} (${carToDelete?.plate}) ? Cette action est irréversible.",
            "هل أنت متأكد من حذف ${carToDelete?.name} (${carToDelete?.plate})؟ هذا الإجراء لا يمكن التراجع عنه."
          )
        )
      },
      confirmButton = {
        Button(
          onClick = {
            carToDelete?.let { viewModel.deleteCar(it.id) }
            carToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
        ) {
          Text(StringsHelper.tr(lang, "Supprimer", "حذف"))
        }
      },
      dismissButton = {
        TextButton(onClick = { carToDelete = null }) {
          Text(StringsHelper.tr(lang, "Annuler", "إلغاء"))
        }
      }
    )
  }
}

@Composable
fun CarItemCard(
  car: Car,
  role: UserRole,
  lang: com.example.data.model.AppLanguage,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
  onNewBookingForCar: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = BorderStroke(1.dp, CardBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = car.name,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = Color(0xFF0F172A)
          )
          Spacer(modifier = Modifier.height(2.dp))
          Text(
            text = "${car.year} • ${car.fuelType} • ${car.transmission}",
            fontSize = 12.sp,
            color = Color(0xFF64748B)
          )
        }
        CarStatusChip(status = car.status, lang = lang)
      }

      Spacer(modifier = Modifier.height(10.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        MoroccanPlateBadge(plate = car.plate)

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = StringsHelper.formatCurrency(car.dailyPrice, lang),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = EmeraldGreen
          )
          Text(
            text = StringsHelper.tr(lang, "par jour", "لليوم"),
            fontSize = 11.sp,
            color = Color(0xFF64748B)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Mileage & Maintenance Info
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xFFF8FAFC),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(8.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Speed, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
              text = "${car.currentMileage} km",
              fontSize = 12.sp,
              color = Color(0xFF334155),
              fontWeight = FontWeight.Medium
            )
          }

          val kmLeft = car.nextOilChangeMileage - car.currentMileage
          Text(
            text = StringsHelper.tr(
              lang,
              "Vidange: $kmLeft km restants",
              "الزيت: متبقي $kmLeft كلم"
            ),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (kmLeft <= 1000) StatusOrange else Color(0xFF64748B)
          )
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Actions row
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row {
          IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Modifier", tint = EmeraldGreen, modifier = Modifier.size(18.dp))
          }
          if (role == UserRole.OWNER) {
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
              Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = StatusRed, modifier = Modifier.size(18.dp))
            }
          }
        }

        if (car.status == CarStatus.AVAILABLE) {
          OutlinedButton(
            onClick = onNewBookingForCar,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, EmeraldGreen),
            modifier = Modifier.height(34.dp)
          ) {
            Text(
              text = StringsHelper.tr(lang, "Réserver", "حجز"),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = EmeraldGreen
            )
          }
        }
      }
    }
  }
}
