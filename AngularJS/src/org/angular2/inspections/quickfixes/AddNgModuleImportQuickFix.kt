// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.entities.Angular2Declaration
import org.angular2.inspections.actions.Angular2ActionFactory
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

class AddNgModuleImportQuickFix(context: PsiElement,
                                importableDeclarations: Collection<Angular2Declaration>)
  : LocalQuickFixAndIntentionActionOnPsiElement(context) {

  private val myModuleName: String?

  init {
    val scope = Angular2DeclarationsScope(context)
    val names = importableDeclarations
      .asSequence()
      .flatMap { declaration ->
        if (declaration.isStandalone)
          listOf(declaration)
        else
          scope.getPublicModulesExporting(declaration)
      }
      .map { it.className }
      .distinct()
      .toList()
    if (names.size == 1) {
      myModuleName = names[0]
    }
    else {
      myModuleName = null
    }
  }

  override fun getText(): String {
    return if (myModuleName == null)
      Angular2Bundle.message("angular.quickfix.ngmodule.import.name.choice")
    else
      Angular2Bundle.message("angular.quickfix.ngmodule.import.name", myModuleName)
  }

  @Nls(capitalization = Nls.Capitalization.Sentence)
  override fun getFamilyName(): String {
    return Angular2Bundle.message("angular.quickfix.ngmodule.import.family")
  }

  override fun invoke(project: Project,
                      file: PsiFile,
                      editor: Editor?,
                      startElement: PsiElement,
                      endElement: PsiElement) {
    val action = Angular2ActionFactory.createNgModuleImportAction(editor, startElement, text, false)
    val candidates = action.rawCandidates
    if (candidates.size == 1 || editor != null) {
      action.execute()
    }
  }

  override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
    return IntentionPreviewInfo.EMPTY
  }

  override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
    return IntentionPreviewInfo.EMPTY
  }
}
