package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AgencyProfile
import com.example.data.model.AlertSeverity
import com.example.data.model.AppLanguage
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.Car
import com.example.data.model.CarAlert
import com.example.data.model.CarStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.SignaturePoint
import com.example.data.model.UserRole
import com.example.data.repository.FirestoreRentDarRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
  DASHBOARD,
  CARS,
  CALENDAR,
  ALERTS,
  SETTINGS
}

data class DashboardStats(
  val todayPickups: List<Booking>,
  val todayReturns: List<Booking>,
  val overdueReturns: List<Booking>,
  val availableCarsCount: Int,
  val totalCarsCount: Int,
  val monthlyIncome: Double,
  val totalUnpaidBalance: Double,
  val carIncomeMap: Map<String, Double>,
  val urgentAlertsCount: Int
)

class RentDarViewModel(
  application: Application,
  private val repository: FirestoreRentDarRepository = FirestoreRentDarRepository(application)
) : AndroidViewModel(application) {

  val agency = repository.agency
  val currentRole = repository.currentRole
  val currentLanguage = repository.currentLanguage
  val cars = repository.cars
  val bookings = repository.bookings
  val payments = repository.payments

  private val _currentTab = MutableStateFlow(ScreenTab.DASHBOARD)
  val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

  private val _selectedBooking = MutableStateFlow<Booking?>(null)
  val selectedBooking: StateFlow<Booking?> = _selectedBooking.asStateFlow()

  private val _showNewBookingSheet = MutableStateFlow(false)
  val showNewBookingSheet: StateFlow<Boolean> = _showNewBookingSheet.asStateFlow()

  private val _showAddCarSheet = MutableStateFlow(false)
  val showAddCarSheet: StateFlow<Boolean> = _showAddCarSheet.asStateFlow()

  private val _carToEdit = MutableStateFlow<Car?>(null)
  val carToEdit: StateFlow<Car?> = _carToEdit.asStateFlow()

  private val _errorMessage = MutableStateFlow<String?>(null)
  val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

  private val _successMessage = MutableStateFlow<String?>(null)
  val successMessage: StateFlow<String?> = _successMessage.asStateFlow()

  init {
    val uid = Firebase.auth.currentUser?.uid
    if (uid != null) {
      repository.attachUser(uid, viewModelScope)
    }
  }

  fun attachUser(userId: String) {
    repository.attachUser(userId, viewModelScope)
  }

  // Dynamic Dashboard Stats
  val dashboardStats: StateFlow<DashboardStats> = combine(
    cars,
    bookings,
    payments
  ) { carList, bookingList, paymentList ->
    val today = "2026-11-07"

    val todayPickups = bookingList.filter {
      it.pickupDate == today && (it.status == BookingStatus.RESERVED || it.status == BookingStatus.ACTIVE)
    }

    val todayReturns = bookingList.filter {
      it.returnDate == today && it.status == BookingStatus.ACTIVE
    }

    val overdueReturns = bookingList.filter {
      it.returnDate < today && it.status == BookingStatus.ACTIVE
    }

    val availableCount = carList.count { it.status == CarStatus.AVAILABLE }

    // Monthly income = sum of payments
    val monthlyIncome = paymentList.sumOf { it.amount }

    // Unpaid balance = sum of (totalPrice - paidAmount) for all active and reserved bookings
    val totalUnpaid = bookingList
      .filter { it.status == BookingStatus.ACTIVE || it.status == BookingStatus.RESERVED }
      .sumOf { maxOf(0.0, it.totalPrice - it.paidAmount) }

    // Income per car
    val carIncome = mutableMapOf<String, Double>()
    bookingList.forEach { bk ->
      val current = carIncome.getOrDefault(bk.carId, 0.0)
      carIncome[bk.carId] = current + bk.paidAmount
    }

    val alerts = repository.getAlerts()
    val urgentCount = alerts.count { it.severity == AlertSeverity.CRITICAL || it.severity == AlertSeverity.WARNING }

    DashboardStats(
      todayPickups = todayPickups,
      todayReturns = todayReturns,
      overdueReturns = overdueReturns,
      availableCarsCount = availableCount,
      totalCarsCount = carList.size,
      monthlyIncome = monthlyIncome,
      totalUnpaidBalance = totalUnpaid,
      carIncomeMap = carIncome,
      urgentAlertsCount = urgentCount
    )
  }.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    DashboardStats(
      todayPickups = emptyList(),
      todayReturns = emptyList(),
      overdueReturns = emptyList(),
      availableCarsCount = 0,
      totalCarsCount = 0,
      monthlyIncome = 0.0,
      totalUnpaidBalance = 0.0,
      carIncomeMap = emptyMap(),
      urgentAlertsCount = 0
    )
  )

  fun getAlerts(): List<CarAlert> = repository.getAlerts()

  fun setTab(tab: ScreenTab) {
    _currentTab.value = tab
  }

  fun setRole(role: UserRole) {
    repository.setRole(role)
  }

  fun toggleRole() {
    val newRole = if (currentRole.value == UserRole.OWNER) UserRole.EMPLOYEE else UserRole.OWNER
    repository.setRole(newRole)
    val roleStr = if (newRole == UserRole.OWNER) "Propriétaire (Admin)" else "Employé"
    _successMessage.value = "Mode changé : $roleStr"
  }

  fun setLanguage(lang: AppLanguage) {
    repository.setLanguage(lang)
  }

  fun toggleLanguage() {
    val newLang = if (currentLanguage.value == AppLanguage.FR) AppLanguage.AR else AppLanguage.FR
    repository.setLanguage(newLang)
  }

  fun updateAgency(profile: AgencyProfile) {
    repository.updateAgency(profile)
    _successMessage.value = "Profil de l'agence mis à jour !"
  }

  fun setPlan(plan: String) {
    repository.setPlan(plan)
    _successMessage.value = "Forfait mis à jour : $plan"
  }

  fun selectBooking(booking: Booking?) {
    _selectedBooking.value = booking
  }

  fun openNewBooking() {
    _showNewBookingSheet.value = true
  }

  fun closeNewBooking() {
    _showNewBookingSheet.value = false
  }

  fun openAddCar(car: Car? = null) {
    _carToEdit.value = car
    _showAddCarSheet.value = true
  }

  fun closeAddCar() {
    _showAddCarSheet.value = false
    _carToEdit.value = null
  }

  fun clearMessages() {
    _errorMessage.value = null
    _successMessage.value = null
  }

  fun canAddCar(): Boolean = repository.canAddCar()
  fun getPlanLimit(): Int = repository.getPlanLimit()

  fun saveCar(car: Car) {
    viewModelScope.launch {
      if (car.id.isEmpty() || car.id.startsWith("temp_")) {
        val res = repository.addCar(car)
        if (res.isFailure) {
          _errorMessage.value = res.exceptionOrNull()?.message ?: "Impossible d'ajouter le véhicule"
        } else {
          _successMessage.value = "Véhicule ${car.name} ajouté avec succès !"
          closeAddCar()
        }
      } else {
        val res = repository.updateCar(car)
        if (res.isFailure) {
          _errorMessage.value = res.exceptionOrNull()?.message ?: "Erreur de mise à jour"
        } else {
          _successMessage.value = "Véhicule ${car.name} modifié !"
          closeAddCar()
        }
      }
    }
  }

  fun deleteCar(carId: String) {
    viewModelScope.launch {
      val res = repository.deleteCar(carId)
      if (res.isFailure) {
        _errorMessage.value = res.exceptionOrNull()?.message ?: "Erreur de suppression"
      } else {
        _successMessage.value = "Véhicule supprimé."
      }
    }
  }

  fun checkOverlap(carId: String, pickupDate: String, returnDate: String, excludeId: String? = null): Booking? {
    return repository.checkOverlap(carId, pickupDate, returnDate, excludeId)
  }

  fun createBooking(booking: Booking): Boolean {
    var success = false
    viewModelScope.launch {
      val res = repository.createBooking(booking)
      if (res.isSuccess) {
        _successMessage.value = "Réservation créée avec succès !"
        _showNewBookingSheet.value = false
        success = true
      } else {
        _errorMessage.value = res.exceptionOrNull()?.message ?: "Erreur de réservation"
      }
    }
    return success
  }

  fun updateBookingStatus(bookingId: String, newStatus: BookingStatus) {
    viewModelScope.launch {
      val current = bookings.value.find { it.id == bookingId } ?: return@launch
      val updated = current.copy(status = newStatus)
      val res = repository.updateBooking(updated)
      if (res.isSuccess) {
        _selectedBooking.value = updated
        _successMessage.value = "Statut mis à jour : ${newStatus.name}"
      } else {
        _errorMessage.value = res.exceptionOrNull()?.message ?: "Erreur de mise à jour"
      }
    }
  }

  fun recordPayment(bookingId: String, amount: Double, method: PaymentMethod, note: String) {
    viewModelScope.launch {
      val res = repository.recordPayment(bookingId, amount, method, note)
      if (res.isSuccess) {
        val updated = bookings.value.find { it.id == bookingId }
        _selectedBooking.value = updated
        _successMessage.value = "Paiement de $amount MAD enregistré !"
      } else {
        _errorMessage.value = res.exceptionOrNull()?.message ?: "Erreur d'enregistrement"
      }
    }
  }

  fun saveSignature(bookingId: String, points: List<SignaturePoint>) {
    repository.saveSignature(bookingId, points)
    val updated = bookings.value.find { it.id == bookingId }?.copy(signaturePoints = points)
    _selectedBooking.value = updated
    _successMessage.value = "Signature du client enregistrée !"
  }

  fun deleteBooking(bookingId: String) {
    viewModelScope.launch {
      val res = repository.deleteBooking(bookingId)
      if (res.isFailure) {
        _errorMessage.value = res.exceptionOrNull()?.message ?: "Erreur de suppression"
      } else {
        _selectedBooking.value = null
        _successMessage.value = "Réservation supprimée."
      }
    }
  }
}
