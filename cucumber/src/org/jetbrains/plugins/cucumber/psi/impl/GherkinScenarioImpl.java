package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.*;

/**
 * @author yole
 */
public class GherkinScenarioImpl extends GherkinPsiElementBase implements GherkinScenario {
  public GherkinScenarioImpl(@NotNull final ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    if (isBackground()) {
      return "GherkinScenario(Background):";
    }
    return "GherkinScenario:" + getScenarioName();
  }

  public String getScenarioName() {
    return getElementText();
  }

  public GherkinStep[] getSteps() {
    final GherkinStep[] steps = PsiTreeUtil.getChildrenOfType(this, GherkinStep.class);
    return steps == null ? GherkinStep.EMPTY_ARRAY : steps;
  }

  @Override
  public GherkinTag[] getTags() {
    final GherkinTag[] tags = PsiTreeUtil.getChildrenOfType(this, GherkinTag.class);
    return tags == null ? GherkinTag.EMPTY_ARRAY : tags;
  }

  public boolean isBackground() {
    return getNode().getFirstChildNode().getElementType() == GherkinTokenTypes.BACKGROUND_KEYWORD;
  }

  @Override
  protected String getPresentableText() {
    return buildPresentableText(isBackground() ? "Background" : "Scenario");
  }

  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitScenario(this);
  }
}
