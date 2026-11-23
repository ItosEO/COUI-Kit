# 图标全目录（CouiIcons 全部公开名称）

本分册完整列出 `coui-icons` 当前公开的每个图标入口、对应资源名与来源线索。名称与资源映射逐条读自 `coui-icons/src/main/java/com/itos/coui/icons/CouiIcons.kt`；资源目录读自 `coui-icons/src/main/res/`；用法规则读自 `docs/components/icons.md`、`docs/development/icon-workflow.md`、`Pages/GENERATION.md`。

## 1. 结构总览

| 分组 | 数量 | 返回类型 | 加载函数 |
| --- | --- | --- | --- |
| `CouiIcons.Filled` | 74（含 `AppUpdate(color)` 重载） | `ImageVector` | `couiIconResource` |
| `CouiIcons.Outlined` | 30 | `ImageVector` | `couiIconResource` |
| `CouiIcons.Bitmap` | 7 | `Painter` | `couiBitmapResource` |
| `private object Monochrome` | 9 | `ImageVector` | 两套样式共用同一资源 |

- 日夜选择由 `couiIconContext()` 按 `CouiTheme.isDark` 覆盖 `Configuration.uiMode` 后加载（`CouiIconResources.kt`）。
- 仅 `Back`、`Next` 支持 RTL 自动镜像；`Search`、`SliderCustom`、`LayoutSpliced` 仅提供 `Outlined`（`docs/components/icons.md`）。

## 2. Monochrome（两套样式共用）

| 名称 | 资源 |
| --- | --- |
| `Back` | `coui_back_filled` |
| `Next` | `coui_next_filled` |
| `Popup` | `coui_pop_up_next`（设置选择行/子菜单上下双箭头） |
| `Close` | `coui_close_filled` |
| `Add` | `coui_add_filled` |
| `DarkLight` | `coui_dark_light_filled` |
| `ListFilter` | `coui_list_filter_outlined` |
| `More` | `coui_more_outlined` |
| `Scan` | `coui_scan_outlined` |

## 3. Filled（74）

