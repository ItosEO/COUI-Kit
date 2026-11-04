# 横向双主体融合

将两个同色主体靠近时，CouiMetaballRenderer 绘制横向 CONIC 融合场。它也可作为内容遮罩，用于宿主自己的列表、按钮或其他形变区域。

## 绘制两个主体

下面函数在 API 33 及以上使用。first 和 second 由宿主提供，中心、尺寸和圆角均为当前 Canvas 的本地像素；modifier 必须给 Canvas 有效尺寸。

~~~kotlin
@RequiresApi(33)
@Composable
fun MetaballPair(
    modifier: Modifier = Modifier,
    first: CouiMetaballBody,
    second: CouiMetaballBody,
    color: Color,
    fusionEnabled: Boolean = true,
) {
    val renderer = remember { CouiMetaballRenderer() }
    Canvas(modifier) {
        renderer.draw(
            scope = this,
            first = first,
            second = second,
            color = color,
            blendRangePx = CouiSwipeRevealDefaults.metaballBlendRange.roundToPx().toFloat(),
            fusionEnabled = fusionEnabled,
        )
    }
}
~~~

主体的尺寸和半径须为有限非负值，零尺寸表示收缩后的主体。secondAlpha 控制第二主体透明度，fusionEnabled 控制融合参与。算法只支持同色横向双主体，不采样背景。

## 内容遮罩

背景与遮罩使用相同几何及融合参数，但各自持有 renderer。遮罩先在独立离屏层绘制内容，再使用 Color.White 与 BlendMode.DstIn 调用同一算法。

宿主负责将背景坐标映射到内容层；收缩布局、RTL 或右侧对齐时，不能直接沿用未转换的节点坐标。完整实现见 [DemoRecordMetaball](../../demo/src/main/java/com/itos/coui/demo/DemoRecordMetaball.kt)。

## 跟随与交互

[运动与跟随回弹](motion.md)提供随距离移动与释放回弹。距离、布局、RTL、裁剪和手势由宿主装配；库不提供侧滑容器、删除确认或业务提交。

侧滑驱动效果见 Demo 的融合页，操作说明见 [Demo 指南](../guide/demo.md#主要示例)。示例列表及其手势属于 Demo：[记录入口](../../demo/src/main/java/com/itos/coui/demo/DemoRecordSamples.kt)、[侧滑装配](../../demo/src/main/java/com/itos/coui/demo/DemoRecordSwipeRow.kt)。

## 公开入口

[CouiMetaballBody / CouiMetaballRenderer](../../coui/src/main/java/com/itos/coui/effect/CouiMetaballRenderer.kt)。构造前检查平台，失败处理见[资源规则](backdrop.md#资源与失败处理)；固定三主体的多层材质见[浮动表面](floating-surfaces.md)。
