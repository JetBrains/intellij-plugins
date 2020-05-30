// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.lang.javascript.psi.JSElement;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2Module;
import org.angular2.inspections.actions.Angular2ActionFactory;
import org.angular2.inspections.actions.NgModuleImportAction;
import org.angular2.lang.Angular2Bundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

public class AddNgModuleImportQuickFix extends LocalQuickFixAndIntentionActionOnPsiElement {

  private final @Nullable String myModuleName;

  public AddNgModuleImportQuickFix(@NotNull PsiElement context,
                                   @NotNull Collection<Angular2Declaration> declarations) {
    super(context);
    List<String> names = StreamEx.of(declarations)
      .flatCollection(new Angular2DeclarationsScope(context)::getPublicModulesExporting)
      .distinct()
      .map(Angular2Module::getName)
      .distinct()
      .toList();
    if (names.size() == 1) {
      myModuleName = names.get(0);
    }
    else {
      myModuleName = null;
    }
  }

  @Override
  public @NotNull String getText() {
    return Angular2Bundle.message(myModuleName == null ? "angular.quickfix.ngmodule.import.name.choice"
                                                       : "angular.quickfix.ngmodule.import.name",
                                  myModuleName);
  }

  @Override
  public @Nls(capitalization = Nls.Capitalization.Sentence) @NotNull String getFamilyName() {
    return Angular2Bundle.message("angular.quickfix.ngmodule.import.family");
  }

  @Override
  public void invoke(@NotNull Project project,
                     @NotNull PsiFile file,
                     @Nullable Editor editor,
                     @NotNull PsiElement startElement,
                     @NotNull PsiElement endElement) {
    NgModuleImportAction action = Angular2ActionFactory.createNgModuleImportAction(editor, startElement, getText(), false);
    List<JSElement> candidates = action.getCandidates();
    if (candidates.size() == 1 || editor != null) {
      action.execute();
    }
  }
}
