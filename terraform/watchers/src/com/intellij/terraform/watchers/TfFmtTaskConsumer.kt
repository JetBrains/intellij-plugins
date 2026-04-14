// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.terraform.watchers

import com.intellij.ide.macro.FilePathMacro
import com.intellij.plugins.watcher.config.BackgroundTaskConsumer
import com.intellij.plugins.watcher.model.TaskOptions
import com.intellij.psi.PsiFile
import org.intellij.terraform.config.TerraformFileType
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.macros.TfExecutableMacro

class TfFmtTaskConsumer : BackgroundTaskConsumer() {
  override fun isAvailable(file: PsiFile): Boolean {
    return false
  }

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

  fun createDefaultOptions(): TaskOptions {
    val options = TaskOptions()
    options.output = ""
    options.isImmediateSync = false
    options.exitCodeBehavior = TaskOptions.ExitCodeBehavior.ERROR
    options.fileExtension = TerraformFileType.defaultExtension
    return options
  }
}