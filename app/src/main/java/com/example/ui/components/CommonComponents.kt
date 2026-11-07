package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSeverity
import com.example.data.model.AppLanguage
import com.example.data.model.BookingStatus
import com.example.data.model.CarStatus
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.StatusBlue
import com.example.ui.theme.StatusBlueBg
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusGreenBg
import com.example.ui.theme.StatusOrange
import com.example.ui.theme.StatusOrangeBg
import com.example.ui.theme.StatusRed
import com.example.ui.theme.StatusRedBg
import com.example.util.StringsHelper

@Composable
fun MoroccanPlateBadge(
  plate: String,
  modifier: Modifier = Modifier
) {
  // Typical Moroccan format e.g. 14285-A-26 or 58392-B-6
  // Renders with an authentic embossed license plate look
  val parts = plate.split("-")
  val num = if (parts.isNotEmpty()) parts[0] else plate
  val letterCode = if (parts.size > 1) parts[1] else "A"
  val region = if (parts.size > 2) parts[2] else "26"

  // Arabic letter correspondence for plate
  val arabicLetter = when (letterCode.uppercase()) {
    "A", "أ" -> "أ"
    "B", "ب" -> "ب"
    "D", "د" -> "د"
    "H", "ه" -> "هـ"
    "W", "و" -> "و"
    else -> letterCode
  }

  Surface(
    modifier = modifier.testTag("moroccan_plate_badge"),
    shape = RoundedCornerShape(4.dp),
    color = Color(0xFFFAF9F6),
    border = BorderStroke(1.5.dp, Color(0xFF1E293B)),
    shadowElevation = 1.dp
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Text(
        text = num,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = Color(0xFF0F172A),
        fontFamily = FontFamily.Monospace
      )
      Spacer(modifier = Modifier.width(6.dp))
      Box(
        modifier = Modifier
          .width(1.dp)
          .height(14.dp)
          .background(Color(0xFF94A3B8))
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = arabicLetter,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color(0xFF0D5C3A)
      )
      Spacer(modifier = Modifier.width(6.dp))
      Box(
        modifier = Modifier
          .width(1.dp)
          .height(14.dp)
          .background(Color(0xFF94A3B8))
      )
      Spacer(modifier = Modifier.width(6.dp))
      Text(
        text = region,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = Color(0xFF0F172A),
        fontFamily = FontFamily.Monospace
      )
    }
  }
}

@Composable
fun CarStatusChip(
  status: CarStatus,
  lang: AppLanguage,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, label) = when (status) {
    CarStatus.AVAILABLE -> Triple(StatusGreenBg, StatusGreen, StringsHelper.carStatusLabel(status, lang))
    CarStatus.RENTED -> Triple(StatusOrangeBg, StatusOrange, StringsHelper.carStatusLabel(status, lang))
    CarStatus.MAINTENANCE -> Triple(StatusBlueBg, StatusBlue, StringsHelper.carStatusLabel(status, lang))
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(bgColor)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Box(
        modifier = Modifier
          .size(6.dp)
          .clip(RoundedCornerShape(3.dp))
          .background(textColor)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = label,
        color = textColor,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.sp
      )
    }
  }
}

@Composable
fun BookingStatusChip(
  status: BookingStatus,
  lang: AppLanguage,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, label) = when (status) {
    BookingStatus.RESERVED -> Triple(StatusOrangeBg, StatusOrange, StringsHelper.bookingStatusLabel(status, lang))
    BookingStatus.ACTIVE -> Triple(StatusGreenBg, StatusGreen, StringsHelper.bookingStatusLabel(status, lang))
    BookingStatus.RETURNED -> Triple(Color(0xFFE2E8F0), Color(0xFF475569), StringsHelper.bookingStatusLabel(status, lang))
    BookingStatus.CANCELLED -> Triple(StatusRedBg, StatusRed, StringsHelper.bookingStatusLabel(status, lang))
  }

  Box(
    modifier = modifier
      .clip(RoundedCornerShape(12.dp))
      .background(bgColor)
      .padding(horizontal = 8.dp, vertical = 4.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      color = textColor,
      fontWeight = FontWeight.Bold,
      fontSize = 11.sp
    )
  }
}

@Composable
fun AlertSeverityChip(
  severity: AlertSeverity,
  text: String,
  modifier: Modifier = Modifier
) {
  val (bgColor, textColor, icon) = when (severity) {
    AlertSeverity.CRITICAL -> Triple(StatusRedBg, StatusRed, Icons.Default.Error)
    AlertSeverity.WARNING -> Triple(StatusOrangeBg, StatusOrange, Icons.Default.Warning)
    AlertSeverity.NOTICE -> Triple(Color(0xFFFEF9C3), Color(0xFF854D0E), Icons.Default.Warning)
    AlertSeverity.OK -> Triple(StatusGreenBg, StatusGreen, Icons.Default.CheckCircle)
  }

  Row(
    modifier = modifier
      .clip(RoundedCornerShape(8.dp))
      .background(bgColor)
      .padding(horizontal = 6.dp, vertical = 3.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = textColor,
      modifier = Modifier.size(12.dp)
    )
    Spacer(modifier = Modifier.width(4.dp))
    Text(
      text = text,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.Medium
    )
  }
}

@Composable
fun StatCard(
  title: String,
  value: String,
  subtitle: String? = null,
  icon: ImageVector,
  iconTint: Color = EmeraldGreen,
  backgroundColor: Color = Color.White,
  onClick: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = backgroundColor),
    border = BorderStroke(1.dp, CardBorder),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
  ) {
    Column(
      modifier = Modifier.padding(14.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyMedium,
          color = Color(0xFF64748B),
          fontWeight = FontWeight.Medium
        )
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(iconTint.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(18.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF0F172A)
      )
      if (subtitle != null) {
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = subtitle,
          style = MaterialTheme.typography.labelSmall,
          color = Color(0xFF64748B)
        )
      }
    }
  }
}

@Composable
fun WhatsAppActionButton(
  label: String,
  phone: String,
  message: String,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  Button(
    onClick = {
      StringsHelper.openWhatsAppMessage(context, phone, message)
    },
    modifier = modifier
      .height(44.dp)
      .testTag("whatsapp_action_button"),
    colors = ButtonDefaults.buttonColors(
      containerColor = Color(0xFF25D366),
      contentColor = Color.White
    ),
    shape = RoundedCornerShape(10.dp)
  ) {
    Icon(
      imageVector = Icons.Default.Chat,
      contentDescription = "WhatsApp",
      modifier = Modifier.size(16.dp)
    )
    Spacer(modifier = Modifier.width(6.dp))
    Text(
      text = label,
      fontWeight = FontWeight.Bold,
      fontSize = 12.sp
    )
  }
}
