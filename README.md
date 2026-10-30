# COUI-Kit

面向 Android Jetpack Compose 的 COUI 组件与视觉效果库，使用公开 Android / Compose API 实现。

COUI-Kit 参考 ColorOS 17 的「凝光视效」，提供渐进模糊、材质混色、轮廓光和跟手动效。

你可以使用顶栏、底栏、菜单、滑条、开关、弹窗等组件，也可以单独使用阴影、模糊、光效和融合效果。库不依赖厂商 SDK 或私有 SO。

## 开始使用

在模块的 `build.gradle.kts` 中添加依赖：

```kotlin
dependencies {
    implementation("io.github.itoseo:coui:0.0.1-alpha2")
}
```

当前版本为早期预览版，API 可能发生不兼容的变化。[快速开始](docs/guide/getting-started.md)介绍仓库配置、环境要求和第一个组件，也提供源码模块与 GitHub Packages 快照的接入方式。

组件和效果的平台要求不同，使用前请查看[兼容要求](docs/guide/compatibility.md#效果门槛)。

## 文档

| 你要做什么 | 从这里开始 |
| --- | --- |
| 将 COUI 用到自己的 App | [快速开始](docs/guide/getting-started.md) |
| 下载开发快照 | [GitHub Packages 接入](docs/guide/github-packages.md) |
| 查找组件及示例 | [组件目录](docs/components/README.md) |
| 单独使用绘制效果或算法 | [效果目录](docs/effects/README.md) |
| 确认环境和平台要求 | [兼容要求](docs/guide/compatibility.md) |
| 设置配色、形状和公共预设 | [外观与预设](docs/guide/appearance.md) |
| 运行示例 App | [Demo 指南](docs/guide/demo.md) |
| 修改 COUI 或提交文档 | [贡献指南](CONTRIBUTING.md) |

## Demo

仓库里的 `demo` 模块包含组件、效果和预设三页，覆盖点击、拖动、加载、弹窗、材质和融合等交互。构建和运行见 [Demo 指南](docs/guide/demo.md)。

## 许可与名称使用

本项目自有源码采用 [MPL 2.0](LICENSE)，第三方内容保留各自许可与版权声明，具体范围见 [NOTICE](NOTICE)。

ColorOS 及相关名称归 OPPO 所有；本项目与 OPPO 没有隶属、授权或背书关系。

修改版和分叉版的发行名称、Logo 及来源说明遵循[名称与标识使用政策](TRADEMARKS.md)。
