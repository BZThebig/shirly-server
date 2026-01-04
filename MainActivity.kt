package com.example.shirly

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.shirly.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Locale
import java.util.UUID

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var binding: ActivityMainBinding
    private var textToSpeech: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private val sessionId: String = UUID.randomUUID().toString()

    private val speechRecognizerIntent by lazy {
        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale("he", "IL"))
            putExtra(RecognizerIntent.EXTRA_PROMPT, "דבר עם שירלי...")
        }
    }

    private val audioPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (!isGranted) {
                Toast.makeText(this, "צריך הרשאה למיקרופון כדי להשתמש בשירלי", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textToSpeech = TextToSpeech(this, this)
        setupSpeechRecognizer()
        checkAudioPermission()

        binding.btnMic.setOnClickListener {
            startListening()
        }
    }

    private fun checkAudioPermission() {
        val permission = Manifest.permission.RECORD_AUDIO
        if (ContextCompat.checkSelfPermission(this, permission)
            != PackageManager.PERMISSION_GRANTED
        ) {
            audioPermissionLauncher.launch(permission)
        }
    }

    private fun setupSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "זיהוי דיבור לא זמין במכשיר", Toast.LENGTH_LONG).show()
            return
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer?.setRecognitionListener(object : android.speech.RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                binding.tvStatus.text = "מקשיבה לך..."
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                binding.tvStatus.text = "מעבדת את מה שאמרת..."
            }

            override fun onError(error: Int) {
                binding.tvStatus.text = "שגיאה בזיהוי הדיבור"
            }

            override fun onResults(results: Bundle?) {
                val texts =
                    results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = texts?.firstOrNull() ?: return
                binding.tvUserText.text = "אתה: $spokenText"

                sendToServer(spokenText)
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        if (speechRecognizer == null) {
            setupSpeechRecognizer()
        }
        speechRecognizer?.startListening(speechRecognizerIntent)
    }

    private fun sendToServer(userText: String) {
        binding.tvStatus.text = "שולחת לשרת של שירלי..."

        val request = ShirlyChatRequest(
            sessionId = sessionId,
            message = userText,
            language = "he"
        )

        ShirlyApiClient.api.chat(request).enqueue(object : Callback<ShirlyChatResponse> {
            override fun onResponse(
                call: Call<ShirlyChatResponse>,
                response: Response<ShirlyChatResponse>
            ) {
                if (!response.isSuccessful) {
                    binding.tvStatus.text = "שגיאה בתשובה מהשרת"
                    speak("הייתה בעיה לתקשר עם המוח שלי.")
                    return
                }

                val body = response.body()
                if (body == null || !body.success || body.reply == null) {
                    binding.tvStatus.text = "תשובה לא תקינה מהשרת"
                    speak("קיבלתי תשובה לא תקינה מהשרת.")
                    return
                }

                val reply = body.reply
                binding.tvShirlyReply.text = "שירלי: ${reply.replyText}"
                binding.tvStatus.text = "מוכנה לפקודה הבאה."
                speak(reply.replyText)

                handleActions(reply.actions)
            }

            override fun onFailure(call: Call<ShirlyChatResponse>, t: Throwable) {
                binding.tvStatus.text = "שגיאת רשת"
                speak("לא הצלחתי להתחבר לשרת. בדוק חיבור אינטרנט.")
            }
        })
    }

    private fun handleActions(actions: List<ShirlyAction>) {
        // כאן בעתיד אפשר לממש פתיחת אפליקציות, קישורים וכו'
        // למשל, אם type == "OPEN_URL" -> לפתוח דפדפן
        // אם type == "OPEN_APP" -> לנסות לפתוח אפליקציה (עם package name מתאים)
    }

    private fun speak(text: String) {
        textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "SHIRLY_TTS")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech?.setLanguage(Locale("he", "IL"))
            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Toast.makeText(this, "עברית לא נתמכת בקריינות במכשיר", Toast.LENGTH_LONG).show()
            } else {
                speak("שלום, אני שירלי.")
            }
        } else {
            Toast.makeText(this, "שגיאה בהפעלת מנוע הדיבור", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        textToSpeech?.stop()
        textToSpeech?.shutdown()
        speechRecognizer?.destroy()
    }
}