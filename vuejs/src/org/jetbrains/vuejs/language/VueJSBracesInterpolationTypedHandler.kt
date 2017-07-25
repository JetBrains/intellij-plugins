package org.jetbrains.vuejs.language

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.lang.javascript.JSInjectionBracesUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

/**
 * @author Irina.Chernushina on 7/24/2017.
 */
class VueJSBracesInterpolationTypedHandler : TypedHandlerDelegate() {
  private val myBracesCompleter: JSInjectionBracesUtil.InterpolationBracesCompleter = JSInjectionBracesUtil.InterpolationBracesCompleter(
    VueInjector.BRACES_FACTORY)

  override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): TypedHandlerDelegate.Result {
    if (!org.jetbrains.vuejs.index.hasVue(project)) return Result.CONTINUE
    return myBracesCompleter.beforeCharTyped(c, project, editor, file)
  }
}