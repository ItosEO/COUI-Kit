package com.itos.coui.demo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.itos.coui.icons.CouiIcons
import com.itos.coui.layout.CouiPageDefaults
import com.itos.coui.text.CouiTextKind
import com.itos.coui.text.Text
import com.itos.coui.textfield.CouiTextFieldDefaults
import com.itos.coui.textfield.TextField

/** Demo 自备的错误色：组件不内置校验色，宿主按自己的主题覆盖 label 与 cursor。 */
private val ErrorRed = Color(0xFFD32F2F)

/**
 * 输入框只接当前值或宿主状态；焦点、光标和图标槽的点击都由组件自己装配。
 * 示例按提示文案的组合排列：无标签只有占位、只有标签、标签加占位、标签当占位、两者都不传。
 */
@Composable
internal fun TextFieldSample(dark: Boolean) {
    val colors = CouiTextFieldDefaults.colors(dark)
    val focus = LocalFocusManager.current
    var location by rememberSaveable { mutableStateOf("") }
    var message by rememberSaveable { mutableStateOf("") }
    var recipient by rememberSaveable { mutableStateOf("") }
    var address by rememberSaveable { mutableStateOf("") }
    var topic by rememberSaveable { mutableStateOf("") }
    var bare by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    // 初始就有内容：占位提示在首帧就不会出现。
    var filled by rememberSaveable { mutableStateOf("ColorOS 17") }
    val emailPattern = remember { Regex("[a-zA-Z0-9._-]+@[a-z]+\\.[a-z]+") }
    val emailInvalid = email.isNotEmpty() && !emailPattern.matches(email)
    // 多行示例改用宿主状态重载：编辑事务与光标由 TextFieldState 持有。
    val note = rememberTextFieldState()
    Column(Modifier.fillMaxWidth().padding(top = CouiPageDefaults.groupSpacing),
        verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // 无标签，只有占位提示：空值显示，有内容消失。
        TextField(modifier = Modifier.fillMaxWidth().padding(horizontal = CouiPageDefaults.horizontalMargin),
            value = location, onValueChange = { location = it },
            placeholder = stringResource(R.string.text_field_location),
            trailing = {
                Icon(CouiIcons.Filled.Location, null, Modifier.size(CouiPageDefaults.iconSize),
                    tint = colors.text)
            })
        // 同上但初始就有值：占位提示不会出现，卡片只剩内容。
        TextField(modifier = Modifier.fillMaxWidth().padding(horizontal = CouiPageDefaults.horizontalMargin),
            value = filled, onValueChange = { filled = it },
            placeholder = stringResource(R.string.text_field_hint))
        // 只有标签：空值时标签占据输入位当大字提示，有内容后上浮缩小。
        TextField(modifier = Modifier.fillMaxWidth().padding(horizontal = CouiPageDefaults.horizontalMargin),
            value = recipient, onValueChange = { recipient = it },
            label = stringResource(R.string.text_field_recipient))
        // 标签加占位提示：标签常驻卡片顶部，输入区显示占位提示，有内容后提示消失、标签不动。
        TextField(modifier = Modifier.fillMaxWidth().padding(horizontal = CouiPageDefaults.horizontalMargin),
            value = address, onValueChange = { address = it },
            label = stringResource(R.string.text_field_address),
            placeholder = stringResource(R.string.text_field_hint))
        // 标签当占位提示：空值占输入位，有内容后整段隐藏而不上浮。
        TextField(modifier = Modifier.fillMaxWidth().padding(horizontal = CouiPageDefaults.horizontalMargin),
            value = topic, onValueChange = { topic = it },
            label = stringResource(R.string.text_field_topic), useLabelAsPlaceholder = true)
        // 两个提示都不传：空卡片只由图标槽表达用途，适合已有外部标题或预填值的场景。
        TextField(modifier = Modifier.fillMaxWidth().padding(horizontal = CouiPageDefaults.horizontalMargin),
            value = bare, onValueChange = { bare = it },
            leading = {
                Icon(CouiIcons.Filled.BookOpen, null, Modifier.size(CouiPageDefaults.iconSize),
                    tint = colors.text)
            })
        // 头部图标槽 + 可点击的尾部清空按钮；槽位控件先消费点击，不会一并聚焦输入框。
        TextField(modifier = Modifier.fillMaxWidth().padding(horizontal = CouiPageDefaults.horizontalMargin),
            value = message, onValueChange = { message = it },
            placeholder = stringResource(R.string.text_field_message),
            leading = {
                Icon(CouiIcons.Filled.BookOpen, null, Modifier.size(CouiPageDefaults.iconSize),
                    tint = colors.text)
            },
            trailing = {
                if (message.isNotEmpty()) Box(Modifier.size(CouiPageDefaults.iconSize)
                    .clickable(role = Role.Button) { message = "" }, contentAlignment = Alignment.Center) {
                    Icon(CouiIcons.Filled.Close, stringResource(R.string.text_field_clear), Modifier.size(20.dp),
                        tint = colors.placeholder)
                }
            })
        // 密码：掩码由 VisualTransformation 提供，尾部按钮切换明文；Done 只收起键盘，不提交业务。
        TextField(modifier = Modifier.fillMaxWidth().padding(horizontal = CouiPageDefaults.horizontalMargin),
            value = password, onValueChange = { password = it },
            label = stringResource(R.string.text_field_password), useLabelAsPlaceholder = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
            trailing = {
                Box(Modifier.size(CouiPageDefaults.iconSize)
                    .clickable(role = Role.Button) { passwordVisible = !passwordVisible },
                    contentAlignment = Alignment.Center) {
                    Icon(if (passwordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                        stringResource(if (passwordVisible) R.string.text_field_password_hide
                            else R.string.text_field_password_show),
                        Modifier.size(20.dp), tint = colors.placeholder)
                }
            })
        // 校验错误：宿主覆盖标签与光标色，错误文字由页面自己排版。
        Column {
            TextField(modifier = Modifier.fillMaxWidth().padding(horizontal = CouiPageDefaults.horizontalMargin),
                value = email, onValueChange = { email = it },
                label = stringResource(R.string.text_field_email),
                colors = if (emailInvalid) colors.copy(label = ErrorRed, cursor = ErrorRed) else colors,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }))
            if (emailInvalid) Text(text = stringResource(R.string.text_field_email_error),
                kind = CouiTextKind.GroupFooter, color = ErrorRed,
                modifier = Modifier.fillMaxWidth().padding(start = CouiPageDefaults.rowPadding + 4.dp, top = 4.dp))
        }
        // 多行：首行到 minLines 撑高，超过 maxLines 后内部滚动。
        TextField(modifier = Modifier.fillMaxWidth().padding(horizontal = CouiPageDefaults.horizontalMargin),
            state = note, placeholder = stringResource(R.string.text_field_note),
            singleLine = false, minLines = 3, maxLines = 5)
        // 只读仍可聚焦、选择与复制；禁用停止输入并改用禁用色。
        TextField(modifier = Modifier.fillMaxWidth().padding(horizontal = CouiPageDefaults.horizontalMargin),
            value = stringResource(R.string.text_field_sample_value), onValueChange = {},
            readOnly = true)
        TextField(modifier = Modifier.fillMaxWidth().padding(horizontal = CouiPageDefaults.horizontalMargin),
            value = stringResource(R.string.text_field_sample_value), onValueChange = {},
            enabled = false)
    }
}
