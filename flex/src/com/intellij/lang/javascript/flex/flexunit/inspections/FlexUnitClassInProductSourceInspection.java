package com.intellij.lang.javascript.flex.flexunit.inspections;

import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.ide.DataManager;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.flexunit.FlexUnitSupport;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringActionHandlerFactory;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class FlexUnitClassInProductSourceInspection extends FlexUnitClassInspectionBase {

  @Nls
  @NotNull
  public String getDisplayName() {
    return FlexBundle.message("flexunit.inspection.testclassinproductsource.displayname");
  }

  @NotNull
  public String getShortName() {
    return "FlexUnitClassInProductSourceInspection";
  }

  protected void visitPotentialTestClass(JSClass aClass, @NotNull ProblemsHolder holder, FlexUnitSupport support) {
    final VirtualFile file = aClass.getContainingFile().getVirtualFile();
    if (file == null) {
      return;
    }

    if (!ProjectRootManager.getInstance(aClass.getProject()).getFileIndex().isInTestSourceContent(file)) {
      final ASTNode nameIdentifier = aClass.findNameIdentifier();
      if (nameIdentifier != null) {
        LocalQuickFix[] fixes = holder.isOnTheFly() ? new LocalQuickFix[]{new MoveClassFix(aClass)} : LocalQuickFix.EMPTY_ARRAY;
        holder.registerProblem(nameIdentifier.getPsi(), FlexBundle.message("flexunit.inspection.testclassinproductsource.message"),
                               ProblemHighlightType.GENERIC_ERROR_OR_WARNING, fixes);
      }
    }
  }

  private class MoveClassFix implements LocalQuickFix, IntentionAction {
    private final JSClass myClass;

    public MoveClassFix(JSClass aClass) {
      myClass = aClass;
    }

    @NotNull
    public String getFamilyName() {
      return getText();
    }

    @NotNull
    public String getText() {
      return "Move class";
    }

    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
      return myClass.isValid();
    }

    public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
      ApplicationManager.getApplication().invokeLater(() -> RefactoringActionHandlerFactory.getInstance().createMoveHandler()
        .invoke(project, editor, file, DataManager.getInstance().getDataContext()));
    }

    public boolean startInWriteAction() {
      return false;
    }

    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
      invoke(project, null, descriptor.getPsiElement().getContainingFile());
    }
  }
}