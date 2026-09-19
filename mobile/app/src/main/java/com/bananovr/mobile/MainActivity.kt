package com.bananovr.mobile

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val title = TextView(this).apply {
            text = "🍌 BananoVR\n\nEtapa 1 — Base do aplicativo"
            textSize = 24f
            setPadding(48, 80, 48, 48)
        }

        setContentView(title)
    }
}
