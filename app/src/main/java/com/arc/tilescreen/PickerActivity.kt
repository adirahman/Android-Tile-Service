package com.arc.tilescreen

import android.content.ComponentName
import android.os.Bundle
import android.service.quicksettings.TileService
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class PickerActivity : ComponentActivity() {

    private val store by lazy { QuickStatusStore(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val statuses = QuickStatus.values()
        val labels = statuses.map { it.label }.toTypedArray()

        val dialog = MaterialAlertDialogBuilder(this)
            .setTitle("Set Status")
            .setItems(labels) { d, which ->
                val chosen = statuses[which]
                lifecycleScope.launch {
                    store.setStatus(chosen)
                    TileService.requestListeningState(
                        this@PickerActivity,
                        ComponentName(this@PickerActivity, QuickStatusTileService::class.java)
                    )
                    finish()
                }
                d.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { d, _ ->
                d.dismiss()
            }
            .setOnDismissListener { finish() }
            .create()

        dialog.show()
    }
}
