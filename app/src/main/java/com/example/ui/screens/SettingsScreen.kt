package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AgencyProfile
import com.example.data.model.AppLanguage
import com.example.data.model.UserRole
import com.example.ui.RentDarViewModel
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.EmeraldGreenLight
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusOrange
import com.example.util.StringsHelper

@Composable
fun SettingsScreen(
  viewModel: RentDarViewModel,
  modifier: Modifier = Modifier
) {
  val agency by viewModel.agency.collectAsState()
  val role by viewModel.currentRole.collectAsState()
  val lang by viewModel.currentLanguage.collectAsState()
  val cars by viewModel.cars.collectAsState()

  var agencyName by remember(agency) { mutableStateOf(agency.name) }
  var city by remember(agency) { mutableStateOf(agency.city) }
  var phone by remember(agency) { mutableStateOf(agency.phone) }
  var license by remember(agency) { mutableStateOf(agency.licenseNumber) }
  var termsFr by remember(agency) { mutableStateOf(agency.contractTermsFr) }
  var termsAr by remember(agency) { mutableStateOf(agency.contractTermsAr) }

  Column(
    modifier = modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .verticalScroll(rememberScrollState()),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    Spacer(modifier = Modifier.height(8.dp))

    Text(
      text = StringsHelper.tr(lang, "Paramètres de l'agence", "إعدادات الوكالة"),
      fontWeight = FontWeight.Bold,
      fontSize = 20.sp,
      color = Color(0xFF0F172A)
    )

    // 1. Language Toggle
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = BorderStroke(1.dp, CardBorder)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Language, contentDescription = null, tint = EmeraldGreen)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = StringsHelper.tr(lang, "Langue de l'application", "لغة التطبيق"),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }
        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          val isFr = lang == AppLanguage.FR
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isFr) EmeraldGreen else Color(0xFFF1F5F9),
            modifier = Modifier
              .weight(1f)
              .clickable { viewModel.setLanguage(AppLanguage.FR) }
              .testTag("lang_fr_button")
          ) {
            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
              Text(
                text = "Français (Défaut)",
                color = if (isFr) Color.White else Color(0xFF334155),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
          }

          val isAr = lang == AppLanguage.AR
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isAr) EmeraldGreen else Color(0xFFF1F5F9),
            modifier = Modifier
              .weight(1f)
              .clickable { viewModel.setLanguage(AppLanguage.AR) }
              .testTag("lang_ar_button")
          ) {
            Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
              Text(
                text = "العربية (RTL)",
                color = if (isAr) Color.White else Color(0xFF334155),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
            }
          }
        }
      }
    }

    // 2. User Role Switcher
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = BorderStroke(1.dp, CardBorder)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.People, contentDescription = null, tint = AmberGold)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = StringsHelper.tr(lang, "Rôle utilisateur actuel", "صفة المستخدم الحالية"),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = StringsHelper.tr(
            lang,
            "Basculez entre Propriétaire et Employé pour tester les restrictions de permissions (revenus masqués, interdiction de suppression).",
            "قم بالتبديل بين المالك والمستخدم لتجربة صلاحيات كل دور."
          ),
          fontSize = 12.sp,
          color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(12.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          val isOwner = role == UserRole.OWNER
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isOwner) AmberGold else Color(0xFFF1F5F9),
            modifier = Modifier
              .weight(1f)
              .clickable { viewModel.setRole(UserRole.OWNER) }
              .testTag("role_owner_button")
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = StringsHelper.tr(lang, "Propriétaire (Admin)", "المالك (مدير)"),
                color = if (isOwner) Color.White else Color(0xFF334155),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
              Text(
                text = StringsHelper.tr(lang, "Accès total + revenus", "صلاحيات كاملة"),
                color = if (isOwner) Color.White.copy(alpha = 0.8f) else Color(0xFF64748B),
                fontSize = 10.sp
              )
            }
          }

          val isEmployee = role == UserRole.EMPLOYEE
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isEmployee) Color(0xFF475569) else Color(0xFFF1F5F9),
            modifier = Modifier
              .weight(1f)
              .clickable { viewModel.setRole(UserRole.EMPLOYEE) }
              .testTag("role_employee_button")
          ) {
            Column(
              modifier = Modifier.padding(12.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Text(
                text = StringsHelper.tr(lang, "Employé", "مستخدم"),
                color = if (isEmployee) Color.White else Color(0xFF334155),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
              )
              Text(
                text = StringsHelper.tr(lang, "Revenus masqués", "حجب المداخيل"),
                color = if (isEmployee) Color.White.copy(alpha = 0.8f) else Color(0xFF64748B),
                fontSize = 10.sp
              )
            }
          }
        }
      }
    }

    // 3. Billing & Subscription Plans
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = BorderStroke(1.dp, CardBorder)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Star, contentDescription = null, tint = AmberGold)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = StringsHelper.tr(lang, "Abonnement & Forfaits", "الباقات والاشتراك"),
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp
            )
          }
          Surface(shape = RoundedCornerShape(6.dp), color = EmeraldGreenLight) {
            Text(
              text = StringsHelper.tr(lang, "${agency.trialDaysLeft}j essai restant", "متبقي 14 يوم تجربة"),
              color = EmeraldGreen,
              fontSize = 10.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = StringsHelper.tr(
            lang,
            "Parc actuel : ${cars.size} véhicules enregistrés.",
            "الأسطول الحالي: ${cars.size} سيارات مسجلة."
          ),
          fontSize = 12.sp,
          color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(10.dp))

        // 3 Plans: Starter, Pro, Unlimited
        listOf(
          Triple("Starter", "5 voitures max", "290 MAD / mois"),
          Triple("Pro", "15 voitures max", "590 MAD / mois"),
          Triple("Unlimited", "Voitures illimitées", "990 MAD / mois")
        ).forEach { (planName, limitDesc, price) ->
          val isCurrent = agency.plan == planName
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = if (isCurrent) EmeraldGreenLight.copy(alpha = 0.4f) else Color(0xFFF8FAFC),
            border = BorderStroke(if (isCurrent) 1.5.dp else 1.dp, if (isCurrent) EmeraldGreen else CardBorder),
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              .clickable { viewModel.setPlan(planName) }
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Text(text = planName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                  if (isCurrent) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(shape = RoundedCornerShape(4.dp), color = EmeraldGreen) {
                      Text(
                        text = StringsHelper.tr(lang, "ACTIF", "مفعل"),
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                      )
                    }
                  }
                }
                Text(text = limitDesc, fontSize = 11.sp, color = Color(0xFF64748B))
              }
              Text(text = price, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = EmeraldGreen)
            }
          }
        }
      }
    }

    // 4. Agency Information
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = BorderStroke(1.dp, CardBorder)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Business, contentDescription = null, tint = EmeraldGreen)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = StringsHelper.tr(lang, "Informations de l'agence", "معلومات الوكالة"),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
          value = agencyName,
          onValueChange = { agencyName = it },
          label = { Text(StringsHelper.tr(lang, "Nom de l'agence", "اسم الوكالة")) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedTextField(
            value = city,
            onValueChange = { city = it },
            label = { Text(StringsHelper.tr(lang, "Ville", "المدينة")) },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
          OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(StringsHelper.tr(lang, "Téléphone", "الهاتف")) },
            modifier = Modifier.weight(1f),
            singleLine = true
          )
        }
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
          value = license,
          onValueChange = { license = it },
          label = { Text(StringsHelper.tr(lang, "N° Registre de Commerce (RC)", "السجل التجاري")) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        Button(
          onClick = {
            viewModel.updateAgency(
              agency.copy(
                name = agencyName,
                city = city,
                phone = phone,
                licenseNumber = license,
                contractTermsFr = termsFr,
                contractTermsAr = termsAr
              )
            )
          },
          colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
          modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
            .testTag("save_agency_button")
        ) {
          Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = StringsHelper.tr(lang, "Enregistrer les modifications", "حفظ التعديلات"),
            fontWeight = FontWeight.Bold
          )
        }
      }
    }

    // 5. Account & Cloud Database Session
    val authUser = FirebaseAuth.getInstance().currentUser
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val credentialManager = remember { androidx.credentials.CredentialManager.create(context) }

    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = Color.White),
      border = BorderStroke(1.dp, CardBorder)
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Security, contentDescription = null, tint = EmeraldGreen)
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = StringsHelper.tr(lang, "Compte & Synchronisation Firebase", "الحساب ومزامنة فايربيس"),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = if (authUser != null) {
            "${authUser.email ?: authUser.displayName ?: authUser.uid} • Connecté"
          } else {
            "Non connecté"
          },
          fontSize = 12.sp,
          color = Color(0xFF64748B)
        )
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
          onClick = {
            com.example.ui.auth.signOutGoogle(
              credentialManager = credentialManager,
              scope = coroutineScope
            )
          },
          border = BorderStroke(1.dp, com.example.ui.theme.StatusRed),
          shape = RoundedCornerShape(10.dp),
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .testTag("sign_out_button")
        ) {
          Text(
            text = StringsHelper.tr(lang, "Se déconnecter", "تسجيل الخروج"),
            color = com.example.ui.theme.StatusRed,
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
          )
        }
      }
    }

    Spacer(modifier = Modifier.height(40.dp))
  }
}
