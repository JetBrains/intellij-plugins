// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.actions;

import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.source.Angular2SourceDeclaration;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.intellij.util.ObjectUtils.tryCast;

public class Angular2ActionFactory {

  public static @Nullable AddNgModuleDeclarationAction createAddNgModuleDeclarationAction(@Nullable Editor editor,
                                                                                          @NotNull PsiElement element,
                                                                                          @NotNull Angular2Declaration declaration,
                                                                                          boolean codeCompletion) {

    Angular2SourceDeclaration sourceDeclaration = tryCast(declaration, Angular2SourceDeclaration.class);
    String className;
    return sourceDeclaration == null || (className = sourceDeclaration.getTypeScriptClass().getName()) == null
           ? null
           : createAddNgModuleDeclarationAction(editor, element,
                                                SmartPointerManager.createPointer(sourceDeclaration.getDecorator()), className,
                                                Angular2Bundle.message("angular.quickfix.ngmodule.declare.name.choice",
                                                                       className),
                                                codeCompletion);
  }

  public static @NotNull AddNgModuleDeclarationAction createAddNgModuleDeclarationAction(@Nullable Editor editor,
                                                                                         @NotNull PsiElement element,
                                                                                         @NotNull SmartPsiElementPointer<ES6Decorator> declarationDecorator,
                                                                                         @NotNull String declarationName,
                                                                                         @NotNull String actionName,
                                                                                         boolean codeCompletion) {
    return new AddNgModuleDeclarationAction(editor, element, declarationDecorator, declarationName, actionName, codeCompletion);
  }

  public static @NotNull NgModuleImportAction createNgModuleImportAction(@Nullable Editor editor,
                                                                         @NotNull PsiElement element,
                                                                         boolean codeCompletion) {
    return createNgModuleImportAction(editor, element, Angular2Bundle.message("angular.quickfix.ngmodule.import.name.choice"),
                                      codeCompletion);
  }

  public static @NotNull NgModuleImportAction createNgModuleImportAction(@Nullable Editor editor,
                                                                         @NotNull PsiElement element,
                                                                         @NotNull String actionName,
                                                                         boolean codeCompletion) {
    return new NgModuleImportAction(editor, element, actionName, codeCompletion);
  }

  public static @Nullable ExportNgModuleDeclarationAction createExportNgModuleDeclarationAction(@Nullable Editor editor,
                                                                                                @NotNull PsiElement element,
                                                                                                @NotNull Angular2Declaration declaration,
                                                                                                boolean codeCompletion) {
    Angular2SourceDeclaration sourceDeclaration = tryCast(declaration, Angular2SourceDeclaration.class);
    String className;
    return sourceDeclaration == null || (className = sourceDeclaration.getTypeScriptClass().getName()) == null
           ? null
           : createExportNgModuleDeclarationAction(editor, element, SmartPointerManager.createPointer(sourceDeclaration.getDecorator()),
                                                   Angular2Bundle.message("angular.quickfix.ngmodule.export.name", className),
                                                   codeCompletion);
  }

  public static @NotNull ExportNgModuleDeclarationAction createExportNgModuleDeclarationAction(@Nullable Editor editor,
                                                                                               @NotNull PsiElement element,
                                                                                               @NotNull SmartPsiElementPointer<ES6Decorator> declarationDecorator,
                                                                                               @NotNull String actionName,
                                                                                               boolean codeCompletion) {
    return new ExportNgModuleDeclarationAction(editor, element, declarationDecorator, actionName, codeCompletion);
  }
}
