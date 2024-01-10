// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.actions

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.NlsContexts
import com.intellij.psi.PsiElement
import com.intellij.psi.SmartPointerManager
import com.intellij.psi.SmartPsiElementPointer
import org.angular2.entities.Angular2Declaration
import org.angular2.entities.source.Angular2SourceDeclaration
import org.angular2.lang.Angular2Bundle
import org.jetbrains.annotations.Nls

object Angular2ActionFactory {

  fun createAddNgModuleDeclarationAction(editor: Editor?,
                                         element: PsiElement,
                                         declaration: Angular2Declaration,
                                         codeCompletion: Boolean): AddNgModuleDeclarationAction? {

    val sourceDeclaration = declaration as? Angular2SourceDeclaration
                            ?: return null
    val className = sourceDeclaration.typeScriptClass.name
                    ?: return null

    return createAddNgModuleDeclarationAction(editor, element,
                                              SmartPointerManager.createPointer(sourceDeclaration.decorator), className,
                                              Angular2Bundle.message("angular.quickfix.ngmodule.declare.name.choice",
                                                                     className),
                                              codeCompletion)
  }

  fun createAddNgModuleDeclarationAction(editor: Editor?,
                                         element: PsiElement,
                                         declarationDecorator: SmartPsiElementPointer<ES6Decorator>,
                                         declarationName: String,
                                         @NlsContexts.Command actionName: String,
                                         codeCompletion: Boolean): AddNgModuleDeclarationAction {
    return AddNgModuleDeclarationAction(editor, element, declarationDecorator, declarationName, actionName, codeCompletion)
  }

  fun createNgModuleImportAction(editor: Editor?,
                                 element: PsiElement,
                                 codeCompletion: Boolean): NgModuleImportAction {
    @Suppress("DialogTitleCapitalization")
    return createNgModuleImportAction(editor, element, Angular2Bundle.message("angular.quickfix.ngmodule.import.name.choice"),
                                      codeCompletion)
  }

  fun createNgModuleImportAction(editor: Editor?,
                                 element: PsiElement,
                                 @NlsContexts.Command actionName: String,
                                 codeCompletion: Boolean): NgModuleImportAction {
    return NgModuleImportAction(editor, element, actionName, codeCompletion)
  }

  fun createExportNgModuleDeclarationAction(editor: Editor?,
                                            element: PsiElement,
                                            declaration: Angular2Declaration,
                                            codeCompletion: Boolean): ExportNgModuleDeclarationAction? {
    val sourceDeclaration = declaration as? Angular2SourceDeclaration
                            ?: return null
    val className = sourceDeclaration.typeScriptClass.name
                    ?: return null
    return createExportNgModuleDeclarationAction(editor, element, SmartPointerManager.createPointer(sourceDeclaration.decorator),
                                                 Angular2Bundle.message("angular.quickfix.ngmodule.export.name", className),
                                                 codeCompletion)
  }

  fun createExportNgModuleDeclarationAction(editor: Editor?,
                                            element: PsiElement,
                                            declarationDecorator: SmartPsiElementPointer<ES6Decorator>,
                                            @Nls actionName: String,
                                            codeCompletion: Boolean): ExportNgModuleDeclarationAction {
    return ExportNgModuleDeclarationAction(editor, element, declarationDecorator, actionName, codeCompletion)
  }
}
