# 图标

`CouiIcons` 在 Composable 中提供矢量 `ImageVector` 与位图 `Painter`，并根据 `CouiTheme` 加载日夜资源。

使用图标时，在调用模块中添加依赖，并与核心库使用相同版本：

```kotlin
implementation("io.github.itoseo:coui-icons:$couiVersion")
```

以下示例中的 `contentDescription` 由调用方根据界面语义提供。

## 基本显示

在 Composable 中使用 `Image` 可直接显示图标内置颜色；使用 Material 3 `Icon` 时，传 `tint = Color.Unspecified` 以保留图标默认配色：

```kotlin
Image(
    imageVector = CouiIcons.Filled.Wifi,
    contentDescription = contentDescription,
)

Icon(
    imageVector = CouiIcons.Filled.Wifi,
    contentDescription = contentDescription,
    tint = Color.Unspecified,
)
```

图标按 `CouiIcons.Filled` 和 `CouiIcons.Outlined` 提供。`Back`、`Next`、`Close`、`Add`、`DarkLight`、`ListFilter`、`More` 和 `Scan` 在两种样式中使用相同图形；`Search`、`SliderCustom` 和 `LayoutSpliced` 仅提供 `Outlined`。它们默认日间为黑色、夜间为白色，其中 `Back` 和 `Next` 支持 RTL 自动镜像。

矢量图形保存在 VectorDrawable 资源中，传统 View 可直接引用，例如 `@drawable/coui_wifi_filled`。传统 View 根据 Android 配置选择日夜资源。

`Search`、`Back`、`Next`、`Close`、`Add`、`DarkLight`、`CloudData`、`Album` 和 `WandSpark` 的可见图形均为不透明单色，默认日间黑色、夜间白色。底栏状态透明度由 `NavigationBar` 应用；独立 FAB 默认以不透明白色染色，可通过 `contentColor` 覆盖。

## 显示位图

`CouiIcons.Bitmap` 返回 `Painter`。在 `Image` 中通过 `Modifier.size` 指定显示尺寸，图标保留 PNG 的颜色和透明度。

```kotlin
Image(
    painter = CouiIcons.Bitmap.DevicePair,
    contentDescription = contentDescription,
    modifier = Modifier.size(24.dp),
)
```

`DevicePair` 根据 COUI 主题使用日间或夜间原图。在 `Item` 的 `icon` 插槽内显示时，可将 `contentDescription` 设为 `null`，由列表标签说明含义。使用 Material 3 `Icon` 时，将同一个 `Painter` 传给 `painter` 参数，并设置 `tint = Color.Unspecified` 保留原色。传统 View 可通过 `@drawable/coui_device_pair` 引用该资源。

## 指定颜色与特殊图标

`CouiColor.Day` 和 `CouiColor.Night` 均提供 `GRAY`（`#474747`）和 `DARK_GRAY`（`#191919`）。

- **单色图标指定 `tint`**：可结合 `CouiTheme.isDark` 从 `CouiColor.Day` / `CouiColor.Night` 选择颜色并传给 `Icon`：

```kotlin
val iconTint = if (CouiTheme.isDark) CouiColor.Night.BLUE else CouiColor.Day.BLUE
Icon(
    imageVector = CouiIcons.Filled.Wifi,
    contentDescription = contentDescription,
    tint = iconTint,
)
```

- **多色图标**：`AI`、`Google`、`SettingDetail`、`Phone` 内置多色或渐变路径，应使用 `Image` 或 `Icon(..., tint = Color.Unspecified)`，避免整体 `tint` 覆盖原有配色。
- **应用更新图标（`AppUpdate`）**：自定义圆形背景色时调用 `CouiIcons.Filled.AppUpdate(color = ...)`，并在 `Icon` 中传 `tint = Color.Unspecified` 以保留白色前景：

```kotlin
val badgeColor = if (CouiTheme.isDark) CouiColor.Night.GREEN else CouiColor.Day.GREEN
Icon(
    imageVector = CouiIcons.Filled.AppUpdate(color = badgeColor),
    contentDescription = contentDescription,
    tint = Color.Unspecified,
)
```

[CouiIcons.kt](../../coui-icons/src/main/java/com/itos/coui/icons/CouiIcons.kt) · [CouiColor.kt](../../coui/src/main/java/com/itos/coui/theme/CouiColor.kt)
