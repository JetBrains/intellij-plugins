// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring.extractComponent

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.actions.BaseRefactoringAction
import org.angular2.lang.html.Angular2HtmlFileType
import org.angular2.lang.html.Angular2HtmlLanguage

class Angular2ExtractComponentAction : BaseRefactoringAction() {
  init {
    setInjectedContext(true)
  }

  override fun isAvailableInEditorOnly() = true

  override fun isEnabledOnElements(elements: Array<out PsiElement>) = true

  override fun isAvailableOnElementInEditorAndFile(element: PsiElement, editor: Editor, file: PsiFile, context: DataContext) = true

  override fun isAvailableForLanguage(language: Language?): Boolean = Angular2HtmlLanguage.INSTANCE == language

  override fun isAvailableForFile(file: PsiFile?): Boolean = Angular2HtmlFileType.INSTANCE == file?.fileType

  override fun getHandler(dataContext: DataContext): RefactoringActionHandler {
    return Angular2ExtractComponentHandler()
  }
}
