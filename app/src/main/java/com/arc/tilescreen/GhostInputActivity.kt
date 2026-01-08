package com.arc.tilescreen

import android.content.ComponentName
import android.os.Bundle
import android.service.quicksettings.TileService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.activity.ComponentActivity
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class GhostInputActivity : ComponentActivity() {

    private val store by lazy { QuickNoteStore(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ghost_input)

        val dim = findViewById<View>(R.id.dim)
        val sheet = findViewById<View>(R.id.sheet)
        val et = findViewById<TextInputEditText>(R.id.etNote)

        //Tap dim-> close
        dim.setOnClickListener { finish() }

        // Sheet anim(opsional)
        sheet.translationY = 300f
        sheet.alpha = 0f
        sheet.animate().translationY(0f).alpha(1f).setDuration(160).start()

        //Load existing note + focus input
        lifecycleScope.launch {
            val existing = store.noteFlow.first()
            et.setText(existing)
            et.setSelection(et.text?.length ?: 0)

            //show keyboard setelah layout siap
            et.requestFocus()
            window.decorView.doOnPreDraw {
                showKeyboard(et)
            }
        }

        findViewById<View>(R.id.btnCancel).setOnClickListener { finish() }

        findViewById<View>(R.id.btnSave).setOnClickListener {
            val text = et.text?.toString().orEmpty().trim()
            saveAndClose(text)
        }

        findViewById<View>(R.id.btnClear).setOnClickListener {
            lifecycleScope.launch {
                store.clear()
                requestTitleRefresh()
                finish()
            }
        }

        et.setOnEditorActionListener { _, actionId, _ ->
            if(actionId == EditorInfo.IME_ACTION_DONE){
                val text = et.text?.toString().orEmpty().trim()
                saveAndClose(text)
                true
            } else false
        }
    }

    private fun saveAndClose(text:String){
        lifecycleScope.launch {
            store.setNote(text)
            requestTitleRefresh()
            finish()
        }
    }

    private fun requestTitleRefresh(){
        // PENTING: ganti componentName ke TileService yang benar
        TileService.requestListeningState(
            this@GhostInputActivity,
            ComponentName(this@GhostInputActivity, QuickStatusTileService::class.java)
        )
    }

    private fun showKeyboard(target:View){
        val imm = getSystemService(InputMethodManager::class.java)
        imm.showSoftInput(target, InputMethodManager.SHOW_IMPLICIT)
    }


    override fun finish() {
        // anim keluar (sheet slide down)
        val sheet = findViewById<View>(R.id.sheet)
        sheet.animate()
            .translationY(300f)
            .alpha(0f)
            .setDuration(140)
            .withEndAction {
                super.finish()
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            }
            .start()
    }
}
