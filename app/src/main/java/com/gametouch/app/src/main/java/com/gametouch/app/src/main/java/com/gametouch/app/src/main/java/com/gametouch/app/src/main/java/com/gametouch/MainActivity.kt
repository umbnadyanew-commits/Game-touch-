package com.gametouch

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var view: BlackHoleView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        supportActionBar?.hide()

        view = BlackHoleView(this)
        setContentView(view)
    }

    override fun onResume() {
        super.onResume()
        view.resume()
    }

    override fun onPause() {
        super.onPause()
        view.pause()
    }
}
