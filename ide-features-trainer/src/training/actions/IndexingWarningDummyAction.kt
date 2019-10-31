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
