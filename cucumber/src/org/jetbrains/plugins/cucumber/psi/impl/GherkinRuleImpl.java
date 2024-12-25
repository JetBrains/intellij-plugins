// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinRule;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;


public class GherkinRuleImpl extends GherkinPsiElementBase implements GherkinRule {
  public GherkinRuleImpl(final @NotNull ASTNode node) {
    super(node);
  }

  @Override
  public @NotNull String toString() {
    return "GherkinRule:" + getRuleName();
  }

  @Override
  public String getRuleName() {
    ASTNode node = getNode();
    final ASTNode firstText = node.findChildByType(GherkinTokenTypes.TEXT);
    if (firstText != null) {
      return firstText.getText();
    }
    return getElementText();
  }

  @Override
  public GherkinStepsHolder[] getScenarios() {
    final GherkinStepsHolder[] children = PsiTreeUtil.getChildrenOfType(this, GherkinStepsHolder.class);
    return children == null ? GherkinStepsHolder.EMPTY_ARRAY : children;
  }

  @Override
  protected String getPresentableText() {
    return "Rule: " + getRuleName();
  }

  @Override
  protected void acceptGherkin(@NotNull GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitRule(this);
  }
}
