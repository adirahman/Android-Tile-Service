package com.arc.tilescreen

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import com.arc.tilescreen.ui.theme.TileScreenTheme
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.first

class MainActivity : ComponentActivity() {

    private val store by lazy{ QuickNoteStore(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val tvQuickNote = findViewById<TextView>(R.id.tvQuickNote)
        val btnEdit = findViewById<Button>(R.id.btnEditNote)
        val btnClear = findViewById<Button>(R.id.btnClearNote)
        val root = findViewById<View>(R.id.root)

        // 1) Listen note changes -> update UI realtime
        lifecycleScope.launch {
            store.noteFlow.collectLatest{ note ->
                tvQuickNote.text = if (note.isBlank()) "(No Note yet)" else note
            }
        }

        // 2) Edit -> buka ghost input cactivity dari dalam app juga
        btnEdit.setOnClickListener {
            startActivity(Intent(this, GhostInputActivity::class.java))
        }

        // 3) Clear -> hapus note
        btnClear.setOnClickListener {
            lifecycleScope.launch {
                store.clear()
                Snackbar.make(root, "Note cleared", Snackbar.LENGTH_SHORT).show()
            }
        }

        val card = findViewById<View>(R.id.cardQuickNote)
        card.setOnClickListener {
            lifecycleScope.launch {
                val note = store.noteFlow.first()
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle("Quick Note")
                    .setMessage(if (note.isBlank()) "(No Note yet)" else note)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

}
