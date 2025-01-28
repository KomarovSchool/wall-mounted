package com.example.wallmount

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.mozilla.geckoview.GeckoResult
import org.mozilla.geckoview.GeckoSession
import org.mozilla.geckoview.GeckoRuntime
import org.mozilla.geckoview.GeckoSessionSettings
import org.mozilla.geckoview.GeckoView

class MainActivity : AppCompatActivity() {

    private lateinit var geckoSession: GeckoSession
    private lateinit var geckoRuntime: GeckoRuntime
    private lateinit var sharedPreferences: SharedPreferences

    private val BASE_URL = "http://192.168.1.105:8888/index.html"  // change this to your real base URL

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // GeckoView setup
        geckoRuntime = GeckoRuntime.create(this)
        geckoSession = GeckoSession()
        
        geckoSession.permissionDelegate = object : GeckoSession.PermissionDelegate {
            override fun onContentPermissionRequest(
                session: GeckoSession,
                permission: GeckoSession.PermissionDelegate.ContentPermission,
            ): GeckoResult<Int> {
                val result = GeckoResult<Int>();
                result.complete(1);
                return result;
            }
        }

        val geckoView = GeckoView(this)

        geckoSession.open(geckoRuntime)
        geckoView.setSession(geckoSession)

        // Put the GeckoView inside our layout
        val geckoViewContainer = findViewById<View>(R.id.geckoViewContainer)
        (geckoViewContainer as? android.widget.FrameLayout)?.addView(geckoView)

        val savedDeviceId = sharedPreferences.getString("device_id", "default_id")
        loadDevicePage(savedDeviceId)

        setupHoverBehavior()
        setupSettingsButton()
    }

    private fun loadDevicePage(deviceId: String?) {
        if (deviceId.isNullOrEmpty()) return
        val url = BASE_URL; // + "/$deviceId"
        geckoSession.loadUri(url)
    }

    private fun setupHoverBehavior() {
        val hoverArea = findViewById<View>(R.id.hoverArea)
        val btnSettings = findViewById<View>(R.id.btnSettings)

        // 1) MOUSE HOVER
        hoverArea.setOnHoverListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_HOVER_ENTER -> {
                    // Show the button
                    btnSettings.visibility = View.VISIBLE
                }
                MotionEvent.ACTION_HOVER_EXIT -> {
                    // Optionally hide after 1 second
                    btnSettings.postDelayed({
                        btnSettings.visibility = View.GONE
                    }, 1000)
                }
            }
            // Return false so the event can continue to other listeners if needed
            false
        }

        // 2) TAP/CLICK
        hoverArea.setOnClickListener {
            // Show the settings button on tap
            btnSettings.visibility = View.VISIBLE

            // Optionally auto-hide after 3 seconds if you want:
            btnSettings.postDelayed({
                btnSettings.visibility = View.GONE
            }, 3000)
        }
    }


    private fun setupSettingsButton() {
        val btnSettings = findViewById<View>(R.id.btnSettings)
        btnSettings.setOnClickListener {
            showSettingsDialog()
        }
    }

    private fun showSettingsDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)
        val etDeviceId = dialogView.findViewById<android.widget.EditText>(R.id.etDeviceId)

        val currentId = sharedPreferences.getString("device_id", "default_id")
        etDeviceId.setText(currentId)

        AlertDialog.Builder(this)
            .setTitle("Settings")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val newDeviceId = etDeviceId.text.toString()
                saveDeviceId(newDeviceId)
                loadDevicePage(newDeviceId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun saveDeviceId(deviceId: String) {
        sharedPreferences.edit().putString("device_id", deviceId).apply()
    }
}
