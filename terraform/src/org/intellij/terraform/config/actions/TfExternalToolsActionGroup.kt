// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.actions

import com.intellij.ide.actions.NonTrivialActionGroup
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.project.DumbAware
import org.intellij.terraform.config.util.getApplicableToolType
import org.intellij.terraform.install.TfToolType

internal sealed class TfExternalToolsActionGroup(private val toolType: TfToolType) : NonTrivialActionGroup(), DumbAware {
  override fun update(e: AnActionEvent) {
    val project = e.project
    val file = e.getData(CommonDataKeys.VIRTUAL_FILE)
    e.presentation.isEnabledAndVisible = project != null &&
                                         file != null &&
                                         isTfOrTofuAvailable(file) &&
                                         toolType == getApplicableToolType(file)
  }
}

internal class TfToolsActionGroup : TfExternalToolsActionGroup(TfToolType.TERRAFORM)

internal class TofuToolsActionGroup : TfExternalToolsActionGroup(TfToolType.OPENTOFU)
