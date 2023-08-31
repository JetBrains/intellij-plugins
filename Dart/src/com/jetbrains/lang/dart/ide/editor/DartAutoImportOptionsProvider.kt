// Copyright 2000-2021 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.editor

import com.intellij.application.options.editor.AutoImportOptionsProvider
import com.intellij.codeInsight.CodeInsightSettings
import com.intellij.openapi.application.ApplicationBundle
import com.intellij.openapi.options.UiDslUnnamedConfigurable
import com.intellij.ui.dsl.builder.Panel
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.toNullableProperty
import com.intellij.ui.dsl.listCellRenderer.textListCellRenderer
import com.jetbrains.lang.dart.DartBundle
import com.jetbrains.lang.dart.ide.codeInsight.DartCodeInsightSettings

class DartAutoImportOptionsProvider : UiDslUnnamedConfigurable.Simple(), AutoImportOptionsProvider {

  override fun Panel.createContent() {
    group(DartBundle.message("dart.title")) {
      row(DartBundle.message("label.update.imports.on.paste")) {
        val values = mutableListOf(CodeInsightSettings.YES, CodeInsightSettings.ASK, CodeInsightSettings.NO)
        if (!values.contains(DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE)) {
          values.add(DartCodeInsightSettings.getInstance().ADD_IMPORTS_ON_PASTE)
        }

        comboBox(values,
                 textListCellRenderer {
                   when (it) {
                     CodeInsightSettings.YES -> ApplicationBundle.message("combobox.insert.imports.all")
                     CodeInsightSettings.NO -> ApplicationBundle.message("combobox.insert.imports.none")
                     CodeInsightSettings.ASK -> ApplicationBundle.message("combobox.insert.imports.ask")
                     else -> ""
                   }
                 })
          .bindItem(DartCodeInsightSettings.getInstance()::ADD_IMPORTS_ON_PASTE.toNullableProperty())
      }
    }
  }
}
