// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
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
import icons.VuejsIcons
import org.jetbrains.vuejs.VueBundle
import org.jetbrains.vuejs.VueFileType
import org.jetbrains.vuejs.VueLanguage

/**
 * @author Irina.Chernushina on 3/9/2018.
 */
class VueExtractComponentAction: BaseRefactoringAction() {
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
    return object: RefactoringActionHandler {
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