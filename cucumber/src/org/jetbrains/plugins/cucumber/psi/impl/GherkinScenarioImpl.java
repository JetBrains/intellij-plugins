// Copyright 2000-2021 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinScenario;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;


public class GherkinScenarioImpl extends GherkinStepsHolderBase implements GherkinScenario {
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

  @Override
  public boolean isBackground() {
    ASTNode node = getNode().getFirstChildNode();
    return node != null && node.getElementType() == GherkinTokenTypes.BACKGROUND_KEYWORD;
  }

  @Override
  protected String getPresentableText() {
    return buildPresentableText(isBackground() ? "Background" : getScenarioKeyword());
  }

  @Override
  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitScenario(this);
  }
}
