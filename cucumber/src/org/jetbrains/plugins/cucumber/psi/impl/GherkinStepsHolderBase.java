package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;

public abstract class GherkinStepsHolderBase extends GherkinPsiElementBase implements GherkinStepsHolder {
  protected GherkinStepsHolderBase(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public final String getScenarioName() {
    ASTNode node = getNode().getFirstChildNode();
    while (node != null && node.getElementType() != GherkinTokenTypes.TEXT) {
      node = node.getTreeNext();
    }

    return node != null ? node.getText() : "";
  }

  @NotNull
  @Override
  public final GherkinStep[] getSteps() {
    final GherkinStep[] steps = PsiTreeUtil.getChildrenOfType(this, GherkinStep.class);
    return steps == null ? GherkinStep.EMPTY_ARRAY : steps;
  }

  @Override
  public final GherkinTag[] getTags() {
    final GherkinTag[] tags = PsiTreeUtil.getChildrenOfType(this, GherkinTag.class);
    return tags == null ? GherkinTag.EMPTY_ARRAY : tags;
  }

  @Nullable
  @Override
  public final String getScenarioTitle() {
    // Scenario's title is the line after Scenario[ Outline]: keyword
    final PsiElement psiElement = getShortDescriptionText();
    if (psiElement == null) {
      return null;
    }
    final String text = psiElement.getText();
    return StringUtil.isEmptyOrSpaces(text) ? null : text.trim();
  }
}
