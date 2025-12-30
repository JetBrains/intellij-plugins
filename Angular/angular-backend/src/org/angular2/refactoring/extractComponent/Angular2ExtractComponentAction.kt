// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring.extractComponent

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.actions.BaseRefactoringAction
import org.angular2.lang.Angular2LangUtil
import org.angular2.lang.html.Angular2HtmlLanguage
import org.angular2.lang.svg.Angular2SvgLanguage

class Angular2ExtractComponentAction : BaseRefactoringAction() {
  init {
    setInjectedContext(true)
  }

  override fun isAvailableInEditorOnly(): Boolean = true

  override fun isEnabledOnElements(elements: Array<out PsiElement>): Boolean = true

  override fun isAvailableOnElementInEditorAndFile(element: PsiElement, editor: Editor, file: PsiFile, context: DataContext): Boolean = true

  override fun isAvailableForLanguage(language: Language?): Boolean = language?.isKindOf(Angular2HtmlLanguage) == true
                                                                      && !language.isKindOf(Angular2SvgLanguage)

  override fun isAvailableForFile(file: PsiFile?): Boolean = file?.fileType.let { Angular2LangUtil.isAngular2HtmlFileType(it) }

  override fun getHandler(dataContext: DataContext): RefactoringActionHandler {
    return Angular2ExtractComponentHandler()
  }
}
