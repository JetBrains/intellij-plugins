// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.*;


public class GherkinRuleImpl extends GherkinPsiElementBase implements GherkinRule {
  public GherkinRuleImpl(@NotNull final ASTNode node) {
    super(node);
  }

  @NotNull
  @Override
  public String toString() {
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
