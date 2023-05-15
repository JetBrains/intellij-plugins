// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2

import com.intellij.injected.editor.EditorWindow
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.xml.XmlAttribute
import org.angular2.lang.expr.psi.Angular2EmbeddedExpression

object Angular2InjectionUtils {

  @JvmStatic
  fun getFirstInjectedFile(element: PsiElement?): PsiFile? {
    if (element != null) {
      val injections = InjectedLanguageManager.getInstance(element.project).getInjectedPsiFiles(element)
      if (injections != null) {
        for (injection in injections) {
          if (injection.getFirst() is PsiFile) {
            return injection.getFirst() as PsiFile
          }
        }
      }
    }
    return null
  }

  @JvmStatic
  fun getElementAtCaretFromContext(context: DataContext): PsiElement? {
    val editor = context.getData(CommonDataKeys.EDITOR)
    val file = context.getData(CommonDataKeys.PSI_FILE)
    val project = context.getData(CommonDataKeys.PROJECT)
    if (editor == null || project == null
        || file == null || DumbService.isDumb(project)) {
      return null
    }
    val caretOffset = editor.caretModel.offset
    if (editor !is EditorWindow) {
      val injected = InjectedLanguageManager.getInstance(project)
        .findInjectedElementAt(file, caretOffset)
      if (injected != null && injected.isValid) {
        return injected
      }
    }
    return file.findElementAt(caretOffset)
  }

  @JvmStatic
  fun <T : Angular2EmbeddedExpression> findInjectedAngularExpression(attribute: XmlAttribute,
                                                                     expressionClass: Class<T>): T? {
    val value = attribute.valueElement
    if (value != null && value.textLength >= 2) {
      val injection = InjectedLanguageManager.getInstance(attribute.project).findInjectedElementAt(
        value.containingFile, value.textOffset + 1)
      return PsiTreeUtil.getParentOfType(injection, expressionClass)
    }
    return null
  }
}
