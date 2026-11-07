package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.model.AlertSeverity
import com.example.data.model.AlertType
import com.example.data.model.AppLanguage
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.CarStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.UserRole
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.Locale

object StringsHelper {

  fun tr(lang: AppLanguage, fr: String, ar: String): String {
    return if (lang == AppLanguage.AR) ar else fr
  }

  fun formatCurrency(amount: Double, lang: AppLanguage): String {
    val rounded = String.format(Locale.FRENCH, "%,.0f", amount).replace(',', ' ')
    return if (lang == AppLanguage.AR) "$rounded د.م." else "$rounded MAD"
  }

  fun formatDate(dateStr: String): String {
    // converts YYYY-MM-DD to DD/MM/YYYY
    return try {
      val parts = dateStr.split("-")
      if (parts.size == 3) {
        "${parts[2]}/${parts[1]}/${parts[0]}"
      } else {
        dateStr
      }
    } catch (_: Exception) {
      dateStr
    }
  }

  fun formatPlate(plate: String): String {
    // Normalizes or prettifies Moroccan plates, e.g., "14285-A-26" -> "14285 | أ | 26"
    return plate
  }

  fun carStatusLabel(status: CarStatus, lang: AppLanguage): String {
    return when (status) {
      CarStatus.AVAILABLE -> tr(lang, "Disponible", "متاح")
      CarStatus.RENTED -> tr(lang, "En location", "مؤجر")
      CarStatus.MAINTENANCE -> tr(lang, "Maintenance", "صيانة")
    }
  }

  fun bookingStatusLabel(status: BookingStatus, lang: AppLanguage): String {
    return when (status) {
      BookingStatus.RESERVED -> tr(lang, "Réservé", "محجوز")
      BookingStatus.ACTIVE -> tr(lang, "En cours", "جارية")
      BookingStatus.RETURNED -> tr(lang, "Restitué", "تم الإرجاع")
      BookingStatus.CANCELLED -> tr(lang, "Annulé", "ملغاة")
    }
  }

  fun alertTypeLabel(type: AlertType, lang: AppLanguage): String {
    return when (type) {
      AlertType.INSURANCE -> tr(lang, "Assurance", "تأمين السيارة")
      AlertType.VIGNETTE -> tr(lang, "Vignette fiscale", "الضريبة السنوية")
      AlertType.CONTROLE_TECHNIQUE -> tr(lang, "Contrôle technique", "الفحص التقني")
      AlertType.OIL_CHANGE -> tr(lang, "Vidange moteur", "تغيير زيت المحرك")
    }
  }

  fun paymentMethodLabel(method: PaymentMethod, lang: AppLanguage): String {
    return when (method) {
      PaymentMethod.CASH -> tr(lang, "Espèces", "نقداً")
      PaymentMethod.TRANSFER -> tr(lang, "Virement bancaire", "تحويل بنكي")
      PaymentMethod.CARD -> tr(lang, "Carte / TPE", "بطاقة بنكية")
    }
  }

  fun userRoleLabel(role: UserRole, lang: AppLanguage): String {
    return when (role) {
      UserRole.OWNER -> tr(lang, "Propriétaire (Admin)", "مالك الوكالة (مدير)")
      UserRole.EMPLOYEE -> tr(lang, "Employé", "مستخدم")
    }
  }

  // Generate WhatsApp messages and URLs
  fun openWhatsAppMessage(
    context: Context,
    phone: String,
    message: String
  ) {
    try {
      // Clean phone number (e.g. 0612345678 -> 212612345678)
      var cleanPhone = phone.replace(" ", "").replace("-", "").replace("+", "")
      if (cleanPhone.startsWith("0") && cleanPhone.length == 10) {
        cleanPhone = "212" + cleanPhone.substring(1)
      }
      val encoded = URLEncoder.encode(message, StandardCharsets.UTF_8.toString())
      val url = "https://wa.me/$cleanPhone?text=$encoded"
      val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(intent)
    } catch (_: Exception) {
      // Fallback intent to standard share
      val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, message)
        type = "text/plain"
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
      }
      context.startActivity(Intent.createChooser(sendIntent, "Partager"))
    }
  }

  fun createBookingConfirmationMsg(booking: Booking, agencyName: String, lang: AppLanguage): String {
    return if (lang == AppLanguage.AR) {
      """
      مرحبا ${booking.clientName}،
      تؤكد لكم وكالة $agencyName حجز سيارتكم بنجاح:
      🚗 السيارة: ${booking.carName} (${booking.carPlate})
      📅 تاريخ الاستلام: ${formatDate(booking.pickupDate)} في ${booking.pickupTime}
      📅 تاريخ الإرجاع: ${formatDate(booking.returnDate)} في ${booking.returnTime}
      💰 المبلغ الإجمالي: ${booking.totalPrice} د.م.
      🛡️ مبلغ الضمان (الكفالة): ${booking.depositAmount} د.م.
      شكراً لثقتكم بنا!
      """.trimIndent()
    } else {
      """
      Bonjour ${booking.clientName},
      L'agence $agencyName confirme votre réservation :
      🚗 Véhicule : ${booking.carName} (Immat : ${booking.carPlate})
      📅 Départ : ${formatDate(booking.pickupDate)} à ${booking.pickupTime}
      📅 Retour : ${formatDate(booking.returnDate)} à ${booking.returnTime}
      💰 Montant total : ${booking.totalPrice} MAD
      🛡️ Caution déposée : ${booking.depositAmount} MAD
      Merci de votre confiance !
      """.trimIndent()
    }
  }

  fun createReturnReminderMsg(booking: Booking, agencyName: String, lang: AppLanguage): String {
    return if (lang == AppLanguage.AR) {
      """
      تذكير من $agencyName:
      مرحبا ${booking.clientName}،
      نذكركم بأن موعد إرجاع سيارة ${booking.carName} (${booking.carPlate}) هو اليوم على الساعة ${booking.returnTime}.
      يرجى إرجاع السيارة بنفس مستوى الوقود. شكراً لكم.
      """.trimIndent()
    } else {
      """
      Rappel RentDar - $agencyName :
      Bonjour ${booking.clientName},
      Nous vous rappelons que la restitution de votre véhicule ${booking.carName} (${booking.carPlate}) est prévue aujourd'hui à ${booking.returnTime}.
      Merci de restituer avec le même niveau de carburant.
      """.trimIndent()
    }
  }

  fun createPaymentReminderMsg(booking: Booking, unpaidAmount: Double, agencyName: String, lang: AppLanguage): String {
    return if (lang == AppLanguage.AR) {
      """
      تذكير بالأداء - $agencyName:
      مرحبا ${booking.clientName}،
      نحيطكم علماً بأن هناك رصيد متبقٍ قدره $unpaidAmount د.م. خاص بكراء سيارة ${booking.carName}.
      يرجى تسوية المبلغ عند الإرجاع. شكراً لكم.
      """.trimIndent()
    } else {
      """
      Rappel de paiement - $agencyName :
      Bonjour ${booking.clientName},
      Un solde restant de $unpaidAmount MAD est en attente pour la location de votre ${booking.carName} (${booking.carPlate}).
      Merci de bien vouloir régulariser auprès de l'agence.
      """.trimIndent()
    }
  }
}
