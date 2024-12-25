// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinStep;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;
import org.jetbrains.plugins.cucumber.psi.GherkinTag;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;

public abstract class GherkinStepsHolderBase extends GherkinPsiElementBase implements GherkinStepsHolder {
  protected GherkinStepsHolderBase(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public final @NotNull String getScenarioName() {
    final StringBuilder result = new StringBuilder();

    ASTNode node = getNode().getFirstChildNode();
    while (node != null && node.getElementType() != GherkinTokenTypes.COLON) {
      node = node.getTreeNext();
    }
    if (node != null) {
      node = node.getTreeNext();
    }

    while (node != null && !node.getText().contains("\n")) {
      result.append(node.getText());
      node = node.getTreeNext();
    }
    return result.toString().trim();
  }

  @Override
  public final GherkinStep @NotNull [] getSteps() {
    final GherkinStep[] steps = PsiTreeUtil.getChildrenOfType(this, GherkinStep.class);
    return steps == null ? GherkinStep.EMPTY_ARRAY : steps;
  }

  @Override
  public final GherkinTag[] getTags() {
    final GherkinTag[] tags = PsiTreeUtil.getChildrenOfType(this, GherkinTag.class);
    return tags == null ? GherkinTag.EMPTY_ARRAY : tags;
  }

  @Override
  public @NotNull String getScenarioKeyword() {
    return getFirstChild().getText();
  }
}
