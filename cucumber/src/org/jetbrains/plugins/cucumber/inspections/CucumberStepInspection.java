package org.jetbrains.plugins.cucumber.inspections;

import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.codeInspection.ex.UnfairLocalInspectionTool;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;
import org.jetbrains.plugins.cucumber.steps.AbstractStepDefinition;
import org.jetbrains.plugins.cucumber.steps.CucumberStepsIndex;
import org.jetbrains.plugins.cucumber.steps.reference.CucumberStepReference;

/**
 * @author yole
 */
public class CucumberStepInspection extends GherkinInspection implements UnfairLocalInspectionTool {
  @Override
  public boolean isEnabledByDefault() {
    return true;
  }

  @Nls
  @NotNull
  public String getDisplayName() {
    return CucumberBundle.message("cucumber.inspection.undefined.step.name");
  }

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
          final PsiReference[] references = step.getReferences();
          if (references.length != 1 || !(references[0] instanceof CucumberStepReference)) return;

          CucumberStepReference reference = (CucumberStepReference)references[0];
          final AbstractStepDefinition definition = reference.resolveToDefinition();
          if (definition == null) {
            CucumberCreateStepFix createStepFix = null;
            CucumberCreateAllStepsFix createAllStepsFix = null;
            if (CucumberStepsIndex.getInstance(step.getProject()).getExtensionCount() > 0) {
              createStepFix = new CucumberCreateStepFix();
              createAllStepsFix = new CucumberCreateAllStepsFix();
            }
            holder.registerProblem(reference.getElement(), reference.getRangeInElement(),
                                   CucumberBundle.message("cucumber.inspection.undefined.step.msg.name") + " #loc #ref",
                                   createStepFix, createAllStepsFix);
          }
        }
      }
    };
  }
}
