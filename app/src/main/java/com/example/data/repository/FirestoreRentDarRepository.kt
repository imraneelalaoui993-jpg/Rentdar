package com.example.data.repository

import android.content.Context
import com.example.R
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
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class FirestoreRentDarRepository(
  val db: FirebaseFirestore
) {

  // Primary constructor resolving database ID from resources
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

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

  private var carsListener: ListenerRegistration? = null
  private var bookingsListener: ListenerRegistration? = null
  private var paymentsListener: ListenerRegistration? = null
  private var userListener: ListenerRegistration? = null

  private fun requireUserId(): String {
    return Firebase.auth.currentUser?.uid
      ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
  }

  fun attachUser(userId: String, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
    detachListeners()

    val userDoc = db.collection("users").document(userId)

    // Listen to User Profile
    userListener = userDoc.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.GET, userDoc.path)
        return@addSnapshotListener
      }
      if (snapshot != null && snapshot.exists()) {
        val data = snapshot.data ?: return@addSnapshotListener
        _agency.value = AgencyProfile(
          name = data["agencyName"] as? String ?: "RentDar Auto Maroc",
          city = data["city"] as? String ?: "Casablanca",
          phone = data["phone"] as? String ?: "+212 661 234567",
          email = data["email"] as? String ?: "",
          licenseNumber = data["licenseNumber"] as? String ?: "RC-CAS-98432",
          plan = data["plan"] as? String ?: "Starter"
        )
      } else {
        // First login: initialize user profile doc
        scope.launch {
          try {
            userDoc.set(
              mapOf(
                "userId" to userId,
                "agencyName" to "RentDar Auto Maroc",
                "city" to "Casablanca",
                "phone" to "+212 661 234567",
                "email" to (Firebase.auth.currentUser?.email ?: ""),
                "role" to "OWNER",
                "plan" to "Starter",
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
              )
            ).await()
          } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, userDoc.path)
          }
        }
      }
    }

    // Listen to Cars
    val carsCol = userDoc.collection("cars")
    carsListener = carsCol.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, carsCol.path)
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val carList = snapshot.documents.mapNotNull { doc ->
          val data = doc.data ?: return@mapNotNull null
          Car(
            id = doc.id,
            name = data["name"] as? String ?: "",
            plate = data["plate"] as? String ?: "",
            year = (data["year"] as? Number)?.toInt() ?: 2024,
            dailyPrice = (data["dailyPrice"] as? Number)?.toDouble() ?: 350.0,
            currentMileage = (data["currentMileage"] as? Number)?.toInt() ?: 30000,
            status = try { CarStatus.valueOf((data["status"] as? String) ?: "AVAILABLE") } catch (_: Exception) { CarStatus.AVAILABLE },
            fuelType = data["fuelType"] as? String ?: "Diesel",
            transmission = data["transmission"] as? String ?: "Manuelle",
            insuranceExpiry = data["insuranceExpiry"] as? String ?: "2027-06-30",
            vignetteExpiry = data["vignetteExpiry"] as? String ?: "2027-01-31",
            controleTechniqueExpiry = data["controleTechniqueExpiry"] as? String ?: "2027-10-15",
            nextOilChangeMileage = (data["nextOilChangeMileage"] as? Number)?.toInt() ?: 40000
          )
        }
        _cars.value = carList

        // If newly created account has 0 cars, seed initial demo data into Firestore!
        if (carList.isEmpty() && snapshot.metadata.isFromCache.not()) {
          scope.launch {
            seedInitialFirestoreData(userId)
          }
        }
      }
    }

    // Listen to Bookings
    val bookingsCol = userDoc.collection("bookings")
    bookingsListener = bookingsCol.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, bookingsCol.path)
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val bookingList = snapshot.documents.mapNotNull { doc ->
          val data = doc.data ?: return@mapNotNull null
          Booking(
            id = doc.id,
            carId = data["carId"] as? String ?: "",
            carName = data["carName"] as? String ?: "",
            carPlate = data["carPlate"] as? String ?: "",
            clientName = data["clientName"] as? String ?: "",
            clientPhone = data["clientPhone"] as? String ?: "",
            clientCin = data["clientCin"] as? String ?: "",
            pickupDate = data["pickupDate"] as? String ?: "",
            pickupTime = data["pickupTime"] as? String ?: "10:00",
            returnDate = data["returnDate"] as? String ?: "",
            returnTime = data["returnTime"] as? String ?: "18:00",
            dailyPrice = (data["dailyPrice"] as? Number)?.toDouble() ?: 350.0,
            days = (data["days"] as? Number)?.toInt() ?: 1,
            totalPrice = (data["totalPrice"] as? Number)?.toDouble() ?: 350.0,
            depositAmount = (data["depositAmount"] as? Number)?.toDouble() ?: 3000.0,
            depositMethod = data["depositMethod"] as? String ?: "Empreinte TPE",
            paymentMethod = try { PaymentMethod.valueOf((data["paymentMethod"] as? String) ?: "CASH") } catch (_: Exception) { PaymentMethod.CASH },
            paidAmount = (data["paidAmount"] as? Number)?.toDouble() ?: 0.0,
            status = try { BookingStatus.valueOf((data["status"] as? String) ?: "RESERVED") } catch (_: Exception) { BookingStatus.RESERVED },
            notes = data["notes"] as? String ?: ""
          )
        }
        _bookings.value = bookingList
      }
    }

    // Listen to Payments
    val paymentsCol = userDoc.collection("payments")
    paymentsListener = paymentsCol.addSnapshotListener { snapshot, error ->
      if (error != null) {
        handleFirestoreError(error, OperationType.LIST, paymentsCol.path)
        return@addSnapshotListener
      }
      if (snapshot != null) {
        val paymentList = snapshot.documents.mapNotNull { doc ->
          val data = doc.data ?: return@mapNotNull null
          PaymentRecord(
            id = doc.id,
            bookingId = data["bookingId"] as? String ?: "",
            clientName = data["clientName"] as? String ?: "",
            amount = (data["amount"] as? Number)?.toDouble() ?: 0.0,
            date = data["date"] as? String ?: "",
            method = try { PaymentMethod.valueOf((data["method"] as? String) ?: "CASH") } catch (_: Exception) { PaymentMethod.CASH },
            note = data["note"] as? String ?: ""
          )
        }
        _payments.value = paymentList
      }
    }
  }

  fun detachListeners() {
    carsListener?.remove()
    bookingsListener?.remove()
    paymentsListener?.remove()
    userListener?.remove()
  }

  private suspend fun seedInitialFirestoreData(userId: String) {
    val batch = db.batch()
    val userDoc = db.collection("users").document(userId)

    val demoCars = listOf(
      mapOf(
        "userId" to userId,
        "name" to "Dacia Logan 1.5 dCi",
        "plate" to "14285-A-26",
        "year" to 2023,
        "dailyPrice" to 300.0,
        "currentMileage" to 45200,
        "status" to "AVAILABLE",
        "fuelType" to "Diesel",
        "transmission" to "Manuelle",
        "insuranceExpiry" to "2026-11-20",
        "vignetteExpiry" to "2027-01-31",
        "controleTechniqueExpiry" to "2027-05-15",
        "nextOilChangeMileage" to 46000,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      ),
      mapOf(
        "userId" to userId,
        "name" to "Renault Clio 5 TCe",
        "plate" to "58392-B-6",
        "year" to 2022,
        "dailyPrice" to 350.0,
        "currentMileage" to 62100,
        "status" to "RENTED",
        "fuelType" to "Essence",
        "transmission" to "Manuelle",
        "insuranceExpiry" to "2027-02-15",
        "vignetteExpiry" to "2026-11-12",
        "controleTechniqueExpiry" to "2026-11-30",
        "nextOilChangeMileage" to 70000,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      ),
      mapOf(
        "userId" to userId,
        "name" to "Hyundai Tucson Smart",
        "plate" to "29481-D-1",
        "year" to 2024,
        "dailyPrice" to 700.0,
        "currentMileage" to 18500,
        "status" to "AVAILABLE",
        "fuelType" to "Diesel",
        "transmission" to "Automatique",
        "insuranceExpiry" to "2027-06-30",
        "vignetteExpiry" to "2027-01-31",
        "controleTechniqueExpiry" to "2026-12-25",
        "nextOilChangeMileage" to 25000,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      )
    )

    val carIds = listOf("car_1", "car_2", "car_3")
    carIds.forEachIndexed { idx, cid ->
      batch.set(userDoc.collection("cars").document(cid), demoCars[idx])
    }

    val demoBookings = listOf(
      mapOf(
        "userId" to userId,
        "carId" to "car_2",
        "carName" to "Renault Clio 5 TCe",
        "carPlate" to "58392-B-6",
        "clientName" to "Yassine El Amrani",
        "clientPhone" to "+212 661 458920",
        "clientCin" to "BK482910",
        "pickupDate" to "2026-11-05",
        "pickupTime" to "09:00",
        "returnDate" to "2026-11-10",
        "returnTime" to "18:00",
        "dailyPrice" to 350.0,
        "days" to 5,
        "totalPrice" to 1750.0,
        "depositAmount" to 3000.0,
        "depositMethod" to "Empreinte TPE",
        "paymentMethod" to "CASH",
        "paidAmount" to 1350.0,
        "status" to "ACTIVE",
        "notes" to "Client habituel",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      ),
      mapOf(
        "userId" to userId,
        "carId" to "car_3",
        "carName" to "Hyundai Tucson Smart",
        "carPlate" to "29481-D-1",
        "clientName" to "Fatima Zahra Benali",
        "clientPhone" to "+212 662 901122",
        "clientCin" to "AA776512",
        "pickupDate" to "2026-11-15",
        "pickupTime" to "10:00",
        "returnDate" to "2026-11-20",
        "returnTime" to "10:00",
        "dailyPrice" to 700.0,
        "days" to 5,
        "totalPrice" to 3500.0,
        "depositAmount" to 5000.0,
        "depositMethod" to "Chèque de garantie",
        "paymentMethod" to "TRANSFER",
        "paidAmount" to 1000.0,
        "status" to "RESERVED",
        "notes" to "Arrivée Aéroport",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      ),
      mapOf(
        "userId" to userId,
        "carId" to "car_1",
        "carName" to "Dacia Logan 1.5 dCi",
        "carPlate" to "14285-A-26",
        "clientName" to "Mehdi Tazi",
        "clientPhone" to "+212 670 334455",
        "clientCin" to "CD119933",
        "pickupDate" to "2026-11-01",
        "pickupTime" to "10:00",
        "returnDate" to "2026-11-06",
        "returnTime" to "18:00",
        "dailyPrice" to 300.0,
        "days" to 5,
        "totalPrice" to 1500.0,
        "depositAmount" to 3000.0,
        "depositMethod" to "Espèces",
        "paymentMethod" to "CASH",
        "paidAmount" to 1500.0,
        "status" to "ACTIVE",
        "notes" to "Retard de vol",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      ),
      mapOf(
        "userId" to userId,
        "carId" to "car_1",
        "carName" to "Dacia Logan 1.5 dCi",
        "carPlate" to "14285-A-26",
        "clientName" to "Karim Idrissi",
        "clientPhone" to "+212 654 887766",
        "clientCin" to "EE992211",
        "pickupDate" to "2026-10-20",
        "pickupTime" to "09:00",
        "returnDate" to "2026-10-25",
        "returnTime" to "18:00",
        "dailyPrice" to 300.0,
        "days" to 5,
        "totalPrice" to 1500.0,
        "depositAmount" to 3000.0,
        "depositMethod" to "Espèces",
        "paymentMethod" to "CASH",
        "paidAmount" to 1500.0,
        "status" to "RETURNED",
        "notes" to "Rendu propre",
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
      )
    )

    val bkIds = listOf("bk_1", "bk_2", "bk_3", "bk_4")
    bkIds.forEachIndexed { idx, bkid ->
      batch.set(userDoc.collection("bookings").document(bkid), demoBookings[idx])
    }

    try {
      batch.commit().await()
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, userDoc.path)
    }
  }

  fun setRole(role: UserRole) {
    _currentRole.value = role
  }

  fun setLanguage(lang: AppLanguage) {
    _currentLanguage.value = lang
  }

  fun updateAgency(profile: AgencyProfile) {
    val uid = try { requireUserId() } catch (_: Exception) { return }
    val userDoc = db.collection("users").document(uid)
    val map = mapOf(
      "userId" to uid,
      "agencyName" to profile.name,
      "city" to profile.city,
      "phone" to profile.phone,
      "licenseNumber" to profile.licenseNumber,
      "plan" to profile.plan,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    userDoc.update(map).addOnFailureListener {
      handleFirestoreError(it, OperationType.UPDATE, userDoc.path)
    }
  }

  fun setPlan(planName: String) {
    val uid = try { requireUserId() } catch (_: Exception) { return }
    val userDoc = db.collection("users").document(uid)
    userDoc.update(
      mapOf(
        "plan" to planName,
        "updatedAt" to FieldValue.serverTimestamp()
      )
    ).addOnFailureListener {
      handleFirestoreError(it, OperationType.UPDATE, userDoc.path)
    }
  }

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

  suspend fun addCar(car: Car): Result<Unit> {
    if (!canAddCar()) {
      return Result.failure(
        IllegalStateException("Limite du forfait ${_agency.value.plan} atteinte (${getPlanLimit()} max).")
      )
    }
    val uid = requireUserId()
    val newId = if (car.id.isBlank() || car.id.startsWith("temp_")) "car_${System.currentTimeMillis()}" else car.id
    val doc = db.collection("users").document(uid).collection("cars").document(newId)

    val payload = mapOf(
      "userId" to uid,
      "name" to car.name,
      "plate" to car.plate,
      "year" to car.year,
      "dailyPrice" to car.dailyPrice,
      "currentMileage" to car.currentMileage,
      "status" to car.status.name,
      "fuelType" to car.fuelType,
      "transmission" to car.transmission,
      "insuranceExpiry" to car.insuranceExpiry,
      "vignetteExpiry" to car.vignetteExpiry,
      "controleTechniqueExpiry" to car.controleTechniqueExpiry,
      "nextOilChangeMileage" to car.nextOilChangeMileage,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )

    return try {
      doc.set(payload).await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, doc.path)
      Result.failure(e)
    }
  }

  suspend fun updateCar(car: Car): Result<Unit> {
    val uid = requireUserId()
    val doc = db.collection("users").document(uid).collection("cars").document(car.id)
    val payload = mapOf(
      "userId" to uid,
      "name" to car.name,
      "plate" to car.plate,
      "year" to car.year,
      "dailyPrice" to car.dailyPrice,
      "currentMileage" to car.currentMileage,
      "status" to car.status.name,
      "fuelType" to car.fuelType,
      "transmission" to car.transmission,
      "insuranceExpiry" to car.insuranceExpiry,
      "vignetteExpiry" to car.vignetteExpiry,
      "controleTechniqueExpiry" to car.controleTechniqueExpiry,
      "nextOilChangeMileage" to car.nextOilChangeMileage,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    return try {
      doc.update(payload).await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, doc.path)
      Result.failure(e)
    }
  }

  suspend fun deleteCar(carId: String): Result<Unit> {
    if (_currentRole.value == UserRole.EMPLOYEE) {
      return Result.failure(IllegalStateException("Action réservée au propriétaire (Admin)."))
    }
    val uid = requireUserId()
    val doc = db.collection("users").document(uid).collection("cars").document(carId)
    return try {
      doc.delete().await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, doc.path)
      Result.failure(e)
    }
  }

  fun checkOverlap(carId: String, pickupDate: String, returnDate: String, excludeBookingId: String? = null): Booking? {
    val relevant = _bookings.value.filter {
      it.carId == carId &&
        (it.status == BookingStatus.ACTIVE || it.status == BookingStatus.RESERVED) &&
        (excludeBookingId == null || it.id != excludeBookingId)
    }
    for (b in relevant) {
      if (pickupDate <= b.returnDate && returnDate >= b.pickupDate) {
        return b
      }
    }
    return null
  }

  suspend fun createBooking(booking: Booking): Result<Unit> {
    val overlap = checkOverlap(booking.carId, booking.pickupDate, booking.returnDate)
    if (overlap != null) {
      return Result.failure(
        IllegalStateException("Ce véhicule est déjà réservé du ${overlap.pickupDate} au ${overlap.returnDate} (${overlap.clientName}).")
      )
    }

    val uid = requireUserId()
    val newId = if (booking.id.isBlank()) "bk_${System.currentTimeMillis()}" else booking.id
    val doc = db.collection("users").document(uid).collection("bookings").document(newId)

    val payload = mapOf(
      "userId" to uid,
      "carId" to booking.carId,
      "carName" to booking.carName,
      "carPlate" to booking.carPlate,
      "clientName" to booking.clientName,
      "clientPhone" to booking.clientPhone,
      "clientCin" to booking.clientCin,
      "pickupDate" to booking.pickupDate,
      "pickupTime" to booking.pickupTime,
      "returnDate" to booking.returnDate,
      "returnTime" to booking.returnTime,
      "dailyPrice" to booking.dailyPrice,
      "days" to booking.days,
      "totalPrice" to booking.totalPrice,
      "depositAmount" to booking.depositAmount,
      "depositMethod" to booking.depositMethod,
      "paymentMethod" to booking.paymentMethod.name,
      "paidAmount" to booking.paidAmount,
      "status" to booking.status.name,
      "notes" to booking.notes,
      "createdAt" to FieldValue.serverTimestamp(),
      "updatedAt" to FieldValue.serverTimestamp()
    )

    return try {
      doc.set(payload).await()
      if (booking.paidAmount > 0) {
        val payDoc = db.collection("users").document(uid).collection("payments").document("pay_${System.currentTimeMillis()}")
        payDoc.set(
          mapOf(
            "userId" to uid,
            "bookingId" to newId,
            "clientName" to booking.clientName,
            "amount" to booking.paidAmount,
            "date" to booking.pickupDate,
            "method" to booking.paymentMethod.name,
            "note" to "Paiement à la réservation",
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
          )
        ).await()
      }
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, doc.path)
      Result.failure(e)
    }
  }

  suspend fun updateBooking(booking: Booking): Result<Unit> {
    val uid = requireUserId()
    val doc = db.collection("users").document(uid).collection("bookings").document(booking.id)
    val payload = mapOf(
      "status" to booking.status.name,
      "paidAmount" to booking.paidAmount,
      "notes" to booking.notes,
      "updatedAt" to FieldValue.serverTimestamp()
    )
    return try {
      doc.update(payload).await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, doc.path)
      Result.failure(e)
    }
  }

  suspend fun deleteBooking(bookingId: String): Result<Unit> {
    if (_currentRole.value == UserRole.EMPLOYEE) {
      return Result.failure(IllegalStateException("Action réservée au propriétaire (Admin)."))
    }
    val uid = requireUserId()
    val doc = db.collection("users").document(uid).collection("bookings").document(bookingId)
    return try {
      doc.delete().await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.DELETE, doc.path)
      Result.failure(e)
    }
  }

  suspend fun recordPayment(bookingId: String, amount: Double, method: PaymentMethod, note: String): Result<Unit> {
    val uid = requireUserId()
    val booking = _bookings.value.find { it.id == bookingId }
      ?: return Result.failure(IllegalStateException("Réservation introuvable"))

    val newPaid = booking.paidAmount + amount
    val bkDoc = db.collection("users").document(uid).collection("bookings").document(bookingId)
    val payDoc = db.collection("users").document(uid).collection("payments").document("pay_${System.currentTimeMillis()}")

    return try {
      bkDoc.update("paidAmount", newPaid, "updatedAt", FieldValue.serverTimestamp()).await()
      payDoc.set(
        mapOf(
          "userId" to uid,
          "bookingId" to bookingId,
          "clientName" to booking.clientName,
          "amount" to amount,
          "date" to SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
          "method" to method.name,
          "note" to note,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp()
        )
      ).await()
      Result.success(Unit)
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, bkDoc.path)
      Result.failure(e)
    }
  }

  fun saveSignature(bookingId: String, points: List<SignaturePoint>) {
    // In-memory update for fast signature rendering
    _bookings.value = _bookings.value.map {
      if (it.id == bookingId) it.copy(signaturePoints = points) else it
    }
  }

  fun getAlerts(): List<CarAlert> {
    val alerts = mutableListOf<CarAlert>()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val now = try {
      sdf.parse("2026-11-07") ?: Date()
    } catch (_: Exception) {
      Date()
    }

    for (car in _cars.value) {
      checkDateAlert(car, car.insuranceExpiry, AlertType.INSURANCE, now, alerts)
      checkDateAlert(car, car.vignetteExpiry, AlertType.VIGNETTE, now, alerts)
      checkDateAlert(car, car.controleTechniqueExpiry, AlertType.CONTROLE_TECHNIQUE, now, alerts)

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
