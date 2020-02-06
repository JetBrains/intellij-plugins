package com.intellij.grazie.ide.problem.suppress

import com.intellij.codeInspection.SuppressIntentionAction
import com.intellij.grazie.ide.GrazieInspection
import com.intellij.icons.AllIcons
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Iconable
import com.intellij.profile.codeInspection.ProjectInspectionProfileManager.Companion.getInstance
import com.intellij.psi.PsiElement
import javax.swing.Icon

abstract class GrazieDisableIntention: SuppressIntentionAction(), Iconable {
  override fun getIcon(flags: Int): Icon = AllIcons.Actions.Cancel

  override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
    val profileManager = getInstance(project)
    val currentProfile = profileManager.currentProfile
    val toolWrapper = currentProfile.getInspectionTool(GrazieInspection.id, project)
    return toolWrapper != null
  }
}