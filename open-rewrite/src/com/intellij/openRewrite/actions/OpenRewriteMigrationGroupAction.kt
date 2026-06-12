package com.intellij.openRewrite.actions

import com.intellij.ide.util.PropertiesComponent
import com.intellij.openRewrite.OpenRewriteRecipeLibraryContributor
import com.intellij.openRewrite.run.OpenRewriteExternalSystemBridge
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.externalSystem.autoimport.ExternalSystemProjectNotificationAware
import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.IndexNotReadyException
import com.intellij.openapi.roots.ProjectFileIndex
import com.intellij.psi.PsiFile

internal const val HIDE_MIGRATION_PROPERTY_KEY = "open.rewrite.hide.migration"

internal abstract class OpenRewriteMigrationGroupAction : DumbAwareAction() {
  override fun getActionUpdateThread(): ActionUpdateThread = ActionUpdateThread.BGT

  override fun update(e: AnActionEvent) {
    if (e.place == ActionPlaces.MAIN_MENU) {
      e.presentation.isEnabledAndVisible = false
      return
    }

    val project = e.project
    if (project == null) {
      e.presentation.isEnabledAndVisible = false
      return
    }

    val isPopup = ActionPlaces.isPopupPlace(e.place)
    val hide = PropertiesComponent.getInstance().getBoolean(HIDE_MIGRATION_PROPERTY_KEY, false)
    if (hide && !isPopup) {
      e.presentation.isEnabledAndVisible = false
      return
    }

    val importRequired = ExternalSystemProjectNotificationAware.getInstance(project).isNotificationVisible()
    if (importRequired && !isPopup) {
      e.presentation.isEnabledAndVisible = false
      return
    }

    val updateAvailable = isUpdateAvailable(e.getData(PlatformDataKeys.PSI_FILE), isPopup)
    e.presentation.isEnabled = !importRequired && updateAvailable
    e.presentation.isVisible = updateAvailable
  }

  private fun isUpdateAvailable(psiFile: PsiFile?, isPopup: Boolean): Boolean {
    if (psiFile == null) return false

    val virtualFile = psiFile.virtualFile ?: return false
    try {
      if (ProjectFileIndex.getInstance(psiFile.project).isInLibrary(virtualFile)) {
        return false
      }
    }
    catch (_: IndexNotReadyException) {
      return false
    }
    val module = ModuleUtil.findModuleForPsiElement(psiFile) ?: return false
    val bridge = OpenRewriteExternalSystemBridge.EP_NAME.findFirstSafe { it.isBuildFile(module, psiFile) } ?: return false
    return isUpdateAvailable(bridge.adjustModule(module), isPopup)
  }

  private fun isUpdateAvailable(module: Module, filterByLibrary: Boolean): Boolean {
    return OpenRewriteRecipeLibraryContributor.EP_NAME.findFirstSafe {
      it.hasLibrary(module) && (filterByLibrary || it.isUpdateAvailable(module))
    } != null
  }
}