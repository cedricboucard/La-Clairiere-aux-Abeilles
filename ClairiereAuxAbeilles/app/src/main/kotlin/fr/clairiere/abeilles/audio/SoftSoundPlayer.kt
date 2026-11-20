package fr.clairiere.abeilles.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

object SoftSoundPlayer {
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
  @Volatile
  var enabled: Boolean = true

  private fun playTone(freq: Double, durationMs: Int = 180, volume: Double = 0.14, delayMs: Long = 0L) {
    if (!enabled) return
    scope.launch {
      if (delayMs > 0) delay(delayMs)
      if (!enabled) return@launch
      try {
        val sampleRate = 22050
        val count = (sampleRate * (durationMs / 1000.0)).toInt().coerceAtLeast(1)
        val samples = ShortArray(count)
        for (i in 0 until count) {
          val t = i.toDouble() / sampleRate
          val progress = i.toDouble() / count
          val attack = (i / (sampleRate * 0.015)).coerceIn(0.0, 1.0)
          val decay = (1.0 - progress) * (1.0 - progress)
          val wave = sin(2.0 * PI * freq * t)
          samples[i] = (wave * attack * decay * volume * Short.MAX_VALUE).toInt().toShort()
        }
        val track = AudioTrack.Builder()
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
          .setBufferSizeInBytes(samples.size * 2)
          .setTransferMode(AudioTrack.MODE_STATIC)
          .build()
        track.write(samples, 0, samples.size)
        track.play()
        delay(durationMs.toLong() + 40L)
        track.stop()
        track.release()
      } catch (_: Throwable) {
        // Ignore audio errors on emulators without audio sink
      }
    }
  }

  fun pick() {
    playTone(523.25, 160, 0.14, 0)
    playTone(659.25, 200, 0.12, 70)
  }

  fun plant() {
    playTone(392.00, 150, 0.12, 0)
    playTone(493.88, 190, 0.12, 60)
  }

  fun bloom() {
    playTone(587.33, 220, 0.10, 0)
    playTone(783.99, 300, 0.09, 80)
  }

  fun discover() {
    playTone(440.00, 200, 0.13, 0)
    playTone(554.37, 220, 0.13, 110)
    playTone(659.25, 360, 0.14, 220)
  }

  fun repair() {
    playTone(293.66, 120, 0.10, 0)
    playTone(349.23, 140, 0.10, 130)
    playTone(523.25, 300, 0.14, 260)
  }

  fun harvest() {
    playTone(493.88, 150, 0.12, 0)
    playTone(659.25, 180, 0.12, 80)
    playTone(783.99, 260, 0.14, 160)
  }
}
