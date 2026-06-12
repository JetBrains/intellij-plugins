package com.intellij.openRewrite.run.before

import com.intellij.execution.lineMarker.ExecutorAction
import com.intellij.execution.lineMarker.RunLineMarkerContributor
import com.intellij.icons.AllIcons
import com.intellij.ide.scratch.ScratchUtil
import com.intellij.openRewrite.OPEN_REWRITE_PACKAGE_PREFIX
import com.intellij.openRewrite.recipe.OpenRewriteRecipeService
import com.intellij.openRewrite.run.isRecipe
import com.intellij.psi.PsiClass
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiIdentifier
import com.intellij.psi.PsiJavaFile

internal class OpenRewriteScratchClassRunLineMarkerProvider : RunLineMarkerContributor() {
  override fun isDumbAware(): Boolean = true

  override fun getInfo(element: PsiElement): Info? {
    if (element !is PsiIdentifier) return null
    val parent = element.parent ?: return null
    if (parent !is PsiClass) return null
    val virtualFile = element.containingFile?.virtualFile ?: return null
    if (!ScratchUtil.isScratch(virtualFile)) return null
    if (!isRecipe(parent)) {
      val imports = (parent.containingFile as? PsiJavaFile)?.importList ?: return null
      for (importStatement in imports.importStatements) {
        if (importStatement.qualifiedName?.startsWith(OPEN_REWRITE_PACKAGE_PREFIX) == true) {
          OpenRewriteRecipeService.getInstance(element.project).reload()
          break
        }
      }
      return null
    }
    return Info(AllIcons.RunConfigurations.TestState.Run, ExecutorAction.getActions(), null)
  }
}