// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.macros

import com.intellij.ide.macro.Macro
import com.intellij.ide.macro.PathMacro
import com.intellij.ide.trustedProjects.TrustedProjects
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.components.service
import com.intellij.openapi.diagnostic.logger
import org.intellij.terraform.hcl.HCLBundle
import org.intellij.terraform.install.TfToolType
import org.intellij.terraform.runtime.TfProjectSettings

class TfExecutableMacro : Macro(), PathMacro {
  companion object {
    const val NAME: String = "TerraformExecPath"
  }

  override fun getName(): String {
    return NAME
  }

  override fun getDescription(): String {
    return HCLBundle.message("terraform.executable.macro.description")
  }

  @Throws(ExecutionCancelledException::class)
  override fun expand(dataContext: DataContext): String {
    val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return TfToolType.TERRAFORM.getBinaryName()
    // Consumers start a process with the expanded value. Global File Watchers do this in Safe Mode too.
    if (!TrustedProjects.isProjectTrusted(project)) {
      logger<TfExecutableMacro>().debug("Not expanding $NAME: project is not trusted")
      throw ExecutionCancelledException()
    }
    return project.service<TfProjectSettings>().toolPath.ifEmpty { TfToolType.TERRAFORM.getBinaryName() }
  }
}
