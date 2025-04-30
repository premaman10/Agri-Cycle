package com.example.agricycle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import com.example.agricycle.R

import com.example.agricycle.viewmodel.LoginViewModel
import com.example.agricycle.utils.NotificationHelper

@OptIn(ExperimentalMaterial3Api::class, ExperimentalUnitApi::class)
@Composable
fun SettingsScreen(navController: NavController, loginViewModel: LoginViewModel = viewModel()) {
    var darkMode by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }
    var notifications by remember { mutableStateOf(notificationHelper.isNotificationsEnabled()) }
    var locationServices by remember { mutableStateOf(true) }
    var dataSync by remember { mutableStateOf(true) }
    
    val language by loginViewModel.language.collectAsState()
    
    
    // Observe language changes
    LaunchedEffect(Unit) {
        val initialLang = "English"
        withContext(Dispatchers.IO) {
            loginViewModel.setLanguage(initialLang)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // App Settings
            ListItem(
                headlineContent = { Text(stringResource(R.string.app_theme)) },
                leadingContent = { Icon(Icons.Default.DarkMode, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = darkMode,
                        onCheckedChange = { darkMode = it }
                    )
                }
            )
            Divider()

            ListItem(
                headlineContent = { Text(stringResource(R.string.notifications)) },
                supportingContent = { Text(stringResource(R.string.receive_alerts)) },
                leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = notifications,
                        onCheckedChange = {
                            notifications = it
                            notificationHelper.setNotificationsEnabled(it)
                        }
                    )
                }
            )
            Divider()

            ListItem(
                headlineContent = { Text(stringResource(R.string.location_services)) },
                supportingContent = { Text(stringResource(R.string.enable_location)) },
                leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = locationServices,
                        onCheckedChange = { locationServices = it }
                    )
                }
            )
            Divider()

            ListItem(
                headlineContent = { Text(stringResource(R.string.data_sync)) },
                supportingContent = { Text(stringResource(R.string.sync_data)) },
                leadingContent = { Icon(Icons.Default.Sync, contentDescription = null) },
                trailingContent = {
                    Switch(
                        checked = dataSync,
                        onCheckedChange = { dataSync = it }
                    )
                }
            )
            Divider()

            // Account Section
            ListItem(
                headlineContent = { Text(stringResource(R.string.account)) },
                supportingContent = { Text(stringResource(R.string.manage_account)) },
                leadingContent = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
                trailingContent = {
                    IconButton(onClick = { /* TODO: Navigate to account settings */ }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.navigate))
                    }
                }
            )
            Divider()

            ListItem(
                headlineContent = { Text(stringResource(R.string.privacy_policy)) },
                leadingContent = { Icon(Icons.Default.Security, contentDescription = null) },
                trailingContent = {
                    IconButton(onClick = { /* TODO: Show privacy policy */ }) {
                        Icon(Icons.Default.ChevronRight, contentDescription = stringResource(R.string.navigate))
                    }
                }
            )
            Divider()

            ListItem(
                headlineContent = { Text(stringResource(R.string.about)) },
                supportingContent = { Text(stringResource(R.string.version)) },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) }
            )
            Divider()
            
            // Language Settings
            val scope = rememberCoroutineScope()
            val onLanguageClick = remember {
                {
                    scope.launch {
                        val newLang = "English"
                        withContext(Dispatchers.IO) {
                            loginViewModel.setLanguage(newLang)
                            
                        }
                    }
                }
            }
            
            Surface(
                onClick = onLanguageClick as () -> Unit
            ) {
                ListItem(
                    headlineContent = { Text(stringResource(R.string.language)) },
                    supportingContent = { Text(stringResource(R.string.language_setting)) },
                    leadingContent = { Icon(Icons.Default.Language, contentDescription = null) },
                    trailingContent = {
                        Text(
                            text = if (true) {
                                stringResource(R.string.english)
                            } else {
                                stringResource(R.string.hindi)
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                )
            }
        }
    }
}