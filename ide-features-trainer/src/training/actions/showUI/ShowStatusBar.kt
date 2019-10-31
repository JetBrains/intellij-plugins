/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.actions.showUI

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import training.util.LearnUiUtil

class ShowStatusBar : AnAction() {

  override fun actionPerformed(anActionEvent: AnActionEvent) {
    LearnUiUtil.getInstance().highlightIdeComponent(LearnUiUtil.IdeComponent.STATUS_BAR, anActionEvent.project)
  }
}
