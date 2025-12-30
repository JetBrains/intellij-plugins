// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.editor

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate
import com.intellij.lang.javascript.JSInjectionBracesUtil.InterpolationBracesCompleter
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import com.intellij.psi.xml.XmlElement
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.html.lexer.Angular2HtmlTokenTypes

internal class Angular2BracesInterpolationTypedHandler : TypedHandlerDelegate() {
  override fun beforeCharTyped(c: Char,
                               project: Project,
                               editor: Editor,
                               file: PsiFile,
                               fileType: FileType): Result {
    val language = file.language
    return if (language.isKindOf(Angular2HtmlLanguage)) {
      Angular2BracesCompleter.beforeCharTyped(c, project, editor, file)
    }
    else Result.CONTINUE
  }
}

private object Angular2BracesCompleter : InterpolationBracesCompleter(Angular2Injector.Holder.BRACES_FACTORY) {
  override fun checkTypingContext(editor: Editor, file: PsiFile): Boolean {
    val atCaret = getContextElement(editor, file)
    return (atCaret == null
            || atCaret is XmlElement
            || atCaret.node.elementType === Angular2HtmlTokenTypes.INTERPOLATION_END)
  }
}