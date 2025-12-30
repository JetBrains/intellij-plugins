// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.actions

import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.modules.imports.ES6ImportCandidate
import com.intellij.lang.javascript.modules.imports.JSImportCandidate
import com.intellij.lang.javascript.modules.imports.JSImportCandidateWithExecutor
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.NlsContexts
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import org.angular2.Angular2DecoratorUtil.DECLARATIONS_PROP
import org.angular2.Angular2DecoratorUtil.EXPORTS_PROP
import org.angular2.codeInsight.Angular2DeclarationsScope
import org.angular2.editor.scheduleDelayedAutoPopupIfNeeded
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.Angular2Module
import org.angular2.entities.source.Angular2SourceDeclaration
import org.angular2.entities.source.Angular2SourceModule
import org.angular2.inspections.quickfixes.AddNgModuleDeclarationQuickFix
import org.angular2.inspections.quickfixes.Angular2FixesPsiUtil
import org.angular2.lang.Angular2Bundle

class AddNgModuleDeclarationAction(editor: Editor?,
                                   context: PsiElement,
                                   private val myDecorator: SmartPsiElementPointer<ES6Decorator>,
                                   private val myDeclarationName: String,
                                   @NlsContexts.Command actionName: String,
                                   codeCompletion: Boolean)
  : Angular2NgModuleSelectAction(editor, context, "", actionName, codeCompletion) {

  override fun getModuleSelectionPopupTitle(): String {
    return Angular2Bundle.message("angular.quickfix.ngmodule.declare.select.module", myDeclarationName)
  }

  override fun getRawCandidates(): List<JSImportCandidate> {
    if (Angular2EntitiesProvider.getDeclaration(myDecorator.element) !is Angular2SourceDeclaration)
      return emptyList()
    return AddNgModuleDeclarationQuickFix.getCandidateModules(context)
      .mapNotNull { it.entitySource?.let { src -> ES6ImportCandidate(myName, src, context) } }
  }

  override fun runAction(editor: Editor?,
                         candidate: JSImportCandidateWithExecutor,
                         place: PsiElement) {
    val context = context
    if (!context.isValid) {
      return
    }
    val element = candidate.element
    val module = Angular2EntitiesProvider.getModule(element) as? Angular2SourceModule
                 ?: return
    val declaration = Angular2EntitiesProvider.getDeclaration(myDecorator.element) as? Angular2SourceDeclaration
                      ?: return
    val scope = Angular2DeclarationsScope(context)
    val contextModule = scope.importsOwner as? Angular2Module
                        ?: return
    WriteAction.run<RuntimeException> {
      ES6ImportPsiUtil.insertJSImport(module.typeScriptClass, myDeclarationName, declaration.typeScriptClass, editor)
      Angular2FixesPsiUtil.insertEntityDecoratorMember(module, DECLARATIONS_PROP, myDeclarationName)
      if (contextModule !== module) {
        Angular2FixesPsiUtil.insertEntityDecoratorMember(module, EXPORTS_PROP, myDeclarationName)
      }
    }
    if (myCodeCompletion) scheduleDelayedAutoPopupIfNeeded(editor, place)
  }
}
