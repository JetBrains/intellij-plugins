// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.intentions.extractComponent

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import com.intellij.refactoring.RefactoringActionHandler
import com.intellij.refactoring.actions.BaseRefactoringAction
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.VuejsIcons
import org.jetbrains.vuejs.lang.html.VueFileType
import org.jetbrains.vuejs.lang.html.VueLanguage

class VueExtractComponentAction : BaseRefactoringAction() {
  init {
    templatePresentation.text = VueBundle.message("vue.template.intention.extract.component")
    templatePresentation.description = VueBundle.message("vue.template.intention.extract.component.description")
    templatePresentation.icon = VuejsIcons.Vue
  }

  override fun isAvailableInEditorOnly(): Boolean = true

  override fun isEnabledOnElements(elements: Array<out PsiElement>): Boolean = true
  override fun isAvailableOnElementInEditorAndFile(element: PsiElement, editor: Editor, file: PsiFile, context: DataContext): Boolean {
    return VueExtractComponentIntention.getContext(editor, element) != null
  }

  override fun isAvailableForLanguage(language: Language?): Boolean = VueLanguage.INSTANCE == language

  override fun isAvailableForFile(file: PsiFile?): Boolean = VueFileType.INSTANCE == file?.fileType

  override fun getHandler(dataContext: DataContext): RefactoringActionHandler? {
    return object : RefactoringActionHandler {
      override fun invoke(project: Project, editor: Editor?, file: PsiFile?, dataContext: DataContext?) {
        editor ?: return
        val element = PsiUtilBase.getElementAtCaret(editor) ?: return
        val context = VueExtractComponentIntention.getContext(editor, element) ?: return
        VueExtractComponentRefactoring(project, context, editor).perform()
      }

      override fun invoke(project: Project, elements: Array<out PsiElement>, dataContext: DataContext?) {
        // available only in editor
      }
    }
  }
}