| 名称 | 资源 | 备注（KDoc / ICON_SOURCES） |
| --- | --- | --- |
| `Back` | `coui_back_filled` | Monochrome；`com.android.settings:drawable/coui_back_arrow_normal` |
| `Next` | `coui_next_filled` | Monochrome；`com.android.settings:drawable/coui_btn_next_normal` |
| `Popup` | `coui_pop_up_next` | Monochrome；颜色由调用方指定 |
| `Close` | `coui_close_filled` | Monochrome；`com.android.settings:drawable/ic_close` |
| `Add` | `coui_add_filled` | Monochrome；`com.coloros.alarmclock:drawable/color_floating_button_icon_add` |
| `AddCircle` | `coui_add_circle_filled` | |
| `DarkLight` | `coui_dark_light_filled` | Monochrome；`com.android.settings:drawable/dark_theme` |
| `Sun` | `coui_sun_filled` | `settings_brightness_ic` |
| `Bell` | `coui_bell_filled` | `settings_notify_center_ic` |
| `MultiPhone` | `coui_multi_phone_filled` | `settings_personalise_ic` |
| `Volume` | `coui_volume_filled` | `settings_sound_settings_ic` |
| `Plane` | `coui_plane_filled` | |
| `Wifi` | `coui_wifi_filled` | |
| `Bluetooth` | `coui_bluetooth_filled` | |
| `Transfer` | `coui_transfer_filled` | |
| `Satellite` | `coui_satellite_filled` | |
| `SatelliteBD` | `coui_satellite_bd_filled` | |
| `Radar` | `coui_radar_filled` | |
| `Foldable` | `coui_foldable_filled` | |
| `FlipPhone` | `coui_flip_phone_filled` | |
| `Lock` | `coui_lock_filled` | |
| `ShieldUser` | `coui_shield_user_filled` | |
| `Sos` | `coui_sos_filled` | |
| `Location` | `coui_location_filled` | |
| `Wallet` | `coui_wallet_filled` | |
| `Health` | `coui_health_filled` | |
| `Grid` | `coui_grid_filled` | |
| `Battery` | `coui_battery_filled` | |
| `PanelEnterRight` | `coui_panel_enter_right_filled` | |
| `PanelEnterLeft` | `coui_panel_enter_left_filled` | |
| `Fan` | `coui_fan_filled` | |
| `Rings` | `coui_rings_filled` | |
| `Accessibility` | `coui_accessibility_filled` | |
| `AI` | `coui_ai_filled` | 多色/渐变，保留原色 |
| `Gamepad` | `coui_gamepad_filled` | |
| `CloudLink` | `coui_cloud_link_filled` | |
| `CloudData` | `coui_cloud_data_filled` | `com.heytap.cloud:drawable/ic_homepage_tab_cloud_space_checked` |
| `SettingDetail` | `coui_setting_detail_filled` | 多色 |
| `Phone` | `coui_phone_filled` | 多色 |
| `UserCircle` | `coui_user_circle_filled` | |
| `Google` | `coui_google_filled` | 多色/渐变 |
| `Certificate` | `coui_certificate_filled` | |
| `Lab` | `coui_lab_filled` | |
| `BookOpen` | `coui_book_open_filled` | |
| `Camera` | `coui_camera_filled` | `permission_list_ic_cameragroup_default` |
| `Microphone` | `coui_microphone_filled` | `permission_list_ic_microphonegroup_default` |
| `Photo` | `coui_photo_filled` | `permission_list_ic_readmediavisual_default` |
| `Users` | `coui_users_filled` | `permission_list_ic_contactsgroup_default` |
| `Call` | `coui_call_filled` | `permission_list_ic_callloggroup_default` |
| `GridSliders` | `coui_grid_sliders_filled` | `privacy_permission_manager_ic` |
| `SquareLock` | `coui_square_lock_filled` | `privacy_apps_lock_ic` |
| `SquareEyeClosed` | `coui_square_eye_closed_filled` | `privacy_apps_hidden_ic` |
| `SafeBox` | `coui_safe_box_filled` | `security_data_protect_kit_ic` |
| `PrivacyLock` | `coui_privacy_lock_filled` | `security_private_info_protect_ic` |
| `ShieldFlash` | `coui_shield_flash_filled` | `privacy_phone_manager_ic` |
| `WalletShield` | `coui_wallet_shield_filled` | `privacy_payment_protection_ic` |
| `ShieldKey` | `coui_shield_key_filled` | `privacy_common_information_password_ic` |
| `PhoneStack` | `coui_phone_stack_filled` | `multi_system_ic` |
| `ShieldCheck` | `coui_shield_check_filled` | `privacy_system_enhanced_protection_ic` |
| `AppUpdate` | `coui_app_update_filled` | 含原始白色前景；另有 `AppUpdate(color)` 只改背景 |
| `Album` | `coui_album_filled` | `main_tab_album_icon_selected` |
| `Delete` | `coui_delete_filled` | 参考 `com.android.systemui:drawable/ic_delete_final` 实心样式 |
| `Favorite` | `coui_favorite_filled` | |
| `Info` | `coui_info_filled` | |
| `Pen` | `coui_pen_filled` | 由原厂轮廓派生 |
| `ScanDocument` | `coui_scan_document_filled` | |
| `Share` | `coui_share_filled` | 由原厂轮廓派生 |
| `Send` | `coui_send_filled` | `aihome_input_ic_send_enable_normal` |
| `WandSpark` | `coui_wand_spark_filled` | `main_tab_creation_icon_selected` |
| `ListFilter` | `coui_list_filter_outlined` | Monochrome |
| `More` | `coui_more_outlined` | Monochrome |
| `Scan` | `coui_scan_outlined` | Monochrome；`com.oplus.account` 菜单扫描 |
| `Setting` | `coui_setting_filled` | 保留外轮廓与中心孔 |
| `Headset` | `coui_headset_filled` | 由 `Outlined.Headset` 填实耳罩派生 |

## 4. Outlined（30）

