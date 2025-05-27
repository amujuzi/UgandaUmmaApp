package com.imaniapp.uganda.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.imaniapp.uganda.domain.model.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterDialog(
    currentFilter: MosqueFilter,
    onFilterApplied: (MosqueFilter) -> Unit,
    onDismiss: () -> Unit
) {
    var hasJummah by remember { mutableStateOf(currentFilter.hasJummah) }
    var hasWomenPrayer by remember { mutableStateOf(currentFilter.hasWomenPrayer) }
    var hasWudu by remember { mutableStateOf(currentFilter.hasWudu) }
    var maxDistance by remember { mutableStateOf(currentFilter.maxDistance) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Filter Mosques",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Facility filters
                Text(
                    text = "Required Facilities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                FilterCheckbox(
                    checked = hasJummah,
                    onCheckedChange = { hasJummah = it },
                    label = "Jummah Prayer",
                    icon = Icons.Default.Event
                )
                
                FilterCheckbox(
                    checked = hasWomenPrayer,
                    onCheckedChange = { hasWomenPrayer = it },
                    label = "Women's Prayer Area",
                    icon = Icons.Default.Female
                )
                
                FilterCheckbox(
                    checked = hasWudu,
                    onCheckedChange = { hasWudu = it },
                    label = "Wudu Facilities",
                    icon = Icons.Default.WaterDrop
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Distance filter
                Text(
                    text = "Maximum Distance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                val distanceOptions = listOf(
                    null to "Any distance",
                    5.0 to "Within 5 km",
                    10.0 to "Within 10 km",
                    25.0 to "Within 25 km",
                    50.0 to "Within 50 km"
                )
                
                distanceOptions.forEach { (distance, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = maxDistance == distance,
                                onClick = { maxDistance = distance }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = maxDistance == distance,
                            onClick = { maxDistance = distance }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            // Reset filters
                            hasJummah = false
                            hasWomenPrayer = false
                            hasWudu = false
                            maxDistance = null
                            onFilterApplied(MosqueFilter())
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Clear")
                    }
                    
                    Button(
                        onClick = {
                            onFilterApplied(
                                MosqueFilter(
                                    hasJummah = hasJummah,
                                    hasWomenPrayer = hasWomenPrayer,
                                    hasWudu = hasWudu,
                                    maxDistance = maxDistance
                                )
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Apply")
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
        Spacer(modifier = Modifier.width(8.dp))
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun RadiusDialog(
    currentRadius: Double,
    onRadiusSelected: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    val radiusOptions = listOf(1.0, 5.0, 10.0, 25.0, 50.0, 100.0)
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Search Radius",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Select how far to search for mosques",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                radiusOptions.forEach { radius ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = currentRadius == radius,
                                onClick = { onRadiusSelected(radius) }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentRadius == radius,
                            onClick = { onRadiusSelected(radius) }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "${radius.toInt()} km",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMosqueDialog(
    currentLocation: Location?,
    onMosqueAdded: (Mosque) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hasJummah by remember { mutableStateOf(true) }
    var hasWomenPrayer by remember { mutableStateOf(false) }
    var hasWudu by remember { mutableStateOf(true) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "Add New Mosque",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Mosque Name *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address *") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Website") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Facilities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                FilterCheckbox(
                    checked = hasJummah,
                    onCheckedChange = { hasJummah = it },
                    label = "Jummah Prayer",
                    icon = Icons.Default.Event
                )
                
                FilterCheckbox(
                    checked = hasWomenPrayer,
                    onCheckedChange = { hasWomenPrayer = it },
                    label = "Women's Prayer Area",
                    icon = Icons.Default.Female
                )
                
                FilterCheckbox(
                    checked = hasWudu,
                    onCheckedChange = { hasWudu = it },
                    label = "Wudu Facilities",
                    icon = Icons.Default.WaterDrop
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            if (name.isNotBlank() && address.isNotBlank()) {
                                val mosque = Mosque(
                                    id = UUID.randomUUID().toString(),
                                    name = name.trim(),
                                    address = address.trim(),
                                    latitude = currentLocation?.latitude ?: 0.0,
                                    longitude = currentLocation?.longitude ?: 0.0,
                                    phoneNumber = phoneNumber.trim().ifBlank { null },
                                    hasJummah = hasJummah,
                                    hasWomenPrayer = hasWomenPrayer,
                                    hasWudu = hasWudu,
                                    description = description.trim().ifBlank { null },
                                    website = website.trim().ifBlank { null }
                                )
                                onMosqueAdded(mosque)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = name.isNotBlank() && address.isNotBlank()
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportMosqueDialog(
    mosque: Mosque,
    onReportSubmitted: (String, String?) -> Unit,
    onDismiss: () -> Unit
) {
    var issue by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var selectedIssueType by remember { mutableStateOf("") }
    
    val issueTypes = listOf(
        "Incorrect location",
        "Mosque is closed/demolished",
        "Wrong contact information",
        "Incorrect facilities information",
        "Inappropriate content",
        "Other"
    )
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = "Report Issue",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "Report an issue with ${mosque.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "Issue Type",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                issueTypes.forEach { issueType ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedIssueType == issueType,
                                onClick = { selectedIssueType = issueType }
                            )
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedIssueType == issueType,
                            onClick = { selectedIssueType = issueType }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = issueType,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                OutlinedTextField(
                    value = issue,
                    onValueChange = { issue = it },
                    label = { Text("Additional Details") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 4,
                    placeholder = { Text("Please provide more details about the issue...") }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Your Email (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("For follow-up if needed") }
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val reportText = if (selectedIssueType.isNotEmpty()) {
                                "$selectedIssueType: $issue".trim()
                            } else {
                                issue.trim()
                            }
                            onReportSubmitted(
                                reportText,
                                email.trim().ifBlank { null }
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedIssueType.isNotEmpty() || issue.isNotBlank()
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MosqueDetailsBottomSheet(
    mosque: Mosque,
    onDismiss: () -> Unit,
    onGetDirections: (Mosque) -> Unit,
    onReport: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = mosque.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = mosque.address,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            if (mosque.distance != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${String.format("%.1f", mosque.distance)} km away",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Facilities
            Text(
                text = "Facilities",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (mosque.hasJummah) {
                    FacilityChip("Jummah", Icons.Default.Event, true)
                }
                if (mosque.hasWomenPrayer) {
                    FacilityChip("Women", Icons.Default.Female, true)
                }
                if (mosque.hasWudu) {
                    FacilityChip("Wudu", Icons.Default.WaterDrop, true)
                }
            }
            
            // Contact info
            if (mosque.phoneNumber != null || mosque.website != null) {
                Text(
                    text = "Contact",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                mosque.phoneNumber?.let { phone ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = phone,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                
                mosque.website?.let { website ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = website,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Description
            mosque.description?.let { description ->
                Text(
                    text = "About",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onGetDirections(mosque) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Directions")
                }
                
                OutlinedButton(
                    onClick = onReport,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Report, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Report")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FacilityChip(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isAvailable: Boolean
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isAvailable) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceVariant
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = if (isAvailable) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = if (isAvailable) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
} 