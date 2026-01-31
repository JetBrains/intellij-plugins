// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.plugins.jade.validation;

import com.intellij.codeInsight.actions.ReformatCodeProcessor;
import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.idea.ActionsBundle;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.plugins.jade.JadeBundle;
import com.jetbrains.plugins.jade.JadeLanguage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public final class JadeTabsAndSpacesInspection extends LocalInspectionTool {

  private static final LocalQuickFix FORMAT_QUICKFIX = new LocalQuickFix() {
    @Override
    public @NotNull String getName() {
      return ActionsBundle.message("action.ReformatCode.description");
    }

    @Override
    public @NotNull String getFamilyName() {
      return JadeBundle.message("pug.intention.family.name");
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      final PsiFile file = descriptor.getStartElement().getContainingFile();
      new ReformatCodeProcessor(project, file, file.getTextRange(), false).run();
    }
  };

  //@NotNull
  //@Override
  //public PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
  //  final TextRange
  //
  //  return new PsiElementVisitor() {
  //    @Override
  //    public void visitElement(PsiElement element) {
  //      if (element.getNode().getElementType() != JadeTokenTypes.INDENT) {
  //        return;
  //      }
  //
  //
  //    }
  //  }
  //}

  //private static class TextRangeHolder


  @Override
  public ProblemDescriptor @Nullable [] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
    List<ProblemDescriptor> result = new ArrayList<>(2);

    if (file.getLanguage() != JadeLanguage.INSTANCE) {
      return ProblemDescriptor.EMPTY_ARRAY;
    }

    final String text = file.getText();
    TextRange lastRange = null;
    char lastCharType = 0;

    int newline = -1;
    while ((newline = text.indexOf("\n", newline + 1)) != -1) {
      if (text.length() == newline + 1) {
        break;
      }

      final char firstCharAfterNewline = text.charAt(newline + 1);
      if (firstCharAfterNewline != ' ' && firstCharAfterNewline != '\t') {
        continue;
      }

      int firstDifferent = newline + 2;
      while (firstDifferent < text.length() && text.charAt(firstDifferent) == firstCharAfterNewline) {
        ++firstDifferent;
      }

      if (lastCharType != 0 && lastCharType != firstCharAfterNewline) {
        result.add(reportProblem(manager, file, lastRange));
        result.add(reportProblem(manager, file, TextRange.create(newline + 1, firstDifferent)));
        break;
      }

      if (firstDifferent == text.length()) {
        break;
      }
      final char firstDifferentChar = text.charAt(firstDifferent);

      if (firstDifferentChar == ' ' || firstDifferentChar == '\t') {
        result.add(reportProblem(manager, file, TextRange.create(firstDifferent - 1, firstDifferent)));
        result.add(reportProblem(manager, file, TextRange.create(firstDifferent, firstDifferent + 1)));
        break;
      }

      lastCharType = firstCharAfterNewline;
      lastRange = TextRange.create(newline + 1, firstDifferent);
    }

    return result.toArray(ProblemDescriptor.EMPTY_ARRAY);
  }

  private static ProblemDescriptor reportProblem(InspectionManager manager, PsiFile file, TextRange range) {
    final PsiElement at = file.findElementAt(range.getStartOffset());
    if (at == null) {
      throw new IndexOutOfBoundsException("could not find element at index");
    }

    return manager.createProblemDescriptor(at, range.shiftRight(-at.getTextOffset()),
                                           JadeBundle.message("pug.inspections.tabs.and.spaces.name"),
                                           ProblemHighlightType.GENERIC_ERROR_OR_WARNING, true, FORMAT_QUICKFIX);
  }
}
