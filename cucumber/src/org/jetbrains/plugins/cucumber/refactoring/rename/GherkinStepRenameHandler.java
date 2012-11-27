package org.jetbrains.plugins.cucumber.refactoring.rename;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.RefactoringBundle;
import com.intellij.refactoring.rename.RenameHandler;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.GherkinStepParameter;
import org.jetbrains.plugins.cucumber.psi.GherkinTableCell;

/**
 * @author Max Medvedev
 */
public class GherkinStepRenameHandler implements RenameHandler {
  @Override
  public boolean isAvailableOnDataContext(DataContext dataContext) {
    final PsiFile file = LangDataKeys.PSI_FILE.getData(dataContext);
    final Editor editor = PlatformDataKeys.EDITOR.getData(dataContext);
    if (editor == null || file == null) return false;

    final PsiElement position = file.findElementAt(editor.getCaretModel().getOffset());
    if (position instanceof GherkinStepParameter || position instanceof GherkinTableCell) {
      return false;
    }

    return position != null && position.getParent() instanceof GherkinStep;
  }

  @Override
  public boolean isRenaming(DataContext dataContext) {
    return isAvailableOnDataContext(dataContext);
  }

  @Override
  public void invoke(@NotNull Project project, Editor editor, PsiFile file, DataContext dataContext) {
    showErrorMessage(project, editor);
  }

  private static void showErrorMessage(Project project, Editor editor) {
    final String message = RefactoringBundle.getCannotRefactorMessage(CucumberBundle.message("step.rename.is.not.supported"));
    CommonRefactoringUtil.showErrorHint(project, editor, message, RefactoringBundle.message("rename.title"), null);
  }

  @Override
  public void invoke(@NotNull Project project, @NotNull PsiElement[] elements, DataContext dataContext) {
    showErrorMessage(project, PlatformDataKeys.EDITOR.getData(dataContext));
  }
}
