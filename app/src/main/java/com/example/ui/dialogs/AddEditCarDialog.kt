package com.example.ui.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.Car
import com.example.data.model.CarStatus
import com.example.ui.RentDarViewModel
import com.example.ui.theme.EmeraldGreen
import com.example.util.StringsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCarDialog(
  viewModel: RentDarViewModel,
  carToEdit: Car?,
  onDismiss: () -> Unit
) {
  val lang by viewModel.currentLanguage.collectAsState()

  var name by remember { mutableStateOf(carToEdit?.name ?: "") }
  var plate by remember { mutableStateOf(carToEdit?.plate ?: "") }
  var year by remember { mutableStateOf(carToEdit?.year?.toString() ?: "2024") }
  var dailyPrice by remember { mutableStateOf(carToEdit?.dailyPrice?.toInt()?.toString() ?: "350") }
  var currentKm by remember { mutableStateOf(carToEdit?.currentMileage?.toString() ?: "30000") }
  var fuelType by remember { mutableStateOf(carToEdit?.fuelType ?: "Diesel") }
  var transmission by remember { mutableStateOf(carToEdit?.transmission ?: "Manuelle") }
  var insuranceExpiry by remember { mutableStateOf(carToEdit?.insuranceExpiry ?: "2027-06-30") }
  var vignetteExpiry by remember { mutableStateOf(carToEdit?.vignetteExpiry ?: "2027-01-31") }
  var ctExpiry by remember { mutableStateOf(carToEdit?.controleTechniqueExpiry ?: "2027-10-15") }
  var nextOilKm by remember { mutableStateOf(carToEdit?.nextOilChangeMileage?.toString() ?: "40000") }
  var status by remember { mutableStateOf(carToEdit?.status ?: CarStatus.AVAILABLE) }

  var statusExpanded by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = Color.White,
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 16.dp)
    ) {
      Column(
        modifier = Modifier
          .padding(20.dp)
          .verticalScroll(rememberScrollState())
      ) {
        Text(
          text = if (carToEdit == null) {
            StringsHelper.tr(lang, "Ajouter un véhicule", "إضافة سيارة جديدة")
          } else {
            StringsHelper.tr(lang, "Modifier le véhicule", "تعديل بيانات السيارة")
          },
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = EmeraldGreen
        )
        Spacer(modifier = Modifier.height(14.dp))

        // Model Name
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text(StringsHelper.tr(lang, "Nom du modèle (ex: Dacia Logan)", "اسم الطراز")) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("car_name_input"),
          singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Plate
        OutlinedTextField(
          value = plate,
          onValueChange = { plate = it },
          label = { Text(StringsHelper.tr(lang, "Matricule marocain (ex: 12345-A-26)", "رقم اللوحة المغربية")) },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("car_plate_input"),
          singleLine = true
        )
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = year,
            onValueChange = { year = it },
            label = { Text(StringsHelper.tr(lang, "Année", "السنة")) },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = dailyPrice,
            onValueChange = { dailyPrice = it },
            label = { Text(StringsHelper.tr(lang, "Prix / jour (MAD)", "السعر اليومي")) },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = fuelType,
            onValueChange = { fuelType = it },
            label = { Text(StringsHelper.tr(lang, "Carburant", "الوقود")) },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = transmission,
            onValueChange = { transmission = it },
            label = { Text(StringsHelper.tr(lang, "Boîte", "ناقل الحركة")) },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = currentKm,
            onValueChange = { currentKm = it },
            label = { Text(StringsHelper.tr(lang, "Kilométrage actuel", "الكيلومتراج")) },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = nextOilKm,
            onValueChange = { nextOilKm = it },
            label = { Text(StringsHelper.tr(lang, "Prochaine vidange", "الزيت المقبل")) },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Expiry dates
        Text(
          text = StringsHelper.tr(lang, "Dates d'échéance (Alertes)", "تواريخ الصلاحية والتنبيهات"),
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = Color(0xFF334155)
        )
        Spacer(modifier = Modifier.height(6.dp))

        OutlinedTextField(
          value = insuranceExpiry,
          onValueChange = { insuranceExpiry = it },
          label = { Text(StringsHelper.tr(lang, "Fin d'assurance (AAAA-MM-JJ)", "نهاية التأمين")) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = vignetteExpiry,
            onValueChange = { vignetteExpiry = it },
            label = { Text(StringsHelper.tr(lang, "Vignette", "الضريبة")) },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = ctExpiry,
            onValueChange = { ctExpiry = it },
            label = { Text(StringsHelper.tr(lang, "Visite techn.", "الفحص التقني")) },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }
        Spacer(modifier = Modifier.height(14.dp))

        // Status Dropdown
        ExposedDropdownMenuBox(
          expanded = statusExpanded,
          onExpandedChange = { statusExpanded = !statusExpanded }
        ) {
          OutlinedTextField(
            value = StringsHelper.carStatusLabel(status, lang),
            onValueChange = {},
            readOnly = true,
            label = { Text(StringsHelper.tr(lang, "Statut", "الحالة")) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(
            expanded = statusExpanded,
            onDismissRequest = { statusExpanded = false }
          ) {
            CarStatus.values().forEach { st ->
              DropdownMenuItem(
                text = { Text(StringsHelper.carStatusLabel(st, lang)) },
                onClick = {
                  status = st
                  statusExpanded = false
                }
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End
        ) {
          OutlinedButton(onClick = onDismiss) {
            Text(StringsHelper.tr(lang, "Annuler", "إلغاء"))
          }
          Spacer(modifier = Modifier.width(10.dp))
          Button(
            onClick = {
              if (name.isNotBlank() && plate.isNotBlank()) {
                val car = Car(
                  id = carToEdit?.id ?: "temp_${System.currentTimeMillis()}",
                  name = name,
                  plate = plate,
                  year = year.toIntOrNull() ?: 2024,
                  dailyPrice = dailyPrice.toDoubleOrNull() ?: 350.0,
                  currentMileage = currentKm.toIntOrNull() ?: 30000,
                  status = status,
                  fuelType = fuelType,
                  transmission = transmission,
                  insuranceExpiry = insuranceExpiry,
                  vignetteExpiry = vignetteExpiry,
                  controleTechniqueExpiry = ctExpiry,
                  nextOilChangeMileage = nextOilKm.toIntOrNull() ?: 40000
                )
                viewModel.saveCar(car)
              }
            },
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
            modifier = Modifier.testTag("save_car_button")
          ) {
            Text(StringsHelper.tr(lang, "Enregistrer", "حفظ"))
          }
        }
      }
    }
  }
}
