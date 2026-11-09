# COUI 网站

`Pages/` 提供 COUI 项目首页与图标工具，无外部 CDN 依赖，支持离线或通过任意静态 HTTP 服务运行。首页提供 GitHub 仓库、图标和文档入口；文档直接链接到 GitHub。

- `index.html`：项目首页。
- `icons/index.html`：图标浏览与源码复制。
- `style.css`、`theme.js`：两页共用的样式和主题切换。默认日间模式，浏览器允许本地存储时记住用户选择。

## 功能说明

- **图标搜索与预览**：列表按名称从 A 到 Z 排序，可按名称搜索并筛选 Filled、Outlined 或 Bitmap。Bitmap 详情显示当前主题的 PNG 原图。
- **图标样式**：同名图标同时提供 `Filled` 和 `Outlined` 时，可在详情中切换；共用图形的样式会标明。
- **日间 / 夜间模式切换**：点击顶部 DarkLight 图标按钮，切换页面主题与图标的日夜默认原图。
- **COUI Colors 预设与手动选色**：
  - 展示 `CouiColor.Day` 与 `CouiColor.Night` 去重后的日夜预设色板（隐藏 `HOME_*` 重复别名），并提供 `<input type="color">` 手动选色。
  - `mode = "tint"`：仅修改带 `data-coui-fill="true"` 或 `data-coui-stroke="true"` 的路径，保留其余路径与原始透明度。
  - `mode = "background"`（如 `AppUpdate`）：仅修改带标记的圆形背景路径，保留白色前景。
  - `mode = "original"`（多色/渐变图标）：禁止染色并展示说明，保持原色。
- **复制图标源码**：复制当前主题和颜色的 SVG，或包含导入语句的 Compose `ImageVector` Kotlin 定义。复制的颜色为当前选择的固定值，不自动切换日夜主题。使用 `Icon` 显示时，设置 `tint = Color.Unspecified` 可保留颜色。

## 数据契约

Bitmap 条目的 `style` 为 `Bitmap`，`day` 和 `night` 保存 PNG 数据地址。矢量条目提供 `vector` 供生成 ImageVector 源码。

图标页从相对路径 `../data.js` 读取 `window.COUI_ICON_DATA`：

```js
window.COUI_ICON_DATA = {
  icons: [
    {
      name: "Wifi",
      style: "Filled", // "Filled" | "Outlined"
      source: "Wifi", // 指向被复用的图标入口
      day: "<svg ...>...</svg>",
      night: "<svg ...>...</svg>",
      mode: "tint", // "tint" | "background" | "original"
      vector: { day: { /* ImageVector 数据 */ }, night: { /* ImageVector 数据 */ } }
    }
  ],
  colors: {
    Day: { BLUE: "#...", /* ... */ },
    Night: { BLUE: "#...", /* ... */ }
  }
};
```

## 生成数据文件

在仓库根目录执行图标数据生成脚本，将 `CouiIcons` 与 `CouiColor` 导出到 `Pages/data.js`：

```sh
node Pages/generate.mjs
```

新增图标、更新色板、特殊染色规则及 GitHub Pages 手动发布流程详见[图标网页数据生成指南](./GENERATION.md)。

## 本地预览命令

生成或确认 `Pages/data.js` 存在后，可在仓库根目录启动本地静态服务器预览：

```sh
# 方式 1：使用 Python 3 内置 HTTP 服务
python3 -m http.server 8080 --directory Pages
```

然后在浏览器访问 `http://localhost:8080`，图标工具位于 `http://localhost:8080/icons/`。

也可直接使用浏览器打开 `Pages/index.html` 或 `Pages/icons/index.html`。页面导航与资源使用相对路径，可部署在 GitHub Pages 的仓库子路径。
