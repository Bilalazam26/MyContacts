package com.bilalazzam.mycontacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bilalazzam.contacts_provider.ContactsProvider
import dev.icerock.moko.permissions.PermissionState
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(contactsProvider: ContactsProvider,
        syncManager: ContactsSyncManager) {
    MaterialTheme {
        val factory = rememberPermissionsControllerFactory()
        val controller = remember(factory) {
            factory.createPermissionsController()
        }

        BindEffect(controller)

        val viewModel = viewModel {
            ContactsViewModel(contactsProvider, controller, syncManager)
        }

        val contacts = viewModel.contacts
        val permissionState = viewModel.permissionState
        val isLoading = viewModel.isLoading
        val workManagerStatus = viewModel.workManagerStatus
        val cacheInfo = viewModel.cacheInfo

        when (permissionState) {
            PermissionState.Granted -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .systemBarsPadding()
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                            )
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            // Status Cards
                            StatusCard(
                                title = "Work Manager Status / حالة مدير العمل",
                                status = workManagerStatus.getStatusText(),
                                isRunning = workManagerStatus.isRunning,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            )
                            
                            StatusCard(
                                title = "Cache Status / حالة التخزين المؤقت",
                                status = cacheInfo,
                                isRunning = false,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )

                            // Action Buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.reSyncContacts() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Re-Sync Contacts / إعادة مزامنة")
                                }
                                
                                Button(
                                    onClick = { viewModel.clearCache() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Clear Cache / مسح التخزين")
                                }
                            }
                            
                            // Test Work Manager Button
                            Button(
                                onClick = { 
                                    viewModel.testWorkManager() 
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text("Test One-Time Work Manager / اختبار مدير العمل")
                            }

                            LazyColumn(
                                modifier = Modifier.weight(1f),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                items(contacts) { contact ->
                                    ContactItem(contact)
                                }
                            }
                        }
                    }
                }
            }

            PermissionState.DeniedAlways -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Permission permanently declined.")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { controller.openAppSettings() }) {
                        Text("Open App Settings")
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Button(onClick = { viewModel.requestContactsPermission() }) {
                        Text("Sync Contacts")
                    }
                }
            }
        }
    }
}

@Composable
fun StatusCard(
    title: String,
    status: String,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isRunning) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = status,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            if (isRunning) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
