// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import org.angular2.entities.Angular2Declaration
import org.angular2.entities.source.Angular2SourceDeclaration
import org.angular2.inspections.actions.Angular2ActionFactory
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

class ExportNgModuleDeclarationQuickFix private constructor(context: PsiElement,
                                                            declaration: Angular2SourceDeclaration)
  : LocalQuickFixAndIntentionActionOnPsiElement(context) {

  private val myDeclarationName: String
  private val myDeclarationDecorator: SmartPsiElementPointer<ES6Decorator>

  init {
    myDeclarationName = declaration.typeScriptClass.name!!
    myDeclarationDecorator = SmartPointerManager.createPointer(declaration.decorator)
  }

  override fun getText(): String {
    return Angular2Bundle.message("angular.quickfix.ngmodule.export.name", myDeclarationName)
  }

  @Nls(capitalization = Nls.Capitalization.Sentence)
  override fun getFamilyName(): String {
    return Angular2Bundle.message("angular.quickfix.ngmodule.export.family")
  }

  override fun invoke(project: Project,
                      file: PsiFile,
                      editor: Editor?,
                      startElement: PsiElement,
                      endElement: PsiElement) {
    if (myDeclarationDecorator.element == null) return
    val action = Angular2ActionFactory.createExportNgModuleDeclarationAction(
      editor, startElement, myDeclarationDecorator, text, false)
    val candidates = action.candidates
    if (candidates.size == 1 || editor != null) {
      action.execute()
    }
  }

  override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
    return IntentionPreviewInfo.EMPTY
  }

  override fun generatePreview(project: Project, previewDescriptor: ProblemDescriptor): IntentionPreviewInfo {
    return IntentionPreviewInfo.EMPTY
  }

  companion object {

    fun add(context: PsiElement,
            declaration: Angular2Declaration,
            fixes: MutableList<LocalQuickFix>) {
      if (declaration is Angular2SourceDeclaration && declaration.typeScriptClass.name != null) {
        fixes.add(ExportNgModuleDeclarationQuickFix(context, declaration))
      }
    }
  }

}
