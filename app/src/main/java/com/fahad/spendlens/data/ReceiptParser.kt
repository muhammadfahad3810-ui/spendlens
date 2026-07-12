package com.fahad.spendlens.data

import java.text.SimpleDateFormat
import java.util.Locale

data class ParsedReceipt(
    val merchant: String,
    val total: Double?,
    val dateMillis: Long?
)

object ReceiptParser {

    // Matches amounts like 1,234.50 / 450 / 89.99
    private val amountRegex = Regex("""(\d{1,3}(?:[,]\d{3})*(?:\.\d{1,2})?|\d+(?:\.\d{1,2})?)""")

    // Lines that suggest "this line holds the final amount"
    private val totalKeywords = listOf(
        "total", "grand total", "amount due", "net amount",
        "balance", "amount payable", "net total", "payable"
    )

    // Lines that are definitely not merchant names
    private val junkKeywords = listOf(
        "receipt", "invoice", "tel", "phone", "ph:", "fax", "gst", "ntn",
        "date", "time", "cashier", "till", "vat", "tax", "www", ".com", "thank"
    )

    // Lines that typically contain address or other non-merchant info
    private val merchantJunk = listOf(
        "address", "st.", "street", "road", "ave", "avenue", "city", "zip", "code", "tel:", "phone:",
        "branch", "store", "welcome", "customer", "terminal", "auth", "sale", "order", "visa", "mastercard"
    )

    // Lines whose numbers are NOT amounts (receipt numbers, phones, tax IDs...)
    private val numberJunkKeywords = listOf(
        "no.", "no:", "receipt #", "invoice", "ntn", "gst #", "tel", "phone",
        "ph:", "fax", "till", "cashier", "reg", "str #", "trans", "member"
    )

    private val dateFormats = listOf(
        "dd/MM/yyyy", "dd-MM-yyyy", "dd.MM.yyyy",
        "dd/MM/yy", "dd-MM-yy",
        "yyyy-MM-dd", "MM/dd/yyyy",
        "dd MMM yyyy", "dd MMMM yyyy"
    )

    private val dateRegex = Regex(
        """(\d{1,2}[/\-.]\d{1,2}[/\-.]\d{2,4}|\d{4}-\d{2}-\d{2}|\d{1,2}\s+[A-Za-z]{3,9}\s+\d{4})"""
    )

    fun parse(ocrText: String): ParsedReceipt {
        val lines = ocrText.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }

        return ParsedReceipt(
            merchant = extractMerchant(lines),
            total = extractTotal(lines),
            dateMillis = extractDate(ocrText)
        )
    }

    private fun extractMerchant(lines: List<String>): String {
        // Score the first several lines; highest score wins
        val candidates = lines.take(8).filter { line ->
            val lower = line.lowercase()
            junkKeywords.none { lower.contains(it) } &&
                    merchantJunk.none { lower.contains(it) } &&
                    line.count { it.isLetter() } >= 3 &&
                    line.length in 3..40
        }

        return candidates.maxByOrNull { scoreMerchantLine(it) } ?: "Unknown merchant"
    }

    private fun scoreMerchantLine(line: String): Int {
        var score = 0
        val letters = line.filter { it.isLetter() }

        // ALL CAPS is the classic receipt store-name style
        if (letters.isNotEmpty() && letters.all { it.isUpperCase() }) score += 30

        // Longer names beat fragments
        score += minOf(line.length, 25)

        // Mostly letters (garbled OCR has stray digits/symbols)
        val letterRatio = letters.length.toFloat() / line.length
        if (letterRatio > 0.7f) score += 15

        // Multiple words look like a real name
        if (line.trim().split(Regex("\\s+")).size >= 2) score += 10

        return score
    }

    private fun extractTotal(lines: List<String>): Double? {
        // Strategy 1: a line containing a total keyword that also has a number
        for (line in lines) {
            val lower = line.lowercase()
            if (totalKeywords.any { lower.contains(it) }) {
                val amount = biggestAmountIn(line)
                if (amount != null) return amount
            }
        }
        // Strategy 2: keyword and amount separated by OCR — check next 2 lines
        for ((index, line) in lines.withIndex()) {
            val lower = line.lowercase()
            if (totalKeywords.any { lower.contains(it) }) {
                for (next in index + 1..minOf(index + 2, lines.lastIndex)) {
                    val amount = biggestAmountIn(lines[next])
                    if (amount != null) return amount
                }
            }
        }
        // Strategy 3: only non-junk lines; prefer decimal amounts
        val candidateLines = lines.filter { line ->
            val lower = line.lowercase()
            numberJunkKeywords.none { lower.contains(it) }
        }
        val decimals = candidateLines.mapNotNull { decimalAmountIn(it) }
        if (decimals.isNotEmpty()) return decimals.max()

        return candidateLines.mapNotNull { biggestAmountIn(it) }.maxOrNull()
    }

    private fun biggestAmountIn(line: String): Double? {
        val lower = line.lowercase()
        if (numberJunkKeywords.any { lower.contains(it) }) return null
        return amountRegex.findAll(line)
            .mapNotNull { it.value.replace(",", "").toDoubleOrNull() }
            .filter { it > 0 && it < 10_000_000 }
            .maxOrNull()
    }

    // Amounts WITH decimals only — strong signal of money vs. an ID number
    private fun decimalAmountIn(line: String): Double? {
        return amountRegex.findAll(line)
            .filter { it.value.contains(".") }
            .mapNotNull { it.value.replace(",", "").toDoubleOrNull() }
            .filter { it > 0 && it < 10_000_000 }
            .maxOrNull()
    }

    private fun extractDate(text: String): Long? {
        val match = dateRegex.find(text)?.value ?: return null
        for (format in dateFormats) {
            try {
                val sdf = SimpleDateFormat(format, Locale.ENGLISH)
                sdf.isLenient = false
                return sdf.parse(match)?.time
            } catch (_: Exception) { /* try next format */ }
        }
        return null
    }
}