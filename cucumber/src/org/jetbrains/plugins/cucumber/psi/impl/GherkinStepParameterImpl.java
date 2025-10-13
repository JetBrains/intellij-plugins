// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.plugins.cucumber.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.search.LocalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNullByDefault;
import org.jetbrains.plugins.cucumber.psi.GherkinElementFactory;
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
  public PsiElement setName(@NonNls String name) throws IncorrectOperationException {
    final LeafPsiElement content = PsiTreeUtil.getChildOfType(this, LeafPsiElement.class);
    if (content != null) {
      PsiElement[] elements = GherkinElementFactory.getTopLevelElements(getProject(), name);
      getNode().replaceChild(content, elements[0].getNode());
    }
    return this;
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
  public PsiElement getNameIdentifier() {
    return this;
  }

  @Override
  public SearchScope getUseScope() {
    return new LocalSearchScope(getContainingFile());
  }
}
