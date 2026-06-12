package com.intellij.openRewrite.actions

import com.intellij.icons.AllIcons
import com.intellij.ide.util.PropertiesComponent
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.externalSystem.util.ExternalSystemBundle

internal class OpenRewriteHideMigrationAction : OpenRewriteMigrationGroupAction() {
  init {
    templatePresentation.text = ExternalSystemBundle.message("external.system.reload.notification.action.hide.text")
    templatePresentation.icon = AllIcons.Actions.Close
    templatePresentation.hoveredIcon = AllIcons.Actions.CloseHovered
  }

  override fun actionPerformed(e: AnActionEvent) {
    PropertiesComponent.getInstance().setValue(HIDE_MIGRATION_PROPERTY_KEY, true, false)
  }
}