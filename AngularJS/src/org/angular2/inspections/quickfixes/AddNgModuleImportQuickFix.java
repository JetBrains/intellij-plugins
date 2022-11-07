// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.inspections.quickfixes;

import com.intellij.codeInsight.intention.preview.IntentionPreviewInfo;
import com.intellij.codeInspection.LocalQuickFixAndIntentionActionOnPsiElement;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.lang.javascript.modules.imports.JSImportCandidate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import one.util.streamex.StreamEx;
import org.angular2.codeInsight.Angular2DeclarationsScope;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2Entity;
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
                                   @NotNull Collection<Angular2Declaration> importableDeclarations) {
    super(context);
    List<String> names = StreamEx.of(importableDeclarations)
      .flatCollection(declaration -> {
        if (declaration.isStandalone()) {
          return List.of(declaration);
        }
        else {
          return new Angular2DeclarationsScope(context).getPublicModulesExporting(declaration);
        }
      })
      .distinct()
      .map(Angular2Entity::getClassName)
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
    List<? extends JSImportCandidate> candidates = action.getRawCandidates();
    if (candidates.size() == 1 || editor != null) {
      action.execute();
    }
  }

  @Override
  public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull ProblemDescriptor previewDescriptor) {
    return IntentionPreviewInfo.EMPTY;
  }

  @Override
  public @NotNull IntentionPreviewInfo generatePreview(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    return IntentionPreviewInfo.EMPTY;
  }
}
