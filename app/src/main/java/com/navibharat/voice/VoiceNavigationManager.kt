package com.navibharat.voice

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceNavigationManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var tts: TextToSpeech? = null
    private var ttsInitialized = false

    private val instructionQueue = mutableListOf<VoiceInstruction>()
    private var isPlaying = false
    private var lastInstruction: VoiceInstruction? = null

    private val _voiceState = MutableStateFlow<VoiceState>(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState

    private var currentLanguage = Locale("en", "IN")
    private var voiceSpeed = 0.9f
    private var isMuted = false

    init {
        initializeTextToSpeech()
    }

    private fun initializeTextToSpeech() {
        try {
            tts = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    setLanguage(currentLanguage)
                    ttsInitialized = true
                    Timber.i("TTS initialized successfully")
                    tts?.setOnUtteranceProgressListener(UtteranceListener())
                } else {
                    Timber.e("TTS initialization failed with status: $status")
                    ttsInitialized = false
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error initializing TTS")
        }
    }

    fun setLanguage(locale: Locale) {
        try {
            val result = tts?.setLanguage(locale)
            when (result) {
                TextToSpeech.LANG_AVAILABLE -> {
                    currentLanguage = locale
                    Timber.i("Language set to: $locale")
                }
                TextToSpeech.LANG_MISSING_DATA -> {
                    Timber.w("Language data missing for: $locale")
                }
                else -> {
                    Timber.w("Language not supported: $locale")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error setting language")
        }
    }

    fun speak(text: String, priority: VoicePriority = VoicePriority.NORMAL) {
        if (isMuted && priority != VoicePriority.ALERT) {
            Timber.d("Voice muted, skipping: $text")
            return
        }

        if (!ttsInitialized) {
            Timber.w("TTS not initialized")
            return
        }

        val instruction = VoiceInstruction(
            id = UUID.randomUUID().toString(),
            text = text,
            priority = priority
        )

        instructionQueue.add(instruction)
        playNextInstruction()
    }

    private fun playNextInstruction() {
        if (isPlaying || instructionQueue.isEmpty()) return
        if (!ttsInitialized) return

        val instruction = instructionQueue.removeAt(0)
        lastInstruction = instruction
        isPlaying = true

        try {
            // Request audio focus
            requestAudioFocus(instruction.priority)

            // Set speech rate and pitch
            tts?.setSpeechRate(voiceSpeed)
            tts?.setPitch(1.0f)

            // Speak
            val params = android.os.Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, instruction.id)
            }
            tts?.speak(instruction.text, TextToSpeech.QUEUE_FLUSH, params)

            _voiceState.value = VoiceState.SPEAKING

            Timber.i("Speaking: ${instruction.text}")
        } catch (e: Exception) {
            Timber.e(e, "Error speaking instruction")
            isPlaying = false
            playNextInstruction()
        }
    }

    private fun requestAudioFocus(priority: VoicePriority) {
        try {
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .setUsage(AudioAttributes.USAGE_ASSISTANT)
                .build()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                    .setAudioAttributes(audioAttributes)
                    .setAcceptsDelayedFocusGain(false)
                    .build()
                audioManager.requestAudioFocus(audioFocusRequest)
            } else {
                @Suppress("DEPRECATION")
                audioManager.requestAudioFocus(
                    null,
                    AudioManager.STREAM_MUSIC,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error requesting audio focus")
        }
    }

    fun mute() {
        isMuted = true
        stop()
        Timber.i("Navigation muted")
    }

    fun unmute() {
        isMuted = false
        Timber.i("Navigation unmuted")
    }

    fun stop() {
        try {
            tts?.stop()
            instructionQueue.clear()
            isPlaying = false
            _voiceState.value = VoiceState.IDLE
            Timber.i("Voice playback stopped")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping TTS")
        }
    }

    fun setSpeechRate(rate: Float) {
        voiceSpeed = rate.coerceIn(0.8f, 1.2f)
        Timber.i("Speech rate set to: $voiceSpeed")
    }

    fun repeatLastInstruction() {
        if (lastInstruction != null) {
            speak(lastInstruction!!.text, lastInstruction!!.priority)
            Timber.i("Repeating last instruction: ${lastInstruction!!.text}")
        } else {
            Timber.w("No previous instruction to repeat")
        }
    }

    fun setLanguage(languageCode: String) {
        val locale = when (languageCode.lowercase()) {
            "ta" -> Locale("ta", "IN")
            "hi" -> Locale("hi", "IN")
            "te" -> Locale("te", "IN")
            "kn" -> Locale("kn", "IN")
            "ml" -> Locale("ml", "IN")
            else -> Locale("en", "IN")
        }
        setLanguage(locale)
    }

    fun destroy() {
        try {
            tts?.stop()
            tts?.shutdown()
            ttsInitialized = false
            Timber.i("TTS destroyed")
        } catch (e: Exception) {
            Timber.e(e, "Error destroying TTS")
        }
    }

    private inner class UtteranceListener : UtteranceProgressListener() {
        override fun onStart(utteranceId: String?) {
            Timber.d("Utterance started: $utteranceId")
        }

        override fun onDone(utteranceId: String?) {
            Timber.d("Utterance done: $utteranceId")
            isPlaying = false
            _voiceState.value = VoiceState.IDLE
            playNextInstruction()
        }

        override fun onError(utteranceId: String?) {
            Timber.e("Utterance error: $utteranceId")
            isPlaying = false
            playNextInstruction()
        }
    }
}

data class VoiceInstruction(
    val id: String,
    val text: String,
    val priority: VoicePriority = VoicePriority.NORMAL
)

enum class VoicePriority {
    ALERT,     // Highest priority (accidents, hazards)
    NAVIGATION, // Navigation instructions
    NORMAL,     // General information
    LOW        // Background updates
}

enum class VoiceState {
    IDLE,
    SPEAKING,
    ERROR
}
