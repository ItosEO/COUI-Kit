# COUI Compose

面向 Android Jetpack Compose 的 COUI 组件与视觉效果库，使用公开 Android / Compose API 实现。

COUI Compose 基于 ColorOS 17 最新的「凝光视效」做还原，包含背景渐进模糊、材质混色、轮廓与跟手光，以及控件按压、拖动时的跟手运动；库把这些拆成可复用的组件和效果。

目前提供顶栏与搜索、底栏、FAB、菜单、滑条、复选框、开关、ChipTab 和弹窗，以及可独立使用的阴影、模糊、材质混色、光效与融合算法。所有实现只依赖 Android、Compose 和 AndroidX，不读应用主题，也不依赖 OEM SDK、私有 SO 或运行时反射。

## 开始使用

源码模块和 GitHub Packages 预览版二选一，不要同时依赖。完整步骤见[快速开始](docs/guide/getting-started.md)。

源码模块：

```kotlin
// settings.gradle.kts
include(":coui")

// 消费者模块
android {
    buildFeatures { compose = true }
}

dependencies {
    implementation(project(":coui"))
}
```

预览版坐标和认证见 [GitHub Packages 接入](docs/guide/github-packages.md)。当前没有 Maven Central 发行，可用坐标以手动发布工作流的成功摘要为准。

平台要求按能力分：普通控件跟随库的最低系统；渐进模糊、轮廓和融合要 API 33；底栏、FAB、顶栏按钮/搜索、ChipTab 要 API 34。完整门槛见[兼容要求](docs/guide/compatibility.md#效果门槛)。

## 文档

| 你要做什么 | 从这里开始 |
| --- | --- |
| 将 COUI 用到自己的 App | [快速开始](docs/guide/getting-started.md) |
| 下载库预览版 | [GitHub Packages 接入](docs/guide/github-packages.md) |
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
