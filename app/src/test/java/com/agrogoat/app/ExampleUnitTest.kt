package com.agrogoat.app

import org.junit.Assert.*
import org.junit.Test
import com.agrogoat.core.shared.AgroGoatViewModel

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testViewModelInitializationAndState() {
    val viewModel = AgroGoatViewModel()

    // 1. Verify default values of the view model
    assertFalse(viewModel.isDarkTheme.value)
    assertTrue(viewModel.isOnline.value)
    assertEquals("Siti Zahfia", viewModel.userName.value)
    assertEquals("Pedagang", viewModel.userRole.value)

    // 2. Verify setOnlineStatus updates the isOnline flow properly
    viewModel.setOnlineStatus(false)
    assertFalse(viewModel.isOnline.value)

    viewModel.setOnlineStatus(true)
    assertTrue(viewModel.isOnline.value)

    // 3. Verify theme toggle updates isDarkTheme flow properly
    viewModel.toggleTheme()
    assertTrue(viewModel.isDarkTheme.value)

    viewModel.toggleTheme()
    assertFalse(viewModel.isDarkTheme.value)

    // 4. Verify profile setters work
    viewModel.setUserProfile(
      name = "Budi Santoso",
      address = "Bengkalis, Riau",
      balance = 2000000L,
      role = "Penjual",
      email = "budi@agrogoat.com",
      phone = "081211112222"
    )
    assertEquals("Budi Santoso", viewModel.userName.value)
    assertEquals("Penjual", viewModel.userRole.value)
    assertEquals(2000000L, viewModel.userBalance.value)
    assertEquals("Bengkalis, Riau", viewModel.userAddress.value)
  }
}


