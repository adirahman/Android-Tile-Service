package com.arc.tilescreen

import android.content.ComponentName
import android.os.Bundle
import android.service.quicksettings.TileService
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.TextFieldValue

import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.arc.tilescreen.ui.theme.TileScreenTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay

class GhostInputActivity : ComponentActivity() {

    private val store by lazy { QuickNoteStore(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TileScreenTheme {
                GhostInputScreen(
                    loadNote = { store.noteFlow.first() },
                    onSave = { text -> saveAndClose(text) },
                    onClear = { clearAndClose() },
                    onCancel = { finish() }
                )
            }
        }
    }

    private fun saveAndClose(text:String){
        lifecycleScope.launch {
            store.setNote(text)
            requestTitleRefresh()
            finish()
        }
    }

    private fun clearAndClose() {
        lifecycleScope.launch {
            store.clear()
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

    override fun finish() {
        super.finish()
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }
}

@Composable
private fun GhostInputScreen(
    loadNote: suspend () -> String,
    onSave: (String) -> Unit,
    onClear: () -> Unit,
    onCancel: () -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }
    val scope = rememberCoroutineScope()
    var textState by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(""))
    }
    var visible by rememberSaveable { mutableStateOf(false) }
    val dimAlpha by animateFloatAsState(
        targetValue = if (visible) 0.5f else 0f,
        animationSpec = tween(durationMillis = 160),
        label = "dim-alpha"
    )

    LaunchedEffect(Unit) {
        val existing = loadNote()
        textState = TextFieldValue(existing, selection = TextRange(existing.length))
        focusRequester.requestFocus()
        keyboardController?.show()
        visible = true
    }

    fun dismissThen(action: () -> Unit) {
        if (!visible) return
        visible = false
        scope.launch {
            delay(160)
            action()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = dimAlpha))
            .clickable { dismissThen(onCancel) }
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it / 3 },
                animationSpec = tween(durationMillis = 160)
            ) + fadeIn(animationSpec = tween(durationMillis = 160)),
            exit = slideOutVertically(
                targetOffsetY = { it / 3 },
                animationSpec = tween(durationMillis = 140)
            ) + fadeOut(animationSpec = tween(durationMillis = 140)),
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) { }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quick Note",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Type something. Tap Save.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    OutlinedTextField(
                        value = textState,
                        onValueChange = { textState = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp)
                            .focusRequester(focusRequester),
                        label = { Text("Note") },
                        maxLines = 4,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Done,
                            capitalization = KeyboardCapitalization.Sentences
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { dismissThen { onSave(textState.text.trim()) } }
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { dismissThen(onCancel) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Button(
                            onClick = { dismissThen { onSave(textState.text.trim()) } },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save")
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { dismissThen(onClear) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Clear saved note")
                    }
                }
            }
        }
    }
}
