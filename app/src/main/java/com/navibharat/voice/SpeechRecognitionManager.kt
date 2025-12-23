package com.navibharat.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.util.Locale

/**
 * Manages Android built-in speech recognition (fallback)
 */
class SpeechRecognitionManager(private val context: Context) {

    private val speechRecognizer: SpeechRecognizer by lazy {
        SpeechRecognizer.createSpeechRecognizer(context)
    }

    private val _recognitionState = MutableStateFlow<RecognitionState>(RecognitionState.Idle)
    val recognitionState: StateFlow<RecognitionState> = _recognitionState

    private val _partialResults = MutableStateFlow<String>("")
    val partialResults: StateFlow<String> = _partialResults

    private val _finalResults = MutableStateFlow<String>("")
    val finalResults: StateFlow<String> = _finalResults

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        setupRecognitionListener()
    }

    private fun setupRecognitionListener() {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _recognitionState.value = RecognitionState.Listening
                _partialResults.value = ""
                _error.value = null
                Timber.d("Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                _recognitionState.value = RecognitionState.Speaking
                Timber.d("User started speaking")
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Audio level change - can be used for visual feedback
            }

            override fun onBufferReceived(buffer: ByteArray?) {
                // Buffer received
            }

            override fun onEndOfSpeech() {
                _recognitionState.value = RecognitionState.Processing
                Timber.d("User finished speaking")
            }

            override fun onError(error: Int) {
                val errorMessage = when (error) {
                    SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                    SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                    SpeechRecognizer.ERROR_NETWORK -> "Network error"
                    SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                    SpeechRecognizer.ERROR_NO_MATCH -> "No match found"
                    SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                    SpeechRecognizer.ERROR_SERVER -> "Server error"
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input detected"
                    else -> "Unknown error"
                }
                _error.value = errorMessage
                _recognitionState.value = RecognitionState.Error
                Timber.e("Speech recognition error: $errorMessage (code: $error)")
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    _finalResults.value = matches[0]
                    _recognitionState.value = RecognitionState.Complete
                    Timber.d("Recognition result: ${matches[0]}")
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!partial.isNullOrEmpty()) {
                    _partialResults.value = partial[0]
                    Timber.d("Partial result: ${partial[0]}")
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {
                // Event received
            }
        })
    }

    /**
     * Start listening for speech in specified language
     */
    fun startListening(language: String = "en-IN") {
        try {
            if (_recognitionState.value != RecognitionState.Idle) {
                return
            }

            val intent = Intent(SpeechRecognizer.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(SpeechRecognizer.EXTRA_LANGUAGE_MODEL, SpeechRecognizer.LANGUAGE_MODEL_FREE_FORM)
                putExtra(SpeechRecognizer.EXTRA_LANGUAGE, language)
                putExtra(SpeechRecognizer.EXTRA_MAX_RESULTS, 3)
                putExtra(SpeechRecognizer.EXTRA_PARTIAL_RESULTS, true)
                putExtra(SpeechRecognizer.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000)
                putExtra(SpeechRecognizer.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
            }

            speechRecognizer.startListening(intent)
            Timber.d("Started listening in language: $language")
        } catch (e: Exception) {
            _error.value = e.message
            _recognitionState.value = RecognitionState.Error
            Timber.e(e, "Error starting speech recognition")
        }
    }

    /**
     * Stop listening
     */
    fun stopListening() {
        try {
            speechRecognizer.stopListening()
            Timber.d("Stopped listening")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping speech recognition")
        }
    }

    /**
     * Cancel recognition
     */
    fun cancel() {
        try {
            speechRecognizer.cancel()
            _recognitionState.value = RecognitionState.Idle
            _error.value = null
            Timber.d("Cancelled speech recognition")
        } catch (e: Exception) {
            Timber.e(e, "Error cancelling speech recognition")
        }
    }

    /**
     * Check if speech recognizer is available
     */
    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    /**
     * Release resources
     */
    fun destroy() {
        try {
            speechRecognizer.destroy()
        } catch (e: Exception) {
            Timber.e(e, "Error destroying speech recognizer")
        }
    }

    enum class RecognitionState {
        Idle,
        Listening,
        Speaking,
        Processing,
        Complete,
        Error
    }
}
