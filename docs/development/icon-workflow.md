# 新增图标

新增图标时，先确认目标应用和页面实际使用的资源，再将图形接入 `CouiIcons`。矢量保存在 VectorDrawable XML 中，供 Compose 加载 `ImageVector` 和网页生成预览。位图保留原始资源，通过 `CouiIcons.Bitmap` 提供 `Painter`。

## 确认目标和资源

1. 确定应用、页面、图标位置和需要的状态。先查已有提取材料；材料足够时不需要连接设备。需要新材料时，按[复刻指南](replication-guide.md#2-从系统页面到实际消费者)确认设备和 APK 版本。可用 `adb shell pm path <包名>` 查实际 APK 路径，再提取相关 APK；应用更新或拆分 APK 可能改变资源位置。
2. 从页面的布局、Preference、菜单或代码中的资源引用追到实际 drawable，并核对条件分支。可用 `aapt2 dump resources <APK路径>` 对照资源名与 ID；代码中还要留意内联的资源 ID。设备当前隐藏但代码或布局明确引用的功能，可以按任务范围纳入。仅有静态引用不能证明某个图标在运行时一定显示；动态查找、跨包引用和反编译失败造成的缺口要单独确认。
3. 只筛选本次目标图标，检查是否已有相同图形。按图形含义使用简短名称；同义但图形不同的图标用特征区分。有实际线框与填充变体时，分别放入 `Outlined`、`Filled`；已有或用户指定名称优先，例如 `SatelliteBD`、`AppUpdate`。

资源名相近不代表图形相同。检查日夜、设备配置、状态及 `selector`、`layer-list` 等引用，追到实际绘制的子资源和颜色；保留路径、尺寸、透明度、渐变及图层顺序。确认是否为同一图标的不同状态，再决定是否分别提供入口。

## 处理资源与颜色

- 对矢量资源，保留可用的 Android VectorDrawable XML 及必要的颜色资源。XML 需要完整的命名空间、宽高、viewport 和路径；资源表、未解析的主题或资源引用不能直接当作完整图标。预览异常时先检查实际资源类型和依赖，不要只改文件名。
- PNG 或 WebP 可保留原图接入位图入口。通用资源放入 `drawable-nodpi`，夜间原图放入 `drawable-night-nodpi` 并使用相同文件名。
- 单色图标保留各路径的 alpha，再按已确认的日夜色值使用 [CouiColor](../../coui/src/main/java/com/itos/coui/theme/CouiColor.kt)。多条路径只是透明度不同，仍可使用同一颜色；不能把未证实的颜色近似归入现有色板。需要新色时，先确认日夜值，再补充对应角色。
- 多色和渐变图标保留各路径颜色及渐变，不使用整体单色 tint。若只有部分图层可换色，应分别处理：`AppUpdate` 只更改背景，白色前景保持不变。

## 接入图标库

1. 在 [drawable](../../coui-icons/src/main/res/drawable) 中添加 VectorDrawable XML；需要不同的夜间图形时，在 `drawable-night` 中添加同名资源。保留实际路径、viewport、填充、描边、裁剪、渐变与透明度；日夜颜色资源分别放入 `values` 和 `values-night`。
2. 在 [CouiIcons.kt](../../coui-icons/src/main/java/com/itos/coui/icons/CouiIcons.kt) 的对应样式中提供 `CouiIcons.Filled.名称` 或 `CouiIcons.Outlined.名称` 入口，通过 `couiIconResource` 加载 XML。图标跟随 `CouiTheme.isDark` 选择日夜资源；供静态目录使用时，保存 `@Composable () -> ImageVector` 并在组合时读取。
3. 按实际来源应用更新[图标来源](../../coui/ICON_SOURCES.md)中的图标分组。详细使用方法维护在[图标用法](../components/icons.md)，来源清单不记录路径细节或提取日志。

`Image` 可显示矢量自带颜色；Material 3 `Icon` 要保留这些颜色，应传 `tint = Color.Unspecified`。显式 tint 可覆盖单色图标，但会覆盖整张多色图。需要可换色的局部时，在图标实现中单独处理该图层。

## 生成预览与核对

在仓库根目录执行：

```sh
npm ci --prefix Pages
node Pages/generate.mjs
```

脚本根据库中的 XML 图标和色板生成网页数据。打开预览、处理新增色板或特殊图标，以及发布网页的步骤见[网页数据生成指南](../../Pages/GENERATION.md)。遇到不支持的资源写法应检查报错，并按实际需要扩展生成器。

核对新增名称和调用、日夜默认颜色、单色透明度、多色路径，以及不同状态或方向；有可换色图层时检查改色后其他图层是否保持不变。将网页预览与目标资源对照，但不要把网页 SVG 正常显示当作 Android Compose 或真机显示已验证。Android 构建和设备验证按项目授权执行，记录实际完成的检查及未验证部分。
