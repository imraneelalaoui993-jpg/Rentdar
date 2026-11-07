package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Booking
import com.example.data.model.BookingStatus
import com.example.data.model.Car
import com.example.data.model.CarStatus
import com.example.data.model.UserRole
import com.example.data.repository.RentDarRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("RentDar", appName)
  }

  @Test
  fun `verify demo seed data contains 3 cars and 4 bookings`() {
    val repo = RentDarRepository()
    assertEquals(3, repo.cars.value.size)
    assertEquals(4, repo.bookings.value.size)
  }

  @Test
  fun `verify calendar overlap detection blocks overlapping dates`() {
    val repo = RentDarRepository()
    // Clio 5 (car_2) has active booking from 2026-11-05 to 2026-11-10
    val overlap = repo.checkOverlap("car_2", "2026-11-08", "2026-11-12")
    assertNotNull("Should detect overlap with existing booking", overlap)

    // Creating booking on overlapping dates should fail
    val duplicateBooking = Booking(
      id = "dup_1",
      carId = "car_2",
      carName = "Renault Clio 5 TCe",
      carPlate = "58392-B-6",
      clientName = "Nabil Chraibi",
      clientPhone = "+212600112233",
      clientCin = "A123456",
      pickupDate = "2026-11-08",
      returnDate = "2026-11-12",
      dailyPrice = 350.0,
      days = 4,
      totalPrice = 1400.0,
      depositAmount = 3000.0,
      paidAmount = 0.0,
      status = BookingStatus.RESERVED
    )
    val result = repo.createBooking(duplicateBooking)
    assertTrue(result.isFailure)
  }

  @Test
  fun `verify employee cannot delete car or booking`() {
    val repo = RentDarRepository()
    repo.setRole(UserRole.EMPLOYEE)

    val carDelete = repo.deleteCar("car_1")
    assertTrue("Employee cannot delete cars", carDelete.isFailure)

    val bookingDelete = repo.deleteBooking("bk_1")
    assertTrue("Employee cannot delete bookings", bookingDelete.isFailure)
  }

  @Test
  fun `verify Starter plan enforces car limit of 5`() {
    val repo = RentDarRepository()
    assertEquals(5, repo.getPlanLimit())
    assertEquals(3, repo.cars.value.size)
    assertTrue(repo.canAddCar())

    // Add 2 cars to reach 5
    for (i in 4..5) {
      repo.addCar(
        Car(
          id = "car_$i",
          name = "Test Car $i",
          plate = "0000$i-A-26",
          year = 2024,
          dailyPrice = 300.0,
          currentMileage = 10000,
          status = CarStatus.AVAILABLE,
          insuranceExpiry = "2027-01-01",
          vignetteExpiry = "2027-01-01",
          controleTechniqueExpiry = "2027-01-01",
          nextOilChangeMileage = 20000
        )
      )
    }

    assertEquals(5, repo.cars.value.size)
    // 6th car should be blocked
    val sixthCarResult = repo.addCar(
      Car(
        id = "car_6",
        name = "Test Car 6",
        plate = "00006-A-26",
        year = 2024,
        dailyPrice = 300.0,
        currentMileage = 10000,
        status = CarStatus.AVAILABLE,
        insuranceExpiry = "2027-01-01",
        vignetteExpiry = "2027-01-01",
        controleTechniqueExpiry = "2027-01-01",
        nextOilChangeMileage = 20000
      )
    )
    assertTrue("6th car should exceed Starter plan limit", sixthCarResult.isFailure)
  }
}
