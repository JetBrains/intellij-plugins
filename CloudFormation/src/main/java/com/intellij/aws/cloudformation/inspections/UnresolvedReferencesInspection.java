package com.intellij.aws.cloudformation.inspections;

import com.intellij.aws.cloudformation.CloudFormationPsiUtils;
import com.intellij.aws.cloudformation.references.CloudFormationReferenceBase;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.json.psi.JsonElementVisitor;
import com.intellij.json.psi.JsonStringLiteral;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.NotNull;

public class UnresolvedReferencesInspection extends LocalInspectionTool {
  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly, @NotNull LocalInspectionToolSession session) {
    if (!CloudFormationPsiUtils.isCloudFormationFile(session.getFile())) {
      return super.buildVisitor(holder, isOnTheFly, session);
    }

    return new JsonElementVisitor() {
      @Override
      public void visitStringLiteral(@NotNull JsonStringLiteral o) {
        for (PsiReference reference : o.getReferences()) {
          if (reference instanceof CloudFormationReferenceBase) {
            PsiElement element = reference.resolve();
            if (element == null) {
              holder.registerProblem(reference);
            }
          }
        }
      }
    };
  }
}
