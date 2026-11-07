package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.Car
import com.example.data.model.CarStatus
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class RentDarRepositoryRuleTest : FirestoreEmulatorTestBase() {

  @Test
  fun addCar_authenticatedUser_createsDocument() = runBlocking {
    val uid = signInTestUser("agency_owner@test.com")
    val repo = FirestoreRentDarRepository(firestore)

    val car = Car(
      id = "test_car_${UUID.randomUUID()}",
      name = "Dacia Logan 1.5",
      plate = "99887-A-26",
      year = 2024,
      dailyPrice = 300.0,
      currentMileage = 15000,
      status = CarStatus.AVAILABLE,
      fuelType = "Diesel",
      transmission = "Manuelle",
      insuranceExpiry = "2027-01-01",
      vignetteExpiry = "2027-01-01",
      controleTechniqueExpiry = "2027-01-01",
      nextOilChangeMileage = 25000
    )

    val res = withTimeout(5000) { repo.addCar(car) }
    assertTrue("Adding car should succeed", res.isSuccess)

    val doc = firestore.collection("users").document(uid).collection("cars").document(car.id).get().await()
    assertTrue(doc.exists())
    assertEquals("Dacia Logan 1.5", doc.getString("name"))
  }

  @Test
  fun createBooking_authenticatedUser_createsBookingAndPayment() = runBlocking {
    val uid = signInTestUser("booking_user@test.com")
    val repo = FirestoreRentDarRepository(firestore)

    val booking = Booking(
      id = "bk_${UUID.randomUUID()}",
      carId = "car_1",
      carName = "Renault Clio 5",
      carPlate = "12345-B-6",
      clientName = "Anas Benjelloun",
      clientPhone = "+212611223344",
      clientCin = "BK998877",
      pickupDate = "2026-11-20",
      returnDate = "2026-11-25",
      dailyPrice = 350.0,
      days = 5,
      totalPrice = 1750.0,
      depositAmount = 3000.0,
      paidAmount = 500.0,
      status = BookingStatus.RESERVED
    )

    val res = withTimeout(5000) { repo.createBooking(booking) }
    assertTrue("Creating booking should succeed", res.isSuccess)

    val doc = firestore.collection("users").document(uid).collection("bookings").document(booking.id).get().await()
    assertTrue(doc.exists())
    assertEquals("Anas Benjelloun", doc.getString("clientName"))
  }

  @Test
  fun crossUserAccess_bobCannotReadAliceCars() = runBlocking {
    val aliceUid = signInTestUser("alice@test.com")
    val carId = "car_alice_${UUID.randomUUID()}"
    val aliceCarMap = mapOf(
      "userId" to aliceUid,
      "name" to "Alice Car",
      "plate" to "11111-A-26",
      "status" to "AVAILABLE",
      "year" to 2024,
      "dailyPrice" to 300.0,
      "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
      "updatedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
    )
    firestore.collection("users").document(aliceUid).collection("cars").document(carId).set(aliceCarMap).await()

    // Sign in as Bob
    signInTestUser("bob@test.com")

    try {
      withTimeout(5000) {
        firestore.collection("users").document(aliceUid).collection("cars").document(carId).get().await()
      }
      fail("Bob reading Alice's car document should be denied by rules")
    } catch (e: Exception) {
      val fError = e.unwrapFirestoreException()
      assertNotNull("Expected FirebaseFirestoreException PERMISSION_DENIED", fError)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fError?.code)
    }
  }

  @Test
  fun unauthenticatedAccess_failsWithPermissionDenied() = runBlocking {
    auth.signOut()
    try {
      withTimeout(5000) {
        firestore.collection("users").document("someone").collection("cars").get().await()
      }
      fail("Unauthenticated query should be rejected")
    } catch (e: Exception) {
      val fError = e.unwrapFirestoreException()
      assertNotNull("Expected PERMISSION_DENIED", fError)
      assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fError?.code)
    }
  }
}
