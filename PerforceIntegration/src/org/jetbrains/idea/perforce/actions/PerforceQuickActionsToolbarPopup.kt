package org.jetbrains.idea.perforce.actions

import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.vcs.actions.VcsQuickActionsToolbarPopup
import com.intellij.util.IconUtil
import org.jetbrains.idea.perforce.PerforceBundle
import org.jetbrains.idea.perforce.application.PerforceManager
import org.jetbrains.idea.perforce.application.PerforceVcs
import javax.swing.JComponent

class PerforceQuickActionsToolbarPopup : VcsQuickActionsToolbarPopup() {

  override fun getName(project: Project): String {
    return PerforceVcs.getInstance(project).name
  }

  override fun createCustomComponent(presentation: Presentation, place: String): JComponent {
    return MyActionButtonWithText(this, presentation.apply { text = "" }, place)
  }


  override fun update(e: AnActionEvent) {

    val project = e.project
    val presentation = e.presentation

    if (!updateVcs(project, e)) return
    val repo = PerforceManager.getInstance(project).isActive

    presentation.icon = if (repo) {
      IconUtil.toSize(AllIcons.Actions.More, ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE.width,
                      ActionToolbar.DEFAULT_MINIMUM_BUTTON_SIZE.height)
    }
    else {
      AllIcons.Vcs.BranchNode
    }

    presentation.text = if (repo) {
      ""
    }
    else {
      PerforceBundle.message("action.Vcs.Toolbar.ShowMoreActions.text") + " "
    }
  }
}