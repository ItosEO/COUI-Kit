# 功能图标

`CouiIcons` 提供可用于顶栏、菜单和导航的 Compose ImageVector。颜色和交互状态由使用它的组件提供。

## 基本用法

以下片段只使用 Foundation 与 COUI，位于宿主 Composable 中。`label` 和 `color` 由宿主提供；M3 应用也可把同一矢量传给 `Icon`：

```kotlin
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import com.itos.coui.icons.CouiIcons

Image(
    painter = rememberVectorPainter(CouiIcons.Search),
    contentDescription = label,
    colorFilter = ColorFilter.tint(color),
)
```

## 图标与状态

搜索、更多、设置、主题、添加、关闭、确认和返回分别使用 `Search`、`More`、`Settings`、`Theme`、`Add`、`Close`、`Check`、`Back`。导航可按选中状态切换 `Components` / `ComponentsSelected` 或 `Featured` / `FeaturedSelected`。

## 尺寸与方向

`Back` / `Next` 自动跟随 RTL 镜像，其他图标维持原方向。通过 `Modifier.size` 调整显示大小；`Next` 和 `Popup` 为竖长图标，用于设置行时可分别设置为 12 × 24dp 和 14 × 24dp。

只表达选中状态的图标将 contentDescription 设为 null，让父级选择节点持有语义；独立按钮提供操作说明。点击、热区、禁用状态及反馈仍由组件承担。

## 资源来源

资源未附带独立许可，本项目不授予额外第三方权利，详见[来源与权利说明](../../coui/ICON_SOURCES.md)。

[矢量实现](../../coui/src/main/java/com/itos/coui/icons/CouiIcons.kt) · [Demo 用途分配](../../demo/ICON_SOURCES.md) · [首页消费](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)
