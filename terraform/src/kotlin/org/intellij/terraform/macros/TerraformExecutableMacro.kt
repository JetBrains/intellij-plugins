// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.macros

import com.intellij.ide.macro.Macro
import com.intellij.ide.macro.PathMacro
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.runtime.TerraformToolProjectSettings

class TerraformExecutableMacro : Macro(), PathMacro {
  companion object {
    const val NAME = "TerraformExecPath"
  }

  override fun getName(): String {
    return NAME
  }

  override fun getDescription(): String {
    return HCLBundle.message("terraform.executable.macro.description")
  }

  @Throws(ExecutionCancelledException::class)
  override fun expand(dataContext: DataContext): String? {
    val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return TerraformToolProjectSettings.getDefaultTerraformPath()
    return TerraformToolProjectSettings.getInstance(project).terraformPath
  }
}
