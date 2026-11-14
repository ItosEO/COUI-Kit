package com.itos.coui.demo

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.itos.coui.effect.CouiFloatingRenderer
import com.itos.coui.icons.CouiIcons
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.layout.rememberCouiImeInsets
import com.itos.coui.messageinput.CouiMessageInputAction
import com.itos.coui.messageinput.MessageInputBar
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch

@Composable
internal fun MessageInputSample(dark: Boolean, preferences: DemoPreferences, back: () -> Unit) {
    val state = rememberTextFieldState()
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val colors = CouiPageDefaults.themeColors(dark)
    var bottomHasFocus by remember { mutableStateOf(false) }
    val followBottom = bottomHasFocus
    val pageModifier = Modifier.background(colors.background)
        .windowInsetsPadding(rememberCouiImeInsets()).layout { measurable, constraints ->
            // 固定底栏编辑时，由示例宿主沿用同一 ScrollState 保持末端可见。
            val keepBottom = Snapshot.withoutReadObservation {
                followBottom && scrollState.viewportSize > 0 && scrollState.value == scrollState.maxValue
            }
            val placeable = measurable.measure(constraints)
            Snapshot.withoutReadObservation {
                if (keepBottom && scrollState.value < scrollState.maxValue) {
                    val bottom = scrollState.maxValue
                    scope.launch(start = CoroutineStart.UNDISPATCHED) { scrollState.scrollTo(bottom) }
                }
            }
            layout(placeable.width, placeable.height) { placeable.placeRelative(0, 0) }
        }
    val hasText = state.text.isNotEmpty()
    var enabled by rememberSaveable { mutableStateOf(true) }
    var event by rememberSaveable { mutableIntStateOf(R.string.message_input_ready) }
    var message by rememberSaveable { mutableStateOf("") }
    val submit: () -> Unit = {
        val text = state.text.toString().trim()
        if (text.isNotEmpty()) {
            message = text
            event = R.string.message_input_sent
            state.edit { replace(0, length, "") }
            focusManager.clearFocus()
        }
    }
    val rendering = if (Build.VERSION.SDK_INT >= 34) remember {
        runCatching { CouiFloatingRenderer.standalone() }
    } else null
    val renderer = rendering?.getOrNull()
    DemoSettingsPage(modifier = pageModifier, title = stringResource(R.string.message_input_title),
        backDescription = stringResource(R.string.settings_back), onBack = back,
        dark = dark, colors = colors, scrollState = scrollState, bottomBar = {
            if (Build.VERSION.SDK_INT >= 34 && renderer != null) {
                Box(Modifier.onFocusChanged { bottomHasFocus = it.hasFocus }.focusGroup()) {
                    MessageInputBar(Modifier.fillMaxWidth().navigationBarsPadding()
                        .padding(horizontal = 16.dp).padding(top = 22.dp, bottom = 16.dp),
                        state = state, onSubmit = submit,
                        placeholder = stringResource(R.string.message_input_placeholder),
                        enabled = enabled, dark = dark, renderer = renderer,
                        leadingAction = CouiMessageInputAction(rememberVectorPainter(CouiIcons.Outlined.Camera),
                            stringResource(R.string.message_input_camera_description),
                            onClick = { event = R.string.message_input_camera_event }),
                        trailingAction = CouiMessageInputAction(rememberVectorPainter(CouiIcons.Outlined.AddCircle),
                            stringResource(R.string.message_input_more_description),
                            onClick = { event = R.string.message_input_more_event }),
                        primaryAction = CouiMessageInputAction(rememberVectorPainter(CouiIcons.Outlined.Send),
                            stringResource(R.string.message_input_send_description), onClick = submit,
                            enabled = state.text.isNotBlank()),
                        showLeadingAction = preferences.messageInputCamera && !hasText,
                        showTrailingAction = preferences.messageInputMore,
                        showPrimaryAction = preferences.messageInputSend && hasText)
                }
            }
        }) {
        Group {
            DemoSwitchPreference(title = stringResource(R.string.message_input_enabled), checked = enabled,
                onCheckedChange = { enabled = it })
        }
        Group(title = stringResource(R.string.message_input_buttons)) {
            DemoSwitchPreference(title = stringResource(R.string.message_input_show_camera), checked = preferences.messageInputCamera,
                onCheckedChange = preferences::messageInputCamera)
            Divider()
            DemoSwitchPreference(title = stringResource(R.string.message_input_show_more), checked = preferences.messageInputMore,
                onCheckedChange = preferences::messageInputMore)
            Divider()
            DemoSwitchPreference(title = stringResource(R.string.message_input_show_send), checked = preferences.messageInputSend,
                onCheckedChange = preferences::messageInputSend)
        }
        Group(title = stringResource(R.string.message_input_event)) {
            Text(stringResource(event), Modifier.padding(CouiPageDefaults.rowPadding))
            if (message.isNotEmpty()) {
                Divider()
                Text(message, Modifier.padding(CouiPageDefaults.rowPadding))
            }
        }
        if (Build.VERSION.SDK_INT < 34) DemoPreference(title = stringResource(R.string.message_input_requires_api_34), enabled = false)
        rendering?.exceptionOrNull()?.let { error ->
            Text(stringResource(R.string.renderer_error, error.message ?: error.javaClass.simpleName),
                Modifier.padding(CouiPageDefaults.rowPadding), color = MaterialTheme.colorScheme.error)
        }
    }
}
