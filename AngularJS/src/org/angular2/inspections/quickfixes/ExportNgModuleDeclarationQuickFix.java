// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.source.Angular2SourceDeclaration;
import org.angular2.inspections.actions.Angular2ActionFactory;
import org.angular2.inspections.actions.ExportNgModuleDeclarationAction;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class ExportNgModuleDeclarationQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {

  public static void add(@NotNull PsiElement context,
                         @NotNull Angular2Declaration declaration,
                         @NotNull List<LocalQuickFix> fixes) {
    if (declaration instanceof Angular2SourceDeclaration
        && ((Angular2SourceDeclaration)declaration).getTypeScriptClass().getName() != null) {
      fixes.add(new ExportNgModuleDeclarationQuickFix(context, (Angular2SourceDeclaration)declaration));
    }
  }

  private final @NotNull String myDeclarationName;
  private final @NotNull SmartPsiElementPointer<ES6Decorator> myDeclarationDecorator;

  private ExportNgModuleDeclarationQuickFix(@NotNull PsiElement context,
                                            @NotNull Angular2SourceDeclaration declaration) {
    super(context);
    myDeclarationName = Objects.requireNonNull(declaration.getTypeScriptClass().getName());
    myDeclarationDecorator = SmartPointerManager.createPointer(declaration.getDecorator());
  }

  @Override
  public @NotNull String getText() {
    return Angular2Bundle.message("angular.quickfix.ngmodule.export.name", myDeclarationName);
  }

  @Override
  public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
    return Angular2Bundle.message("angular.quickfix.ngmodule.export.family");
  }

  @Override
  public void invoke(@NotNull Project project,
                     @NotNull PsiFile file,
                     @Nullable Editor editor,
                     @NotNull PsiElement startElement,
                     @NotNull PsiElement endElement) {
    if (myDeclarationDecorator.getElement() == null) return;
    ExportNgModuleDeclarationAction action = Angular2ActionFactory.createExportNgModuleDeclarationAction(
      editor, startElement, myDeclarationDecorator, getText(), false);
    List<JSElement> candidates = action.getCandidates();
    if (candidates.size() == 1 || editor != null) {
      action.execute();
    }
  }
}
