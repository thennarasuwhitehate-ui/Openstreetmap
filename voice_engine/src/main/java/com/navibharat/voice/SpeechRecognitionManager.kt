package com.navibharat.voice

import android.app.Activity
import android.content.Intent
import android.content.ReceiverCallNotInProgressException
import android.os.Build
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber

class SpeechRecognitionManager(
    private val activity: Activity
) {

    private var speechRecognizer: SpeechRecognizer? = null
    private val _recognitionState = MutableStateFlow<RecognitionState>(RecognitionState.IDLE)
    val recognitionState: StateFlow<RecognitionState> = _recognitionState

    private val _recognizedText = MutableStateFlow<String>("")
    val recognizedText: StateFlow<String> = _recognizedText

    private val _confidence = MutableStateFlow<Float>(0f)
    val confidence: StateFlow<Float> = _confidence

    private var language = "en-IN"

    init {
        initializeRecognizer()
    }

    private fun initializeRecognizer() {
        try {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(activity)
            speechRecognizer?.setRecognitionListener(RecognitionListenerImpl())
            Timber.i("SpeechRecognizer initialized")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize SpeechRecognizer")
        }
    }

    fun startListening(language: String = "en-IN") {
        this.language = language

        if (speechRecognizer == null) {
            initializeRecognizer()
        }

        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, language)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 2000)
            }

            _recognitionState.value = RecognitionState.LISTENING
            _recognizedText.value = ""
            _confidence.value = 0f

            speechRecognizer?.startListening(intent)
            Timber.i("Listening started for language: $language")
        } catch (e: Exception) {
            Timber.e(e, "Error starting listening")
            _recognitionState.value = RecognitionState.ERROR
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _recognitionState.value = RecognitionState.PROCESSING
            Timber.i("Listening stopped")
        } catch (e: ReceiverCallNotInProgressException) {
            Timber.d("Speech recognizer not active")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping listening")
        }
    }

    fun cancel() {
        try {
            speechRecognizer?.cancel()
            _recognitionState.value = RecognitionState.IDLE
            Timber.i("Recognition cancelled")
        } catch (e: Exception) {
            Timber.e(e, "Error cancelling recognition")
        }
    }

    fun destroy() {
        try {
            speechRecognizer?.destroy()
            speechRecognizer = null
            Timber.i("SpeechRecognizer destroyed")
        } catch (e: Exception) {
            Timber.e(e, "Error destroying SpeechRecognizer")
        }
    }

    private inner class RecognitionListenerImpl : RecognitionListener {
        override fun onReadyForSpeech(params: android.os.Bundle?) {
            Timber.d("Ready for speech")
        }

        override fun onBeginningOfSpeech() {
            Timber.d("Beginning of speech detected")
            _recognitionState.value = RecognitionState.LISTENING
        }

        override fun onRmsChanged(rmsdB: Float) {
            // Audio level changed
        }

        override fun onBufferReceived(buffer: ByteArray?) {
            // Audio buffer received
        }

        override fun onEndOfSpeech() {
            Timber.d("End of speech detected")
            _recognitionState.value = RecognitionState.PROCESSING
        }

        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                SpeechRecognizer.ERROR_CLIENT -> "Client side error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No recognition match"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                else -> "Unknown error"
            }
            Timber.e("Speech recognition error: $errorMessage")
            _recognitionState.value = RecognitionState.ERROR
        }

        override fun onResults(results: android.os.Bundle?) {
            Timber.d("Results received")
            if (results != null) {
                val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val confidences = results.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)

                if (!matches.isNullOrEmpty()) {
                    _recognizedText.value = matches[0]
                    _confidence.value = confidences?.getOrNull(0) ?: 0f
                    _recognitionState.value = RecognitionState.COMPLETED

                    Timber.i("Recognized: '${matches[0]}' with confidence: ${_confidence.value}")
                }
            }
        }

        override fun onPartialResults(partialResults: android.os.Bundle?) {
            if (partialResults != null) {
                val partial = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!partial.isNullOrEmpty()) {
                    _recognizedText.value = partial[0]
                    Timber.d("Partial result: '${partial[0]}'")
                }
            }
        }

        override fun onEvent(eventType: Int, params: android.os.Bundle?) {
            Timber.d("Speech event: $eventType")
        }
    }
}

enum class RecognitionState {
    IDLE,
    LISTENING,
    PROCESSING,
    COMPLETED,
    ERROR
}
