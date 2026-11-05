# Footer

`Footer` 显示分组下方的辅助说明，位于 `com.itos.coui.layout`。

```kotlin
Footer(text = description)
```

`description` 由宿主提供。`text` 必填，采用 `Description` 文字角色并按逻辑起始边对齐；组件自带水平和垂直留白，外部 `Modifier.padding` 会累加。

`color` 控制说明文字，默认读取 `CouiTheme.colorScheme.onSurfaceVariant`。使用 [Group](group.md) 的 `footer` 参数时，由 Group 自动装配说明并使用该组的说明颜色。

[公开布局与 Defaults](../../coui/src/main/java/com/itos/coui/layout/CouiPageComponents.kt)
