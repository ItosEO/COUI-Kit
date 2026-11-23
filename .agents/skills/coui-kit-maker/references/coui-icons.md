# coui-icons 图标模块制作分册

本分册规则出自 COUI-Kit 仓库内文件，每条标注出处。路径均相对仓库根目录。

## 1. 模块定位

- 可选 `coui-icons` 提供 `CouiIcons`，依赖核心以读取同一主题和色板；核心不依赖图标包。核心搜索组件持有内部搜索图形，不使用公开图标时无需引入该模块。（`docs/development/architecture.md`「模块边界」）
- 依赖配置（`coui-icons/build.gradle.kts`）：`implementation(project(":coui"))` + `api(libs.androidx.compose.ui)` + `api(libs.androidx.compose.runtime)`；`namespace = "com.itos.coui.icons"`，显式 `coordinates(artifactId = "coui-icons")`（根 `POM_ARTIFACT_ID` 仍归核心）；minSdk/compileSdk/aarMetadata 与 coui 相同；Jar 附带 `META-INF/coui` 的根 `LICENSE`、`NOTICE`、`TRADEMARKS.md` 与 `coui/ICON_SOURCES.md`。
- `CouiIcons` 提供矢量 `ImageVector` 与位图 `Painter`，按当前 `CouiTheme` 自动加载日/夜资源；颜色、点击热区与交互状态由宿主或外层组件承担。（`docs/components/icons.md` 开头）

## 2. 新增图标流程（`docs/development/icon-workflow.md` 全文持有）

### 2.0 先判断该不该进 coui-icons

`docs/development/icon-workflow.md` 首段已要求「先按图标选用建议确认场景」，对应页面是 `docs/components/icons.md`「怎么选图标」。先读这两处再动手：

