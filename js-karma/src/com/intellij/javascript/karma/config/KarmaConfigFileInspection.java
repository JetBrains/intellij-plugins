package com.intellij.javascript.karma.config;

import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.lang.javascript.inspections.JSInspection;
import com.intellij.lang.javascript.psi.JSElementVisitor;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Sergey Simonchik
 */
public class KarmaConfigFileInspection extends JSInspection {
  @NotNull
  @Override
  protected PsiElementVisitor createVisitor(final ProblemsHolder holder, LocalInspectionToolSession session) {
    return new JSElementVisitor() {
      @Override
      public void visitJSLiteralExpression(final JSLiteralExpression node) {
        for (PsiReference ref : node.getReferences()) {
          if (ref instanceof KarmaConfigFileReference) {
            handleReference((KarmaConfigFileReference) ref, holder);
          }
        }
      }
    };
  }

  private static void handleReference(@NotNull KarmaConfigFileReference ref,
                                      @NotNull ProblemsHolder holder) {
    PsiFileSystemItem fileItem = resolve(ref);
    if (fileItem == null) {
      holder.registerProblemForReference(
        ref,
        ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
        ref.getUnresolvedMessagePattern()
      );
    }
    else {
      KarmaConfigFileReference.FileType fileType = ref.getRequiredFileType();
      if (fileType != null && ref.isLast()) {
        if (fileType == KarmaConfigFileReference.FileType.DIRECTORY
            && !fileItem.isDirectory()) {
          holder.registerProblemForReference(
            ref,
            ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
            "Directory is expected"
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
