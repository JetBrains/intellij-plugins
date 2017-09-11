package org.jetbrains.vuejs.language

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.ide.highlighter.HtmlFileType
import com.intellij.lang.javascript.JSInjectionBracesUtil
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Pair
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlElement
import com.intellij.psi.xml.XmlTag
import com.intellij.util.NullableFunction
import org.jetbrains.vuejs.VueFileType

/**
 * @author Irina.Chernushina on 7/24/2017.
 */
private val myBracesCompleter: JSInjectionBracesUtil.InterpolationBracesCompleter =
  VueInterpolationBracesCompleter(VueInjector.BRACES_FACTORY)

class VueJSBracesInterpolationTypedHandler : TypedHandlerDelegate() {
  override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): TypedHandlerDelegate.Result {
    if (!org.jetbrains.vuejs.index.hasVue(project) ||
        fileType != VueFileType.INSTANCE && fileType != HtmlFileType.INSTANCE) return Result.CONTINUE
    return myBracesCompleter.beforeCharTyped(c, project, editor, file)
  }
}

private val myExcludedTopLevelTags = arrayOf("script", "style")
class VueInterpolationBracesCompleter(factory: NullableFunction<PsiElement, Pair<String, String>>) :
  JSInjectionBracesUtil.InterpolationBracesCompleter(factory) {

  override fun checkTypingContext(editor: Editor, file: PsiFile): Boolean {
    val atCaret = getContextElement(editor, file)
    val tag = atCaret as? XmlTag ?: atCaret?.parent as? XmlTag
    return atCaret == null || atCaret is XmlElement && tag?.name !in myExcludedTopLevelTags
  }
}