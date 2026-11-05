package com.itos.coui.demo

import android.content.Context
import android.graphics.SurfaceTexture
import android.graphics.Typeface
import android.media.MediaPlayer
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.Surface
import android.view.TextureView
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.findViewTreeLifecycleOwner
import com.itos.coui.shape.CouiShape
import kotlin.math.roundToInt

@Composable
internal fun DemoAboutHero(modifier: Modifier = Modifier) {
    AndroidView(
        factory = { DemoAboutHeroView(it) },
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, end = 16.dp)
            .height(328.dp).clip(CouiShape(28.dp, 3f)),
        onRelease = { it.release() },
    )
}

private class DemoAboutHeroView(context: Context) : FrameLayout(context),
    TextureView.SurfaceTextureListener, DefaultLifecycleObserver {
    private val texture = TextureView(context)
    private val placeholder = ImageView(context).apply {
        scaleType = ImageView.ScaleType.CENTER_CROP
        setImageResource(R.drawable.about_hero_first_frame)
    }
    private var player: MediaPlayer? = null
    private var surface: Surface? = null
    private var owner: LifecycleOwner? = null
    private var finished = false

    init {
        isForceDarkAllowed = false
        addView(texture, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(placeholder, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        addView(ImageView(context).apply {
            setImageResource(R.drawable.about_hero_logo)
            contentDescription = context.getString(R.string.about_hero_brand)
        }, LayoutParams(dp(220), dp(60), Gravity.TOP or Gravity.CENTER_HORIZONTAL).apply {
            topMargin = dp(104)
            marginStart = dp(4)
        })
        addView(TextView(context).apply {
            setText(R.string.about_hero_version)
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            setTextColor(0xD9FFFFFF.toInt())
            typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            gravity = Gravity.CENTER
            includeFontPadding = true
        }, LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.CENTER_HORIZONTAL).apply {
            topMargin = dp(154)
        })
        texture.surfaceTextureListener = this
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        val lifecycleOwner = checkNotNull(findViewTreeLifecycleOwner())
        owner = lifecycleOwner
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onDetachedFromWindow() {
        release()
        super.onDetachedFromWindow()
    }

    override fun onResume(owner: LifecycleOwner) {
        texture.surfaceTexture?.let { start(it) }
    }

    override fun onPause(owner: LifecycleOwner) {
        showLastFrame()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        start(surface)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) = Unit

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        showLastFrame()
        return true
    }

    fun release() {
        owner?.lifecycle?.removeObserver(this)
        owner = null
        showLastFrame()
    }

    private fun start(surfaceTexture: SurfaceTexture) {
        if (finished || player != null ||
            owner?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.RESUMED) != true) return
        val mediaPlayer = MediaPlayer()
        player = mediaPlayer
        surface = Surface(surfaceTexture)
        try {
            resources.openRawResourceFd(R.raw.about_hero).use {
                mediaPlayer.setDataSource(it.fileDescriptor, it.startOffset, it.length)
            }
            mediaPlayer.setSurface(surface)
            mediaPlayer.isLooping = false
            mediaPlayer.setVolume(0f, 0f)
            mediaPlayer.setOnPreparedListener {
                if (player === it && !finished) it.start()
            }
            mediaPlayer.setOnInfoListener { current, what, _ ->
                if (player === current && what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                    placeholder.visibility = GONE
                }
                false
            }
            mediaPlayer.setOnCompletionListener { finished = true }
            mediaPlayer.setOnErrorListener { _, what, extra ->
                Log.e("DemoAboutHero", "播放 Hero 动画失败: what=$what, extra=$extra")
                showLastFrame()
                true
            }
            mediaPlayer.prepareAsync()
        } catch (error: Exception) {
            Log.e("DemoAboutHero", "初始化 Hero 动画失败", error)
            showLastFrame()
        }
    }

    private fun showLastFrame() {
        finished = true
        placeholder.setImageResource(R.drawable.about_hero_last_frame)
        placeholder.visibility = VISIBLE
        player?.apply {
            setOnPreparedListener(null)
            setOnInfoListener(null)
            setOnCompletionListener(null)
            setOnErrorListener(null)
            release()
        }
        player = null
        surface?.release()
        surface = null
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).roundToInt()
}
