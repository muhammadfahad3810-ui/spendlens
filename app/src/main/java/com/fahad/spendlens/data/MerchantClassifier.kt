package com.fahad.spendlens.data

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.sqrt

class MerchantClassifier(context: Context) {

    companion object {
        private const val DIM = 1024
        val CATEGORIES = listOf(
            "Groceries", "Food", "Transport", "Fuel",
            "Shopping", "Utilities", "Health", "Other"
        )
    }

    private val interpreter: Interpreter

    init {
        val assetFd = context.assets.openFd("merchant_classifier.tflite")
        val channel = assetFd.createInputStream().channel
        val buffer = channel.map(
            FileChannel.MapMode.READ_ONLY,
            assetFd.startOffset,
            assetFd.declaredLength
        )
        interpreter = Interpreter(buffer)
    }

    /** Hash character trigrams into a fixed-size bag vector.
     *  MUST stay identical to the Python implementation in train.py. */
    private fun trigramHashVector(text: String): FloatArray {
        val v = FloatArray(DIM)
        val t = "  " + text.lowercase().trim() + "  "
        for (i in 0..t.length - 3) {
            var h = 0L
            for (j in i until i + 3) {
                h = (h * 31 + t[j].code) % 2147483647L
            }
            v[(h % DIM).toInt()] += 1.0f
        }
        // L2 normalize
        var sumSq = 0.0f
        for (x in v) sumSq += x * x
        val norm = sqrt(sumSq)
        if (norm > 0f) for (i in v.indices) v[i] /= norm
        return v
    }

    /** Returns the predicted category and its confidence (0..1). */
    fun classify(merchant: String): Pair<String, Float> {
        val input = ByteBuffer.allocateDirect(4 * DIM).order(ByteOrder.nativeOrder())
        trigramHashVector(merchant).forEach { input.putFloat(it) }
        input.rewind()

        val output = Array(1) { FloatArray(CATEGORIES.size) }
        interpreter.run(input, output)

        val probs = output[0]
        var best = 0
        for (i in probs.indices) if (probs[i] > probs[best]) best = i
        return CATEGORIES[best] to probs[best]
    }
}