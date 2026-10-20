// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.flex.build

import com.intellij.lang.javascript.flex.FlexBundle
import com.intellij.openapi.options.BoundSearchableConfigurable
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.RawCommandLineEditor
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.RightGap
import com.intellij.ui.dsl.builder.bindIntText
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.builder.toMutableProperty

class FlexCompilerProjectConfigurable(project: Project) :
  BoundSearchableConfigurable(FlexBundle.message("configurable.FlexCompilerProjectConfigurable.display.name"),
                              "reference.projectsettings.compiler.flex",
                              "flex.compiler") {

  private val config = FlexCompilerProjectConfiguration.getInstance(project)

  override fun createPanel(): DialogPanel {
    return panel {
      buttonsGroup(FlexBundle.message("flex.compiler.compile.with")) {
        row {
          radioButton(FlexBundle.message("flex.compiler.built.in.compiler.shell"))
            .bindSelected(config::USE_BUILT_IN_COMPILER)
        }
        row {
          radioButton(FlexBundle.message("flex.compiler.mxmlc.compc"))
            .bindSelected(config::USE_MXMLC_COMPC)
        }
      }
      row {
        checkBox(FlexBundle.message("flex.compiler.prefer.asc.2.0"))
          .bindSelected(config::PREFER_ASC_20)
      }
      row(FlexBundle.message("flex.compiler.heap.size")) {
        intTextField(range = 1..Int.MAX_VALUE)
          .bindIntText(config::HEAP_SIZE_MB)
          .gap(RightGap.SMALL)
        label(FlexBundle.message("flex.compiler.heap.size.mb"))
      }
      row(FlexBundle.message("flex.compiler.vm.options")) {
        cell(RawCommandLineEditor())
          .align(AlignX.FILL)
          .bind({ it.text.trim() }, { editor, text -> editor.text = text }, config::VM_OPTIONS.toMutableProperty())
      }
    }
  }
}
