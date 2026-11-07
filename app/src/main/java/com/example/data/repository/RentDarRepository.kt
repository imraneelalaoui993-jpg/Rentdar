package com.example.data.repository

import com.example.data.model.AgencyProfile
import com.example.data.model.AlertSeverity
import com.example.data.model.AlertType
import com.example.data.model.AppLanguage
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.Car
import com.example.data.model.CarAlert
import com.example.data.model.CarStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PaymentRecord
import com.example.data.model.SignaturePoint
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class RentDarRepository {

  private val _agency = MutableStateFlow(AgencyProfile())
  val agency: StateFlow<AgencyProfile> = _agency.asStateFlow()

  private val _currentRole = MutableStateFlow(UserRole.OWNER)
  val currentRole: StateFlow<UserRole> = _currentRole.asStateFlow()

  private val _currentLanguage = MutableStateFlow(AppLanguage.FR)
  val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

  private val _cars = MutableStateFlow<List<Car>>(emptyList())
  val cars: StateFlow<List<Car>> = _cars.asStateFlow()

  private val _bookings = MutableStateFlow<List<Booking>>(emptyList())
  val bookings: StateFlow<List<Booking>> = _bookings.asStateFlow()

  private val _payments = MutableStateFlow<List<PaymentRecord>>(emptyList())
  val payments: StateFlow<List<PaymentRecord>> = _payments.asStateFlow()

  init {
    seedDemoData()
  }

  private fun seedDemoData() {
    val seededCars = listOf(
      Car(
        id = "car_1",
        name = "Dacia Logan 1.5 dCi",
        plate = "14285-A-26",
        year = 2023,
        dailyPrice = 300.0,
        currentMileage = 45200,
        status = CarStatus.AVAILABLE,
        fuelType = "Diesel",
        transmission = "Manuelle",
        insuranceExpiry = "2026-11-20", // In ~13 days
        vignetteExpiry = "2027-01-31",
        controleTechniqueExpiry = "2027-05-15",
        nextOilChangeMileage = 46000 // 800 km left
      ),
      Car(
        id = "car_2",
        name = "Renault Clio 5 TCe",
        plate = "58392-B-6",
        year = 2022,
        dailyPrice = 350.0,
        currentMileage = 62100,
        status = CarStatus.RENTED,
        fuelType = "Essence",
        transmission = "Manuelle",
        insuranceExpiry = "2027-02-15",
        vignetteExpiry = "2026-11-12", // In 5 days!
        controleTechniqueExpiry = "2026-11-30", // In 23 days
        nextOilChangeMileage = 70000
      ),
      Car(
        id = "car_3",
        name = "Hyundai Tucson Smart",
        plate = "29481-D-1",
        year = 2024,
        dailyPrice = 700.0,
        currentMileage = 18500,
        status = CarStatus.AVAILABLE,
        fuelType = "Diesel",
        transmission = "Automatique",
        insuranceExpiry = "2027-06-30",
        vignetteExpiry = "2027-01-31",
        controleTechniqueExpiry = "2026-12-25",
        nextOilChangeMileage = 25000
      )
    )

    val seededBookings = listOf(
      Booking(
        id = "bk_1",
        carId = "car_2",
        carName = "Renault Clio 5 TCe",
        carPlate = "58392-B-6",
        clientName = "Yassine El Amrani",
        clientPhone = "+212 661 458920",
        clientCin = "BK482910",
        pickupDate = "2026-11-05",
        pickupTime = "09:00",
        returnDate = "2026-11-10",
        returnTime = "18:00",
        dailyPrice = 350.0,
        days = 5,
        totalPrice = 1750.0,
        depositAmount = 3000.0,
        depositMethod = "Empreinte TPE",
        paymentMethod = PaymentMethod.CASH,
        paidAmount = 1350.0, // Remaining 400 MAD
        status = BookingStatus.ACTIVE,
        notes = "Client habituel de Casablanca"
      ),
      Booking(
        id = "bk_2",
        carId = "car_3",
        carName = "Hyundai Tucson Smart",
        carPlate = "29481-D-1",
        clientName = "Fatima Zahra Benali",
        clientPhone = "+212 662 901122",
        clientCin = "AA776512",
        pickupDate = "2026-11-15",
        pickupTime = "10:00",
        returnDate = "2026-11-20",
        returnTime = "10:00",
        dailyPrice = 700.0,
        days = 5,
        totalPrice = 3500.0,
        depositAmount = 5000.0,
        depositMethod = "Chèque de garantie",
        paymentMethod = PaymentMethod.TRANSFER,
        paidAmount = 1000.0, // Acompte
        status = BookingStatus.RESERVED,
        notes = "Arrivée Aéroport Mohammed V"
      ),
      Booking(
        id = "bk_3",
        carId = "car_1",
        carName = "Dacia Logan 1.5 dCi",
        carPlate = "14285-A-26",
        clientName = "Mehdi Tazi",
        clientPhone = "+212 670 334455",
        clientCin = "CD119933",
        pickupDate = "2026-11-01",
        pickupTime = "10:00",
        returnDate = "2026-11-06", // Yesterday! (Overdue return)
        returnTime = "18:00",
        dailyPrice = 300.0,
        days = 5,
        totalPrice = 1500.0,
        depositAmount = 3000.0,
        depositMethod = "Espèces",
        paymentMethod = PaymentMethod.CASH,
        paidAmount = 1500.0,
        status = BookingStatus.ACTIVE, // Not returned yet -> Overdue!
        notes = "Contacté par téléphone, retard de vol"
      ),
      Booking(
        id = "bk_4",
        carId = "car_1",
        carName = "Dacia Logan 1.5 dCi",
        carPlate = "14285-A-26",
        clientName = "Karim Idrissi",
        clientPhone = "+212 654 887766",
        clientCin = "EE992211",
        pickupDate = "2026-10-20",
        pickupTime = "09:00",
        returnDate = "2026-10-25",
        returnTime = "18:00",
        dailyPrice = 300.0,
        days = 5,
        totalPrice = 1500.0,
        depositAmount = 3000.0,
        depositMethod = "Espèces",
        paymentMethod = PaymentMethod.CASH,
        paidAmount = 1500.0,
        status = BookingStatus.RETURNED,
        notes = "Véhicule rendu propre et plein fait"
      )
    )

    val seededPayments = listOf(
      PaymentRecord(
        id = "pay_1",
        bookingId = "bk_1",
        clientName = "Yassine El Amrani",
        amount = 1350.0,
        date = "2026-11-05",
        method = PaymentMethod.CASH,
        note = "Paiement partiel au départ"
      ),
      PaymentRecord(
        id = "pay_2",
        bookingId = "bk_2",
        clientName = "Fatima Zahra Benali",
        amount = 1000.0,
        date = "2026-11-02",
        method = PaymentMethod.TRANSFER,
        note = "Acompte virement bancaire CIH"
      ),
      PaymentRecord(
        id = "pay_3",
        bookingId = "bk_3",
        clientName = "Mehdi Tazi",
        amount = 1500.0,
        date = "2026-11-01",
        method = PaymentMethod.CASH,
        note = "Règlement complet au départ"
      ),
      PaymentRecord(
        id = "pay_4",
        bookingId = "bk_4",
        clientName = "Karim Idrissi",
        amount = 1500.0,
        date = "2026-10-20",
        method = PaymentMethod.CASH,
        note = "Règlement complet"
      )
    )

    _cars.value = seededCars
    _bookings.value = seededBookings
    _payments.value = seededPayments
  }

  fun setRole(role: UserRole) {
    _currentRole.value = role
  }

  fun setLanguage(lang: AppLanguage) {
    _currentLanguage.value = lang
  }

  fun updateAgency(profile: AgencyProfile) {
    _agency.value = profile
  }

  fun setPlan(planName: String) {
    _agency.value = _agency.value.copy(plan = planName)
  }

  // Plan limit checking:
  // Starter: 5 cars max
  // Pro: 15 cars max
  // Unlimited: 9999 cars
  fun getPlanLimit(): Int {
    return when (_agency.value.plan) {
      "Starter" -> 5
      "Pro" -> 15
      else -> 999
    }
  }

  fun canAddCar(): Boolean {
    return _cars.value.size < getPlanLimit()
  }

  fun addCar(car: Car): Result<Unit> {
    if (!canAddCar()) {
      val limit = getPlanLimit()
      return Result.failure(
        IllegalStateException("Limite du forfait ${_agency.value.plan} atteinte ($limit véhicules maximum). Veuillez mettre à niveau.")
      )
    }
    _cars.value = _cars.value + car.copy(id = UUID.randomUUID().toString())
    return Result.success(Unit)
  }

  fun updateCar(car: Car) {
    _cars.value = _cars.value.map { if (it.id == car.id) car else it }
  }

  fun deleteCar(carId: String): Result<Unit> {
    if (_currentRole.value == UserRole.EMPLOYEE) {
      return Result.failure(IllegalStateException("Action réservée au propriétaire (Admin)."))
    }
    _cars.value = _cars.value.filterNot { it.id == carId }
    return Result.success(Unit)
  }

  // Calendar overlap detection
  fun checkOverlap(carId: String, pickupDate: String, returnDate: String, excludeBookingId: String? = null): Booking? {
    // Only check active or reserved bookings for the same car
    val relevant = _bookings.value.filter {
      it.carId == carId &&
        (it.status == BookingStatus.ACTIVE || it.status == BookingStatus.RESERVED) &&
        (excludeBookingId == null || it.id != excludeBookingId)
    }

    // Overlap condition:
    // [P1, R1] and [P2, R2] overlap if P1 <= R2 and R1 >= P2
    for (b in relevant) {
      if (pickupDate <= b.returnDate && returnDate >= b.pickupDate) {
        return b
      }
    }
    return null
  }

  fun createBooking(booking: Booking): Result<Unit> {
    val overlap = checkOverlap(booking.carId, booking.pickupDate, booking.returnDate)
    if (overlap != null) {
      return Result.failure(
        IllegalStateException("Ce véhicule est déjà réservé du ${overlap.pickupDate} au ${overlap.returnDate} (${overlap.clientName}).")
      )
    }

    val newId = UUID.randomUUID().toString()
    val fullBooking = booking.copy(id = newId)
    _bookings.value = _bookings.value + fullBooking

    // If initial payment was made, record payment
    if (fullBooking.paidAmount > 0) {
      val record = PaymentRecord(
        id = UUID.randomUUID().toString(),
        bookingId = newId,
        clientName = fullBooking.clientName,
        amount = fullBooking.paidAmount,
        date = fullBooking.pickupDate,
        method = fullBooking.paymentMethod,
        note = "Paiement à la réservation"
      )
      _payments.value = _payments.value + record
    }

    // Update car status if pickup is today or in progress
    updateCarStatusForBooking(fullBooking)

    return Result.success(Unit)
  }

  fun updateBooking(booking: Booking) {
    _bookings.value = _bookings.value.map { if (it.id == booking.id) booking else it }
    updateCarStatusForBooking(booking)
  }

  fun deleteBooking(bookingId: String): Result<Unit> {
    if (_currentRole.value == UserRole.EMPLOYEE) {
      return Result.failure(IllegalStateException("Action réservée au propriétaire (Admin)."))
    }
    _bookings.value = _bookings.value.filterNot { it.id == bookingId }
    return Result.success(Unit)
  }

  fun recordPayment(bookingId: String, amount: Double, method: PaymentMethod, note: String) {
    val booking = _bookings.value.find { it.id == bookingId } ?: return
    val newPaid = booking.paidAmount + amount
    _bookings.value = _bookings.value.map {
      if (it.id == bookingId) it.copy(paidAmount = newPaid) else it
    }

    val payment = PaymentRecord(
      id = UUID.randomUUID().toString(),
      bookingId = bookingId,
      clientName = booking.clientName,
      amount = amount,
      date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
      method = method,
      note = note
    )
    _payments.value = _payments.value + payment
  }

  fun saveSignature(bookingId: String, points: List<SignaturePoint>) {
    _bookings.value = _bookings.value.map {
      if (it.id == bookingId) it.copy(signaturePoints = points) else it
    }
  }

  private fun updateCarStatusForBooking(booking: Booking) {
    when (booking.status) {
      BookingStatus.ACTIVE -> {
        _cars.value = _cars.value.map {
          if (it.id == booking.carId) it.copy(status = CarStatus.RENTED) else it
        }
      }
      BookingStatus.RETURNED, BookingStatus.CANCELLED -> {
        // check if car has another active booking
        val hasOtherActive = _bookings.value.any { it.carId == booking.carId && it.status == BookingStatus.ACTIVE && it.id != booking.id }
        if (!hasOtherActive) {
          _cars.value = _cars.value.map {
            if (it.id == booking.carId && it.status == CarStatus.RENTED) it.copy(status = CarStatus.AVAILABLE) else it
          }
        }
      }
      else -> Unit
    }
  }

  // Calculate alerts dynamically against current date: 2026-11-07
  fun getAlerts(): List<CarAlert> {
    val alerts = mutableListOf<CarAlert>()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val now = try {
      sdf.parse("2026-11-07") ?: Date()
    } catch (_: Exception) {
      Date()
    }

    for (car in _cars.value) {
      // 1. Insurance
      checkDateAlert(car, car.insuranceExpiry, AlertType.INSURANCE, now, alerts)
      // 2. Vignette
      checkDateAlert(car, car.vignetteExpiry, AlertType.VIGNETTE, now, alerts)
      // 3. Contrôle technique
      checkDateAlert(car, car.controleTechniqueExpiry, AlertType.CONTROLE_TECHNIQUE, now, alerts)
      // 4. Oil change (Vidange) by km
      val kmLeft = car.nextOilChangeMileage - car.currentMileage
      val (kmSeverity, kmDesc) = when {
        kmLeft <= 0 -> Pair(AlertSeverity.CRITICAL, "Vidange dépassée de ${-kmLeft} km !")
        kmLeft <= 1000 -> Pair(AlertSeverity.WARNING, "Vidange dans $kmLeft km")
        kmLeft <= 3000 -> Pair(AlertSeverity.NOTICE, "Vidange dans $kmLeft km")
        else -> Pair(AlertSeverity.OK, "Vidange dans $kmLeft km")
      }
      if (kmSeverity != AlertSeverity.OK) {
        alerts.add(
          CarAlert(
            id = "${car.id}_oil",
            carId = car.id,
            carName = car.name,
            carPlate = car.plate,
            type = AlertType.OIL_CHANGE,
            dueDate = "${car.nextOilChangeMileage} km",
            daysLeft = 0,
            kmLeft = kmLeft,
            severity = kmSeverity,
            description = kmDesc
          )
        )
      }
    }

    // Sort: CRITICAL first, then WARNING, then NOTICE
    return alerts.sortedBy {
      when (it.severity) {
        AlertSeverity.CRITICAL -> 0
        AlertSeverity.WARNING -> 1
        AlertSeverity.NOTICE -> 2
        AlertSeverity.OK -> 3
      }
    }
  }

  private fun checkDateAlert(
    car: Car,
    dateStr: String,
    type: AlertType,
    now: Date,
    alerts: MutableList<CarAlert>
  ) {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    try {
      val target = sdf.parse(dateStr) ?: return
      val diffMs = target.time - now.time
      val daysLeft = (diffMs / (1000 * 60 * 60 * 24)).toInt()

      val typeLabel = when (type) {
        AlertType.INSURANCE -> "Assurance"
        AlertType.VIGNETTE -> "Vignette"
        AlertType.CONTROLE_TECHNIQUE -> "Contrôle technique"
        AlertType.OIL_CHANGE -> "Vidange"
      }

      val (severity, desc) = when {
        daysLeft < 0 -> Pair(AlertSeverity.CRITICAL, "$typeLabel expirée depuis ${-daysLeft} jour(s) !")
        daysLeft <= 1 -> Pair(AlertSeverity.CRITICAL, "$typeLabel expire aujourd'hui ou demain !")
        daysLeft <= 7 -> Pair(AlertSeverity.WARNING, "$typeLabel expire dans $daysLeft jours")
        daysLeft <= 30 -> Pair(AlertSeverity.NOTICE, "$typeLabel expire dans $daysLeft jours")
        else -> Pair(AlertSeverity.OK, "$typeLabel valide")
      }

      if (severity != AlertSeverity.OK) {
        alerts.add(
          CarAlert(
            id = "${car.id}_${type.name}",
            carId = car.id,
            carName = car.name,
            carPlate = car.plate,
            type = type,
            dueDate = dateStr,
            daysLeft = daysLeft,
            severity = severity,
            description = desc
          )
        )
      }
    } catch (_: Exception) {}
  }
}
