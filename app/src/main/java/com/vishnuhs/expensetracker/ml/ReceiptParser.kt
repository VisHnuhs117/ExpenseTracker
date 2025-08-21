package com.vishnuhs.expensetracker.ml

import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern
import javax.inject.Inject
import javax.inject.Singleton

data class ReceiptData(
    val amount: Double? = null,
    val merchant: String? = null,
    val date: Date? = null,
    val items: List<String> = emptyList(),
    val confidence: Float = 0f
)

@Singleton
class ReceiptParser @Inject constructor() {

    companion object {
        private const val TAG = "ReceiptParser"
    }

    // More comprehensive amount patterns
    private val amountPatterns = listOf(
        // Standard patterns
        Pattern.compile("(?i)(?:total|amount|sum|balance)\\s*:?\\s*\\$?([0-9]+\\.?[0-9]{0,2})"),
        Pattern.compile("(?i)(?:grand total|final total|total amount)\\s*:?\\s*\\$?([0-9]+\\.?[0-9]{0,2})"),
        Pattern.compile("(?i)(?:subtotal)\\s*:?\\s*\\$?([0-9]+\\.?[0-9]{0,2})"),

        // Currency symbols
        Pattern.compile("\\$\\s*([0-9]+\\.[0-9]{2})(?!\\s*[0-9])"), // $XX.XX not followed by more numbers
        Pattern.compile("([0-9]+\\.[0-9]{2})\\s*\\$"),

        // End of line patterns
        Pattern.compile("([0-9]+\\.[0-9]{2})\\s*$", Pattern.MULTILINE),

        // Common receipt formats
        Pattern.compile("(?i)(?:pay|paid|due)\\s*:?\\s*\\$?([0-9]+\\.?[0-9]{0,2})"),
        Pattern.compile("([0-9]+\\.[0-9]{2})\\s*(?i)(?:total|usd|eur|gbp)"),

        // Simple number patterns (as fallback)
        Pattern.compile("\\b([0-9]{1,4}\\.[0-9]{2})\\b")
    )

