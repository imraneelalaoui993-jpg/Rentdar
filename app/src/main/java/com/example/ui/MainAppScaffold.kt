package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.dialogs.AddEditCarDialog
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.BookingDetailScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.CarsScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.NewBookingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenDark
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.StatusRed
import com.example.util.StringsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScaffold(
  viewModel: RentDarViewModel,
  modifier: Modifier = Modifier
) {
  val currentTab by viewModel.currentTab.collectAsState()
  val selectedBooking by viewModel.selectedBooking.collectAsState()
  val showNewBookingSheet by viewModel.showNewBookingSheet.collectAsState()
  val showAddCarSheet by viewModel.showAddCarSheet.collectAsState()
  val carToEdit by viewModel.carToEdit.collectAsState()
  val lang by viewModel.currentLanguage.collectAsState()
  val role by viewModel.currentRole.collectAsState()
  val stats by viewModel.dashboardStats.collectAsState()
  val agency by viewModel.agency.collectAsState()

  val errorMessage by viewModel.errorMessage.collectAsState()
  val successMessage by viewModel.successMessage.collectAsState()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(errorMessage) {
    errorMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearMessages()
    }
  }

  LaunchedEffect(successMessage) {
    successMessage?.let {
      snackbarHostState.showSnackbar(it)
      viewModel.clearMessages()
    }
  }

  // Handle Android physical back button
  BackHandler(enabled = selectedBooking != null || showNewBookingSheet || showAddCarSheet) {
    when {
      selectedBooking != null -> viewModel.selectBooking(null)
      showNewBookingSheet -> viewModel.closeNewBooking()
      showAddCarSheet -> viewModel.closeAddCar()
    }
  }

  // Set LayoutDirection based on selected language (RTL for Arabic, LTR for French)
  val layoutDirection = if (lang == AppLanguage.AR) LayoutDirection.Rtl else LayoutDirection.Ltr

  CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
    Scaffold(
      modifier = modifier.fillMaxSize(),
      snackbarHost = { SnackbarHost(snackbarHostState) },
      topBar = {
        CenterAlignedTopAppBar(
          title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Box(
                modifier = Modifier
                  .size(30.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(Color.White),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = "RD",
                  fontWeight = FontWeight.Black,
                  color = EmeraldGreen,
                  fontSize = 14.sp
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(
                  text = "RentDar",
                  fontWeight = FontWeight.Bold,
                  fontSize = 17.sp,
                  color = Color.White
                )
                Text(
                  text = if (lang == AppLanguage.AR) "دار الكراء • المغرب" else "Gestion Location Maroc",
                  fontSize = 10.sp,
                  color = EmeraldGreenLight
                )
              }
            }
          },
          actions = {
            // Quick Language Switch Button
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color.White.copy(alpha = 0.18f),
              modifier = Modifier
                .clickable { viewModel.toggleLanguage() }
                .padding(end = 8.dp)
                .testTag("quick_lang_toggle")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  Icons.Default.Language,
                  contentDescription = "Langue",
                  tint = Color.White,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (lang == AppLanguage.FR) "AR" else "FR",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            // Quick Role Switch Button (allows instant switching between Owner & Employee)
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = if (role == UserRole.OWNER) AmberGold else Color(0xFF64748B),
              modifier = Modifier
                .clickable { viewModel.toggleRole() }
                .padding(end = 12.dp)
                .testTag("quick_role_toggle")
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  Icons.Default.Person,
                  contentDescription = "Rôle",
                  tint = Color.White,
                  modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                  text = if (role == UserRole.OWNER) "Admin" else "Employé",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          },
          colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = EmeraldGreen,
            titleContentColor = Color.White
          )
        )
      },
      bottomBar = {
        NavigationBar(
          containerColor = Color.White,
          tonalElevation = 6.dp,
          modifier = Modifier.testTag("bottom_nav_bar")
        ) {
          NavigationBarItem(
            selected = currentTab == ScreenTab.DASHBOARD,
            onClick = { viewModel.setTab(ScreenTab.DASHBOARD) },
            icon = {
              Icon(Icons.Default.Dashboard, contentDescription = "Dashboard")
            },
            label = {
              Text(
                StringsHelper.tr(lang, "Accueil", "الرئيسية"),
                fontSize = 11.sp,
                fontWeight = if (currentTab == ScreenTab.DASHBOARD) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = EmeraldGreen,
              selectedTextColor = EmeraldGreen,
              indicatorColor = EmeraldGreenLight
            ),
            modifier = Modifier.testTag("nav_tab_dashboard")
          )

          NavigationBarItem(
            selected = currentTab == ScreenTab.CARS,
            onClick = { viewModel.setTab(ScreenTab.CARS) },
            icon = {
              Icon(Icons.Default.DirectionsCar, contentDescription = "Voitures")
            },
            label = {
              Text(
                StringsHelper.tr(lang, "Véhicules", "السيارات"),
                fontSize = 11.sp,
                fontWeight = if (currentTab == ScreenTab.CARS) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = EmeraldGreen,
              selectedTextColor = EmeraldGreen,
              indicatorColor = EmeraldGreenLight
            ),
            modifier = Modifier.testTag("nav_tab_cars")
          )

          NavigationBarItem(
            selected = currentTab == ScreenTab.CALENDAR,
            onClick = { viewModel.setTab(ScreenTab.CALENDAR) },
            icon = {
              Icon(Icons.Default.CalendarMonth, contentDescription = "Calendrier")
            },
            label = {
              Text(
                StringsHelper.tr(lang, "Planning", "التقويم"),
                fontSize = 11.sp,
                fontWeight = if (currentTab == ScreenTab.CALENDAR) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = EmeraldGreen,
              selectedTextColor = EmeraldGreen,
              indicatorColor = EmeraldGreenLight
            ),
            modifier = Modifier.testTag("nav_tab_calendar")
          )

          NavigationBarItem(
            selected = currentTab == ScreenTab.ALERTS,
            onClick = { viewModel.setTab(ScreenTab.ALERTS) },
            icon = {
              if (stats.urgentAlertsCount > 0) {
                BadgedBox(
                  badge = {
                    Badge(containerColor = StatusRed) {
                      Text("${stats.urgentAlertsCount}")
                    }
                  }
                ) {
                  Icon(Icons.Default.Notifications, contentDescription = "Alertes")
                }
              } else {
                Icon(Icons.Default.Notifications, contentDescription = "Alertes")
              }
            },
            label = {
              Text(
                StringsHelper.tr(lang, "Alertes", "التنبيهات"),
                fontSize = 11.sp,
                fontWeight = if (currentTab == ScreenTab.ALERTS) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = EmeraldGreen,
              selectedTextColor = EmeraldGreen,
              indicatorColor = EmeraldGreenLight
            ),
            modifier = Modifier.testTag("nav_tab_alerts")
          )

          NavigationBarItem(
            selected = currentTab == ScreenTab.SETTINGS,
            onClick = { viewModel.setTab(ScreenTab.SETTINGS) },
            icon = {
              Icon(Icons.Default.Settings, contentDescription = "Paramètres")
            },
            label = {
              Text(
                StringsHelper.tr(lang, "Réglages", "الإعدادات"),
                fontSize = 11.sp,
                fontWeight = if (currentTab == ScreenTab.SETTINGS) FontWeight.Bold else FontWeight.Normal
              )
            },
            colors = NavigationBarItemDefaults.colors(
              selectedIconColor = EmeraldGreen,
              selectedTextColor = EmeraldGreen,
              indicatorColor = EmeraldGreenLight
            ),
            modifier = Modifier.testTag("nav_tab_settings")
          )
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        when (currentTab) {
          ScreenTab.DASHBOARD -> DashboardScreen(viewModel = viewModel)
          ScreenTab.CARS -> CarsScreen(viewModel = viewModel)
          ScreenTab.CALENDAR -> CalendarScreen(viewModel = viewModel)
          ScreenTab.ALERTS -> AlertsScreen(viewModel = viewModel)
          ScreenTab.SETTINGS -> SettingsScreen(viewModel = viewModel)
        }

        // Subscreen: Booking Detail Overlay
        selectedBooking?.let { bk ->
          BookingDetailScreen(
            booking = bk,
            viewModel = viewModel,
            onClose = { viewModel.selectBooking(null) }
          )
        }

        // Subscreen: 3-Step New Booking Wizard
        if (showNewBookingSheet) {
          NewBookingScreen(
            viewModel = viewModel,
            onClose = { viewModel.closeNewBooking() }
          )
        }

        // Dialog: Add or Edit Car
        if (showAddCarSheet) {
          AddEditCarDialog(
            viewModel = viewModel,
            carToEdit = carToEdit,
            onDismiss = { viewModel.closeAddCar() }
          )
        }
      }
    }
  }
}
