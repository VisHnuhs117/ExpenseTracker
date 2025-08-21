package com.vishnuhs.expensetracker.presentation.ui.components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.vishnuhs.expensetracker.ml.ReceiptData
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReceiptPreview(
    imageUri: Uri,
    receiptData: ReceiptData?,
    isProcessing: Boolean,
    extractedText: String? = null, // Add this parameter
    onRetakePhoto: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Image preview
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(imageUri),
                contentDescription = "Receipt photo",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentScale = ContentScale.Crop
            )
        }

        // Processing indicator
        if (isProcessing) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    Text(
                        text = "Processing receipt...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Debug: Show extracted text
        extractedText?.let { text ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "🔍 Raw Extracted Text",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (text.isBlank()) "No text found" else text,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.heightIn(max = 150.dp)
                    )
                }
            }
        }

        // Extracted data
        receiptData?.let { data ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "📋 Parsed Information",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Divider()

                    // Show what was found vs not found
                    InfoRow(
                        label = "Amount:",
                        value = data.amount?.let { "$${String.format("%.2f", it)}" } ?: "❌ Not found",
                        isFound = data.amount != null
                    )

                    InfoRow(
                        label = "Merchant:",
                        value = data.merchant ?: "❌ Not found",
                        isFound = data.merchant != null
                    )

                    InfoRow(
                        label = "Date:",
                        value = data.date?.let {
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it)
                        } ?: "❌ Not found",
                        isFound = data.date != null
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Confidence:", fontWeight = FontWeight.Medium)
                        Text(
                            text = "${(data.confidence * 100).toInt()}%",
                            color = if (data.confidence > 0.5f) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            }
                        )
                    }

                    if (data.items.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Items found:",
                            fontWeight = FontWeight.Medium
                        )
                        data.items.take(3).forEach { item ->
                            Text(
                                text = "• $item",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                        if (data.items.size > 3) {
                            Text(
                                text = "... and ${data.items.size - 3} more items",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Show message if no data was extracted
        if (!isProcessing && receiptData?.confidence == 0f) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "⚠️ No receipt data found",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Try retaking the photo with better lighting and make sure the receipt is clearly visible.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onRetakePhoto,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Retake")
            }

            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f),
                enabled = !isProcessing
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Continue")
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    isFound: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontWeight = FontWeight.Medium)
        Text(
            text = value,
            color = if (isFound) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.error
            }
        )
    }
}