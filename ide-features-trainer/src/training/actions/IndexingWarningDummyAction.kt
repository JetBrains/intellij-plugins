/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationNamesInfo
import training.learn.LearnBundle

class IndexingWarningDummyAction : AnAction(LearnBundle.message("action.IndexingWarningDummyAction.description", ApplicationNamesInfo.getInstance().getFullProductName())) {
  init {
    this.templatePresentation.isEnabled = false
  }

  override fun actionPerformed(anActionEvent: AnActionEvent) {
    //do nothing
  }

}
