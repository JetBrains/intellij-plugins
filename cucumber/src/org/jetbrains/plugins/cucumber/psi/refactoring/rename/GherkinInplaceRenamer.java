package org.jetbrains.plugins.cucumber.psi.refactoring.rename;

import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.rename.inplace.VariableInplaceRenamer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinScenarioOutline;

public class GherkinInplaceRenamer extends VariableInplaceRenamer {
  public GherkinInplaceRenamer(@NotNull PsiNamedElement elementToRename, Editor editor) {
    super(elementToRename, editor);
  }

  @Override
  public void finish(boolean success) {
    super.finish(success);

    if (success) {
      final PsiNamedElement newVariable = getVariable();
      if (newVariable != null) {
        final GherkinScenarioOutline scenario = PsiTreeUtil.getParentOfType(newVariable, GherkinScenarioOutline.class);

        if (scenario != null) {
          final CodeStyleManager csManager = CodeStyleManager.getInstance(newVariable.getProject());
          csManager.reformat(scenario);
        }
      }
    }
  }
}
