package com.arc.tilescreen

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import android.content.Intent

class QuickStatusTileService: TileService() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val store by lazy { QuickStatusStore(applicationContext) }

    private val note by lazy { QuickNoteStore(applicationContext) }

    override fun onStartListening() {
        super.onStartListening()
        //Dipanggil saat panel QS dibuka cocok untuk sync state tile
        scope.launch {
            render(store.statusFlow.first())
        }
    }

    override fun onClick() {
        super.onClick()

        val intent = Intent(this, GhostInputActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        if (isLocked) {
            unlockAndRun { startActivityAndCollapse(intent) }
        } else {
            startActivityAndCollapse(intent)
        }

        /*val action = { showPickerDialog() }

        //showDialog() tidak akan terlihat kalau lock screen sedang tampil.
        // jadi kalau terkunci, minta unlock dulu lalu jalankan aksinya
        if(isLocked){
            unlockAndRun{action()}
        }else{
            action()
        }*/
    }

    private fun showPickerDialog(){
        val intent = Intent(this, PickerActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        // Collapse QS panel, lalu tampilkan activity dialog.
        startActivityAndCollapse(intent)
    }


    private fun render(status: QuickStatus){
        val tile = qsTile ?: return

        tile.label = status.label
        //tile.contentDescription = "Status: {${status.label}}"

        // Contoh: ACTIVATE hanya saat Available (kamu bisa mengubah sesuai kebutuhan)
        //tile.state = if(status == QuickStatus.AVAILABLE) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE

        //tile.updateTile()

        tile.label = "Label"
        tile.contentDescription = "Deskripsi"
        tile.state = Tile.STATE_ACTIVE

        tile.updateTile()
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
