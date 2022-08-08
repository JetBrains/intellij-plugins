// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.editor

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
import com.intellij.xml.util.HtmlUtil.SCRIPT_TAG_NAME
import com.intellij.xml.util.HtmlUtil.STYLE_TAG_NAME
import org.jetbrains.vuejs.context.isVueContext
import org.jetbrains.vuejs.lang.html.VueFileType

private val myBracesCompleter: JSInjectionBracesUtil.InterpolationBracesCompleter =
  VueInterpolationBracesCompleter(VueInjector.Holder.BRACES_FACTORY)

class VueJSBracesInterpolationTypedHandler : TypedHandlerDelegate() {
  override fun beforeCharTyped(c: Char, project: Project, editor: Editor, file: PsiFile, fileType: FileType): Result {
    if (fileType != VueFileType.INSTANCE
        && fileType != HtmlFileType.INSTANCE
        || !isVueContext(file)) return Result.CONTINUE
    return myBracesCompleter.beforeCharTyped(c, project, editor, file)
  }
}

private val myExcludedTopLevelTags = arrayOf(SCRIPT_TAG_NAME, STYLE_TAG_NAME)

class VueInterpolationBracesCompleter(factory: NullableFunction<PsiElement, Pair<String, String>>) :
  JSInjectionBracesUtil.InterpolationBracesCompleter(factory) {

  override fun checkTypingContext(editor: Editor, file: PsiFile): Boolean {
    val atCaret = getContextElement(editor, file)
    val tag = atCaret as? XmlTag ?: atCaret?.parent as? XmlTag
    return atCaret == null || atCaret is XmlElement && tag?.name !in myExcludedTopLevelTags
  }
}
