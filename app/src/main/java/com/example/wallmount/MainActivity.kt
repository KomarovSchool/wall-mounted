package com.example.wallmount

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
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

    private val BASE_URL = "http://192.168.1.105:8888/index.html"

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
                val result = GeckoResult<Int>()
                result.complete(1)
                return result
            }
        }

        val geckoView = GeckoView(this)

        geckoSession.open(geckoRuntime)
        geckoView.setSession(geckoSession)

        val geckoViewContainer = findViewById<View>(R.id.geckoViewContainer)
        (geckoViewContainer as? android.widget.FrameLayout)?.addView(geckoView)

        val savedDeviceId = sharedPreferences.getString("device_id", "default_id")
        loadDevicePage(savedDeviceId)

        setupHoverBehavior()
        setupButtons()
    }

    private fun loadDevicePage(deviceId: String?) {
        if (deviceId.isNullOrEmpty()) return
        val url = BASE_URL
        geckoSession.loadUri(url)
    }

    private fun refreshPage() {
        geckoSession.reload()
    }

    private fun setupHoverBehavior() {
        val hoverArea = findViewById<View>(R.id.hoverArea)
        val btnPanel = findViewById<View>(R.id.btnPanel) // Panel containing buttons

        hoverArea.setOnHoverListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_HOVER_ENTER -> {
                    btnPanel.visibility = View.VISIBLE
                }
                MotionEvent.ACTION_HOVER_EXIT -> {
                    btnPanel.postDelayed({
                        btnPanel.visibility = View.GONE
                    }, 1000)
                }
            }
            false
        }

        hoverArea.setOnClickListener {
            btnPanel.visibility = View.VISIBLE
            btnPanel.postDelayed({
                btnPanel.visibility = View.GONE
            }, 3000)
        }
    }

    private fun setupButtons() {
        val btnSettings = findViewById<View>(R.id.btnSettings)
        val btnRefresh = findViewById<View>(R.id.btnRefresh)

        btnSettings.setOnClickListener {
            showSettingsDialog()
        }

        btnRefresh.setOnClickListener {
            refreshPage()
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
