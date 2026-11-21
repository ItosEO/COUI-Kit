# Agent Note: 菜单默认宽度范围与子菜单下限

Status: implemented

## Problem

`Menu` 此前沿用 `128.dp` 至 `280.dp` 的宽度范围，未区分主菜单是否包含子菜单项。这与 ColorOS 17 原厂 `COUIPopupListWindow` 的默认最小宽度、含子菜单下限及最大宽度不一致，导致短文本菜单过窄、含二级菜单项的主菜单缺少预留宽度，而超长项又可能拉宽至 `280.dp`。

## Decision

[Menu](../../../../coui/src/main/java/com/itos/coui/menu/Menu.kt) 将默认最小宽度设为 `CouiMenuMinWidth = 168.dp`，含子菜单项时的最小宽度设为 `CouiMenuMinWidthWithSubMenu = 196.dp`，最大宽度设为 `CouiMenuMaxWidth = 256.dp`。

普通单级菜单（`items.none { it.children.isNotEmpty() }`）使用 `widthIn(min = CouiMenuMinWidth, max = CouiMenuMaxWidth)`。存在子菜单项时，[CouiMenuWithSubMenu](../../../../coui/src/main/java/com/itos/coui/menu/CouiMenuSubMenu.kt) 的主菜单使用 `widthIn(min = CouiMenuMinWidthWithSubMenu, max = CouiMenuMaxWidth)`；子面板在取得主菜单实测宽度后继续沿用主菜单宽度，未取得实测宽度时的回退约束同步使用 `CouiMenuMinWidthWithSubMenu` 至 `CouiMenuMaxWidth`。

宿主传入的 `modifier` 与 [BottomSheet](../feature/2026-11-07-modal-bottom-sheet.md) 提供的 `LocalCouiMenuVisibleBounds` 宽度上限仍外层叠加并约束最终测量结果；`Menu` 公开签名保持不变。装配说明同步更新于[菜单文档](../../../../docs/components/menu.md)。

## 原厂依据与核查

- 对照 ColorOS 17 `Settings.apk`（`com.android.settings`，versionName `17.0.0`，SHA-256 `d97df8a9227520a6f673e5d6f5efbc901b5d9d4c501d12e5d8bf717daae079d5`）的 `classes7.dex` 与资源表：
  - `coui_popup_list_window_min_width` = `168.0dp`
  - `coui_popup_list_window_width_with_icon` = `196.0dp`
  - `coui_popup_list_window_max_width` = `256.0dp`
- `COUIPopupListWindow.getMainMenuMinWidth()` 与 `getMainMenuMaxWidth()` 的反编译源码及 fallback 字节码核实：在未设置自定义固定宽度（`mCustomMenuWidth`）和自定义最大宽度（`mCustomMenuMaxWidth`）时，最小宽度遍历菜单项并在存在 `hasSubMenu()` 为真时返回 `coui_popup_list_window_width_with_icon`（`196.0dp`），否则返回 `coui_popup_list_window_min_width`（`168.0dp`）；`getIconId()` / `getIcon()` / `hasIcon()` 调用结果未改变该分支标志位，因此“仅带普通左侧图标但不含子菜单”的菜单仍走 `168.0dp` 下限。最大宽度统一返回 `coui_popup_list_window_max_width`（`256.0dp`）。
- 子菜单在 `measurePopupWindowForRecyclerView` / `measurePopupWindow` 中固定取 `this.mSubMenuWidth = this.mMainMenuWidth`，与现有 `CouiMenuSubMenu` 读取主菜单实测宽度的做法一致。

## Existing-note audit

- [菜单输入会话与合成层序](2026-10-15-coui-submenu-input-dispatch.md)：无关／不冲突；该篇记录主子菜单触摸会话、合成层序与背景隔离生命周期，不涉及默认宽度约束。
- [底部面板的运动与窗口采样归属](../feature/2026-11-07-modal-bottom-sheet.md)：部分重叠；该篇持有 `LocalCouiMenuVisibleBounds` 对面板内菜单可见宽高的约束，本篇仅调整菜单自身的默认最小/最大宽度下限与上限，面板可见边界约束继续生效。
- 活跃 Note 中没有其他记录 `CouiMenuMinWidth` / `CouiMenuMaxWidth` 的条目。

## Alternatives considered

- **统一改为固定范围 `168.dp` 至 `256.dp`。** 改动更少，但含子菜单项的主菜单在短文本场景下会少于原厂预留的 `196.dp`，与 `COUIPopupListWindow.getMainMenuMinWidth()` 不一致。
- **把 `196.dp` 绑定到 `item.icon != null`。** 资源名 `coui_popup_list_window_width_with_icon` 字面容易误导，但核对 `COUIPopupListWindow` 字节码后确认其实际只由 `hasSubMenu()` 触发，因此按实际分支绑定到 `children.isNotEmpty()`。
- **暴露 `Menu` 专属宽度参数。** 已有 `modifier` 可由宿主覆盖宽度约束，不新增多余公开参数以保持 API 收窄。

## Consequences

菜单默认宽度范围与 ColorOS 17 `COUIPopupListWindow` 对齐：普通菜单在 `168.dp`–`256.dp` 之间按内容测量，含子菜单项时从 `196.dp` 起步。由于 `168.dp` / `196.dp` 高于原先的 `128.dp`、`256.dp` 低于原先的 `280.dp`，极短菜单会略变宽，超宽长文本菜单会更早换行。

当前已核对原厂字节码、资源与静态校验，但尚未在真机或模拟器上目视对比不同长度文本、带图标与带子菜单场景的视觉效果及换行表现。
