// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.plugins.drools.lang.psi.impl;

import com.intellij.ide.presentation.Presentation;
import com.intellij.lang.ASTNode;
import com.intellij.plugins.drools.lang.psi.DroolsNameId;
import com.intellij.plugins.drools.lang.psi.util.DroolsElementsFactory;
import com.intellij.psi.*;
import com.intellij.psi.impl.light.JavaIdentifier;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Presentation(typeName = DroolsAbstractVariableImpl.VARIABLE)
public abstract class DroolsAbstractVariableImpl extends DroolsPsiCompositeElementImpl implements PsiVariable {

  public static final String VARIABLE = "Variable";

  public DroolsAbstractVariableImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public String getName() {
    final PsiIdentifier identifier = getNameIdentifier();
    return identifier == null ? null : identifier.getText();
  }

  @Override
  public PsiTypeElement getTypeElement() {
    return null;
  }

  @Override
  public PsiExpression getInitializer() {
    return null;
  }

  @Override
  public boolean hasInitializer() {
    return false;
  }

  @Override
  public void normalizeDeclaration() throws IncorrectOperationException {

  }

  @Override
  public Object computeConstantValue() {
    return null;
  }

  @Nullable
  protected  abstract DroolsNameId getNamedIdElement();

  @Override
  public PsiIdentifier getNameIdentifier() {
    if (!isValid()) return null;
    final DroolsNameId bindIdentifier = getNamedIdElement();
    return bindIdentifier == null? null : new JavaIdentifier(getManager(), bindIdentifier);
  }

  @Override
  public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
    final DroolsNameId oldIdentifier = getNamedIdElement();
    if (oldIdentifier != null) {
      final PsiElement patternBindIdentifier = DroolsElementsFactory.createPatternBindIdentifier(name, getProject());
      if (patternBindIdentifier != null) {
        oldIdentifier.replace(patternBindIdentifier);
      }
    }
    return this;
  }

  @Override
  public PsiModifierList getModifierList() {
    return null;
  }

  @Override
  public boolean hasModifierProperty(@PsiModifier.ModifierConstant @NonNls @NotNull String name) {
    return false;
  }
}