- 底栏导航用同名 `Outlined` / `Filled` 配对（未选中 / 选中）；ColorOS 特有图形（如设置选择行的上下双箭头 `Popup`）与组件内置操作图标（FAB 加号 `Filled.Add`、自定义搜索按钮 `Outlined.Search`）用 COUI。
- **其他通用图标优先用 [Lucide Icons](https://lucide.dev/icons/)，不往 `coui-icons` 里补**。`coui-icons` 只承载与 ColorOS 视觉一致、且没有等价通用图标的图形；该页只给推荐范围，未展开取舍理由，需要例外时按 `icon-workflow.md` 正常走确认流程并说明来源。

### 2.1 确认目标和资源

1. 确定应用、页面、图标位置和需要的状态。先查已有提取材料；材料足够时不需要连接设备。需要新材料时按 `docs/development/replication-guide.md` §2 确认设备和 APK 版本；可用 `adb shell pm path <包名>` 查实际 APK 路径再提取。
2. 从页面布局、Preference、菜单或代码中的资源引用追到实际 drawable 并核对条件分支；可用 `aapt2 dump resources <APK路径>` 对照资源名与 ID；留意代码中的内联资源 ID。仅有静态引用不能证明图标运行时一定显示；动态查找、跨包引用和反编译缺口单独确认。
3. 只筛选本次目标图标，检查是否已有相同图形。按图形含义使用简短名称；同义但图形不同的图标用特征区分。有实际线框与填充变体时分别放入 `Outlined`、`Filled`；已有或用户指定名称优先（例：`SatelliteBD`、`AppUpdate`）。
4. 资源名相近不代表图形相同。检查日夜、设备配置、状态及 `selector`、`layer-list` 等引用，追到实际绘制的子资源和颜色；保留路径、尺寸、透明度、渐变及图层顺序。

### 2.2 处理资源与颜色

- 矢量资源保留可用的 Android VectorDrawable XML 及必要颜色资源；XML 需完整命名空间、宽高、viewport 和路径；资源表、未解析主题或资源引用不能直接当完整图标。
- PNG/WebP 保留原图接入位图入口：通用资源放 `drawable-nodpi`，夜间原图放 `drawable-night-nodpi` 并使用相同文件名。
- 单色图标保留各路径 alpha，再按已确认的日夜色值使用 `coui/src/main/java/com/itos/coui/theme/CouiColor.kt`；多条路径只是透明度不同仍可用同一颜色；不能把未证实的颜色近似归入现有色板；需要新色时先确认日夜值再补充对应角色。
- 多色和渐变图标保留各路径颜色及渐变，不用整体单色 tint；若只有部分图层可换色应分别处理（`AppUpdate` 只改背景，白色前景保持不变）。

### 2.3 接入图标库

1. 在 `coui-icons/src/main/res/drawable` 添加 VectorDrawable XML；需要不同夜间图形时在 `drawable-night` 添加同名资源；保留实际路径、viewport、填充、描边、裁剪、渐变与透明度；日夜颜色资源分别放 `values` 与 `values-night`（现有颜色资源文件为 `values/coui_icon_colors.xml` 与 `values-night/coui_icon_colors.xml`）。
2. 在 `coui-icons/src/main/java/com/itos/coui/icons/CouiIcons.kt` 对应样式中提供 `CouiIcons.Filled.名称` 或 `CouiIcons.Outlined.名称` 入口，通过 `couiIconResource` 加载 XML；图标跟随 `CouiTheme.isDark` 选日夜资源；供静态目录使用时保存 `@Composable () -> ImageVector` 并在组合时读取。
3. 按实际来源应用更新 `coui/ICON_SOURCES.md` 的图标分组；来源清单不记录路径细节或提取日志。

### 2.4 CouiIcons.kt 现行结构（实现时沿用）

出自 `CouiIcons.kt`（442 行）：

- `object CouiIcons` 内含 `private object Monochrome`（Back/Next/Popup/Close/Add/DarkLight/ListFilter/More/Scan 等在两套样式中共用同一资源的单色图标）、`object Filled`、`object Outlined`、`object Bitmap`。
- 每个入口是 `val 名称: ImageVector @Composable get() = couiIconResource(R.drawable.coui_xxx_filled|outlined)`；KDoc 标注原厂来源（如 `/** com.android.settings:drawable/coui_back_arrow_normal. */`）。
- `Bitmap` 入口：`val 名称: Painter @Composable get() = couiBitmapResource(R.drawable.coui_xxx)`（现有：IdCard、RadarSweep、Family、Smile、WarrantyCard、GamepadWired、DevicePair）。
- 参数化图标写法：`AppUpdate` 提供属性入口 + `@Composable fun AppUpdate(color: Color): ImageVector`，通过 `withAppUpdateBackground` 重建 ImageVector；该方法 `check` 要求根节点恰有一个 `name == "coui_background"` 的 VectorPath 且全部节点为 VectorPath，否则抛「应用升级图标必须包含一个背景路径」等异常。（`CouiIcons.kt` + `CouiIconResources.kt`）
- 资源加载实现（`CouiIconResources.kt`）：`couiIconContext()` 依据 `CouiTheme.isDark` 强制覆盖 `Configuration.uiMode` 的 `UI_MODE_NIGHT_MASK` 后 `createConfigurationContext`；`couiIconResource` 用 `ImageVector.vectorResource(configuredContext.theme, resources, id)` 并 `remember(configuredContext, id)`；`couiBitmapResource` 用 `BitmapPainter(ImageBitmap.imageResource(...))`。两者均为 `internal`。
- 仅 `Back` 与 `Next` 支持 RTL 自动镜像；`Search`、`SliderCustom`、`LayoutSpliced` 仅提供 `Outlined`。（`docs/components/icons.md` 变体表）

### 2.5 显示与染色规则（使用侧，影响新增图标的可换色设计）

出自 `docs/components/icons.md`：

- `Image` 直接显示内置配色；M3 `Icon` 保留原色须传 `tint = Color.Unspecified`；显式 tint 可覆盖单色图标，但会覆盖整张多色图。
- 常规单色图标默认日间黑色、夜间白色；多色图标（`AI`、`Google`、`SettingDetail`、`Phone`）内置多色或渐变路径，避免整体 tint。
- `CouiColor.Day` / `CouiColor.Night` 提供常用色盘与 `GRAY`（#474747）、`DARK_GRAY`（#191919）；单色 tint 结合 `CouiTheme.isDark` 取色。
- 需要可换色图层时在图标实现内单独处理（如 `AppUpdate(color)`）。

## 3. 网页预览生成（`Pages/GENERATION.md` 全文持有）

- 生成器读取 `coui-icons/src/main/res` 的 VectorDrawable、PNG 和颜色资源，以及 `CouiIcons` 从核心模块共享的矢量资源，生成 `Pages/data.js`。
- 本地命令（Node.js 24，仓库根目录）：

```sh
npm ci --prefix Pages
node Pages/generate.mjs
python3 -m http.server 8080 --directory Pages
```

浏览器访问 `http://localhost:8080/icons/`；修改资源后重跑 `node Pages/generate.mjs` 刷新。（`icon-workflow.md` 只给前两条命令，`GENERATION.md` 补充预览服务）

- XML 命名与结构硬规则：
  - 日间图标文件名 `drawable/coui_<名称>_<filled|outlined>.xml`；`drawable-night/` 有同名文件才提供夜间图形，否则网页用日间图形并按夜间颜色资源解析颜色引用。
  - 每个 vector 的 `android:name` 必须是准确的 PascalCase 图标名；文件后缀对应 `Filled`/`Outlined`；共用图标两套样式读取 `CouiIcons.kt` 内部 `Monochrome` 指定的同一资源。
  - `AI`、`Google`、`SettingDetail`、`Phone` 保持原色；`coui_background` 路径用于 `AppUpdate` 的网页背景色调整。
  - 网页色板读取 `coui/src/main/java/com/itos/coui/theme/CouiColor.kt`，生成器核对 XML 中蓝、绿、黄、红、橙的日夜值与 `CouiColor` 一致。
  - 生成器只接受网页数据格式能表示的 VectorDrawable 子集；遇到不支持的属性、颜色引用或渐变会报错，不静默丢弃；应检查报错并按需扩展生成器。
- PNG：按 `CouiIcons.Bitmap` 的资源引用读 `drawable-nodpi`；有同名 `drawable-night-nodpi` 时夜间预览用对应原图（当前仅 `coui_device_pair.png` 有夜间原图，见 res 目录清单）。
- 发布：`.github/workflows/coui-icons-pages.yml` 用 `workflow_dispatch` 手动触发，安装 `Pages/package-lock.json` 锁定依赖，生成 `Pages/data.js` 并部署静态站点；首次发布前在仓库 Settings → Pages → Build and deployment 把 Source 设为 GitHub Actions。

## 4. 核对清单（`icon-workflow.md`「生成预览与核对」）

- 核对新增名称和调用、日夜默认颜色、单色透明度、多色路径，以及不同状态或方向；有可换色图层时检查改色后其他图层保持不变。
- 将网页预览与目标资源对照；不把网页 SVG 正常显示当作 Android Compose 或真机显示已验证。
- Android 构建和设备验证按项目授权执行，记录实际完成的检查及未验证部分。

## 5. 来源登记（`coui/ICON_SOURCES.md` 现有分组）

时钟（`com.coloros.alarmclock`）、相册（`com.coloros.gallery3d`）、通知侧滑菜单（`com.android.systemui`）、安全与隐私（`com.oplus.securitypermission`）、云服务（`com.heytap.cloud`）、账号页（`com.oplus.account`）、设置（`com.android.settings`）、小布助手（`com.heytap.speechassist`）。派生关系（如 `Filled.Headset` 由 `Outlined.Headset` 填实耳罩派生、`Filled.Share`/`Filled.Pen` 从原厂轮廓派生）在该文件逐条登记；新增图标按实际来源更新对应分组。
