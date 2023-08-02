package com.intellij.dts.settings

import com.intellij.dts.DtsBundle
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.util.NlsSafe
import com.intellij.ui.dsl.builder.*
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.PropertyKey

class DtsSettingsConfigurable(private val project: Project) : BoundConfigurable(DtsBundle.message("settings.name")) {
    private val state: DtsSettings.State = DtsSettings.of(project).state

    private fun bundle(key: @PropertyKey(resourceBundle = DtsBundle.BUNDLE) String, suffix: @NlsSafe String = ""): @Nls String {
        return DtsBundle.message(key) + suffix
    }

    override fun createPanel(): DialogPanel = panel {
        group(bundle("settings.zephyr.group")) {
            row(bundle("settings.zephyr.path",  ":")) {
                textFieldWithBrowseButton().columns(COLUMNS_LARGE).bindText(state::zephyrRoot)
            }
            row(bundle("settings.zephyr.arch",  ":")) {
                textField().bindText(state::zephyrArch)
            }
            row(bundle("settings.zephyr.board",  ":")) {
                textField().bindText(state::zephyrBoard)
            }
        }
    }

    override fun apply() {
        DtsSettings.of(project).update { super.apply() }
    }
}