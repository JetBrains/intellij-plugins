// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.actions

import com.intellij.codeInsight.hint.QuestionAction
import com.intellij.lang.ecmascript6.psi.impl.ES6ImportPsiUtil
import com.intellij.lang.javascript.modules.imports.JSImportCandidate
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.NotNullLazyValue
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPsiElementPointer
import org.angular2.Angular2DecoratorUtil.EXPORTS_PROP
import org.angular2.entities.Angular2EntitiesProvider
import org.angular2.entities.Angular2EntityUtils
import org.angular2.entities.source.Angular2SourceDeclaration
import org.angular2.entities.source.Angular2SourceModule
import org.angular2.inspections.quickfixes.Angular2FixesPsiUtil
import org.jetbrains.annotations.Nls

class ExportNgModuleDeclarationAction internal constructor(private val myEditor: Editor?,
                                                           private val myContext: PsiElement,
                                                           private val myDecorator: SmartPsiElementPointer<ES6Decorator>,
                                                           private val myName: @Nls String,
                                                           private val myCodeCompletion: Boolean) : QuestionAction {
  private val myImportAction: NotNullLazyValue<NgModuleImportAction> = NotNullLazyValue.createValue {
    Angular2ActionFactory.createNgModuleImportAction(myEditor, myContext, myCodeCompletion)
  }

  val candidates: List<JSImportCandidate>
    get() = myImportAction.value.rawCandidates

  override fun execute(): Boolean {
    if (addExport()) {
      myImportAction.value.executeForAllVariants()
    }
    return true
  }

  protected fun addExport(): Boolean {
    ApplicationManager.getApplication().assertIsDispatchThread()

    val element = myDecorator.element
    var result = false
    CommandProcessor.getInstance().executeCommand(
      myContext.project,
      {
        if (!myContext.isValid || element == null || !element.isValid) {
          return@executeCommand
        }
        result = executeFor(element)
      },
      myName,
      this
    )
    return result
  }

  private fun executeFor(element: PsiElement): Boolean {
    return WriteAction.compute<Boolean, RuntimeException> {
      val declaration = Angular2EntitiesProvider.getDeclaration(element) as? Angular2SourceDeclaration
                        ?: return@compute false
      val className = declaration.typeScriptClass.name
                      ?: return@compute false
      val module = Angular2EntityUtils.defaultChooseModule(declaration.allDeclaringModules.filterIsInstance<Angular2SourceModule>())
                   ?: return@compute false
      ES6ImportPsiUtil.insertJSImport(module.decorator, className, declaration.typeScriptClass, myEditor)
      Angular2FixesPsiUtil.insertEntityDecoratorMember(module, EXPORTS_PROP, className)
    }
  }
}
