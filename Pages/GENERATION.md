# 图标网页数据生成指南

`Pages/data.js` 由网页生成器从 `coui-icons/src/main/res` 的 VectorDrawable、PNG 和颜色资源生成。

## 本地生成与预览

需要 Node.js 24。首次安装依赖后，在仓库根目录运行生成器：

```sh
npm ci --prefix Pages
node Pages/generate.mjs
python3 -m http.server 8080 --directory Pages
```

浏览器访问 `http://localhost:8080/icons/`。修改资源后，再运行 `node Pages/generate.mjs` 并刷新页面。

## XML 来源

- `coui-icons/src/main/res/drawable/coui_<名称>_<filled|outlined>.xml` 提供日间图标。
- `coui-icons/src/main/res/drawable-night/` 中存在同名文件时，提供夜间图形；没有时，网页使用日间图形，并按夜间颜色资源解析颜色引用。
- `values/coui_icon_colors.xml` 与可选的 `values-night/coui_icon_colors.xml` 提供图标颜色资源。网页色板读取 `coui/src/main/java/com/itos/coui/theme/CouiColor.kt`，生成器会核对 XML 中蓝、绿、黄、红、橙的日夜值与 `CouiColor` 一致。
- 生成器保留路径顺序、透明度、组变换、裁剪、笔画和渐变。
- 每个 vector 的 `android:name` 必须是准确的 PascalCase 图标名。文件后缀对应 `Filled` 或 `Outlined` 样式；共用图标的两个样式读取 `CouiIcons.kt` 内部 `Monochrome` 指定的同一资源。
- `AI`、`Google`、`SettingDetail` 和 `Phone` 保持原色。`coui_background` 路径用于 `AppUpdate` 的网页背景色调整。

生成器只接受网页数据格式能够表示的 VectorDrawable 子集。遇到不支持的 XML 属性、颜色引用或渐变时会报错，不会静默丢弃数据。

## PNG 来源

生成器根据 `CouiIcons.Bitmap` 的资源引用读取 `drawable-nodpi` 的 PNG。有同名 `drawable-night-nodpi` 资源时，夜间预览使用对应原图。两种主题的预览数据都保存在 `data.js` 中，支持离线浏览。

## GitHub Pages 发布

`.github/workflows/coui-icons-pages.yml` 使用 `workflow_dispatch` 手动触发。工作流安装 `Pages/package-lock.json` 中锁定的依赖，生成 `Pages/data.js` 并部署静态站点。

首次发布前，在仓库 `Settings -> Pages -> Build and deployment` 中将 `Source` 设为 **GitHub Actions**。之后在 GitHub `Actions` 页面运行 **部署 COUI 网站** 工作流。
