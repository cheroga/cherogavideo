package com.moisnostudio.jerutiapp

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        MediaPlayer.create(this, R.raw.tadum).start()

        val logo = findViewById<View>(R.id.logoContainer)

        // Animación estilo Netflix (Zoom suave + Aparición)
        logo.alpha = 0f
        logo.scaleX = 0.5f
        logo.scaleY = 0.5f

        logo.animate()
            .alpha(1f)
            .scaleX(1.1f)
            .scaleY(1.1f)
            .setDuration(1500)
            .withEndAction {
                // Ir a la pantalla principal
                startActivity(Intent(this, MainActivity::class.java))
                finish() // Cerramos el splash para que no pueda volver con "atrás"
            }
            .start()
    }
}