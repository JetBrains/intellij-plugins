package org.jetbrains.plugins.cucumber.groovy.steps.search;

import com.intellij.psi.ElementDescriptionLocation;
import com.intellij.psi.ElementDescriptionProvider;
import com.intellij.psi.PsiElement;
import com.intellij.usageView.UsageViewNodeTextLocation;
import com.intellij.usageView.UsageViewTypeLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.CucumberBundle;
import org.jetbrains.plugins.cucumber.groovy.GrCucumberUtil;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrReferenceExpression;

/**
 * @author Max Medvedev
 */
public class GrStepDefinitionDescriptionProvider implements ElementDescriptionProvider {
  @Nullable
  @Override
  public String getElementDescription(@NotNull PsiElement element, @NotNull ElementDescriptionLocation location) {
    if (location instanceof UsageViewNodeTextLocation || location instanceof UsageViewTypeLocation) {
      if (GrCucumberUtil.isStepDefinition(element) || element instanceof GrReferenceExpression && GrCucumberUtil.isStepDefinition(element.getParent())) {
        return CucumberBundle.message("step.definition") ;
      }
    }
    return null;
  }
}