| 名称 | 资源 |
| --- | --- |
| `Search` | `coui_search_outlined`（`coui_search_view_icon_normal`） |
| `Camera` | `coui_camera_outlined`（`aihome_cui_input_bar_btn_camera`） |
| `Keyboard` | `coui_keyboard_outlined`（`aihome_input_ic_keyboard`） |
| `Back` | `coui_back_filled`（Monochrome） |
| `Next` | `coui_next_filled`（Monochrome） |
| `Popup` | `coui_pop_up_next`（Monochrome） |
| `Close` | `coui_close_filled`（Monochrome） |
| `Add` | `coui_add_filled`（Monochrome） |
| `AddCircle` | `coui_add_circle_outlined`（`ai_home_input_ic_tools_normal`） |
| `Send` | `coui_send_outlined`（实心圆外框 + 居中箭头） |
| `DarkLight` | `coui_dark_light_filled`（Monochrome） |
| `CloudLink` | `coui_cloud_link_outlined`（`ic_homepage_tab_cloud_service_normal`） |
| `CloudData` | `coui_cloud_data_outlined`（`ic_homepage_tab_cloud_space_normal`） |
| `Gift` | `coui_gift_outlined`（`ic_homepage_lucky_center`） |
| `Photo` | `coui_photo_outlined`（`main_tab_photo_icon_normal`） |
| `Album` | `coui_album_outlined`（`main_tab_album_icon_normal`） |
| `Delete` | `coui_delete_outlined` |
| `Favorite` | `coui_favorite_outlined` |
| `Info` | `coui_info_outlined` |
| `Pen` | `coui_pen_outlined` |
| `Share` | `coui_share_outlined` |
| `Translate` | `coui_translate_outlined` |
| `WandSpark` | `coui_wand_spark_outlined`（`main_tab_creation_icon_normal`） |
| `LayoutSpliced` | `coui_layout_spliced_outlined`（`main_ic_picked`） |
| `SliderCustom` | `coui_slider_custom_outlined`（`main_ic_segment_album_sort`） |
| `ListFilter` | `coui_list_filter_outlined`（Monochrome；`main_ic_segment_filter`） |
| `Headset` | `coui_headset_outlined`（`ic_homepage_online_service`） |
| `More` | `coui_more_outlined`（Monochrome；`coui_toolbar_menu_icon_more_normal`） |
| `Scan` | `coui_scan_outlined`（Monochrome；`ac_ic_menu_scan`） |
| `Setting` | `coui_setting_outlined`（`menu_ic_setting_normal`） |

## 5. Bitmap（7，`Painter`）

| 名称 | 资源 | 日夜 |
| --- | --- | --- |
| `IdCard` | `coui_id_card` | 仅 `drawable-nodpi` |
| `RadarSweep` | `coui_radar_sweep` | 仅 `drawable-nodpi` |
| `Family` | `coui_family` | 仅 `drawable-nodpi` |
| `Smile` | `coui_smile` | 仅 `drawable-nodpi` |
| `WarrantyCard` | `coui_warranty_card` | 仅 `drawable-nodpi` |
| `GamepadWired` | `coui_gamepad_wired` | 仅 `drawable-nodpi` |
| `DevicePair` | `coui_device_pair` | `drawable-nodpi` + `drawable-night-nodpi` |

## 6. 资源目录现状

