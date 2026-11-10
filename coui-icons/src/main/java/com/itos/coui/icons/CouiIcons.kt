package com.itos.coui.icons

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * COUI 的矢量与位图图标，来源见 ICON_SOURCES.md。
 * 使用 M3 Icon 保留图标原色时，需传入 Color.Unspecified。
 */
object CouiIcons {

    private object Monochrome {
        val Back: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_back_filled)

        val Next: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_next_filled)

        val Close: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_close_filled)

        val Add: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_add_filled)

        val DarkLight: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_dark_light_filled)

        val ListFilter: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_list_filter_outlined)

        val More: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_more_outlined)

        val Scan: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_scan_outlined)
    }

    object Filled {
        /** com.android.settings:drawable/coui_back_arrow_normal。 */
        val Back: ImageVector
            @Composable get() = Monochrome.Back

        /** com.android.settings:drawable/coui_btn_next_normal。 */
        val Next: ImageVector
            @Composable get() = Monochrome.Next

        /** com.android.settings:drawable/ic_close。 */
        val Close: ImageVector
            @Composable get() = Monochrome.Close

        /** com.coloros.alarmclock:drawable/color_floating_button_icon_add。 */
        val Add: ImageVector
            @Composable get() = Monochrome.Add

        /** com.android.settings:drawable/dark_theme。 */
        val DarkLight: ImageVector
            @Composable get() = Monochrome.DarkLight

        /** com.android.settings:drawable/settings_brightness_ic。 */
        val Sun: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_sun_filled)

        /** com.android.settings:drawable/settings_notify_center_ic。 */
        val Bell: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_bell_filled)

        /** com.android.settings:drawable/settings_personalise_ic。 */
        val MultiPhone: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_multi_phone_filled)

        /** com.android.settings:drawable/settings_sound_settings_ic。 */
        val Volume: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_volume_filled)

        val Plane: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_plane_filled)

        val Wifi: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_wifi_filled)

        val Bluetooth: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_bluetooth_filled)

        val Transfer: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_transfer_filled)

        val Satellite: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_satellite_filled)

        val SatelliteBD: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_satellite_bd_filled)

        val Radar: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_radar_filled)

        val Foldable: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_foldable_filled)

        val FlipPhone: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_flip_phone_filled)

        val Lock: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_lock_filled)

        val ShieldUser: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_shield_user_filled)

        val Sos: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_sos_filled)

        val Location: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_location_filled)

        val Wallet: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_wallet_filled)

        val Health: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_health_filled)

        val Grid: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_grid_filled)

        val Battery: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_battery_filled)

        val PanelEnterRight: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_panel_enter_right_filled)

        val PanelEnterLeft: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_panel_enter_left_filled)

        val Fan: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_fan_filled)

        val Rings: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_rings_filled)

        val Accessibility: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_accessibility_filled)

        val AI: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_ai_filled)

        val Gamepad: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_gamepad_filled)

        val CloudLink: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_cloud_link_filled)

        /** com.heytap.cloud:drawable/ic_homepage_tab_cloud_space_checked。 */
        val CloudData: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_cloud_data_filled)

        val SettingDetail: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_setting_detail_filled)

        val Phone: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_phone_filled)

        val UserCircle: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_user_circle_filled)

        val Google: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_google_filled)

        val Certificate: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_certificate_filled)

        val Lab: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_lab_filled)

        val BookOpen: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_book_open_filled)

        /** com.oplus.securitypermission:drawable/permission_list_ic_cameragroup_default。 */
        val Camera: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_camera_filled)

        /** com.oplus.securitypermission:drawable/permission_list_ic_microphonegroup_default。 */
        val Microphone: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_microphone_filled)

        /** com.oplus.securitypermission:drawable/permission_list_ic_readmediavisual_default。 */
        val Photo: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_photo_filled)

        /** com.oplus.securitypermission:drawable/permission_list_ic_contactsgroup_default。 */
        val Users: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_users_filled)

        /** com.oplus.securitypermission:drawable/permission_list_ic_callloggroup_default。 */
        val Call: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_call_filled)

        /** com.oplus.securitypermission:drawable/privacy_permission_manager_ic。 */
        val GridSliders: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_grid_sliders_filled)

        /** com.oplus.securitypermission:drawable/privacy_apps_lock_ic。 */
        val SquareLock: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_square_lock_filled)

        /** com.oplus.securitypermission:drawable/privacy_apps_hidden_ic。 */
        val SquareEyeClosed: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_square_eye_closed_filled)

        /** com.oplus.securitypermission:drawable/security_data_protect_kit_ic。 */
        val SafeBox: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_safe_box_filled)

        /** com.oplus.securitypermission:drawable/security_private_info_protect_ic。 */
        val PrivacyLock: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_privacy_lock_filled)

        /** com.oplus.securitypermission:drawable/privacy_phone_manager_ic。 */
        val ShieldFlash: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_shield_flash_filled)

        /** com.oplus.securitypermission:drawable/privacy_payment_protection_ic。 */
        val WalletShield: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_wallet_shield_filled)

        /** com.oplus.securitypermission:drawable/privacy_common_information_password_ic。 */
        val ShieldKey: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_shield_key_filled)

        /** com.oplus.securitypermission:drawable/multi_system_ic。 */
        val PhoneStack: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_phone_stack_filled)

        /** com.oplus.securitypermission:drawable/privacy_system_enhanced_protection_ic。 */
        val ShieldCheck: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_shield_check_filled)

        /** 当前日夜模式的应用升级图，含原始白色前景。 */
        val AppUpdate: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_app_update_filled)

        /** 生成仅更改背景色的应用升级图，并保留原始白色前景。 */
        @Composable
        fun AppUpdate(color: Color): ImageVector {
            val icon = AppUpdate
            return remember(icon, color) { icon.withAppUpdateBackground(color) }
        }

        /** com.coloros.gallery3d:drawable/main_tab_album_icon_selected。 */
        val Album: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_album_filled)

        /** com.coloros.gallery3d:drawable/main_tab_creation_icon_selected。 */
        val WandSpark: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_wand_spark_filled)

        val ListFilter: ImageVector
            @Composable get() = Monochrome.ListFilter

        val More: ImageVector
            @Composable get() = Monochrome.More

        val Scan: ImageVector
            @Composable get() = Monochrome.Scan

        /** 保留账号页设置图标的外轮廓与中心孔。 */
        val Setting: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_setting_filled)

        /** 填实云服务耳机图标的耳罩。 */
        val Headset: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_headset_filled)
    }

    object Outlined {
        /** com.android.settings:drawable/coui_search_view_icon_normal。 */
        val Search: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_search_outlined)

        val Back: ImageVector
            @Composable get() = Monochrome.Back

        val Next: ImageVector
            @Composable get() = Monochrome.Next

        val Close: ImageVector
            @Composable get() = Monochrome.Close

        val Add: ImageVector
            @Composable get() = Monochrome.Add

        val DarkLight: ImageVector
            @Composable get() = Monochrome.DarkLight

        /** com.heytap.cloud:drawable/ic_homepage_tab_cloud_service_normal。 */
        val CloudLink: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_cloud_link_outlined)

        /** com.heytap.cloud:drawable/ic_homepage_tab_cloud_space_normal。 */
        val CloudData: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_cloud_data_outlined)

        /** com.heytap.cloud:drawable/ic_homepage_lucky_center。 */
        val Gift: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_gift_outlined)

        /** com.coloros.gallery3d:drawable/main_tab_photo_icon_normal。 */
        val Photo: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_photo_outlined)

        /** com.coloros.gallery3d:drawable/main_tab_album_icon_normal。 */
        val Album: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_album_outlined)

        /** com.coloros.gallery3d:drawable/main_tab_creation_icon_normal。 */
        val WandSpark: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_wand_spark_outlined)

        /** com.coloros.gallery3d:drawable/main_ic_picked。 */
        val LayoutSpliced: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_layout_spliced_outlined)

        /** com.coloros.gallery3d:drawable/main_ic_segment_album_sort。 */
        val SliderCustom: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_slider_custom_outlined)

        /** com.coloros.gallery3d:drawable/main_ic_segment_filter。 */
        val ListFilter: ImageVector
            @Composable get() = Monochrome.ListFilter

        /** com.heytap.cloud:drawable/ic_homepage_online_service。 */
        val Headset: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_headset_outlined)

        /** com.heytap.cloud:drawable/coui_toolbar_menu_icon_more_normal。 */
        val More: ImageVector
            @Composable get() = Monochrome.More

        /** com.oplus.account:drawable/ac_ic_menu_scan。 */
        val Scan: ImageVector
            @Composable get() = Monochrome.Scan

        /** com.oplus.account:drawable/menu_ic_setting_normal。 */
        val Setting: ImageVector
            @Composable get() = couiIconResource(R.drawable.coui_setting_outlined)
    }

    /** 位图原件按 COUI 主题选择日夜资源。 */
    object Bitmap {
        val IdCard: Painter
            @Composable get() = couiBitmapResource(R.drawable.coui_id_card)

        val RadarSweep: Painter
            @Composable get() = couiBitmapResource(R.drawable.coui_radar_sweep)

        val Family: Painter
            @Composable get() = couiBitmapResource(R.drawable.coui_family)

        val Smile: Painter
            @Composable get() = couiBitmapResource(R.drawable.coui_smile)

        val WarrantyCard: Painter
            @Composable get() = couiBitmapResource(R.drawable.coui_warranty_card)

        val GamepadWired: Painter
            @Composable get() = couiBitmapResource(R.drawable.coui_gamepad_wired)

        val DevicePair: Painter
            @Composable get() = couiBitmapResource(R.drawable.coui_device_pair)
    }
}
