package com.example.blinkmatch.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticFeedbackHelper(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    enum class HapticType {
        LIGHT_TICK,
        SUCCESS,
        ERROR,
        STREAK
    }

    fun performHaptic(type: HapticType, enabled: Boolean) {
        if (!enabled || vibrator == null || !vibrator.hasVibrator()) return

        try {
            when (type) {
                HapticType.LIGHT_TICK -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                    } else {
                        vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                    }
                }
                HapticType.SUCCESS -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                    } else {
                        vibrator.vibrate(VibrationEffect.createOneShot(50, 180))
                    }
                }
                HapticType.ERROR -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
                    } else {
                        val pattern = longArrayOf(0, 70, 50, 90)
                        val amplitudes = intArrayOf(0, 200, 0, 255)
                        vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
                    }
                }
                HapticType.STREAK -> {
                    val pattern = longArrayOf(0, 40, 40, 60, 40, 80)
                    val amplitudes = intArrayOf(0, 160, 0, 200, 0, 255)
                    vibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, -1))
                }
            }
        } catch (_: Exception) {
            // Silently ignore device-specific vibrator issues
        }
    }
}
