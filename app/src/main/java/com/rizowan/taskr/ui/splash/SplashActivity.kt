package com.rizowan.taskr.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.rizowan.taskr.databinding.ActivitySplashBinding
import com.rizowan.taskr.ui.MainActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate Logo
        binding.ivLogo.apply {
            alpha = 0f
            scaleX = 0.8f
            scaleY = 0.8f
            animate()
                .alpha(1f)
                .scaleX(1.1f) // Slight over-scale for bounce effect
                .scaleY(1.1f)
                .setDuration(800)
                .setInterpolator(android.view.animation.OvershootInterpolator())
                .withEndAction {
                     // Settle back to normal size
                     animate()
                         .scaleX(1f)
                         .scaleY(1f)
                         .setDuration(200)
                         .start()
                }
                .start()
        }

        // Animate App Name
        binding.tvAppName.apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(300)
                .setDuration(600)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }

        // Animate Tagline
        binding.tvTagline.apply {
            alpha = 0f
            translationY = 50f
            animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(500) // Slightly later than app name
                .setDuration(600)
                .setInterpolator(android.view.animation.DecelerateInterpolator())
                .start()
        }

        // Navigate to Main Activity after delay
        lifecycleScope.launch {
            delay(4000) // 4 seconds delay as requested
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            // Apply smooth fade transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            finish()
        }
    }
}
