// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.watchers.consumers

import com.intellij.ide.macro.FilePathMacro
import com.intellij.plugins.watcher.model.TaskOptions
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.macros.TfExecutableMacro

class TfFmtTaskConsumer : TfToolTaskConsumer() {
  override fun getOptionsTemplate(): TaskOptions {
    val options = createDefaultOptions()
    val filePath = FilePathMacro().name
    options.name = "terraform fmt"
    options.description = HCLBundle.message("label.runs.terraform.fmt")
    options.program = "$${TfExecutableMacro.NAME}$"
    options.arguments = "fmt $$filePath$"
    options.output = "$$filePath$"
    return options
  }
}