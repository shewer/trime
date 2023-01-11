package com.osfans.trime.ui.main

import android.content.Context
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import com.osfans.trime.R
import com.osfans.trime.core.Rime
import com.osfans.trime.data.AppPrefs
import com.osfans.trime.data.sound.SoundTheme
import com.osfans.trime.data.sound.SoundThemeManager
import com.osfans.trime.data.theme.Config
import com.osfans.trime.data.theme.ThemeManager
import com.osfans.trime.ime.core.Trime
import com.osfans.trime.ui.components.CoroutineChoiceDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

suspend fun Context.themePicker(
    @StyleRes themeResId: Int = 0
): AlertDialog {
    return CoroutineChoiceDialog(this, themeResId).apply {
        title = getString(R.string.looks__selected_theme_title)
        initDispatcher = Dispatchers.IO
        onInit {
            items = ThemeManager.getAllThemes()
                .map { it.substringBeforeLast('.') }
                .toTypedArray()
            val current = ThemeManager.getActiveTheme().substringBeforeLast('.')
            checkedItem = items.indexOf(current)
        }
        postiveDispatcher = Dispatchers.Default
        onOKButton {
            with(items[checkedItem].toString()) {
                ThemeManager.switchTheme(if (this == "trime") this else "$this.trime")
                Config.get().init()
            }
            launch {
                Trime.getServiceOrNull()?.initKeyboard()
            }
        }
    }.create()
}

suspend fun Context.colorPicker(
    @StyleRes themeResId: Int = 0
): AlertDialog {
    val prefs by lazy { AppPrefs.defaultInstance() }
    return CoroutineChoiceDialog(this, themeResId).apply {
        title = getString(R.string.looks__selected_color_title)
        initDispatcher = Dispatchers.Default
        onInit {
            val all = Config.get().presetColorSchemes
            items = all.map { it.second }.toTypedArray()
            val current = prefs.themeAndColor.selectedColor
            val schemeIds = all.map { it.first }
            checkedItem = schemeIds.indexOf(current)
        }
        postiveDispatcher = Dispatchers.Default
        onOKButton {
            val all = Config.get().presetColorSchemes
            val schemeIds = all.map { it.first }
            prefs.themeAndColor.selectedColor = schemeIds[checkedItem]
            launch {
                Trime.getServiceOrNull()?.initKeyboard() // 立刻重初始化键盘生效
            }
        }
    }.create()
}

suspend fun Context.schemaPicker(
    @StyleRes themeResId: Int = 0
): AlertDialog {
    return CoroutineChoiceDialog(this, themeResId).apply {
        title = getString(R.string.pref_select_schemas)
        initDispatcher = Dispatchers.IO
        onInit {
            items = Rime.get_available_schema_list()
                ?.map { it["schema_id"]!! }
                ?.toTypedArray() ?: arrayOf()
            val checked = Rime.get_selected_schema_list()
                ?.map { it["schema_id"]!! }
                ?.toTypedArray() ?: arrayOf()
            checkedItems = items.map { checked.contains(it) }.toBooleanArray()
        }
        postiveDispatcher = Dispatchers.Default
        onOKButton {
            Rime.select_schemas(
                items.filterIndexed { index, _ ->
                    checkedItems[index]
                }.map { it.toString() }.toTypedArray()
            )
            Rime.destroy()
            Rime.get(true)
        }
    }.create()
}

fun Context.soundPicker(
    @StyleRes themeResId: Int = 0
): AlertDialog {
    val all = SoundThemeManager.getAllSoundThemes().mapNotNull(SoundTheme::name)
    val current = SoundThemeManager.getActiveSoundTheme().getOrNull()?.name ?: ""
    var checked = all.indexOf(current)
    return AlertDialog.Builder(this, themeResId)
        .setTitle(R.string.keyboard__key_sound_package_title)
        .setSingleChoiceItems(
            all.toTypedArray(),
            checked
        ) { _, id -> checked = id }
        .setPositiveButton(android.R.string.ok) { _, _ ->
            SoundThemeManager.switchSound(all[checked])
        }
        .setNegativeButton(android.R.string.cancel, null)
        .create()
}
