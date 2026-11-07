package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.SignaturePoint
import com.example.data.model.UserRole
import com.example.ui.RentDarViewModel
import com.example.ui.components.BookingStatusChip
import com.example.ui.components.MoroccanPlateBadge
import com.example.ui.components.WhatsAppActionButton
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import com.example.util.StringsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingDetailScreen(
  booking: Booking,
  viewModel: RentDarViewModel,
  onClose: () -> Unit,
  modifier: Modifier = Modifier
) {
  val agency by viewModel.agency.collectAsState()
  val role by viewModel.currentRole.collectAsState()
  val lang by viewModel.currentLanguage.collectAsState()
  val payments by viewModel.payments.collectAsState()
  val context = LocalContext.current

  val bookingPayments = payments.filter { it.bookingId == booking.id }
  val unpaidAmount = maxOf(0.0, booking.totalPrice - booking.paidAmount)

  var showPaymentDialog by remember { mutableStateOf(false) }
  var showDeleteDialog by remember { mutableStateOf(false) }
  var detailTab by remember { mutableIntStateOf(0) } // 0 = Aperçu & Paiements, 1 = Contrat & Signature

  // Signature points state
  val signaturePoints = remember {
    mutableStateListOf<SignaturePoint>().apply {
      addAll(booking.signaturePoints)
    }
  }

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

      // Top bar
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = onClose) {
            Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
          }
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = StringsHelper.tr(lang, "Détail Réservation", "تفاصيل الحجز"),
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = Color(0xFF0F172A)
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          BookingStatusChip(status = booking.status, lang = lang)
          if (role == UserRole.OWNER) {
            IconButton(onClick = { showDeleteDialog = true }) {
              Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = StatusRed)
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Tabs: Summary vs Contract
      TabRow(
        selectedTabIndex = detailTab,
        containerColor = Color(0xFFF1F5F9),
        indicator = { tabPositions ->
          TabRowDefaults.SecondaryIndicator(
            modifier = Modifier.tabIndicatorOffset(tabPositions[detailTab]),
            color = EmeraldGreen
          )
        },
        modifier = Modifier
          .clip(RoundedCornerShape(10.dp))
          .fillMaxWidth()
      ) {
        Tab(
          selected = detailTab == 0,
          onClick = { detailTab = 0 },
          text = {
            Text(
              StringsHelper.tr(lang, "Général & Paiements", "عام والأداء"),
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = if (detailTab == 0) EmeraldGreen else Color(0xFF64748B)
            )
          }
        )
        Tab(
          selected = detailTab == 1,
          onClick = { detailTab = 1 },
          text = {
            Text(
              StringsHelper.tr(lang, "Contrat & Signature", "العقد والتوقيع"),
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
              color = if (detailTab == 1) EmeraldGreen else Color(0xFF64748B)
            )
          }
        )
      }

      Spacer(modifier = Modifier.height(14.dp))

      if (detailTab == 0) {
        // OVERVIEW & PAYMENTS TAB

        // Client & Vehicle Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, CardBorder)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = booking.clientName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = Color(0xFF0F172A)
                )
                Text(
                  text = "CIN : ${booking.clientCin} • Tél : ${booking.clientPhone}",
                  fontSize = 12.sp,
                  color = Color(0xFF64748B)
                )
              }
              MoroccanPlateBadge(plate = booking.carPlate)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFF8FAFC),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = "🚗 ${booking.carName}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = EmeraldGreen
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "📅 " + StringsHelper.tr(
                    lang,
                    "Du ${StringsHelper.formatDate(booking.pickupDate)} (${booking.pickupTime}) au ${StringsHelper.formatDate(booking.returnDate)} (${booking.returnTime})",
                    "من ${StringsHelper.formatDate(booking.pickupDate)} إلى ${StringsHelper.formatDate(booking.returnDate)}"
                  ),
                  fontSize = 12.sp,
                  color = Color(0xFF334155)
                )
                Text(
                  text = "⏱️ ${booking.days} " + StringsHelper.tr(lang, "jours de location", "أيام كراء"),
                  fontSize = 12.sp,
                  color = Color(0xFF64748B)
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // WhatsApp Direct Relances
        Text(
          text = StringsHelper.tr(lang, "Relances & Notifications WhatsApp :", "إرساليات واتساب السريعة:"),
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = Color(0xFF334155)
        )
        Spacer(modifier = Modifier.height(6.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          WhatsAppActionButton(
            label = StringsHelper.tr(lang, "Confirmation", "تأكيد الحجز"),
            phone = booking.clientPhone,
            message = StringsHelper.createBookingConfirmationMsg(booking, agency.name, lang),
            modifier = Modifier.weight(1f)
          )
          WhatsAppActionButton(
            label = StringsHelper.tr(lang, "Rappel Retour", "تذكير الإرجاع"),
            phone = booking.clientPhone,
            message = StringsHelper.createReturnReminderMsg(booking, agency.name, lang),
            modifier = Modifier.weight(1f)
          )
        }
        if (unpaidAmount > 0) {
          Spacer(modifier = Modifier.height(6.dp))
          WhatsAppActionButton(
            label = StringsHelper.tr(lang, "Rappel de solde ($unpaidAmount MAD)", "تذكير بالأداء"),
            phone = booking.clientPhone,
            message = StringsHelper.createPaymentReminderMsg(booking, unpaidAmount, agency.name, lang),
            modifier = Modifier.fillMaxWidth()
          )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Financial & Payments Card
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, CardBorder)
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = StringsHelper.tr(lang, "Paiements & Caution", "المدفوعات ومبلغ الضمان"),
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color(0xFF0F172A)
              )

              Button(
                onClick = { showPaymentDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = StringsHelper.tr(lang, "+ Paiement", "+ دفعة"),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Pricing summary row
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text(text = StringsHelper.tr(lang, "Total location", "المجموع"), fontSize = 11.sp, color = Color(0xFF64748B))
                Text(
                  text = StringsHelper.formatCurrency(booking.totalPrice, lang),
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold
                )
              }
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = StringsHelper.tr(lang, "Payé", "المؤدى"), fontSize = 11.sp, color = Color(0xFF64748B))
                Text(
                  text = StringsHelper.formatCurrency(booking.paidAmount, lang),
                  fontSize = 15.sp,
                  fontWeight = FontWeight.Bold,
                  color = StatusGreen
                )
              }
              Column(horizontalAlignment = Alignment.End) {
                Text(text = StringsHelper.tr(lang, "Reste dû", "المتبقي"), fontSize = 11.sp, color = Color(0xFF64748B))
                Text(
                  text = StringsHelper.formatCurrency(unpaidAmount, lang),
                  fontSize = 16.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (unpaidAmount > 0) StatusRed else StatusGreen
                )
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Deposit Info
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Color(0xFFFEF3C7),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "🛡️ " + StringsHelper.tr(lang, "Caution :", "مبلغ الكفالة :"),
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = Color(0xFF92400E)
                )
                Text(
                  text = "${booking.depositAmount.toInt()} MAD (${booking.depositMethod})",
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp,
                  color = Color(0xFF92400E)
                )
              }
            }

            // Payment history
            if (bookingPayments.isNotEmpty()) {
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = StringsHelper.tr(lang, "Historique des encaissements :", "سجل الدفعات:"),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF475569)
              )
              Spacer(modifier = Modifier.height(4.dp))
              bookingPayments.forEach { pay ->
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                  horizontalArrangement = Arrangement.SpaceBetween
                ) {
                  Text(
                    text = "${StringsHelper.formatDate(pay.date)} • ${pay.note}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                  )
                  Text(
                    text = "+${pay.amount.toInt()} MAD (${StringsHelper.paymentMethodLabel(pay.method, lang)})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = StatusGreen
                  )
                }
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Booking Lifecycle Status Actions
        Text(
          text = StringsHelper.tr(lang, "Changer l'état de la location :", "تغيير حالة الكراء:"),
          fontWeight = FontWeight.Bold,
          fontSize = 13.sp,
          color = Color(0xFF334155)
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (booking.status == BookingStatus.RESERVED) {
            Button(
              onClick = { viewModel.updateBookingStatus(booking.id, BookingStatus.ACTIVE) },
              colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
            ) {
              Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(StringsHelper.tr(lang, "Activer départ", "بدء الكراء"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          if (booking.status == BookingStatus.ACTIVE) {
            Button(
              onClick = { viewModel.updateBookingStatus(booking.id, BookingStatus.RETURNED) },
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF475569)),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
            ) {
              Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(StringsHelper.tr(lang, "Restituer véhicule", "تسجيل الإرجاع"), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }

          if (booking.status != BookingStatus.CANCELLED && booking.status != BookingStatus.RETURNED) {
            OutlinedButton(
              onClick = { viewModel.updateBookingStatus(booking.id, BookingStatus.CANCELLED) },
              shape = RoundedCornerShape(10.dp),
              border = BorderStroke(1.dp, StatusRed),
              modifier = Modifier
                .weight(1f)
                .height(44.dp)
            ) {
              Icon(Icons.Default.Cancel, contentDescription = null, tint = StatusRed, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(StringsHelper.tr(lang, "Annuler", "إلغاء"), color = StatusRed, fontSize = 12.sp)
            }
          }
        }
      } else {
        // CONTRACT & SIGNATURE PAD TAB

        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          border = BorderStroke(1.dp, CardBorder)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            // Agency Header on Contract
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(text = agency.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = EmeraldGreen)
                Text(text = "${agency.city} • ${agency.phone}", fontSize = 11.sp, color = Color(0xFF64748B))
                Text(text = "R.C. : ${agency.licenseNumber}", fontSize = 10.sp, color = Color(0xFF94A3B8))
              }
              MoroccanPlateBadge(plate = booking.carPlate)
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = CardBorder)
            Spacer(modifier = Modifier.height(10.dp))

            Text(
              text = StringsHelper.tr(lang, "CONTRAT DE LOCATION DE VÉHICULE", "عقد كراء سيارة بدون سائق"),
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color(0xFF0F172A)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = StringsHelper.tr(
                lang,
                "Locataire : ${booking.clientName}\nCIN/Passeport : ${booking.clientCin}\nTéléphone : ${booking.clientPhone}\nVéhicule : ${booking.carName} (Immat : ${booking.carPlate})\nPériode : du ${StringsHelper.formatDate(booking.pickupDate)} au ${StringsHelper.formatDate(booking.returnDate)} (${booking.days} jours)\nPrix total : ${booking.totalPrice} MAD • Caution : ${booking.depositAmount} MAD",
                "المستأجر : ${booking.clientName}\nالبطاقة الوطنية : ${booking.clientCin}\nالهاتف : ${booking.clientPhone}\nالسيارة : ${booking.carName} (${booking.carPlate})\nالفترة : من ${StringsHelper.formatDate(booking.pickupDate)} إلى ${StringsHelper.formatDate(booking.returnDate)} (${booking.days} أيام)\nالسعر الإجمالي : ${booking.totalPrice} د.م. • الكفالة : ${booking.depositAmount} د.م."
              ),
              fontSize = 12.sp,
              color = Color(0xFF334155),
              lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
            Text(
              text = StringsHelper.tr(lang, "Conditions Générales Maroc :", "الشروط العامة للكراء:"),
              fontWeight = FontWeight.Bold,
              fontSize = 11.sp,
              color = Color(0xFF475569)
            )
            Text(
              text = if (lang == com.example.data.model.AppLanguage.AR) agency.contractTermsAr else agency.contractTermsFr,
              fontSize = 10.sp,
              color = Color(0xFF64748B),
              lineHeight = 15.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Signature Pad Section
            Text(
              text = StringsHelper.tr(
                lang,
                "Signature du Client (Dessiner sur l'écran) :",
                "توقيع المستأجر (رسم بالأصبع على الشاشة):"
              ),
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = EmeraldGreen
            )
            Spacer(modifier = Modifier.height(6.dp))

            // Interactive Signature Canvas
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF8FAFC))
                .border(1.5.dp, if (signaturePoints.isNotEmpty()) EmeraldGreen else CardBorder, RoundedCornerShape(8.dp))
                .pointerInput(Unit) {
                  detectDragGestures(
                    onDragStart = { offset ->
                      signaturePoints.add(SignaturePoint(offset.x, offset.y, false))
                    },
                    onDrag = { change, _ ->
                      change.consume()
                      signaturePoints.add(SignaturePoint(change.position.x, change.position.y, false))
                    },
                    onDragEnd = {
                      if (signaturePoints.isNotEmpty()) {
                        val last = signaturePoints.last()
                        signaturePoints[signaturePoints.size - 1] = last.copy(isEndOfStroke = true)
                      }
                    }
                  )
                }
            ) {
              Canvas(modifier = Modifier.fillMaxSize()) {
                if (signaturePoints.isNotEmpty()) {
                  val path = Path()
                  var isNewStroke = true
                  for (point in signaturePoints) {
                    if (isNewStroke) {
                      path.moveTo(point.x, point.y)
                      isNewStroke = false
                    } else {
                      path.lineTo(point.x, point.y)
                    }
                    if (point.isEndOfStroke) {
                      isNewStroke = true
                    }
                  }
                  drawPath(
                    path = path,
                    color = Color(0xFF0F172A),
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                  )
                }
              }

              if (signaturePoints.isEmpty()) {
                Text(
                  text = StringsHelper.tr(lang, "✍️ Signer ici avec le doigt", "✍️ وقع هنا بالأصبع"),
                  color = Color(0xFF94A3B8),
                  fontSize = 13.sp,
                  modifier = Modifier.align(Alignment.Center)
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Signature Actions
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              TextButton(onClick = { signaturePoints.clear() }) {
                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(StringsHelper.tr(lang, "Effacer", "مسح"), fontSize = 12.sp)
              }

              Button(
                onClick = {
                  viewModel.saveSignature(booking.id, signaturePoints.toList())
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.height(34.dp)
              ) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = StringsHelper.tr(lang, "Valider signature", "تأكيد التوقيع"),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Share / Print Contract Button
            Button(
              onClick = {
                val contractText = """
                *** CONTRAT DE LOCATION - ${agency.name} ***
                Client : ${booking.clientName} (CIN: ${booking.clientCin}, Tél: ${booking.clientPhone})
                Véhicule : ${booking.carName} (${booking.carPlate})
                Période : du ${StringsHelper.formatDate(booking.pickupDate)} au ${StringsHelper.formatDate(booking.returnDate)} (${booking.days} j)
                Montant Total : ${booking.totalPrice} MAD
                Caution : ${booking.depositAmount} MAD
                Signé numériquement par le locataire.
                """.trimIndent()
                val shareIntent = Intent().apply {
                  action = Intent.ACTION_SEND
                  putExtra(Intent.EXTRA_TEXT, contractText)
                  type = "text/plain"
                }
                context.startActivity(Intent.createChooser(shareIntent, "Partager le contrat"))
              },
              colors = ButtonDefaults.buttonColors(containerColor = AmberGold),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
            ) {
              Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = StringsHelper.tr(lang, "Partager le Contrat (WhatsApp)", "مشاركة العقد عبر واتساب"),
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(40.dp))
    }
  }

  // Record Payment Dialog
  if (showPaymentDialog) {
    var payAmount by remember { mutableStateOf(unpaidAmount.toInt().toString()) }
    var payMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var payNote by remember { mutableStateOf("Règlement de solde") }

    AlertDialog(
      onDismissRequest = { showPaymentDialog = false },
      title = {
        Text(
          text = StringsHelper.tr(lang, "Enregistrer un paiement", "تسجيل دفعة جديدة"),
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column {
          OutlinedTextField(
            value = payAmount,
            onValueChange = { payAmount = it },
            label = { Text(StringsHelper.tr(lang, "Montant à encaisser (MAD)", "المبلغ")) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          Spacer(modifier = Modifier.height(10.dp))
          Text(text = StringsHelper.tr(lang, "Mode de paiement :", "طريقة الأداء:"), fontSize = 12.sp)
          Spacer(modifier = Modifier.height(4.dp))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            PaymentMethod.values().forEach { m ->
              val isSel = payMethod == m
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (isSel) EmeraldGreen else Color(0xFFF1F5F9),
                modifier = Modifier
                  .weight(1f)
                  .clickable { payMethod = m }
              ) {
                Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                  Text(
                    text = StringsHelper.paymentMethodLabel(m, lang),
                    color = if (isSel) Color.White else Color(0xFF334155),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                  )
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = payNote,
            onValueChange = { payNote = it },
            label = { Text(StringsHelper.tr(lang, "Note / Reçu", "ملاحظة أو وصل")) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val amount = payAmount.toDoubleOrNull() ?: 0.0
            if (amount > 0) {
              viewModel.recordPayment(booking.id, amount, payMethod, payNote)
              showPaymentDialog = false
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen)
        ) {
          Text(StringsHelper.tr(lang, "Encaisser", "قبض المبلغ"))
        }
      },
      dismissButton = {
        TextButton(onClick = { showPaymentDialog = false }) {
          Text(StringsHelper.tr(lang, "Annuler", "إلغاء"))
        }
      }
    )
  }

  // Confirm delete dialog
  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = { Text(StringsHelper.tr(lang, "Supprimer la réservation ?", "حذف الحجز؟")) },
      text = {
        Text(
          StringsHelper.tr(
            lang,
            "Êtes-vous sûr de vouloir supprimer la réservation de ${booking.clientName} ? Cette action libérera le véhicule.",
            "هل أنت متأكد من حذف حجز ${booking.clientName}؟"
          )
        )
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.deleteBooking(booking.id)
            showDeleteDialog = false
            onClose()
          },
          colors = ButtonDefaults.buttonColors(containerColor = StatusRed)
        ) {
          Text(StringsHelper.tr(lang, "Supprimer", "حذف"))
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) {
          Text(StringsHelper.tr(lang, "Annuler", "إلغاء"))
        }
      }
    )
  }
}
