package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.CucumberStepHelper;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

import static org.jetbrains.plugins.cucumber.CucumberUtil.getCucumberStepReference;

/**
 * @author yole
 */
public class CucumberStepInspection extends GherkinInspection {
  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @Override
  @NotNull
  public String getShortName() {
    return "CucumberUndefinedStep";
  }

  @NotNull
  @Override
  public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly) {
    return new GherkinElementVisitor() {
      @Override
      public void visitStep(GherkinStep step) {
        super.visitStep(step);

        final PsiElement parent = step.getParent();
        if (parent instanceof GherkinStepsHolder) {
          CucumberStepReference reference = getCucumberStepReference(step);
          if (reference == null) {
            return;
          }
          final AbstractStepDefinition definition = reference.resolveToDefinition();
          if (definition == null) {
            CucumberCreateStepFix createStepFix = null;
            CucumberCreateAllStepsFix createAllStepsFix = null;
            if (CucumberStepHelper.getExtensionCount() > 0) {
              createStepFix = new CucumberCreateStepFix();
              createAllStepsFix = new CucumberCreateAllStepsFix();
            }
            holder.registerProblem(reference.getElement(), reference.getRangeInElement(),
                                   CucumberBundle.message("cucumber.inspection.undefined.step.msg.name"),
                                   createStepFix, createAllStepsFix);
          }
        }
      }
    };
  }
}
