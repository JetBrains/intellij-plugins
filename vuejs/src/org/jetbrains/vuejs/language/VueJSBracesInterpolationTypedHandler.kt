package org.jetbrains.vuejs.language

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.lang.javascript.JSInjectionBracesUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import org.jetbrains.vuejs.VueFileType

/**
 * @author Irina.Chernushina on 7/24/2017.
 */
class VueJSBracesInterpolationTypedHandler : TypedHandlerDelegate() {
  private val myBracesCompleter: JSInjectionBracesUtil.InterpolationBracesCompleter = JSInjectionBracesUtil.InterpolationBracesCompleter(
    VueInjector.BRACES_FACTORY)

  override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): TypedHandlerDelegate.Result {
    if (!org.jetbrains.vuejs.index.hasVue(project) ||
        fileType != VueFileType.INSTANCE && fileType != HtmlFileType.INSTANCE) return Result.CONTINUE
    return myBracesCompleter.beforeCharTyped(c, project, editor, file)
  }
}