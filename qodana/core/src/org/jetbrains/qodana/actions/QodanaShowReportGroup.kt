// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.qodana.actions

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.DumbAware

class QodanaShowReportGroup : DefaultActionGroup(), DumbAware {

  override fun getChildren(e: AnActionEvent?): Array<AnAction> {
    val project = e?.project ?: return emptyArray()

    val actions = mutableListOf(
      ActionManager.getInstance().getAction("Qodana.RunQodanaAction"),
      ActionManager.getInstance().getAction("Qodana.AddQodanaToCiAction"),
      OpenQodanaCloudReportAction(),
      OpenQodanaCloudSettingsAction(),

      Separator.getInstance(),

      OpenQodanaCloudOpenInIdeReportAction(),
      ActionManager.getInstance().getAction("Qodana.OpenReportAction")
    ).apply {
      addAll(OpenLocalReportAction.getLocalReportsActions(project))
    }.filterNotNull()

    return actions.toTypedArray()
  }
}
