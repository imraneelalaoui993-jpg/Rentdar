package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.credentials.CredentialManager
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainAppScaffold
import com.example.ui.RentDarViewModel
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.attemptAutoSignIn
import com.example.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        AppContent()
      }
    }
  }
}

@Composable
fun AppContent() {
  var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }
  val context = LocalContext.current
  val coroutineScope = rememberCoroutineScope()
  val credentialManager = remember { CredentialManager.create(context) }

  DisposableEffect(Unit) {
    val listener = FirebaseAuth.AuthStateListener { auth ->
      currentUser = auth.currentUser
    }
    Firebase.auth.addAuthStateListener(listener)

    // Attempt silent auto-sign-in on app start
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = { currentUser = Firebase.auth.currentUser },
      onUnauthenticated = {},
      scope = coroutineScope
    )

    onDispose {
      Firebase.auth.removeAuthStateListener(listener)
    }
  }

  if (currentUser == null) {
    AuthScreen(
      onAuthSuccess = { currentUser = Firebase.auth.currentUser }
    )
  } else {
    val uid = currentUser!!.uid
    key(uid) {
      val viewModel: RentDarViewModel = viewModel(key = uid)
      DisposableEffect(uid) {
        viewModel.attachUser(uid)
        onDispose {}
      }
      MainAppScaffold(viewModel = viewModel)
    }
  }
}
