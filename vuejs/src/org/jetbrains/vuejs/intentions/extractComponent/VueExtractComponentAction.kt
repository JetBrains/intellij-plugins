// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.vuejs.intentions.extractComponent

import com.intellij.lang.Language
import com.intellij.lang.javascript.refactoring.util.beforeRefactoring
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.actions.BaseRefactoringAction
import org.jetbrains.vuejs.lang.html.VueLanguage
import org.jetbrains.vuejs.lang.html.isVueFile

internal class VueExtractComponentAction : BaseRefactoringAction() {
  override fun isAvailableInEditorOnly(): Boolean = true

  override fun isEnabledOnElements(elements: Array<out PsiElement>): Boolean = true
  override fun isAvailableOnElementInEditorAndFile(element: PsiElement, editor: Editor, file: PsiFile, context: DataContext): Boolean {
    return getContextForExtractComponentIntention(editor, element) != null
  }

  override fun isAvailableForLanguage(language: Language?): Boolean = VueLanguage.INSTANCE == language

  override fun isAvailableForFile(file: PsiFile?): Boolean = file?.isVueFile == true

  override fun getHandler(dataContext: DataContext): RefactoringActionHandler {
    return object : RefactoringActionHandler {
      override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext?) {
        editor ?: return
        val element = PsiUtilBase.getElementAtCaret(editor) ?: return
        val context = getContextForExtractComponentIntention(editor, element) ?: return
        beforeRefactoring(project = project, id = REFACTORING_ID, elements = context.toTypedArray())
        VueExtractComponentRefactoring(project, context, editor).perform(fireRefactoringEvents = true)
      }

      override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
        // available only in editor
      }
    }
  }

  companion object {
    const val REFACTORING_ID = "refactoring.javascript.vue.extractComponent"
  }
}
