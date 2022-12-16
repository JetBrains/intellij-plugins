package com.intellij.javascript.karma.config;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.javascript.karma.KarmaBundle;
import com.intellij.lang.javascript.inspections.JSInspection;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class KarmaConfigFileInspection extends JSInspection {
  @NotNull
  @Override
  protected PsiElementVisitor createVisitor(@NotNull ProblemsHolder holder, @NotNull LocalInspectionToolSession session) {
    return new JSElementVisitor() {
      @Override
      public void visitJSLiteralExpression(final @NotNull JSLiteralExpression node) {
        if (!node.isQuotedLiteral()) return;
        for (PsiReference ref : node.getReferences()) {
          if (ref instanceof KarmaConfigFileReference) {
            handleReference((KarmaConfigFileReference) ref, holder);
          }
        }
      }
    };
  }

  private static void handleReference(@NotNull KarmaConfigFileReference ref, @NotNull ProblemsHolder holder) {
    if (ref.isLast()) {
      PsiFileSystemItem fileItem = resolve(ref);
      if (fileItem == null) {
        // JSFileReferencesInspection should highlight the reference as unresolved
        return;
      }
      KarmaConfigFileReference.FileType fileType = ref.getExpectedFileType();
      if (fileType == KarmaConfigFileReference.FileType.DIRECTORY) {
        if (!fileItem.isDirectory()) {
          holder.registerProblemForReference(
            ref,
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            KarmaBundle.message("inspection.KarmaConfigFile.directory_expected.message")
          );
        }
      }
      if (fileType == KarmaConfigFileReference.FileType.FILE) {
        if (fileItem.isDirectory()) {
          holder.registerProblemForReference(
            ref,
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            KarmaBundle.message("inspection.KarmaConfigFile.file_or_pattern_expected.message")
          );
        }
      }
    }
  }

  @Nullable
  private static PsiFileSystemItem resolve(@NotNull KarmaConfigFileReference ref) {
    ResolveResult[] results = ref.multiResolve(false);
    for (ResolveResult result : results) {
      if (result.isValidResult()) {
        PsiElement element = result.getElement();
        if (element instanceof PsiFileSystemItem) {
          return (PsiFileSystemItem) element;
        }
      }
    }
    return null;
  }
}