    // Improved date patterns
    private val datePatterns = listOf(
        SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
        SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()),
        SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()),
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault()),
        SimpleDateFormat("MM/dd/yy", Locale.getDefault()),
        SimpleDateFormat("dd/MM/yy", Locale.getDefault()),
        SimpleDateFormat("M/d/yyyy", Locale.getDefault()),
        SimpleDateFormat("d/M/yyyy", Locale.getDefault())
    )

    // Common merchant patterns
    private val merchantPatterns = listOf(
        // Store names at beginning
        Pattern.compile("^([A-Za-z][A-Za-z\\s&'.-]{2,30})(?=\\n|$)", Pattern.MULTILINE),

        // Lines with store indicators
        Pattern.compile("(?i)^(.+?)\\s*(?:store|shop|market|restaurant|cafe|ltd|llc|inc)\\s*$", Pattern.MULTILINE),

        // Address-like patterns (stores often have addresses)
        Pattern.compile("^([A-Za-z\\s]{3,25})\\n.*(?:[0-9]{5}|street|st\\.|ave|blvd)", Pattern.MULTILINE or Pattern.DOTALL),

        // Thank you patterns
        Pattern.compile("(?i)thank you for (?:shopping|visiting)\\s+(.+?)(?=\\n|$)"),

        // Receipt header patterns
        Pattern.compile("(?i)^([A-Za-z\\s]{3,20}).*receipt", Pattern.MULTILINE)
    )

    fun parseReceiptText(text: String): ReceiptData {
        Log.d(TAG, "Parsing receipt text: ${text.take(200)}...")

        if (text.isBlank()) {
            Log.w(TAG, "Empty text provided")
            return ReceiptData(confidence = 0f)
        }

        val lines = text.split("\n")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        Log.d(TAG, "Processing ${lines.size} lines")

        val amount = extractAmount(lines, text)
        val merchant = extractMerchant(lines, text)
        val date = extractDate(lines, text)
        val items = extractItems(lines)

        val confidence = calculateConfidence(amount, merchant, date, text)

        val result = ReceiptData(
            amount = amount,
            merchant = merchant,
            date = date,
            items = items,
            confidence = confidence
        )

        Log.d(TAG, "Parsing result: $result")
        return result
    }

    private fun extractAmount(lines: List<String>, fullText: String): Double? {
        val foundAmounts = mutableListOf<Double>()

        // Try each pattern
        for (pattern in amountPatterns) {
            val matcher = pattern.matcher(fullText)
            while (matcher.find()) {
                try {
                    val amountStr = matcher.group(1)
                    val amount = amountStr?.toDoubleOrNull()
                    if (amount != null && amount > 0 && amount < 10000) { // Reasonable bounds
                        foundAmounts.add(amount)
                        Log.d(TAG, "Found amount: $amount from pattern: ${pattern.pattern()}")
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Error parsing amount: ${e.message}")
                }
            }
        }

        // Return the largest reasonable amount (likely the total)
        val result = foundAmounts.filter { it > 0.5 }.maxOrNull() // Minimum 50 cents
        Log.d(TAG, "Selected amount: $result from candidates: $foundAmounts")
        return result
    }

    private fun extractMerchant(lines: List<String>, fullText: String): String? {
        // Try first few lines (merchants usually at top)
        for (i in 0..minOf(4, lines.size - 1)) {
            val line = lines[i].trim()

            // Skip lines with mostly numbers or symbols
            if (line.matches(Regex(".*[0-9]{4,}.*")) ||
                line.matches(Regex(".*[#@$%&*+=<>{}\\[\\]]{3,}.*"))) {
                continue
            }

            // Look for reasonable merchant names
            if (line.length in 3..50 &&
                line.matches(Regex(".*[a-zA-Z]{3,}.*")) &&
                !line.contains(Regex("(?i)(receipt|invoice|bill|total|amount|date|time|card|cash)"))) {

                Log.d(TAG, "Found potential merchant: $line")
                return line.trim()
            }
        }

        // Try pattern matching as fallback
        for (pattern in merchantPatterns) {
            val matcher = pattern.matcher(fullText)
            if (matcher.find()) {
                val merchant = matcher.group(1)?.trim()
                if (merchant != null && merchant.length > 2) {
                    Log.d(TAG, "Found merchant via pattern: $merchant")
                    return merchant
                }
            }
        }

        Log.d(TAG, "No merchant found")
        return null
    }

    private fun extractDate(lines: List<String>, fullText: String): Date? {
        // Common date regex patterns
        val dateRegexes = listOf(
            "\\b\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}\\b", // MM/dd/yyyy, dd/MM/yyyy
            "\\b\\d{4}[/-]\\d{1,2}[/-]\\d{1,2}\\b",   // yyyy-MM-dd
            "\\b\\d{1,2}\\s+[A-Za-z]{3}\\s+\\d{2,4}\\b", // dd MMM yyyy
            "\\b[A-Za-z]{3}\\s+\\d{1,2},?\\s+\\d{2,4}\\b" // MMM dd, yyyy
        )

        for (regex in dateRegexes) {
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(fullText)

            while (matcher.find()) {
                val dateStr = matcher.group()
                Log.d(TAG, "Found potential date string: $dateStr")

                for (dateFormat in datePatterns) {
                    try {
                        val date = dateFormat.parse(dateStr)
                        if (date != null) {
                            val now = Date()
                            val yearAgo = Date(now.time - 365L * 24 * 60 * 60 * 1000)
                            val tomorrow = Date(now.time + 24L * 60 * 60 * 1000)

                            // Check if date is reasonable (not too old or in future)
                            if (date.after(yearAgo) && date.before(tomorrow)) {
                                Log.d(TAG, "Found valid date: $date")
                                return date
                            }
                        }
                    } catch (e: Exception) {
                        // Try next format
                        continue
                    }
                }
            }
        }

        Log.d(TAG, "No valid date found")
        return null
    }

    private fun extractItems(lines: List<String>): List<String> {
        val items = mutableListOf<String>()

        // Look for lines that might be items (have letters and numbers/prices)
        val itemPatterns = listOf(
            Pattern.compile("^([A-Za-z][A-Za-z\\s]{2,30})\\s+\\$?([0-9]+\\.?[0-9]{0,2})$"),
            Pattern.compile("^([A-Za-z\\s]{3,30})\\s{2,}\\$?([0-9]+\\.?[0-9]{0,2})$"),
            Pattern.compile("^\\d+\\s+([A-Za-z][A-Za-z\\s]{2,25})\\s+\\$?([0-9]+\\.?[0-9]{0,2})$")
        )

        for (line in lines) {
            for (pattern in itemPatterns) {
                val matcher = pattern.matcher(line.trim())
                if (matcher.find()) {
                    val itemName = matcher.group(1)?.trim()
                    val price = matcher.group(2)?.toDoubleOrNull()

                    if (itemName != null && itemName.length > 2 &&
                        price != null && price > 0 && price < 1000) {
                        items.add(itemName)
                        Log.d(TAG, "Found item: $itemName - $price")

                        if (items.size >= 10) break // Limit items
                    }
                }
            }
        }

        Log.d(TAG, "Found ${items.size} items")
        return items.take(10) // Limit to 10 items
    }

    private fun calculateConfidence(
        amount: Double?,
        merchant: String?,
        date: Date?,
        fullText: String
    ): Float {
        var confidence = 0f
        var maxConfidence = 0f

        // Amount confidence (most important)
        if (amount != null && amount > 0) {
            confidence += 0.5f
            Log.d(TAG, "Amount found: +0.5 confidence")
        }
        maxConfidence += 0.5f

        // Merchant confidence
        if (merchant != null && merchant.isNotEmpty()) {
            confidence += 0.3f
            Log.d(TAG, "Merchant found: +0.3 confidence")
        }
        maxConfidence += 0.3f

        // Date confidence
        if (date != null) {
            confidence += 0.2f
            Log.d(TAG, "Date found: +0.2 confidence")
        }
        maxConfidence += 0.2f

        // Bonus for receipt-like keywords
        val receiptKeywords = listOf("total", "receipt", "subtotal", "tax", "payment", "cash", "card")
        val keywordCount = receiptKeywords.count { fullText.contains(it, ignoreCase = true) }
        if (keywordCount > 0) {
            val bonus = minOf(0.1f, keywordCount * 0.02f)
            confidence += bonus
            Log.d(TAG, "Receipt keywords bonus: +$bonus confidence")
        }

        val finalConfidence = minOf(1.0f, confidence)
        Log.d(TAG, "Final confidence: $finalConfidence")
        return finalConfidence
    }
}