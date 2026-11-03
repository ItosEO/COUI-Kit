# 功能图标

`CouiIcons` 提供不可变的 Compose ImageVector，可用于顶栏、菜单、导航和普通内容。原始资源与调用记录见[来源表](../../coui/ICON_SOURCES.md)，颜色和交互状态由消费者提供。

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

搜索、更多、设置、主题、添加、关闭、确认和返回分别对应 Search、More、Settings、Theme、Add、Close、Check、Back。Components / ComponentsSelected 与 Featured / FeaturedSelected 是两组导航矢量，保留各自选中和普通态的路径或 alpha。

## 尺寸与方向

Back / Next 自动跟随 RTL 镜像，其他矢量维持原方向。默认尺寸如下：

| 图标 | 默认宽 × 高 | viewport |
| --- | --- | --- |
| SearchField | 20 × 20dp | 20 × 20 |
| Next | 24 × 48dp | 12 × 24 |
| Popup | 24 × 约 41.14dp | 14 × 24 |
| 其他图标 | 24 × 24dp | 保留各自原始 viewport |

调用者可以通过 Modifier.size 调整显示大小。用于 24dp 高的设置行箭头时，Next 可指定 12 × 24dp，Popup 可指定 14 × 24dp；默认宽度为 24dp 不代表二者默认是方形图标。Wallpaper 保留独立的用途名称，与 Personalise 共用同一个不可变 ImageVector。

只表达选中状态的图标将 contentDescription 设为 null，让父级选择节点持有语义；独立按钮提供操作说明。点击、热区、禁用状态及反馈仍由组件承担。

## 来源与范围

该子集不包含完整 OEM selector 或主题协议。禁用态、着色和页面中的用途由宿主选择；来源 App 的调用记录不能证明 Demo 使用的是同一官方页面方案。资源未附带独立许可，本项目不授予额外第三方权利，详见[来源与权利说明](../../coui/ICON_SOURCES.md)。

[矢量实现](../../coui/src/main/java/com/itos/coui/icons/CouiIcons.kt) · [Demo 用途分配](../../demo/ICON_SOURCES.md) · [首页消费](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)
