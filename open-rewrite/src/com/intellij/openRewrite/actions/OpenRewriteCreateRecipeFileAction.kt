package com.intellij.openRewrite.actions

import com.intellij.ide.actions.CreateFileFromTemplateAction
import com.intellij.ide.actions.CreateFileFromTemplateDialog
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.openRewrite.OpenRewriteBundle
import com.intellij.openRewrite.OpenRewriteIcons
import com.intellij.openRewrite.run.OpenRewriteExternalSystemBridge
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiDirectory

private const val OPEN_REWRITE_RECIPE_TEMPLATE = "OpenRewrite Recipe"

internal class OpenRewriteCreateRecipeFileAction : CreateFileFromTemplateAction(), DumbAware {
  override fun buildDialog(project: Project, directory: PsiDirectory, builder: CreateFileFromTemplateDialog.Builder) {
    builder
      .setTitle(OpenRewriteBundle.message("open.rewrite.new.recipe.file"))
      .addKind(OpenRewriteBundle.message("open.rewrite.recipe"),
               OpenRewriteIcons.OpenRewrite,
               OPEN_REWRITE_RECIPE_TEMPLATE)
  }

  override fun getActionName(directory: PsiDirectory?, newName: String, templateName: String?): String =
    OpenRewriteBundle.message("action.OpenRewrite.Create.Recipe.File.text")

  override fun isAvailable(dataContext: DataContext): Boolean {
    if (!super.isAvailable(dataContext)) return false

    val project = CommonDataKeys.PROJECT.getData(dataContext) ?: return false
    val view = LangDataKeys.IDE_VIEW.getData(dataContext) ?: return false
    return view.directories.any { directory ->
      val virtualFile = directory.virtualFile
      OpenRewriteExternalSystemBridge.EP_NAME.extensionList.any { bridge -> bridge.hasBuildFile(virtualFile, project) } ||
      ScratchUtil.isScratch(directory.virtualFile)
    }
  }
}