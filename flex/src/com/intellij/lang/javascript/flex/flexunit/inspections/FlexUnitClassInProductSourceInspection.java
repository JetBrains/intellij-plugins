package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.RefactoringQuickFix;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.refactoring.RefactoringActionHandler;
import com.intellij.refactoring.RefactoringActionHandlerFactory;
import org.jetbrains.annotations.NotNull;

public class FlexUnitClassInProductSourceInspection extends FlexUnitClassInspectionBase {

  @Override
  @NotNull
  public String getShortName() {
    return "FlexUnitClassInProductSourceInspection";
  }

  @Override
  protected void visitPotentialTestClass(JSClass aClass, @NotNull ProblemsHolder holder, FlexUnitSupport support) {
    final VirtualFile file = aClass.getContainingFile().getVirtualFile();
    if (file == null) {
      return;
    }

    if (!ProjectRootManager.getInstance(aClass.getProject()).getFileIndex().isInTestSourceContent(file)) {
      final PsiElement nameIdentifier = aClass.getNameIdentifier();
      if (nameIdentifier != null) {
        final LocalQuickFix[] fixes = holder.isOnTheFly() ? new LocalQuickFix[]{new MoveClassFix()} : LocalQuickFix.EMPTY_ARRAY;
        holder.registerProblem(nameIdentifier, FlexBundle.message("flexunit.inspection.testclassinproductsource.message"),
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fixes);
      }
    }
  }

  private static class MoveClassFix implements RefactoringQuickFix {

    @Override
    @NotNull
    public String getFamilyName() {
      return "Move class";
    }

    @NotNull
    @Override
    public RefactoringActionHandler getHandler() {
      return RefactoringActionHandlerFactory.getInstance().createMoveHandler();
    }
  }
}