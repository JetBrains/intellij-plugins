// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinFeature;
import org.jetbrains.plugins.cucumber.psi.GherkinStepsHolder;
import org.jetbrains.plugins.cucumber.psi.GherkinTokenTypes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class GherkinFeatureImpl extends GherkinPsiElementBase implements GherkinFeature {
  public GherkinFeatureImpl(final @NotNull ASTNode node) {
    super(node);
  }

  @Override
  public String toString() {
    return "GherkinFeature:" + getFeatureName();
  }

  @Override
  public String getFeatureName() {
    ASTNode node = getNode();
    final ASTNode firstText = node.findChildByType(GherkinTokenTypes.TEXT);
    if (firstText != null) {
      return firstText.getText();
    }
    final GherkinFeatureHeaderImpl header = PsiTreeUtil.getChildOfType(this, GherkinFeatureHeaderImpl.class);
    if (header != null) {
      return header.getElementText();
    }
    return getElementText();
  }

  @Override
  public GherkinStepsHolder[] getScenarios() {
    List<GherkinStepsHolder> result = new ArrayList<>();

    GherkinStepsHolder[] scenarios = PsiTreeUtil.getChildrenOfType(this, GherkinStepsHolder.class);
    if (scenarios != null) {
      result.addAll(Arrays.asList(scenarios));
    }
    
    GherkinRuleImpl[] rules = PsiTreeUtil.getChildrenOfType(this, GherkinRuleImpl.class);
    if (rules != null) {
      for (GherkinRuleImpl rule : rules) {
        scenarios = PsiTreeUtil.getChildrenOfType(rule, GherkinStepsHolder.class);
        if (scenarios != null) {
          result.addAll(Arrays.asList(scenarios));
        }
      }
    }

    return result.toArray(GherkinStepsHolder.EMPTY_ARRAY);
  }

  @Override
  protected String getPresentableText() {
    return "Feature: " + getFeatureName();
  }

  @Override
  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitFeature(this);
  }
}
