# 锥光进度条

`ConicGlowProgressBar` 提供 ColorOS 流体云歌曲进度的横向轨道、白色亮边与锥形光尾。组件位于 `com.itos.coui.progressbar`，支持 Android 13（API 33）及以上，可独立放在 Compose 布局中，无需背景采样源。

## 展示进度

```kotlin
import com.itos.coui.progressbar.ConicGlowProgressBar

ConicGlowProgressBar(
    progress = playbackFraction,
    playing = isPlaying,
)
```

宿主提供归一化的播放位置和播放状态。播放时锥光随进度移动，暂停时光尾与白色亮边渐退，当前位置继续显示。省略 `onSeek` 时组件只读，仍向无障碍服务报告当前进度。

## 跳转播放位置

```kotlin
ConicGlowProgressBar(
    Modifier.semantics { contentDescription = playbackLabel },
    progress = playbackFraction,
    playing = isPlaying,
    onSeek = { fraction -> seekTo(fraction) },
)
```

点击轨道后，进度头从当前显示位置滑向触点，动画到达目标时调用一次 `onSeek`。再次点击会改向最新目标，开始横拖则接管预览。系统关闭动画时，点击立即提交。

横拖时暂时预览目标位置，轨道随拖动产生横向弹性位移，松手后回到原位并提交。进度换格时触发轻触觉，抵达首尾时触发端点反馈，受系统触觉设置控制。取消横拖后恢复宿主传入的进度。竖向手势可交还给外层滚动容器。键盘和 TalkBack 调节立即提交。宿主在跳转后更新 `progress`，并自行处理播放器时钟和异步 seek 的回执。

## 约束与外观

- `progress` 必须是有限值，显示范围为 0 至 1。
- 进度色默认读取当前 COUI 主题，轨道色使用原厂深色媒体卡片的半透明白。浅色表面可通过 `trackColor` 指定轨道颜色，按卡片内容染色时可覆写 `color`。
- 锥光可以越过轨道。按卡片轮廓裁剪时，在外层容器施加裁剪并为光尾留出空间。紧贴轨道裁剪会截短光尾。
- Android 12 由宿主选择自己的进度展示。锥光使用公开 `RuntimeShader`，Shader 初始化错误会传给调用方。

[组件源码](../../coui/src/main/java/com/itos/coui/progressbar/ConicGlowProgressBar.kt) · [Demo 示例](../../demo/src/main/java/com/itos/coui/demo/DemoConicGlowProgress.kt)
