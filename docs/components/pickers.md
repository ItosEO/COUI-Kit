# 时钟选择器

时间、倒计时、日期滚轮和月历都能独立放进页面，也能使用标准选择弹窗。宿主持有数值、草稿与业务提交，库不启动闹钟、倒计时任务或页面导航。

## 核心与装配

库内保留 CouiWheelPicker 和 CouiCalendarPicker 两种独立控件；时间、倒计时和日期是滚轮列的组合，标准选择弹窗是现有 CouiAlertDialog 的组合，均只在 Demo 中提供示例。

| 组合 | 示例 |
| --- | --- |
| 时间、AM/PM、倒计时、日期顺序与范围 | [DemoWheelPresets](../../demo/src/main/java/com/itos/coui/demo/DemoWheelPresets.kt) |
| 标题、项目材质、确认/取消与任意选择器内容 | [DemoPickerDialog](../../demo/src/main/java/com/itos/coui/demo/DemoPickerDialog.kt) |
| 页面与弹窗共用内容、草稿及确认提交 | [DemoPickerSamples](../../demo/src/main/java/com/itos/coui/demo/DemoPickerSamples.kt) |

以上均为 internal 装配；宿主按需参考和调整，不导入 Demo。此前的 CouiTimePicker、CouiCountdownPicker、CouiDatePicker、CouiPickerDialog 与示例数据类型已移出库，不保留重复公共别名。

以下片段在宿主 Composable 内，标签、主题、整数与更新回调均由宿主提供：

```kotlin
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.itos.coui.picker.CouiWheelPicker

Row {
    CouiWheelPicker(Modifier.width(62.dp), hour, updateHour, 0..23, hourLabel, dark)
    CouiWheelPicker(Modifier.width(62.dp), minute, updateMinute, 0..59, minuteLabel, dark,
        repeatIntervalMillis = 100, formatter = { "%02d".format(it) })
}
```

选择带的整组分割线、冒号、AM/PM 排序、月长夹紧和业务草稿由宿主装配。Demo 保留原厂对应配置：时间/日期有整组选择线，大数字倒计时没有横线。数值没有写死；示例通过受控值、范围和回调传参。

月历使用 CouiCalendarPicker，value、onValueChange、minDate、maxDate、today 和文字说明由调用者提供。翻月箭头只改变浏览月份；点选日期或滚动展开后的年/月/日三列会立即提交受控日期。展开时从当前受控日期初始化，收起后回到该日期所在月份。日期与范围非法时明确失败。

月历只有一个呈现入口；原有调用删除 presentation 实参和 CouiCalendarPresentation import 即可，值、回调与范围语义不变。头部使用 16dp 折叠箭头、16sp 标题与 24dp 翻页箭头；网格为周日起始、16sp/14sp sans-serif-medium、32dp 选中/今日圆及 40/48dp 行高。编辑展开延迟 120ms，网格淡入 280ms、淡出 150ms；翻页按 COUICalendarViewPagerScroller 的 300ms COUIMoveEaseInterpolator 收尾。闹钟多日期重复、节假日及过去日期限制是宿主业务，当前 API 为单日期选择。

选择弹窗直接组合 [CouiAlertDialog](dialog.md) 与任意滚轮/月历内容。取消、返回和外部点击只关闭，确认才保存草稿；backdrop 必须来自当前窗口且不含弹窗和压暗层。Demo 的弹窗装配没有第二套材质、几何或动画后端。

## 自己组合滚轮

CouiWheelPicker 提供受控整数、range、formatter、语义 label、循环、行高、普通/选中字号、diffusion 和渐隐。高度、字号、扩散均用 dp；内核沿原厂 NumberPicker 的像素取整，不将字号再次按 SP 放大。固定预设见 CouiPickerDefaults，业务列布局由宿主组合。

跨项时回调当前值；快速点按上下邻项、拖动、惯性、再次按下中断、长按及键盘调整各自处理。CANCEL 不追加点击，只吸附已有候选值；外部拒绝值在稳定后回到受控值。禁用和移除停止交互与动画，每个节点独立持有 Scroller。厂商马达尝试失败后使用已有标准反馈码，不保证 OEM 波形等价。

## 来源与承载边界

原厂目标为 Clock 17.6.94 的 COUINumberPicker、COUITimeLimitPicker、OplusTimePickerCustomClock、COUIDatePicker，以及 Calendar 17.3.70 的 COUICalendarPicker、COUICalendarDayPickerView、COUIDateMonthView、COUICalendarPickerDelegate 和实际布局；关键绘制与 smali 交叉核对。没有使用 AndroidView、平台 TimePicker 或 M3 Picker 替代。月历分页与无障碍由公开 Compose 承载，未移植 OEM 私有协议、农历、超大字/迷你/分屏专用排布或闹钟业务；源码与 Release 检查不等于动态验收。

[滚轮 API](../../coui/src/main/java/com/itos/coui/picker/CouiWheelPicker.kt)、[时间/日期装配](../../demo/src/main/java/com/itos/coui/demo/DemoWheelPresets.kt)、[月历](../../coui/src/main/java/com/itos/coui/picker/CouiCalendarPicker.kt)、[弹窗装配](../../demo/src/main/java/com/itos/coui/demo/DemoPickerDialog.kt)、[完整 Demo](../../demo/src/main/java/com/itos/coui/demo/DemoPickerSamples.kt)。

## 选择带与日期参数

时间/日期预设由整组绘制两条选择分割线；大数字倒计时保留无整组横线的标准分支；月历的年月编辑使用 48dp 选择区上下分割线与 12dp 上下留白。不能因单列 NumberPicker 未启用背景，就省略父选择器的绘制。


月历使用 Calendar 原版的周日起始、头部/网格规格和三列日期滚轮；今天的文字和描边使用强调色。value、日期范围和 today 始终由调用者传入，没有写死某年某月；Demo 的初始值来自设备当前日期。

配色默认在消费处读取 CouiTheme，显式参数优先；倒三角保持朝下，不作旋转。
