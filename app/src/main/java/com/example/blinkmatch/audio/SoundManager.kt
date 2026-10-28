package com.example.blinkmatch.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundManager {

    private val scope = CoroutineScope(Dispatchers.Default)

    enum class SoundType {
        COUNTDOWN_TICK,
        COUNTDOWN_BLINK,
        SYMBOL_APPEAR,
        CELL_TAP,
        CELL_DESELECT,
        CORRECT,
        PERFECT_ROUND,
        PERFECT_STREAK,
        WRONG,
        GAME_OVER
    }

    private val sampleRate = 44100
    private val soundBuffers = mutableMapOf<SoundType, ShortArray>()

    init {
        // Pre-generate audio buffers in background
        scope.launch {
            soundBuffers[SoundType.COUNTDOWN_TICK] = generateTone(freq = 520.0, durationMs = 80, envelope = EnvelopeType.PERCUSSIVE)
            soundBuffers[SoundType.COUNTDOWN_BLINK] = generateChord(freqs = doubleArrayOf(587.33, 880.0, 1174.66), durationMs = 240)
            soundBuffers[SoundType.SYMBOL_APPEAR] = generateArpeggio(freqs = doubleArrayOf(440.0, 659.25, 880.0), noteDurationMs = 45)
            soundBuffers[SoundType.CELL_TAP] = generateTone(freq = 660.0, durationMs = 45, envelope = EnvelopeType.SHORT_CLICK)
            soundBuffers[SoundType.CELL_DESELECT] = generateTone(freq = 380.0, durationMs = 45, envelope = EnvelopeType.SHORT_CLICK)
            soundBuffers[SoundType.CORRECT] = generateArpeggio(freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50), noteDurationMs = 50)
            soundBuffers[SoundType.PERFECT_ROUND] = generateChord(freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50), durationMs = 350)
            soundBuffers[SoundType.PERFECT_STREAK] = generateStreakFanfare()
            soundBuffers[SoundType.WRONG] = generateDissonance(freqs = doubleArrayOf(240.0, 226.0), durationMs = 250)
            soundBuffers[SoundType.GAME_OVER] = generateSlide(startFreq = 380.0, endFreq = 160.0, durationMs = 400)
        }
    }

    fun play(sound: SoundType, enabled: Boolean) {
        if (!enabled) return
        scope.launch {
            val buffer = soundBuffers[sound] ?: return@launch
            playPcm(buffer)
        }
    }

    private fun playPcm(buffer: ShortArray) {
        try {
            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()

            // Release after playing
            val durationMs = (buffer.size * 1000L) / sampleRate
            Thread.sleep(durationMs + 20)
            audioTrack.stop()
            audioTrack.release()
        } catch (_: Exception) {
            // Ignore audio device errors gracefully
        }
    }

    private enum class EnvelopeType {
        SMOOTH,
        PERCUSSIVE,
        SHORT_CLICK
    }

    private fun generateTone(freq: Double, durationMs: Int, envelope: EnvelopeType = EnvelopeType.SMOOTH): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val raw = sin(2.0 * PI * freq * t)
            val env = when (envelope) {
                EnvelopeType.SHORT_CLICK -> (1.0 - i.toDouble() / numSamples)
                EnvelopeType.PERCUSSIVE -> (1.0 - (i.toDouble() / numSamples)).let { it * it }
                EnvelopeType.SMOOTH -> {
                    val attack = (i.toDouble() / (numSamples * 0.1)).coerceIn(0.0, 1.0)
                    val decay = ((numSamples - i).toDouble() / (numSamples * 0.2)).coerceIn(0.0, 1.0)
                    minOf(attack, decay)
                }
            }
            buffer[i] = (raw * env * 18000).toInt().toShort()
        }
        return buffer
    }

    private fun generateChord(freqs: DoubleArray, durationMs: Int): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            var sum = 0.0
            for (f in freqs) {
                sum += sin(2.0 * PI * f * t)
            }
            sum /= freqs.size
            val attack = (i.toDouble() / (numSamples * 0.08)).coerceIn(0.0, 1.0)
            val decay = ((numSamples - i).toDouble() / (numSamples * 0.3)).coerceIn(0.0, 1.0)
            val env = minOf(attack, decay)
            buffer[i] = (sum * env * 22000).toInt().toShort()
        }
        return buffer
    }

    private fun generateArpeggio(freqs: DoubleArray, noteDurationMs: Int): ShortArray {
        val totalDurationMs = freqs.size * noteDurationMs
        val totalSamples = (sampleRate * (totalDurationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(totalSamples)
        val samplesPerNote = totalSamples / freqs.size

        for (n in freqs.indices) {
            val freq = freqs[n]
            for (i in 0 until samplesPerNote) {
                val globalIdx = n * samplesPerNote + i
                if (globalIdx >= totalSamples) break
                val t = i.toDouble() / sampleRate
                val raw = sin(2.0 * PI * freq * t)
                val decay = 1.0 - (i.toDouble() / samplesPerNote)
                buffer[globalIdx] = (raw * decay * 20000).toInt().toShort()
            }
        }
        return buffer
    }

    private fun generateStreakFanfare(): ShortArray {
        val freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50, 1318.51)
        return generateArpeggio(freqs, 60)
    }

    private fun generateDissonance(freqs: DoubleArray, durationMs: Int): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            var sum = 0.0
            for (f in freqs) {
                sum += sin(2.0 * PI * f * t)
            }
            sum /= freqs.size
            val decay = 1.0 - (i.toDouble() / numSamples)
            buffer[i] = (sum * decay * 22000).toInt().toShort()
        }
        return buffer
    }

    private fun generateSlide(startFreq: Double, endFreq: Double, durationMs: Int): ShortArray {
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val buffer = ShortArray(numSamples)
        var phase = 0.0
        for (i in 0 until numSamples) {
            val progress = i.toDouble() / numSamples
            val currentFreq = startFreq + (endFreq - startFreq) * progress
            phase += 2.0 * PI * currentFreq / sampleRate
            val decay = 1.0 - progress
            buffer[i] = (sin(phase) * decay * 20000).toInt().toShort()
        }
        return buffer
    }
}
