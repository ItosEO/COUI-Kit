package com.itos.coui.demo

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.itos.coui.layout.Divider
import com.itos.coui.layout.Group
import com.itos.coui.progressbar.ConicGlowProgressBar

@Composable
internal fun ConicGlowProgressSample() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
        EffectText(stringResource(R.string.requires_api_33))
        return
    }
    var progress by rememberSaveable { mutableFloatStateOf(.35f) }
    var playing by rememberSaveable { mutableStateOf(true) }
    var seekable by rememberSaveable { mutableStateOf(true) }
    val title = stringResource(R.string.glow_progress_title)
    LaunchedEffect(playing) {
        if (playing) {
            var previous = withFrameNanos { it }
            while (progress < 1f) {
                val frame = withFrameNanos { it }
                progress = (progress + (frame - previous) / 180_000_000_000f).coerceAtMost(1f)
                previous = frame
            }
            playing = false
        }
    }
    Column(
        Modifier.padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF202343))
            .padding(horizontal = 20.dp, vertical = 7.dp),
    ) {
        ConicGlowProgressBar(
            Modifier.height(48.dp).semantics { contentDescription = title },
            progress = progress,
            playing = playing,
            color = Color(0xFF81A2FF),
            onSeek = if (seekable) { next -> progress = next } else null,
        )
    }
    Group {
        DemoSwitchPreference(
            title = stringResource(R.string.glow_progress_playing),
            checked = playing,
            onCheckedChange = {
                if (it && progress >= 1f) progress = 0f
                playing = it
            },
        )
        Divider()
        DemoSwitchPreference(
            title = stringResource(R.string.glow_progress_seekable),
            checked = seekable,
            onCheckedChange = { seekable = it },
        )
    }
}
