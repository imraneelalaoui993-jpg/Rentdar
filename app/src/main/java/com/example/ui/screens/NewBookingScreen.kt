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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.PaymentMethod
import com.example.ui.RentDarViewModel
import com.example.ui.components.MoroccanPlateBadge
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import com.example.util.StringsHelper
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewBookingScreen(
  viewModel: RentDarViewModel,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val cars by viewModel.cars.collectAsState()
  val lang by viewModel.currentLanguage.collectAsState()

  var currentStep by remember { mutableIntStateOf(1) } // 1, 2, 3

  // Step 1: Car & Dates
  var selectedCarId by remember { mutableStateOf(cars.firstOrNull()?.id ?: "") }
  var pickupDate by remember { mutableStateOf("2026-11-12") }
  var pickupTime by remember { mutableStateOf("10:00") }
  var returnDate by remember { mutableStateOf("2026-11-15") }
  var returnTime by remember { mutableStateOf("18:00") }

  // Step 2: Client
  var clientName by remember { mutableStateOf("") }
  var clientPhone by remember { mutableStateOf("+212 6") }
  var clientCin by remember { mutableStateOf("") }
  var clientAddress by remember { mutableStateOf("Casablanca") }
  var isCinVerified by remember { mutableStateOf(true) }

  // Step 3: Pricing & Deposit
  val selectedCar = cars.find { it.id == selectedCarId }
  val dailyPrice = selectedCar?.dailyPrice ?: 350.0

  // Calculate days
  val rentalDays = remember(pickupDate, returnDate) {
    try {
      val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
      val d1 = sdf.parse(pickupDate)
      val d2 = sdf.parse(returnDate)
      if (d1 != null && d2 != null) {
        val diff = (d2.time - d1.time) / (1000 * 60 * 60 * 24)
        if (diff > 0) diff.toInt() else 1
      } else 1
    } catch (_: Exception) {
      3
    }
  }

  val totalPrice = rentalDays * dailyPrice
  var depositAmount by remember { mutableStateOf("3000") }
  var depositMethod by remember { mutableStateOf("Empreinte TPE / Carte") }
  var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
  var paidAmount by remember { mutableStateOf("500") }
  var bookingStatus by remember { mutableStateOf(BookingStatus.RESERVED) }
  var notes by remember { mutableStateOf("") }

  // Live Overlap Check
  val overlappingBooking = remember(selectedCarId, pickupDate, returnDate) {
    viewModel.checkOverlap(selectedCarId, pickupDate, returnDate)
  }
  val isOverlapping = overlappingBooking != null

  Surface(
    color = Color(0xFFF8FAFC),
    modifier = modifier.fillMaxSize()
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp)
        .verticalScroll(rememberScrollState())
    ) {
      Spacer(modifier = Modifier.height(10.dp))

      // Top Back & Title Bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
      ) {
        IconButton(onClick = onClose) {
          Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = StringsHelper.tr(lang, "Nouvelle Réservation", "إنشاء حجز جديد"),
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
          color = Color(0xFF0F172A)
        )
      }

      Spacer(modifier = Modifier.height(12.dp))

      // 3-Step Indicator
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        StepIndicatorPill(step = 1, currentStep = currentStep, title = StringsHelper.tr(lang, "Véhicule", "السيارة"))
        Box(modifier = Modifier.width(28.dp).height(2.dp).background(if (currentStep > 1) EmeraldGreen else Color(0xFFCBD5E1)))
        StepIndicatorPill(step = 2, currentStep = currentStep, title = StringsHelper.tr(lang, "Client", "الزبون"))
        Box(modifier = Modifier.width(28.dp).height(2.dp).background(if (currentStep > 2) EmeraldGreen else Color(0xFFCBD5E1)))
        StepIndicatorPill(step = 3, currentStep = currentStep, title = StringsHelper.tr(lang, "Règlement", "الأداء"))
      }

      Spacer(modifier = Modifier.height(18.dp))

      when (currentStep) {
        1 -> {
          // STEP 1: VEHICLE & DATES
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CardBorder)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = StringsHelper.tr(lang, "Étape 1 : Choix du véhicule & dates", "المرحلة 1: اختيار السيارة والتواريخ"),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = EmeraldGreen
              )
              Spacer(modifier = Modifier.height(14.dp))

              Text(
                text = StringsHelper.tr(lang, "Sélectionner le véhicule :", "اختر السيارة:"),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
              Spacer(modifier = Modifier.height(8.dp))

              cars.forEach { car ->
                val isSelected = car.id == selectedCarId
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSelected) EmeraldGreenLight.copy(alpha = 0.5f) else Color(0xFFF8FAFC),
                  border = BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) EmeraldGreen else CardBorder),
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { selectedCarId = car.id }
                ) {
                  Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(text = car.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                      Text(
                        text = "${car.year} • ${car.dailyPrice.toInt()} MAD/jour",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                      )
                    }
                    MoroccanPlateBadge(plate = car.plate)
                  }
                }
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Dates & Times
              Text(
                text = StringsHelper.tr(lang, "Période de location :", "فترة الكراء:"),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedTextField(
                  value = pickupDate,
                  onValueChange = { pickupDate = it },
                  label = { Text(StringsHelper.tr(lang, "Date départ (AAAA-MM-JJ)", "تاريخ الاستلام")) },
                  modifier = Modifier.weight(1.3f),
                  singleLine = true
                )
                OutlinedTextField(
                  value = pickupTime,
                  onValueChange = { pickupTime = it },
                  label = { Text(StringsHelper.tr(lang, "Heure", "الوقت")) },
                  modifier = Modifier.weight(0.7f),
                  singleLine = true
                )
              }
              Spacer(modifier = Modifier.height(8.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedTextField(
                  value = returnDate,
                  onValueChange = { returnDate = it },
                  label = { Text(StringsHelper.tr(lang, "Date retour (AAAA-MM-JJ)", "تاريخ الإرجاع")) },
                  modifier = Modifier.weight(1.3f),
                  singleLine = true
                )
                OutlinedTextField(
                  value = returnTime,
                  onValueChange = { returnTime = it },
                  label = { Text(StringsHelper.tr(lang, "Heure", "الوقت")) },
                  modifier = Modifier.weight(0.7f),
                  singleLine = true
                )
              }

              Spacer(modifier = Modifier.height(14.dp))

              // Overlap Banner (BLOCKING CHECK)
              if (isOverlapping) {
                Card(
                  shape = RoundedCornerShape(8.dp),
                  colors = CardDefaults.cardColors(containerColor = StatusRedBg),
                  border = BorderStroke(1.dp, StatusRed),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Default.Error, contentDescription = null, tint = StatusRed)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                      text = StringsHelper.tr(
                        lang,
                        "Conflit : ce véhicule est déjà réservé du ${overlappingBooking?.pickupDate} au ${overlappingBooking?.returnDate} (${overlappingBooking?.clientName}) !",
                        "تعارض : هذه السيارة محجوزة بالفعل من ${overlappingBooking?.pickupDate} إلى ${overlappingBooking?.returnDate} !"
                      ),
                      color = StatusRed,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
              } else {
                Card(
                  shape = RoundedCornerShape(8.dp),
                  colors = CardDefaults.cardColors(containerColor = StatusGreenBg),
                  modifier = Modifier.fillMaxWidth()
                ) {
                  Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                      text = StringsHelper.tr(
                        lang,
                        "Véhicule libre pour cette période ($rentalDays jours).",
                        "السيارة شاغرة لهذه الفترة ($rentalDays أيام)."
                      ),
                      color = StatusGreen,
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(16.dp))

              Button(
                onClick = { currentStep = 2 },
                enabled = !isOverlapping && selectedCarId.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                modifier = Modifier
                  .fillMaxWidth()
                  .height(48.dp)
                  .testTag("step1_next_button")
              ) {
                Text(
                  StringsHelper.tr(lang, "Suivant : Infos Client ➔", "التالي: بيانات الزبون ➔"),
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }

        2 -> {
          // STEP 2: CLIENT INFO & ID UPLOAD
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CardBorder)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = StringsHelper.tr(lang, "Étape 2 : Coordonnées du client & CIN", "المرحلة 2: بيانات الزبون والبطاقة الوطنية"),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = EmeraldGreen
              )
              Spacer(modifier = Modifier.height(14.dp))

              OutlinedTextField(
                value = clientName,
                onValueChange = { clientName = it },
                label = { Text(StringsHelper.tr(lang, "Nom complet du locataire", "الاسم الكامل للمستأجر")) },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("client_name_input"),
                singleLine = true
              )
              Spacer(modifier = Modifier.height(10.dp))

              OutlinedTextField(
                value = clientPhone,
                onValueChange = { clientPhone = it },
                label = { Text(StringsHelper.tr(lang, "Numéro de téléphone (ex: +212 6...)", "رقم الهاتف")) },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("client_phone_input"),
                singleLine = true
              )
              Spacer(modifier = Modifier.height(10.dp))

              OutlinedTextField(
                value = clientCin,
                onValueChange = { clientCin = it },
                label = { Text(StringsHelper.tr(lang, "N° CIN ou Passeport (ex: BK654321)", "رقم البطاقة الوطنية أو جواز السفر")) },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("client_cin_input"),
                singleLine = true
              )
              Spacer(modifier = Modifier.height(10.dp))

              OutlinedTextField(
                value = clientAddress,
                onValueChange = { clientAddress = it },
                label = { Text(StringsHelper.tr(lang, "Ville / Adresse", "المدينة أو العنوان")) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
              )
              Spacer(modifier = Modifier.height(12.dp))

              // ID Document Photo / Verification Box
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF1F5F9),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                  .fillMaxWidth()
                  .clickable { isCinVerified = !isCinVerified }
              ) {
                Row(
                  modifier = Modifier.padding(12.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                      Text(
                        text = StringsHelper.tr(lang, "Pièce d'identité / Permis", "بطاقة الهوية ورخصة السياقة"),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                      )
                      Text(
                        text = if (isCinVerified) {
                          StringsHelper.tr(lang, "Document photographié et validé ✓", "تم التحقق من الوثائق بنجاح ✓")
                        } else {
                          StringsHelper.tr(lang, "Cliquer pour joindre la photo", "انقر لإرفاق الصورة")
                        },
                        fontSize = 11.sp,
                        color = if (isCinVerified) StatusGreen else Color(0xFF64748B)
                      )
                    }
                  }
                  Icon(
                    imageVector = if (isCinVerified) Icons.Default.CheckCircle else Icons.Default.Image,
                    contentDescription = null,
                    tint = if (isCinVerified) StatusGreen else Color(0xFF64748B)
                  )
                }
              }

              Spacer(modifier = Modifier.height(18.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                OutlinedButton(
                  onClick = { currentStep = 1 },
                  modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                ) {
                  Text(StringsHelper.tr(lang, "Précédent", "السابق"))
                }
                Button(
                  onClick = { currentStep = 3 },
                  enabled = clientName.isNotBlank() && clientPhone.isNotBlank(),
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                  modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp)
                    .testTag("step2_next_button")
                ) {
                  Text(
                    StringsHelper.tr(lang, "Suivant : Tarifs ➔", "التالي: الأسعار ➔"),
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }

        3 -> {
          // STEP 3: PRICING, DEPOSIT & CONFIRM
          Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, CardBorder)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = StringsHelper.tr(lang, "Étape 3 : Tarifs, Caution & Règlement", "المرحلة 3: السعر، الضمان وطريقة الأداء"),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = EmeraldGreen
              )
              Spacer(modifier = Modifier.height(14.dp))

              // Pricing Summary Box
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = "${selectedCar?.name} ($rentalDays j x ${dailyPrice.toInt()} MAD)",
                      fontSize = 13.sp,
                      color = Color(0xFF475569)
                    )
                    Text(
                      text = StringsHelper.formatCurrency(totalPrice, lang),
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp
                    )
                  }
                  Spacer(modifier = Modifier.height(6.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    Text(
                      text = StringsHelper.tr(lang, "Total location :", "المجموع الإجمالي:"),
                      fontWeight = FontWeight.Bold,
                      fontSize = 15.sp
                    )
                    Text(
                      text = StringsHelper.formatCurrency(totalPrice, lang),
                      fontWeight = FontWeight.Bold,
                      fontSize = 18.sp,
                      color = EmeraldGreen
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(12.dp))

              // Deposit (Caution)
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                OutlinedTextField(
                  value = depositAmount,
                  onValueChange = { depositAmount = it },
                  label = { Text(StringsHelper.tr(lang, "Caution (MAD)", "مبلغ الضمان")) },
                  modifier = Modifier.weight(1f),
                  singleLine = true
                )
                OutlinedTextField(
                  value = depositMethod,
                  onValueChange = { depositMethod = it },
                  label = { Text(StringsHelper.tr(lang, "Type caution", "نوع الضمان")) },
                  modifier = Modifier.weight(1f),
                  singleLine = true
                )
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Payment Method
              Text(
                text = StringsHelper.tr(lang, "Mode de règlement :", "طريقة الأداء:"),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
              )
              Spacer(modifier = Modifier.height(6.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                PaymentMethod.values().forEach { method ->
                  val isSelected = paymentMethod == method
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) EmeraldGreen else Color(0xFFF1F5F9),
                    modifier = Modifier
                      .weight(1f)
                      .clickable { paymentMethod = method }
                  ) {
                    Box(
                      modifier = Modifier.padding(vertical = 10.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = StringsHelper.paymentMethodLabel(method, lang),
                        color = if (isSelected) Color.White else Color(0xFF334155),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                      )
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(10.dp))

              // Amount paid now
              OutlinedTextField(
                value = paidAmount,
                onValueChange = { paidAmount = it },
                label = { Text(StringsHelper.tr(lang, "Montant encaissé aujourd'hui (MAD)", "المبلغ المحصل الآن")) },
                modifier = Modifier
                  .fillMaxWidth()
                  .testTag("paid_amount_input"),
                singleLine = true
              )

              Spacer(modifier = Modifier.height(10.dp))

              // Initial Status
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (bookingStatus == BookingStatus.RESERVED) AmberGold else Color(0xFFF1F5F9),
                  modifier = Modifier
                    .weight(1f)
                    .clickable { bookingStatus = BookingStatus.RESERVED }
                ) {
                  Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                    Text(
                      text = StringsHelper.tr(lang, "Réservé (Futur)", "محجوز"),
                      color = if (bookingStatus == BookingStatus.RESERVED) Color.White else Color(0xFF334155),
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp
                    )
                  }
                }
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = if (bookingStatus == BookingStatus.ACTIVE) StatusGreen else Color(0xFFF1F5F9),
                  modifier = Modifier
                    .weight(1f)
                    .clickable { bookingStatus = BookingStatus.ACTIVE }
                ) {
                  Box(modifier = Modifier.padding(10.dp), contentAlignment = Alignment.Center) {
                    Text(
                      text = StringsHelper.tr(lang, "En cours (Départ)", "جارية"),
                      color = if (bookingStatus == BookingStatus.ACTIVE) Color.White else Color(0xFF334155),
                      fontWeight = FontWeight.Bold,
                      fontSize = 12.sp
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(18.dp))

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                OutlinedButton(
                  onClick = { currentStep = 2 },
                  modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                ) {
                  Text(StringsHelper.tr(lang, "Précédent", "السابق"))
                }
                Button(
                  onClick = {
                    val booking = Booking(
                      id = "",
                      carId = selectedCarId,
                      carName = selectedCar?.name ?: "",
                      carPlate = selectedCar?.plate ?: "",
                      clientName = clientName,
                      clientPhone = clientPhone,
                      clientCin = clientCin,
                      pickupDate = pickupDate,
                      pickupTime = pickupTime,
                      returnDate = returnDate,
                      returnTime = returnTime,
                      dailyPrice = dailyPrice,
                      days = rentalDays,
                      totalPrice = totalPrice,
                      depositAmount = depositAmount.toDoubleOrNull() ?: 3000.0,
                      depositMethod = depositMethod,
                      paymentMethod = paymentMethod,
                      paidAmount = paidAmount.toDoubleOrNull() ?: 0.0,
                      status = bookingStatus,
                      notes = notes
                    )
                    viewModel.createBooking(booking)
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                  modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp)
                    .testTag("submit_booking_button")
                ) {
                  Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    StringsHelper.tr(lang, "Confirmer Réservation", "تأكيد الحجز"),
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(30.dp))
    }
  }
}

@Composable
fun StepIndicatorPill(step: Int, currentStep: Int, title: String) {
  val isActive = step == currentStep
  val isCompleted = step < currentStep

  Row(verticalAlignment = Alignment.CenterVertically) {
    Box(
      modifier = Modifier
        .size(26.dp)
        .clip(CircleShape)
        .background(
          when {
            isCompleted -> StatusGreen
            isActive -> EmeraldGreen
            else -> Color(0xFFCBD5E1)
          }
        ),
      contentAlignment = Alignment.Center
    ) {
      Text(
        text = if (isCompleted) "✓" else "$step",
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold
      )
    }
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = title,
      fontSize = 12.sp,
      fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
      color = if (isActive) Color(0xFF0F172A) else Color(0xFF64748B)
    )
  }
}
