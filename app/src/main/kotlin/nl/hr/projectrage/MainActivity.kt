package nl.hr.projectrage

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    companion object {
        const val requestAudio = 1
    }

    private val sharedPreferences by lazy { getSharedPreferences("App", 0) }
    private val speechRecognizer by lazy { SpeechRecognizer.createSpeechRecognizer(this) }
    private val speechRecognizerIntent by lazy {
        CodeWordListener(sharedPreferences) { matches ->
            root.setBackgroundResource(if (matches) android.R.color.holo_green_light else android.R.color.holo_red_light)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        speechRecognizer.setRecognitionListener(speechRecognizerIntent)

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), requestAudio)
        } else startAudioRecording()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == requestAudio) {
            if (resultCode != PackageManager.PERMISSION_GRANTED)
                AlertDialog.Builder(this)
                    .setTitle("No.")
                    .setMessage("Permissions Required.")
                    .setCancelable(false)
                    .setPositiveButton("OK") { _, _ -> TODO() }
                    .show()

            startAudioRecording()
        }
    }

    private fun startAudioRecording() {
        val speachIntent = RecognizerIntent.getVoiceDetailsIntent(this)
        //set speech language to something else than Local.default()
        speachIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "nl")
        speechRecognizer.startListening(speachIntent)
    }


    override fun onStop() {
        super.onStop()
        speechRecognizer.destroy()
    }
}

class CodeWordListener(val sharedPreferences: SharedPreferences, val onResult: (match: Boolean) -> Unit) : RecognitionListener {
    override fun onReadyForSpeech(p0: Bundle?) {}
    override fun onRmsChanged(p0: Float) {}
    override fun onBufferReceived(p0: ByteArray?) {}
    override fun onPartialResults(p0: Bundle?) {}
    override fun onEvent(p0: Int, p1: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onEndOfSpeech() {}
    override fun onError(p0: Int) {}
    override fun onResults(p0: Bundle?) {
        val results = (p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: arrayListOf()) as ArrayList<String?>
        val codeword = sharedPreferences.getString("codeword", "kiwi")!!.toLowerCase()

        onResult(results.any { it?.toLowerCase()?.contains(codeword) ?: false })
    }
}