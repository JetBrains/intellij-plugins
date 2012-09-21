package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeHighlighting.HighlightDisplayLevel;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.impl.GherkinExamplesBlockImpl;

/**
 * @author Dennis.Ushakov
 */
public class CucumberExamplesColonInspection extends GherkinInspection {

  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @Nls
  @NotNull
  @Override
  public String getDisplayName() {
    return CucumberBundle.message("inspection.missed.colon.example.name");
  }

  @NotNull
  @Override
  public String getShortName() {
    return "CucumberExamplesColon";
  }

  @NotNull
  @Override
  public HighlightDisplayLevel getDefaultLevel() {
    return HighlightDisplayLevel.ERROR;
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, boolean isOnTheFly) {
    return new GherkinElementVisitor() {
      @Override
      public void visitExamplesBlock(GherkinExamplesBlockImpl block) {
        final PsiElement examples = block.getFirstChild();
        assert examples != null;
        final PsiElement next = examples.getNextSibling();
        final String text = next != null ? next.getText() : null;
        if (text == null || !text.contains(":")) {

          holder.registerProblem(examples,
                                 new TextRange(0, examples.getTextRange().getEndOffset() - examples.getTextOffset()),
                                 CucumberBundle.message("inspection.missed.colon.example.name"),
                                 new CucumberAddExamplesColonFix());
        }
      }
    };
  }
}
