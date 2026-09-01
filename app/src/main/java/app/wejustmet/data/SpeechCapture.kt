package app.wejustmet.data

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Platform SpeechRecognizer wrapper (PLAN hard cut 2: dumb ears, smart brain, human net).
 * Continuous: each recognizer session ends at a pause, so results are accumulated and
 * listening restarts until the user taps stop or a sustained silence follows real speech.
 */
class SpeechCapture(
    private val context: Context,
    private val onTranscript: (String) -> Unit,
    private val onHearing: () -> Unit,
    private val onFinished: (String) -> Unit,
) : RecognitionListener {
    private var recognizer: SpeechRecognizer? = null
    private val finalized = StringBuilder()
    private var partial = ""
    private var finished = false
    private var stopRequested = false
    private var idleSessions = 0
    private var blankRestarts = 0

    val transcript: String
        get() = "$finalized $partial".trim()

    fun start() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            finish()
            return
        }
        recognizer?.destroy()
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).also {
            it.setRecognitionListener(this)
            it.startListening(listenIntent())
        }
    }

    /** Tap-to-stop: asks the recognizer to wrap up; results arrive via onResults. */
    fun stop() {
        stopRequested = true
        recognizer?.stopListening()
    }

    fun destroy() {
        finished = true
        recognizer?.destroy()
        recognizer = null
    }

    private fun listenIntent() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, SILENCE_MS)
        putExtra(
            RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
            SILENCE_MS,
        )
    }

    override fun onPartialResults(bundle: Bundle?) {
        val text = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull().orEmpty()
        if (text.isNotBlank()) {
            partial = text
            onHearing()
            onTranscript(transcript)
        }
    }

    override fun onResults(bundle: Bundle?) {
        val text = bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull().orEmpty()
        if (text.isNotBlank()) {
            if (finalized.isNotEmpty()) finalized.append(' ')
            finalized.append(text)
            partial = ""
            idleSessions = 0
        } else {
            idleSessions += 1
        }
        onTranscript(transcript)
        continueOrFinish()
    }

    override fun onError(error: Int) {
        if (finished) return
        if (transcript.isBlank()) {
            // Nothing said yet (spurious NO_MATCH/timeouts): keep the mic alive.
            blankRestarts += 1
            if (stopRequested || blankRestarts >= MAX_BLANK_RESTARTS) finish() else start()
        } else {
            idleSessions += 1
            continueOrFinish()
        }
    }

    /** Auto-stop only after the user tapped, or after sustained silence following speech. */
    private fun continueOrFinish() {
        when {
            finished -> Unit
            stopRequested -> finish()
            transcript.isNotBlank() && idleSessions >= MAX_IDLE_SESSIONS -> finish()
            else -> start()
        }
    }

    override fun onRmsChanged(rmsdB: Float) {
        if (rmsdB > HEARING_RMS_DB) onHearing()
    }

    override fun onBeginningOfSpeech() = onHearing()
    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onEvent(eventType: Int, params: Bundle?) {}

    private fun finish() {
        if (finished) return
        finished = true
        onFinished(transcript)
    }

    private companion object {
        const val SILENCE_MS = 4000
        const val HEARING_RMS_DB = 4f
        /** Empty sessions (~SILENCE_MS each) after real speech before auto-finishing. */
        const val MAX_IDLE_SESSIONS = 2
        /** Sessions with zero speech before giving up entirely. */
        const val MAX_BLANK_RESTARTS = 60
    }
}
