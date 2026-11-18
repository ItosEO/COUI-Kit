# 图标

`CouiIcons` 在 Composable 中提供矢量 `ImageVector` 与位图 `Painter`，并根据当前 `CouiTheme` 自动加载日间或夜间资源；颜色、点击热区与交互状态由宿主或外层组件承担。

- **系统版本要求**：支持库最低系统版本（**API 31+**）。
- **可选依赖**：图标位于独立的 `coui-icons` 模块，使用时需在调用模块添加依赖并与核心库保持相同版本（仅使用核心组件时无需引入）：

~~~kotlin
val couiVersion = "0.0.1-alpha11"

dependencies {
    implementation("io.github.itoseo:coui-icons:$couiVersion")
}
~~~

以下示例中的 `contentDescription` 均由调用方根据界面语义提供（仅作装饰或由父节点持有语义时传 `null`）。

## 基本显示

在 Composable 中使用 `Image` 可直接显示图标内置配色；使用 Material 3 `Icon` 时，传 `tint = Color.Unspecified` 以保留图标默认配色：

~~~kotlin
@Composable
fun VectorIconExample(
    modifier: Modifier = Modifier,
    contentDescription: String?,
) {
    Image(
        imageVector = CouiIcons.Filled.Wifi,
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
~~~

若改用 Material 3 `Icon`：

~~~kotlin
Icon(
    imageVector = CouiIcons.Filled.Wifi,
    contentDescription = contentDescription,
    tint = Color.Unspecified,
)
~~~

| 变体与规则 | 说明 |
| --- | --- |
| `CouiIcons.Filled` / `CouiIcons.Outlined` | 矢量图标按实心与描边两套提供；`Back`、`Next`、`Close`、`Add`、`DarkLight`、`ListFilter`、`More` 与 `Scan` 在两套样式中共用同一图形，`Search`、`SliderCustom` 与 `LayoutSpliced` 仅提供 `Outlined`。 |
| 默认配色与 RTL 镜像 | 常规单色图标默认日间黑色、夜间白色；仅 `Back` 与 `Next` 支持 RTL 自动镜像。 |
| `Search` / `Back` / `Next` 等单色图标边界 | `Search`、`Back`、`Next`、`Close`、`Add`、`DarkLight`、`CloudData`、`Album` 与 `WandSpark` 的可见图形均为不透明单色；底栏状态透明度由 `NavigationBar` 自动应用，独立 `Fab` 默认以不透明白色染色（可通过 `contentColor` 覆盖）。 |
| 传统 View 引用 | 矢量图形保存在 VectorDrawable 资源中（如 `@drawable/coui_wifi_filled`），传统 View 可直接引用并按 Android 日夜配置自动切换。 |

## 显示位图

`CouiIcons.Bitmap` 返回 `Painter`。在 `Image` 中通过 `Modifier.size` 指定显示尺寸，图标会保留 PNG 的原色与透明度：

~~~kotlin
@Composable
fun BitmapIconExample(
    modifier: Modifier = Modifier,
    contentDescription: String?,
) {
    Image(
        painter = CouiIcons.Bitmap.DevicePair,
        contentDescription = contentDescription,
        modifier = modifier.size(24.dp),
    )
}
~~~

- **日夜切换与插槽用法**：`DevicePair` 根据当前 `CouiTheme` 选择日间或夜间原图；放在 [Item](item.md) 的 `icon` 插槽内时可将 `contentDescription` 设为 `null`，由列表标签说明含义。
- **M3 `Icon` 与传统 View**：传给 Material 3 `Icon` 的 `painter` 参数时须设置 `tint = Color.Unspecified` 保留原色；传统 View 可直接引用 `@drawable/coui_device_pair`。

## 指定颜色与特殊图标

`CouiColor.Day` 和 `CouiColor.Night` 均提供常用色盘与 `GRAY`（`#474747`）、`DARK_GRAY`（`#191919`）预设：

- **单色图标指定 `tint`**：结合 `CouiTheme.isDark` 从 `CouiColor.Day` / `CouiColor.Night` 取色后传给 `Icon`：

  ~~~kotlin
  val iconTint = if (CouiTheme.isDark) CouiColor.Night.BLUE else CouiColor.Day.BLUE
  Icon(
      imageVector = CouiIcons.Filled.Wifi,
      contentDescription = contentDescription,
      tint = iconTint,
  )
  ~~~

- **多色图标**：`AI`、`Google`、`SettingDetail`、`Phone` 内置多色或渐变路径，应使用 `Image` 或 `Icon(..., tint = Color.Unspecified)`，避免整体 `tint` 覆盖原有配色。
- **菜单选择箭头（`Popup`）**：`CouiIcons.Outlined.Popup` 与 `CouiIcons.Filled.Popup` 共用同一份上下双箭头矢量。在弹出菜单的设置行尾部使用时按页面主题传入 `tint`，详见[菜单选择行的图标](menu.md#菜单选择行的图标)。
- **消息输入栏图标**：`CouiIcons.Outlined` 下的 `Camera`、`AddCircle` 和 `Send` 用于[消息输入栏](message-input-bar.md)，`Keyboard` 用于 Demo 目录入口。四个图标都保留原厂约 90% 不透明度的日间黑色和夜间白色。使用 `Icon` 显示原色时传入 `tint = Color.Unspecified`。`AddCircle` 是圆圈加号，`Add` 是普通加号。
- **应用更新图标（`AppUpdate`）**：自定义圆形背景色时调用 `CouiIcons.Filled.AppUpdate(color = ...)`，并在 `Icon` 中传 `tint = Color.Unspecified` 以保留白色前景：

  ~~~kotlin
  val badgeColor = if (CouiTheme.isDark) CouiColor.Night.GREEN else CouiColor.Day.GREEN
  Icon(
      imageVector = CouiIcons.Filled.AppUpdate(color = badgeColor),
      contentDescription = contentDescription,
      tint = Color.Unspecified,
  )
  ~~~

## 源码与参考

- **图标与色盘实现**：[CouiIcons.kt](../../coui-icons/src/main/java/com/itos/coui/icons/CouiIcons.kt)、[CouiColor.kt](../../coui/src/main/java/com/itos/coui/theme/CouiColor.kt)
