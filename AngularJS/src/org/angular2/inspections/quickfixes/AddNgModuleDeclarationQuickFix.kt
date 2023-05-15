// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo
import com.intellij.codeInspection.LocalQuickFix
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.entities.Angular2Declaration
import org.angular2.entities.Angular2EntityUtils.forEachModule
import org.angular2.entities.Angular2Module
import org.angular2.entities.source.Angular2SourceDeclaration
import org.angular2.inspections.actions.Angular2ActionFactory
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls
import java.util.*

/**
 * Adds Component/Directive/Pipe to NgModule's declarations array.
 */
class AddNgModuleDeclarationQuickFix private constructor(context: PsiElement, declaration: Angular2SourceDeclaration)
  : LocalQuickFixAndIntentionActionOnPsiElement(context) {

  private val myDeclarationName: String
  private val myDeclarationDecorator: SmartPsiElementPointer<ES6Decorator> = SmartPointerManager.createPointer(declaration.decorator)
  private val myModuleName: String?

  init {
    myDeclarationName = declaration.typeScriptClass.name!!
    val candidates = getCandidateModules(context)
    if (candidates.size == 1) {
      myModuleName = candidates[0].getName()
    }
    else {
      myModuleName = null
    }
  }

  override fun getText(): String {
    return if (myModuleName == null)
      Angular2Bundle.message("angular.quickfix.ngmodule.declare.name.choice",
                             myDeclarationName)
    else
      Angular2Bundle.message("angular.quickfix.ngmodule.declare.name",
                             myDeclarationName, myModuleName)
  }

  @Nls(capitalization = Nls.Capitalization.Sentence)
  override fun getFamilyName(): String {
    return Angular2Bundle.message("angular.quickfix.ngmodule.declare.family")
  }

  override fun invoke(project: Project,
                      file: PsiFile,
                      editor: Editor?,
                      startElement: PsiElement,
                      endElement: PsiElement) {
    if (myDeclarationDecorator.element == null) return
    val action = Angular2ActionFactory.createAddNgModuleDeclarationAction(
      editor, startElement, myDeclarationDecorator, myDeclarationName, text, false)
    val candidates = action.rawCandidates
    if (candidates.size == 1 || editor != null) {
      action.execute()
    }
  }

  companion object {

    fun add(context: PsiElement,
            declaration: Angular2Declaration,
            fixes: MutableList<LocalQuickFix>) {
      if (declaration is Angular2SourceDeclaration && declaration.typeScriptClass.name != null) {
        fixes.add(AddNgModuleDeclarationQuickFix(context, declaration))
      }
    }

    fun getCandidateModules(context: PsiElement): List<Angular2Module> {
      val processingQueue = ArrayDeque<Angular2Module>(20)
      val scope = Angular2DeclarationsScope(context)
      val contextModule = scope.importsOwner as? Angular2Module
      if (contextModule == null || !scope.isInSource(contextModule)) {
        return emptyList()
      }
      processingQueue.addLast(contextModule)
      val processed = HashSet<Angular2Module>()
      val result = ArrayList<Angular2Module>()
      while (!processingQueue.isEmpty()) {
        val module = processingQueue.removeFirst()
        if (processed.add(module) && scope.isInSource(module)) {
          result.add(module)
          forEachModule(module.imports) { processingQueue.addLast(it) }
          forEachModule(module.exports) { processingQueue.addLast(it) }
        }
      }
      return result
    }
  }

  override fun generatePreview(project: Project, editor: Editor, file: PsiFile): IntentionPreviewInfo {
    return IntentionPreviewInfo.EMPTY
  }
}
