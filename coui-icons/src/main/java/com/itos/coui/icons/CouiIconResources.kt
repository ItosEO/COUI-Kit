package com.itos.coui.icons

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPath
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.vectorResource
import com.itos.coui.theme.CouiTheme

@Composable
internal fun couiIconResource(id: Int): ImageVector {
    val configuredContext = couiIconContext()
    return remember(configuredContext, id) {
        ImageVector.vectorResource(configuredContext.theme, configuredContext.resources, id)
    }
}

@Composable
internal fun couiBitmapResource(id: Int): Painter {
    val configuredContext = couiIconContext()
    return remember(configuredContext, id) {
        BitmapPainter(ImageBitmap.imageResource(configuredContext.resources, id))
    }
}

@Composable
private fun couiIconContext(): Context {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val dark = CouiTheme.isDark
    return remember(context, configuration, dark) {
        val iconConfiguration = Configuration(configuration)
        iconConfiguration.uiMode = (iconConfiguration.uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
            (if (dark) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO)
        context.createConfigurationContext(iconConfiguration)
    }
}

internal fun ImageVector.withAppUpdateBackground(color: Color): ImageVector {
    check(root.count { it is VectorPath && it.name == "coui_background" } == 1) {
        "应用升级图标必须包含一个背景路径"
    }
    val builder = ImageVector.Builder(
        name = name,
        defaultWidth = defaultWidth,
        defaultHeight = defaultHeight,
        viewportWidth = viewportWidth,
        viewportHeight = viewportHeight,
        tintColor = tintColor,
        tintBlendMode = tintBlendMode,
        autoMirror = autoMirror,
    )
    for (node in root) {
        check(node is VectorPath) { "应用升级图标只支持路径节点" }
        builder.addPath(
            pathData = node.pathData,
            pathFillType = node.pathFillType,
            name = node.name,
            fill = if (node.name == "coui_background") SolidColor(color) else node.fill,
            fillAlpha = node.fillAlpha,
            stroke = node.stroke,
            strokeAlpha = node.strokeAlpha,
            strokeLineWidth = node.strokeLineWidth,
            strokeLineCap = node.strokeLineCap,
            strokeLineJoin = node.strokeLineJoin,
            strokeLineMiter = node.strokeLineMiter,
            trimPathStart = node.trimPathStart,
            trimPathEnd = node.trimPathEnd,
            trimPathOffset = node.trimPathOffset,
        )
    }
    return builder.build()
}
