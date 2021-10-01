package com.jetbrains.lang.dart.ide.editor

import com.intellij.application.options.CodeCompletionOptionsCustomSection
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindSelected
import com.jetbrains.lang.dart.DartBundle
import com.jetbrains.lang.dart.ide.codeInsight.DartCodeInsightSettings

class DartSmartKeysConfigurable : UiDslUnnamedConfigurable.Simple(), Configurable, CodeCompletionOptionsCustomSection {

  override fun getDisplayName(): String {
    return DartBundle.message("dart.title")
  }

  override fun Panel.createContent() {
    val settings = DartCodeInsightSettings.getInstance()
    group(displayName) {
      row {
        checkBox(DartBundle.message("dart.smartKeys.insertDefaultArgValues.text"))
          .bindSelected(settings::INSERT_DEFAULT_ARG_VALUES)
      }
    }
  }
}