- `drawable/`：各矢量日间版本，命名 `coui_<名称>_<filled|outlined>.xml`。当前 98 个 XML 中有 4 个没有对应公开入口：`coui_add_outlined`、`coui_back_outlined`、`coui_close_outlined`、`coui_dark_light_outlined`（`Monochrome` 走的是同名 `_filled` 资源）。新增 `Outlined` 图标前先确认能否直接复用这 4 个资源，不要重复造图形；是否给它们加公开入口由维护者决定，不自行补。
- `drawable-night/`：当前 21 个同名夜间矢量：`coui_accessibility_filled`、`coui_bell_filled`、`coui_book_open_filled`、`coui_certificate_filled`、`coui_flip_phone_filled`、`coui_foldable_filled`、`coui_gamepad_filled`、`coui_grid_filled`、`coui_health_filled`、`coui_lab_filled`、`coui_location_filled`、`coui_lock_filled`、`coui_multi_phone_filled`、`coui_panel_enter_right_filled`、`coui_phone_filled`、`coui_radar_filled`、`coui_satellite_bd_filled`、`coui_satellite_filled`、`coui_setting_detail_filled`、`coui_translate_outlined`、`coui_wallet_filled`。没有同名夜间文件时使用日间图形 + 夜间颜色资源（`Pages/GENERATION.md`）。
- `drawable-nodpi/`：7 个 PNG（见 §5）；`drawable-night-nodpi/` 当前仅 `coui_device_pair.png`。
- 颜色：`values/coui_icon_colors.xml` 与 `values-night/coui_icon_colors.xml`；色板值来源是 `coui/src/main/java/com/itos/coui/theme/CouiColor.kt`，生成器核对 XML 中蓝、绿、黄、红、橙的日夜值（`GENERATION.md`）。
- 多色/渐变：`AI`、`Google`、`SettingDetail`、`Phone` 保持原色（`icons.md`、`GENERATION.md`）。
- `AppUpdate` 必须恰有一个 `android:name="coui_background"` 的路径，`withAppUpdateBackground` 只替换该路径填充，其余路径（含白色前景）保持不变；违反时抛「应用升级图标必须包含一个背景路径」等异常（`CouiIconResources.kt`）。

## 7. 用法示例（`docs/components/icons.md`）

```kotlin
@Composable
fun VectorIconExample(
    modifier: Modifier = Modifier,
    contentDescription: String?,
) {
    Image(
        imageVector = CouiIcons.Filled.Wifi,
        contentDescription = contentDescription,
        modifier = modifier,
    )
}
```

```kotlin
Icon(
    imageVector = CouiIcons.Filled.Wifi,
    contentDescription = contentDescription,
    tint = Color.Unspecified,
)
```

```kotlin
@Composable
fun BitmapIconExample(
    modifier: Modifier = Modifier,
    contentDescription: String?,
) {
    Image(
        painter = CouiIcons.Bitmap.DevicePair,
        contentDescription = contentDescription,
        modifier = modifier.size(24.dp),
    )
}
```

```kotlin
val iconTint = if (CouiTheme.isDark) CouiColor.Night.BLUE else CouiColor.Day.BLUE
Icon(
    imageVector = CouiIcons.Filled.Wifi,
    contentDescription = contentDescription,
    tint = iconTint,
)
```

```kotlin
val badgeColor = if (CouiTheme.isDark) CouiColor.Night.GREEN else CouiColor.Day.GREEN
Icon(
    imageVector = CouiIcons.Filled.AppUpdate(color = badgeColor),
    contentDescription = contentDescription,
    tint = Color.Unspecified,
)
```

## 8. 新增图标的落点（制作时逐项对照）

**先决判断**：按 `coui-icons.md` §2.0 与 `docs/components/icons.md`「怎么选图标」确认图形该不该进 `coui-icons`——底栏配对、ColorOS 特有图形、组件内置操作图标才补；其他通用图标优先用 Lucide Icons，不新增入口。

1. 资源进 `drawable/`（必要时 `drawable-night/`），位图进 `drawable-nodpi/`（必要时 `drawable-night-nodpi/`，同名）。
2. `CouiIcons.kt`：单色共用图形放 `Monochrome` 并在 `Filled`/`Outlined` 转出；独立图形直接 `couiIconResource`；位图用 `couiBitmapResource`。
3. `coui/ICON_SOURCES.md` 按来源分组补一行（派生关系写明「由…派生」）。
4. `node Pages/generate.mjs` 重新生成预览，确认 `android:name` 为准确 PascalCase、后缀与样式一致。
5. 核对：名称/调用、日夜默认颜色、单色透明度、多色路径、可换色图层改色后其余图层不变、状态与方向（`icon-workflow.md`）。
