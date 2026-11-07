package com.example.data.model

enum class UserRole {
  OWNER,
  EMPLOYEE
}

enum class AppLanguage {
  FR,
  AR
}

enum class CarStatus {
  AVAILABLE,
  RENTED,
  MAINTENANCE
}

enum class BookingStatus {
  RESERVED,
  ACTIVE,
  RETURNED,
  CANCELLED
}

enum class PaymentMethod {
  CASH,
  TRANSFER,
  CARD
}

enum class AlertType {
  INSURANCE,
  VIGNETTE,
  CONTROLE_TECHNIQUE,
  OIL_CHANGE
}

enum class AlertSeverity {
  CRITICAL, // Overdue or <= 1 day (Red)
  WARNING,  // <= 7 days (Orange)
  NOTICE,   // <= 30 days (Yellow)
  OK        // > 30 days (Green)
}

data class AgencyProfile(
  val name: String = "RentDar Auto Maroc",
  val city: String = "Casablanca",
  val phone: String = "+212 661 234567",
  val email: String = "contact@rentdar-maroc.ma",
  val licenseNumber: String = "RC-CAS-98432",
  val plan: String = "Starter", // Starter (5), Pro (15), Unlimited
  val trialDaysLeft: Int = 14,
  val contractTermsFr: String = "1. Le locataire s'engage à restituer le véhicule dans le même état et avec le même niveau de carburant.\n2. Tout sinistre ou accident doit être signalé à l'agence et à la police dans les 24 heures.\n3. La franchise d'assurance reste à la charge du locataire en cas de responsabilité.\n4. La sous-location est formellement interdite.",
  val contractTermsAr: String = "1. يتعهد المستأجر بإرجاع السيارة بنفس الحالة ونفس مستوى الوقود.\n2. يجب إبلاغ الوكالة والشرطة بأي حادث خلال 24 ساعة.\n3. يتحمل المستأجر مبلغ التحمل التأميني في حالة مسؤوليته عن الحادث.\n4. يمنع منعا كليا تأجير السيارة للغير."
)

data class Car(
  val id: String,
  val name: String,
  val plate: String, // e.g. "14285-A-26" (Moroccan registration)
  val year: Int,
  val dailyPrice: Double,
  val currentMileage: Int,
  val status: CarStatus,
  val fuelType: String = "Diesel",
  val transmission: String = "Manuelle",
  val insuranceExpiry: String, // YYYY-MM-DD
  val vignetteExpiry: String, // YYYY-MM-DD
  val controleTechniqueExpiry: String, // YYYY-MM-DD
  val nextOilChangeMileage: Int
)

data class Client(
  val id: String,
  val name: String,
  val phone: String, // e.g. "+212612345678"
  val cinOrPassport: String, // e.g. "BE876543"
  val address: String = "Casablanca, Maroc",
  val driverLicense: String = "12/345678"
)

data class SignaturePoint(
  val x: Float,
  val y: Float,
  val isEndOfStroke: Boolean = false
)

data class Booking(
  val id: String,
  val carId: String,
  val carName: String,
  val carPlate: String,
  val clientName: String,
  val clientPhone: String,
  val clientCin: String,
  val pickupDate: String, // YYYY-MM-DD
  val pickupTime: String = "10:00",
  val returnDate: String, // YYYY-MM-DD
  val returnTime: String = "18:00",
  val dailyPrice: Double,
  val days: Int,
  val totalPrice: Double,
  val depositAmount: Double, // Caution
  val depositMethod: String = "Empreinte TPE / Chèque",
  val paymentMethod: PaymentMethod = PaymentMethod.CASH,
  val paidAmount: Double,
  val status: BookingStatus,
  val signaturePoints: List<SignaturePoint> = emptyList(),
  val notes: String = ""
)

data class PaymentRecord(
  val id: String,
  val bookingId: String,
  val clientName: String,
  val amount: Double,
  val date: String,
  val method: PaymentMethod,
  val note: String
)

data class CarAlert(
  val id: String,
  val carId: String,
  val carName: String,
  val carPlate: String,
  val type: AlertType,
  val dueDate: String,
  val daysLeft: Int,
  val kmLeft: Int? = null,
  val severity: AlertSeverity,
  val description: String
)
