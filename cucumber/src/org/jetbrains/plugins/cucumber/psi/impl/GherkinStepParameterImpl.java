// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.plugins.cucumber.psi.GherkinElementVisitor;
import org.jetbrains.plugins.cucumber.psi.GherkinStepParameter;

@NotNullByDefault
public class GherkinStepParameterImpl extends GherkinPsiElementBase implements GherkinStepParameter {
  public GherkinStepParameterImpl(ASTNode node) {
    super(node);
  }

  @Override
  protected void acceptGherkin(GherkinElementVisitor gherkinElementVisitor) {
    gherkinElementVisitor.visitStepParameter(this);
  }

  @Override
  public String toString() {
    return "GherkinStepParameter:" + getText();
  }

  @Override
  public PsiReference getReference() {
    return new GherkinStepParameterReference(this);
  }

  @Override
  public String getName() {
    return getText();
  }

  @Override
  public SearchScope getUseScope() {
    return new LocalSearchScope(getContainingFile());
  }
}
