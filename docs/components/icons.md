# 功能图标

CouiIcons 提供不可变 Compose ImageVector，供顶栏、菜单、导航和普通内容使用。图标几何来自目标 ROM 的实际矢量；颜色由消费者的 tint 或主题传入，不加载 OEM 运行库。

以下片段位于宿主 Composable 中，label 和 color 由宿主提供：

```kotlin
import androidx.compose.material3.Icon
import com.itos.coui.icons.CouiIcons

Icon(imageVector = CouiIcons.Search, contentDescription = label, tint = color)
```

搜索、更多、设置、主题、添加、关闭、确认和返回分别对应 Search、More、Settings、Theme、Add、Close、Check、Back。Components / ComponentsSelected 与 Featured / FeaturedSelected 是两组导航矢量，保留各自选中和普通态的路径或 alpha。按用途选择图标，不能用业务插画代替功能符号。

Back / Next 自动跟随 RTL 镜像；其他矢量维持原方向。SearchField 保留原始 20dp，其余默认显示尺寸统一为 24dp 功能图标尺寸，内部 viewport 和描边按原比例转换；调用者可以通过 Modifier.size 调整显示大小。只表达选中状态的图标将 contentDescription 设为 null，让父级选择节点持有语义；独立按钮提供操作说明。点击、热区、禁用状态及反馈仍由组件承担。

当前图标集覆盖本地 Demo 所需功能，不代表已导入全部厂商图标。默认黑色填充/描边供 Compose Icon 着色；基础矢量不持有可变 renderer。源码见 [CouiIcons](../../coui/src/main/java/com/itos/coui/icons/CouiIcons.kt)，真实装配见 [DemoApp](../../demo/src/main/java/com/itos/coui/demo/DemoApp.kt)。


实际调用入口、原始资源与哈希见[来源表](../../coui/ICON_SOURCES.md)；Demo 用途映射见[图标分配](../../demo/ICON_SOURCES.md)。
