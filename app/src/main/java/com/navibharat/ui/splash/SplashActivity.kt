package com.navibharat.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.navibharat.ui.auth.AuthActivity
import com.navibharat.ui.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Timber.i("SplashActivity created")

        scope.launch {
            // Delay for splash screen visibility (typically 2 seconds)
            delay(2000)

            // Check if user is authenticated
            val isAuthenticated = checkAuthentication()

            val intent = if (isAuthenticated) {
                Intent(this@SplashActivity, MainActivity::class.java)
            } else {
                Intent(this@SplashActivity, AuthActivity::class.java)
            }

            startActivity(intent)
            finish()
        }
    }

    private fun checkAuthentication(): Boolean {
        // In production, check SharedPreferences or Supabase Auth state
        return false // Default to auth screen
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
